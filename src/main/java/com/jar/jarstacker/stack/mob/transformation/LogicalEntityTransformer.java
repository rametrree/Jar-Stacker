package com.jar.jarstacker.stack.mob.transformation;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.mixin.MushroomCowAccessor;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.MobCompatibility;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import com.jar.jarstacker.stack.mob.MovementDiagnostics;
import com.jar.jarstacker.stack.mob.SplitPlacementResolver;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Encapsulates transactional transformations where one logical entity inside
 * a stack transforms into a different entity type or state (TRANSFORM_ONE).
 * <p>
 * Implements strict transaction phases and the Side-Effect Boundary Principle:
 * Implements strict transaction phases, capture integrity, and the Side-Effect Boundary Principle:
 * - Before Vanilla commit: rollback source stack is allowed.
 * - After Vanilla commit: NEVER restore source stack, preserve transformed destination, recover forward.
 */
public class LogicalEntityTransformer {

	public enum Phase {
		IDLE,
		PREPARE,
		VANILLA_EXECUTION,
		VANILLA_COMMITTED,
		JAR_STACKER_COMMIT,
		POST_COMMIT_FALLBACK,
		ROLLBACK_PRE_COMMIT
	}

	public static class TransformationContext {
		private final UUID sourceId;
		private final UUID extractedId;
		private final EntityType<?> sourceMobType;
		private final EntityType<?> expectedDestinationType;
		private final Vec3 sourcePos;
		private final long tick;
		private final UUID playerId;
		private final Set<UUID> preExistingDestinationUuids;
		private final AtomicReference<Mob> directDestination = new AtomicReference<>();
		private final AtomicReference<Mob> capturedDestination;

		public TransformationContext(
			UUID sourceId,
			EntityType<?> sourceMobType,
			EntityType<?> expectedDestinationType,
			Vec3 sourcePos,
			long tick,
			UUID playerId,
			AtomicReference<Mob> capturedDestination
		) {
			this.sourceId = sourceId;
			this.extractedId = null;
			this.sourceMobType = sourceMobType;
			this.expectedDestinationType = expectedDestinationType;
			this.sourcePos = sourcePos;
			this.tick = tick;
			this.playerId = playerId;
			this.preExistingDestinationUuids = Collections.emptySet();
			this.capturedDestination = capturedDestination != null ? capturedDestination : new AtomicReference<>();
		}

		public TransformationContext(
			UUID sourceId,
			UUID extractedId,
			EntityType<?> sourceMobType,
			EntityType<?> expectedDestinationType,
			Vec3 sourcePos,
			long tick,
			UUID playerId,
			Set<UUID> preExistingDestinationUuids
		) {
			this.sourceId = sourceId;
			this.extractedId = extractedId;
			this.sourceMobType = sourceMobType;
			this.expectedDestinationType = expectedDestinationType;
			this.sourcePos = sourcePos;
			this.tick = tick;
			this.playerId = playerId;
			this.preExistingDestinationUuids = preExistingDestinationUuids != null ? preExistingDestinationUuids : Collections.emptySet();
			this.capturedDestination = new AtomicReference<>();
		}

		public UUID getSourceId() { return sourceId; }
		public UUID getExtractedId() { return extractedId; }
		public EntityType<?> getSourceMobType() { return sourceMobType; }
		public EntityType<?> getExpectedDestinationType() { return expectedDestinationType; }
		public Vec3 getSourcePos() { return sourcePos; }
		public long getTick() { return tick; }
		public UUID getPlayerId() { return playerId; }
		public Set<UUID> getPreExistingDestinationUuids() { return preExistingDestinationUuids; }
		public AtomicReference<Mob> directDestination() { return directDestination; }
		public AtomicReference<Mob> capturedDestination() { return capturedDestination; }

		public void recordDirect(Mob source, Mob destination) {
			if (destination != null && source != null) {
				UUID sid = source.getUUID();
				if (sid.equals(sourceId) || (extractedId != null && sid.equals(extractedId))) {
					directDestination.set(destination);
				}
			}
		}

