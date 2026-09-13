package com.jar.jarstacker.stack.mob.baby;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.AnimalStackState;
import com.jar.jarstacker.stack.mob.MobCompatibility;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import com.jar.jarstacker.stack.mob.SplitPlacementResolver;
import com.jar.jarstacker.stack.mob.interaction.MobInteractionHandler;
import com.jar.jarstacker.stack.mob.logical.LogicalStateValidator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

public class BabyGrowthManager {

	public static boolean simulateMaterializationFailure = false;
	public static boolean simulateReVirtualizationFailure = false;

	public static BabyGrowthState getOrCreateGrowthState(ServerLevel level, Animal animal) {
		BabyGrowthState state = ((StackableEntity) animal).jarstacker$getBabyGrowthState();
		int count = ((StackableEntity) animal).jarstacker$getStackCount();
		if (count <= 0) {
			count = 1;
		}

		if (state == null) {
			state = new BabyGrowthState();
			state.initializeDefault(count, animal.getAge(), level.getGameTime());
			((StackableEntity) animal).jarstacker$setBabyGrowthState(state);
			if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
				JarStackerMod.LOGGER.info("[JarStackerBaby] BABY_STATE_CREATE uuid={} count={} age={} earliestAdultAt={}",
					animal.getUUID(), count, animal.getAge(), state.getEarliestAdultAt());
			}
		} else {
			if (state.size() != count || !state.isSorted() || state.hasInvalidTimestamps()) {
				LogicalStateValidator.repairLogicalState(animal);
			}
		}