		public boolean isPreExisting(UUID uuid) {
			return preExistingDestinationUuids.contains(uuid);
		}

		public boolean matches(Entity entity) {
			if (!(entity instanceof Mob mob)) {
				return false;
			}
			if (expectedDestinationType != null && mob.getType() != expectedDestinationType) {
				return false;
			}
			if (isPreExisting(mob.getUUID())) {
				return false;
			}
			Mob direct = directDestination.get();
			if (direct != null) {
				return mob == direct;
			}
			if (sourcePos != null && mob.position().distanceToSqr(sourcePos) > 16.0) { // 4 block radius max
				return false;
			}
			return true;
		}
	}

	private static final ThreadLocal<Boolean> HANDOFF_ACTIVE = ThreadLocal.withInitial(() -> false);
	private static final ThreadLocal<Deque<TransformationContext>> CONTEXT_STACK = ThreadLocal.withInitial(ArrayDeque::new);

	/**
	 * Test hooks to simulate failures and recovery conditions at explicit transaction boundaries.
	 */
	public static volatile boolean simulateTransformationFailure = false;
	public static volatile boolean simulatePreCommitFailure = false;
	public static volatile boolean simulatePostCommitFailure = false;
	public static volatile boolean simulateDestinationMergeFailure = false;
	public static volatile boolean simulateMissedCapture = false;
	public static volatile boolean simulateAmbiguousDestination = false;
	public static volatile boolean simulateNormalizationFailure = false;
	public static volatile java.util.function.Consumer<ServerLevel> onPreShearSpawnHook = null;

	public static boolean isHandoffActive() {
		return HANDOFF_ACTIVE.get();
	}

	public static void recordDirectTransformation(Mob source, Mob destination) {
		Deque<TransformationContext> stack = CONTEXT_STACK.get();
		if (stack != null && !stack.isEmpty()) {
			TransformationContext ctx = stack.peek();
			if (ctx != null) {
				ctx.recordDirect(source, destination);
				if (!simulateMissedCapture) {
					ctx.capturedDestination().compareAndSet(null, destination);
				}
			}
		}
	}