		return state;
	}

	public static int evaluatePromotion(ServerLevel level, Animal animal) {
		if (!animal.isAlive() || animal.isRemoved() || !animal.isBaby()) {
			return 0;
		}

		int count = ((StackableEntity) animal).jarstacker$getStackCount();
		if (count <= 0) {
			count = 1;
		}

		BabyGrowthState state = getOrCreateGrowthState(level, animal);
		long currentGameTime = level.getGameTime();
		int maturedCount = state.countMatured(currentGameTime);

		if (maturedCount <= 0) {
			long earliest = state.getEarliestAdultAt();
			if (earliest != Long.MAX_VALUE) {
				int expectedAge = (int) Math.min(-1, -(earliest - currentGameTime));
				animal.setAge(expectedAge);
			}
			return 0;
		}

		// Case 1: All babies matured
		if (maturedCount >= count) {
			state.removeMatured(currentGameTime);
			animal.setBaby(false);
			animal.setAge(0);
			((StackableEntity) animal).jarstacker$setStackCount(count);
			((StackableEntity) animal).jarstacker$setBabyGrowthState(null);
			MobStackingManager.updateLabel(animal, count, ModConfig.getInstance().getMobStacking().isShowLabel());

			if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
				JarStackerMod.LOGGER.info("[JarStackerBaby] BABY_PROMOTE_ALL uuid={} count={}",
					animal.getUUID(), count);
			}
			return maturedCount;
		}

		// Case 2: Partial promotion (maturedCount < count)
		ModConfig config = ModConfig.getInstance();
		double radius = MobCompatibility.getRadius(animal, config.getMobStacking());
		AABB searchBox = animal.getBoundingBox().inflate(radius);

		List<Animal> nearbyAdults = level.getEntitiesOfClass(Animal.class, searchBox, other ->
			other != animal && other.isAlive() && !other.isRemoved() && !other.isBaby()
			&& other.getType() == animal.getType()
			&& MobCompatibility.getStackState(other) == AnimalStackState.ADULT
			&& !MobCompatibility.isExcluded(other)
			&& !((StackableEntity) other).jarstacker$isInteractionLocked()
		);

		Animal mergeTarget = null;
		for (Animal adultTarget : nearbyAdults) {
			boolean canMergeInto = adultTarget.getType() == animal.getType()
				&& !adultTarget.isBaby()
				&& MobCompatibility.getStackState(adultTarget) == AnimalStackState.ADULT
				&& !MobCompatibility.isExcluded(adultTarget)
				&& !((StackableEntity) adultTarget).jarstacker$isInteractionLocked();

			if (canMergeInto && animal instanceof net.minecraft.world.entity.VariantHolder<?> vSrc && adultTarget instanceof net.minecraft.world.entity.VariantHolder<?> vDst) {
				if (!java.util.Objects.equals(vSrc.getVariant(), vDst.getVariant())) {
					canMergeInto = false;
				}
			}
			if (canMergeInto && animal instanceof net.minecraft.world.entity.animal.Sheep sSrc && adultTarget instanceof net.minecraft.world.entity.animal.Sheep sDst) {
				if (sSrc.getColor() != sDst.getColor() || sDst.isSheared()) {
					canMergeInto = false;
				}
			}

			if (canMergeInto) {
				int targetCount = ((StackableEntity) adultTarget).jarstacker$getStackCount();
				int maxStack = MobCompatibility.getMaxStackSize(adultTarget, config.getMobStacking());
				if (targetCount + maturedCount <= maxStack) {
					mergeTarget = adultTarget;
					break;
				}
			}
		}

		Animal adultRep = null;
		if (mergeTarget == null) {
			@SuppressWarnings("unchecked")
			EntityType<Animal> type = (EntityType<Animal>) animal.getType();
			adultRep = com.jar.jarstacker.adapter.EntityAdapter.create(type, level);
			if (adultRep == null) {
				// Abort promotion safely without losing baby records
				JarStackerMod.LOGGER.error("[JarStackerBaby] PROMOTION_ABORTED_CANNOT_CREATE_ENTITY uuid={}", animal.getUUID());
				return 0;
			}
		}

		// Apply promotion
		state.removeMatured(currentGameTime);
		int remainingBabyCount = count - maturedCount;
		((StackableEntity) animal).jarstacker$setStackCount(remainingBabyCount);
		long earliest = state.getEarliestAdultAt();
		if (earliest != Long.MAX_VALUE) {
			int expectedAge = (int) Math.min(-1, -(earliest - currentGameTime));
			animal.setAge(expectedAge);
		}
		MobStackingManager.updateLabel(animal, remainingBabyCount, config.getMobStacking().isShowLabel());

		if (mergeTarget != null) {
			int targetCount = ((StackableEntity) mergeTarget).jarstacker$getStackCount();
			int newTotal = targetCount + maturedCount;
			((StackableEntity) mergeTarget).jarstacker$setStackCount(newTotal);
			MobStackingManager.updateLabel(mergeTarget, newTotal, config.getMobStacking().isShowLabel());
			if (config.getPerformance().isDebugLogging()) {
				JarStackerMod.LOGGER.info("[JarStackerBaby] BABY_PROMOTE_MERGE promotedCount={} mergedInto={}",
					maturedCount, mergeTarget.getUUID());
			}
		} else {
			Vec3 safePos = SplitPlacementResolver.findSafeSplitPosition(level, animal, adultRep, Collections.singleton(animal.getBoundingBox()));
			adultRep.moveTo(safePos.x, safePos.y, safePos.z, animal.getYRot(), animal.getXRot());
			adultRep.setBaby(false);
			adultRep.setAge(0);
			adultRep.setHealth(adultRep.getMaxHealth());

			for (EquipmentSlot slot : EquipmentSlot.values()) {
				adultRep.setItemSlot(slot, animal.getItemBySlot(slot).copy());
			}
			if (animal instanceof VariantHolder<?> vSrc && adultRep instanceof VariantHolder<?> vDst) {
				copyVariant(vSrc, vDst);
			}
			if (animal instanceof Sheep sSrc && adultRep instanceof Sheep sDst) {
				sDst.setColor(sSrc.getColor());
				sDst.setSheared(false);
			}

			((StackableEntity) adultRep).jarstacker$setStackCount(maturedCount);
			MobStackingManager.updateLabel(adultRep, maturedCount, config.getMobStacking().isShowLabel());
			level.addFreshEntity(adultRep);

			if (config.getPerformance().isDebugLogging()) {
				JarStackerMod.LOGGER.info("[JarStackerBaby] BABY_PROMOTE_SPAWN promotedCount={} newAdult={}",
					maturedCount, adultRep.getUUID());
			}
		}

		return maturedCount;
	}

	public static InteractionResult handleBabyFeeding(Player player, ServerLevel level, InteractionHand hand, Animal animal, ItemStack heldItem) {
		if (animal == null || !animal.isAlive() || animal.isRemoved() || !animal.isBaby()) {
			return InteractionResult.PASS;
		}

		int count = ((StackableEntity) animal).jarstacker$getStackCount();
		if (count <= 0) count = 1;

		BabyGrowthState state = getOrCreateGrowthState(level, animal);
		long currentGameTime = level.getGameTime();

		// Case: Singleton baby (count == 1)
		if (count == 1) {
			// Forward Vanilla interaction directly
			InteractionResult res = player.interactOn(animal, hand);
			if (res.consumesAction()) {
				int newAge = animal.getAge();
				if (newAge >= 0) {
					// Matured immediately
					animal.setBaby(false);
					animal.setAge(0);
					((StackableEntity) animal).jarstacker$setBabyGrowthState(null);
				} else {
					long newAdultAt = currentGameTime + (-newAge);
					state.initializeDefault(1, newAge, currentGameTime);
				}
			}
			return res;
		}

		// 1. Transactional selection: extract earliest adultAt
		long earliestAdultAt = state.extractEarliest();
		int simulatedAge = (int) Math.min(-1, -(earliestAdultAt - currentGameTime));

		// 2. Materialize transient singleton baby
		@SuppressWarnings("unchecked")
		EntityType<Animal> type = (EntityType<Animal>) animal.getType();
		Animal extracted = com.jar.jarstacker.adapter.EntityAdapter.create(type, level);
		if (extracted == null || simulateMaterializationFailure) {
			// Rollback on materialization failure!
			state.insert(earliestAdultAt);
			((StackableEntity) animal).jarstacker$setStackCount(count);
			if (extracted != null) {
				extracted.discard();
			}
			JarStackerMod.LOGGER.warn("[JarStackerBaby] MATERIALIZATION_ROLLBACK uuid={} restoredCount={}", animal.getUUID(), count);
			return InteractionResult.FAIL;
		}

		Vec3 safePos = SplitPlacementResolver.findSafeSplitPosition(level, animal, extracted, Collections.singleton(animal.getBoundingBox()), player);
		extracted.moveTo(safePos.x, safePos.y, safePos.z, animal.getYRot(), animal.getXRot());
		extracted.setDeltaMovement(Vec3.ZERO);
		extracted.setHealth(animal.getHealth());
		extracted.setBaby(true);
		extracted.setAge(simulatedAge);

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			extracted.setItemSlot(slot, animal.getItemBySlot(slot).copy());
		}
		if (animal instanceof VariantHolder<?> vSrc && extracted instanceof VariantHolder<?> vDst) {
			copyVariant(vSrc, vDst);
		}
		if (animal instanceof Sheep sSrc && extracted instanceof Sheep sDst) {
			sDst.setColor(sSrc.getColor());
			sDst.setSheared(sSrc.isSheared());
		}

		((StackableEntity) extracted).jarstacker$setStackCount(1);
		level.addFreshEntity(extracted);

		// Temporarily decrement anchor stack count
		int remainderCount = count - 1;
		((StackableEntity) animal).jarstacker$setStackCount(remainderCount);

		// 3. Vanilla interaction handoff
		InteractionResult handoffResult;
		try {
			handoffResult = player.interactOn(extracted, hand);
		} finally {
			// Handled
			// Complete
		}

		if (handoffResult.consumesAction()) {
			int newAge = extracted.getAge();
			if (newAge == simulatedAge && simulatedAge > -200) {
				newAge = Math.min(0, simulatedAge + 20);
				extracted.setAge(newAge);
			}
			if (newAge >= 0) {
				// Matured into adult! Keep extracted as adult singleton or merge into nearby adult stack
				extracted.setBaby(false);
				extracted.setAge(0);
				MobStackingManager.updateLabel(extracted, 1, ModConfig.getInstance().getMobStacking().isShowLabel());

				// Anchor stays at remainderCount
				MobStackingManager.updateLabel(animal, remainderCount, ModConfig.getInstance().getMobStacking().isShowLabel());
				long nextEarliest = state.getEarliestAdultAt();
				if (nextEarliest != Long.MAX_VALUE) {
					animal.setAge((int) Math.min(-1, -(nextEarliest - currentGameTime)));
				}

				if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
					JarStackerMod.LOGGER.info("[JarStackerBaby] FEED_PROMOTED_ADULT anchorRemainder={} adultUUID={}",
						remainderCount, extracted.getUUID());
				}
			} else {
				// Still a baby! Transactional re-virtualization
				long newAdultAt = currentGameTime + (-newAge);
				boolean reVirtualizeOk = false;

				if (!simulateReVirtualizationFailure) {
					state.insert(newAdultAt);
					((StackableEntity) animal).jarstacker$setStackCount(count);
					long nextEarliest = state.getEarliestAdultAt();
					animal.setAge((int) Math.min(-1, -(nextEarliest - currentGameTime)));
					MobStackingManager.updateLabel(animal, count, ModConfig.getInstance().getMobStacking().isShowLabel());
					reVirtualizeOk = true;
				}

				if (reVirtualizeOk) {
					extracted.discard();
					if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
						JarStackerMod.LOGGER.info("[JarStackerBaby] FEED_REVIRTUALIZED count={} newAdultAt={}", count, newAdultAt);
					}
				} else {
					// Insertion failed: NEVER delete the physical entity; let it remain physical!
					extracted.setAge(newAge);
					MobStackingManager.updateLabel(extracted, 1, ModConfig.getInstance().getMobStacking().isShowLabel());
					JarStackerMod.LOGGER.error("[JarStackerBaby] REVIRTUALIZATION_FAILED uuid={} preserved physical singleton", extracted.getUUID());
				}
			}
			return handoffResult;
		} else {
			// Interaction failed / did not consume action: rollback original state
			state.insert(earliestAdultAt);
			((StackableEntity) animal).jarstacker$setStackCount(count);
			MobStackingManager.updateLabel(animal, count, ModConfig.getInstance().getMobStacking().isShowLabel());
			extracted.discard();
			return handoffResult;
		}
	}

	public static void mergeGrowthStates(Mob target, Mob source, int countFromSource) {
		if (!(target instanceof Animal aTarget) || !(source instanceof Animal aSource)) {
			return;
		}
		if (!aTarget.isBaby() || !aSource.isBaby()) {
			return;
		}

		ServerLevel level = (ServerLevel) aTarget.level();
		BabyGrowthState targetState = getOrCreateGrowthState(level, aTarget);
		BabyGrowthState sourceState = getOrCreateGrowthState(level, aSource);

		targetState.mergeFrom(sourceState);
		long earliest = targetState.getEarliestAdultAt();
		if (earliest != Long.MAX_VALUE) {
			aTarget.setAge((int) Math.min(-1, -(earliest - level.getGameTime())));
		}
	}

	public static void splitAndMergeGrowthStates(Mob target, Mob source, int countAbsorbed) {
		if (!(target instanceof Animal aTarget) || !(source instanceof Animal aSource)) {
			return;
		}
		if (!aTarget.isBaby() || !aSource.isBaby()) {
			return;
		}

		ServerLevel level = (ServerLevel) aTarget.level();
		BabyGrowthState targetState = getOrCreateGrowthState(level, aTarget);
		BabyGrowthState sourceState = getOrCreateGrowthState(level, aSource);

		BabyGrowthState split = sourceState.split(countAbsorbed);
		targetState.mergeFrom(split);

		long earliestTarget = targetState.getEarliestAdultAt();
		if (earliestTarget != Long.MAX_VALUE) {
			aTarget.setAge((int) Math.min(-1, -(earliestTarget - level.getGameTime())));
		}

		long earliestSource = sourceState.getEarliestAdultAt();
		if (earliestSource != Long.MAX_VALUE) {
			aSource.setAge((int) Math.min(-1, -(earliestSource - level.getGameTime())));
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void copyVariant(VariantHolder<T> src, VariantHolder<?> dst) {
		((VariantHolder<T>) dst).setVariant(src.getVariant());
	}
}