	public static void notifyEntityAdded(Entity entity) {
		Deque<TransformationContext> stack = CONTEXT_STACK.get();
		if (stack != null && !stack.isEmpty()) {
			TransformationContext ctx = stack.peek();
			if (ctx != null) {
				if (simulateMissedCapture) {
					return;
				}
				if (ctx.matches(entity)) {
					if (ctx.capturedDestination().compareAndSet(null, (Mob) entity)) {
						if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
							JarStackerMod.LOGGER.info("[JarStackerTransform] TRANSFORM_VANILLA_COMMITTED captured={} expected={}",
								entity.getType(), ctx.getExpectedDestinationType());
						}
					}
				} else {
					if (entity.getType() == ctx.getExpectedDestinationType()) {
						if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
							JarStackerMod.LOGGER.debug("[JarStackerTransform] TRANSFORM_CAPTURE_CANDIDATE_REJECTED candidate={}", entity.getUUID());
						}
					}
				}
			}
		}
	}

	public static EntityType<?> getExpectedDestinationType(Mob sourceMob) {
		if (sourceMob instanceof MushroomCow) {
			return EntityType.COW;
		}
		return null;
	}

	/**
	 * Recovers destination entity if direct capture failed or was missed after Vanilla commit.
	 */
	public static Mob recoverDestination(ServerLevel level, TransformationContext ctx) {
		if (simulateAmbiguousDestination) {
			JarStackerMod.LOGGER.warn("[JarStackerTransform] TRANSFORM_DESTINATION_AMBIGUOUS simulated=true");
			return null;
		}

		if (ctx == null || ctx.getExpectedDestinationType() == null || ctx.getSourcePos() == null) {
			return null;
		}

		AABB searchBox = new AABB(
			ctx.getSourcePos().x - 4.0, ctx.getSourcePos().y - 3.0, ctx.getSourcePos().z - 4.0,
			ctx.getSourcePos().x + 4.0, ctx.getSourcePos().y + 3.0, ctx.getSourcePos().z + 4.0
		);

		List<Mob> candidates = level.getEntitiesOfClass(Mob.class, searchBox, mob ->
			mob.isAlive()
				&& !mob.isRemoved()
				&& mob.getType() == ctx.getExpectedDestinationType()
				&& !ctx.isPreExisting(mob.getUUID())
		);

		if (candidates.size() == 1) {
			Mob recovered = candidates.get(0);
			JarStackerMod.LOGGER.info("[JarStackerTransform] TRANSFORM_CAPTURE_RECOVERED uuid={}", recovered.getUUID());
			return recovered;
		} else if (candidates.size() > 1) {
			JarStackerMod.LOGGER.warn("[JarStackerTransform] TRANSFORM_DESTINATION_AMBIGUOUS candidateCount={}", candidates.size());
			return null;
		} else {
			JarStackerMod.LOGGER.warn("[JarStackerTransform] TRANSFORM_DESTINATION_NOT_FOUND");
			return null;
		}
	}

	/**
	 * Normalizes destination entity after successful Vanilla transformation.
	 * Idempotent: normalize(normalize(mob)) == normalize(mob).
	 */
	public static void normalizeDestination(Mob sourceMob, Mob destinationMob) {
		if (destinationMob == null) {
			return;
		}
		ModConfig config = ModConfig.getInstance();
		boolean showLabel = config.getMobStacking().isShowLabel();

		// 1. Invariant: count is 1
		((StackableEntity) destinationMob).jarstacker$setStackCount(1);

		// 2. Clear any temporary interaction locks
		((StackableEntity) destinationMob).jarstacker$setInteractionLockTicks(0);

		// 3. Managed label vs Player custom name handling
		boolean sourceHadManagedLabel = sourceMob != null && (
			((StackableEntity) sourceMob).jarstacker$hasManagedLabel()
				|| MobCompatibility.isManagedLabel(sourceMob)
		);
		boolean destHasManagedLabel = ((StackableEntity) destinationMob).jarstacker$hasManagedLabel();

		boolean isStaleSourceTypeName = false;
		if (sourceMob != null && destinationMob.hasCustomName()) {
			String destName = destinationMob.getCustomName().getString();
			String sourceTypeName = sourceMob.getType().getDescription().getString();
			if (destName.startsWith(sourceTypeName)) {
				isStaleSourceTypeName = true;
			}
		}

		if (sourceHadManagedLabel || destHasManagedLabel || isStaleSourceTypeName) {
			// Managed label cleanup: clear stale label and apply destination identity
			destinationMob.setCustomName(null);
			destinationMob.setCustomNameVisible(false);
			((StackableEntity) destinationMob).jarstacker$setManagedLabel(false);

			if (config.getPerformance().isDebugLogging()) {
				JarStackerMod.LOGGER.debug("[JarStackerTransform] TRANSFORM_STALE_LABEL_CLEARED uuid={}", destinationMob.getUUID());
			}

			MobStackingManager.updateLabel(destinationMob, 1, showLabel);
		}

		if (config.getPerformance().isDebugLogging()) {
			JarStackerMod.LOGGER.debug("[JarStackerTransform] TRANSFORM_DESTINATION_NORMALIZED uuid={} type={}",
				destinationMob.getUUID(), destinationMob.getType());
		}
	}

	public static InteractionResult handleTransformation(Player player, ServerLevel level, InteractionHand hand, Mob sourceMob) {
		if (sourceMob == null || !sourceMob.isAlive() || sourceMob.isRemoved() || HANDOFF_ACTIVE.get()) {
			return InteractionResult.PASS;
		}

		int count = ((StackableEntity) sourceMob).jarstacker$getStackCount();
		if (count <= 0) count = 1;

		ModConfig config = ModConfig.getInstance();
		boolean showLabel = config.getMobStacking().isShowLabel();

		// Check pre-commit simulation flags
		if (simulateTransformationFailure || simulatePreCommitFailure) {
			JarStackerMod.LOGGER.warn("[JarStackerTransform] SIMULATED_PRE_COMMIT_FAILURE uuid={}", sourceMob.getUUID());
			return InteractionResult.FAIL;
		}

		EntityType<?> expectedDestType = getExpectedDestinationType(sourceMob);

		// Snapshot pre-existing destination entities in vicinity
		Set<UUID> preExistingUuids = new HashSet<>();
		if (expectedDestType != null) {
			AABB scanBox = sourceMob.getBoundingBox().inflate(8.0);
			for (Mob m : level.getEntitiesOfClass(Mob.class, scanBox)) {
				if (m.getType() == expectedDestType) {
					preExistingUuids.add(m.getUUID());
				}
			}
		}

		// =========================================================================
		// CASE 1: SINGLETON (count == 1)
		// =========================================================================
		if (count == 1) {
			TransformationContext ctx = new TransformationContext(
				sourceMob.getUUID(),
				null,
				sourceMob.getType(),
				expectedDestType,
				sourceMob.position(),
				level.getGameTime(),
				player.getUUID(),
				preExistingUuids
			);

			CONTEXT_STACK.get().push(ctx);
			HANDOFF_ACTIVE.set(true);
			InteractionResult res;
			try {
				if (onPreShearSpawnHook != null) {
					onPreShearSpawnHook.accept(level);
				}
				res = player.interactOn(sourceMob, hand);
			} finally {
				HANDOFF_ACTIVE.set(false);
				CONTEXT_STACK.get().poll();
			}

			Mob transformed = ctx.capturedDestination().get();
			boolean vanillaCommitted = res.consumesAction() && (transformed != null || sourceMob.isRemoved());

			if (vanillaCommitted) {
				// VANILLA_COMMITTED: Never rollback source!
				if (transformed == null) {
					transformed = recoverDestination(level, ctx);
				}

				if (transformed != null) {
					if (simulateNormalizationFailure) {
						JarStackerMod.LOGGER.warn("[JarStackerTransform] SIMULATED_NORMALIZATION_FAILURE");
						return res;
					}

					normalizeDestination(sourceMob, transformed);

					if (simulatePostCommitFailure) {
						logFallback(sourceMob, transformed, 0, "SIMULATED_POST_COMMIT_FAILURE");
						return res;
					}

					boolean mergeBlocked = simulateDestinationMergeFailure;
					boolean merged = false;
					if (!mergeBlocked) {
						try {
							merged = tryMergeDestination(level, transformed);
						} catch (Exception ex) {
							JarStackerMod.LOGGER.error("[JarStackerTransform] Error during tryMergeDestination", ex);
							merged = false;
						}
					}
					if (!merged) {
						logFallback(sourceMob, transformed, 0, "DESTINATION_MERGE_FAILED");
					}
				} else {
					JarStackerMod.LOGGER.warn("[JarStackerTransform] TRANSFORM_POST_COMMIT_RECOVERY_UNAVAILABLE source={}", sourceMob.getUUID());
				}
				return res;
			}
			return res;
		}

		// =========================================================================
		// CASE 2: STACKED (count > 1)
		// =========================================================================
		// 1. PREPARE PHASE
		int remainderCount = count - 1;
		((StackableEntity) sourceMob).jarstacker$setStackCount(remainderCount);
		MobStackingManager.updateLabel(sourceMob, remainderCount, showLabel);

		// Materialize transient physical singleton
		@SuppressWarnings("unchecked")
		EntityType<Mob> type = (EntityType<Mob>) sourceMob.getType();
		Mob extracted = com.jar.jarstacker.adapter.EntityAdapter.create(type, level);
		if (extracted == null) {
			// Pre-commit rollback
			((StackableEntity) sourceMob).jarstacker$setStackCount(count);
			MobStackingManager.updateLabel(sourceMob, count, showLabel);
			JarStackerMod.LOGGER.warn("[JarStackerTransform] TRANSFORM_ROLLBACK_PRE_COMMIT uuid={} reason=materialization_null", sourceMob.getUUID());
			return InteractionResult.FAIL;
		}

		Vec3 safePos = SplitPlacementResolver.findSafeSplitPosition(level, sourceMob, extracted,
			Collections.singleton(sourceMob.getBoundingBox()), player);
		extracted.moveTo(safePos.x, safePos.y, safePos.z, sourceMob.getYRot(), sourceMob.getXRot());
		extracted.setDeltaMovement(Vec3.ZERO);
		extracted.setHealth(sourceMob.getHealth());

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			extracted.setItemSlot(slot, sourceMob.getItemBySlot(slot).copy());
		}
		if (sourceMob instanceof VariantHolder<?> vSrc && extracted instanceof VariantHolder<?> vDst) {
			copyVariant(vSrc, vDst);
		}
		if (sourceMob instanceof MushroomCow mcSrc && extracted instanceof MushroomCow mcDst) {
			mcDst.setVariant(mcSrc.getVariant());
			net.minecraft.world.item.component.SuspiciousStewEffects effects =
				((MushroomCowAccessor) mcSrc).jarstacker$getStewEffects();
			if (effects != null) {
				((MushroomCowAccessor) mcDst).jarstacker$setStewEffects(effects);
			}
		}
		if (sourceMob.hasCustomName()) {
			extracted.setCustomName(sourceMob.getCustomName());
			extracted.setCustomNameVisible(sourceMob.isCustomNameVisible());
			((StackableEntity) extracted).jarstacker$setManagedLabel(((StackableEntity) sourceMob).jarstacker$hasManagedLabel());
		}

		((StackableEntity) extracted).jarstacker$setStackCount(1);
		((StackableEntity) extracted).jarstacker$setInteractionLockTicks(100);
		com.jar.jarstacker.stack.mob.health.LogicalHealthManager.extractHealthState(sourceMob, extracted);
		level.addFreshEntity(extracted);

		// 2. VANILLA EXECUTION PHASE
		TransformationContext ctx = new TransformationContext(
			sourceMob.getUUID(),
			extracted.getUUID(),
			sourceMob.getType(),
			expectedDestType,
			extracted.position(),
			level.getGameTime(),
			player.getUUID(),
			preExistingUuids
		);

		CONTEXT_STACK.get().push(ctx);
		HANDOFF_ACTIVE.set(true);
		InteractionResult res;
		try {
			if (onPreShearSpawnHook != null) {
				onPreShearSpawnHook.accept(level);
			}
			res = player.interactOn(extracted, hand);
		} finally {
			HANDOFF_ACTIVE.set(false);
			CONTEXT_STACK.get().poll();
		}

		Mob transformed = ctx.capturedDestination().get();
		boolean vanillaCommitted = res.consumesAction() && (transformed != null || extracted.isRemoved());

		// 3. COMMIT OR ROLLBACK EVALUATION
		if (vanillaCommitted) {
			// -------------------------------------------------------------
			// VANILLA COMMITTED: Side-effects (drops, shears damage, cow spawn)
			// have occurred! NEVER rollback source stack!
			// -------------------------------------------------------------
			if (extracted.isAlive()) {
				extracted.discard();
			}

			if (transformed == null) {
				transformed = recoverDestination(level, ctx);
			}

			if (transformed != null) {
				if (simulateNormalizationFailure) {
					JarStackerMod.LOGGER.warn("[JarStackerTransform] SIMULATED_NORMALIZATION_FAILURE");
					return res;
				}

				normalizeDestination(sourceMob, transformed);

				// Post-commit failure simulation check
				if (simulatePostCommitFailure) {
					logFallback(sourceMob, transformed, remainderCount, "SIMULATED_POST_COMMIT_FAILURE");
					return res;
				}

				boolean mergeBlocked = simulateDestinationMergeFailure;
				boolean merged = false;
				if (!mergeBlocked) {
					try {
						merged = tryMergeDestination(level, transformed);
					} catch (Exception ex) {
						JarStackerMod.LOGGER.error("[JarStackerTransform] Error during tryMergeDestination", ex);
						merged = false;
					}
				}

				if (merged) {
					if (config.getPerformance().isDebugLogging()) {
						JarStackerMod.LOGGER.info("[JarStackerTransform] TRANSFORM_COMMIT from={} to={} sourceBefore={} sourceAfter={} merged=true",
							sourceMob.getType(), transformed.getType(), count, remainderCount);
					}
				} else {
					// POST-COMMIT FORWARD RECOVERY: keep physical transformed mob in world
					logFallback(sourceMob, transformed, remainderCount, "DESTINATION_MERGE_FAILED");
				}
			} else {
				// POST-COMMIT FORWARD RECOVERY: keep physical transformed mob in world
				JarStackerMod.LOGGER.warn("[JarStackerTransform] TRANSFORM_POST_COMMIT_RECOVERY_UNAVAILABLE source={}", sourceMob.getUUID());
			}

			return res;
		} else {
			// -------------------------------------------------------------
			// PRE-COMMIT ROLLBACK: Vanilla did NOT consume action or produce transformed entity
			// It is completely safe to restore source stack to N.
			// -------------------------------------------------------------
			((StackableEntity) sourceMob).jarstacker$setStackCount(count);
			MobStackingManager.updateLabel(sourceMob, count, showLabel);

			if (transformed != null) {
				transformed.discard();
			}
			if (extracted.isAlive()) {
				extracted.discard();
			}
			JarStackerMod.LOGGER.warn("[JarStackerTransform] TRANSFORM_ROLLBACK_PRE_COMMIT uuid={} res={}", sourceMob.getUUID(), res);
			return InteractionResult.FAIL;
		}
	}

	private static void logFallback(Mob sourceMob, Mob transformedMob, int remainderCount, String reason) {
		JarStackerMod.LOGGER.warn("[JarStackerTransform] TRANSFORM_POST_COMMIT_FALLBACK source={} destination={} sourceCount={} destinationPhysical=1 reason={}",
			sourceMob.getType(), transformedMob.getType(), remainderCount, reason);
		MobStackingManager.updateLabel(transformedMob, 1, ModConfig.getInstance().getMobStacking().isShowLabel());
	}

	public static boolean tryMergeDestination(ServerLevel level, Mob transformedMob) {
		ModConfig config = ModConfig.getInstance();
		ModConfig.MobStackingConfig mobConfig = config.getMobStacking();
		double radius = MobCompatibility.getRadius(transformedMob, mobConfig);
		int maxStack = MobCompatibility.getMaxStackSize(transformedMob, mobConfig);

		AABB box = transformedMob.getBoundingBox().inflate(radius);
		List<Mob> nearby = level.getEntitiesOfClass(Mob.class, box, other ->
			other != transformedMob && other.isAlive() && !other.isRemoved()
				&& !((StackableEntity) other).jarstacker$isInteractionLocked()
				&& MobCompatibility.canStack(transformedMob, other, mobConfig)
		);

		for (Mob target : nearby) {
			int targetCount = ((StackableEntity) target).jarstacker$getStackCount();
			if (targetCount + 1 <= maxStack) {
				int newCount = targetCount + 1;
				((StackableEntity) target).jarstacker$setStackCount(newCount);
				com.jar.jarstacker.stack.mob.health.LogicalHealthManager.mergeHealthStates(target, transformedMob, 1);
				MobStackingManager.updateLabel(target, newCount, mobConfig.isShowLabel());
				transformedMob.discard();
				return true;
			}
		}

		MobStackingManager.updateLabel(transformedMob, 1, mobConfig.isShowLabel());
		return false;
	}

	@SuppressWarnings("unchecked")
	private static <T> void copyVariant(VariantHolder<T> src, VariantHolder<?> dst) {
		((VariantHolder<T>) dst).setVariant(src.getVariant());
	}
}
