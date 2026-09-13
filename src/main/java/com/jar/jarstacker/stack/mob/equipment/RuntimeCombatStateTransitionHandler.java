package com.jar.jarstacker.stack.mob.equipment;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.MobCompatibility;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import com.jar.jarstacker.stack.mob.health.LogicalHealthManager;
import com.jar.jarstacker.stack.mob.health.LogicalHealthState;
import com.jar.jarstacker.stack.mob.SplitPlacementResolver;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager;
import com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState;
import com.jar.jarstacker.stack.mob.status.LogicalStatusRecord;
import com.jar.jarstacker.stack.mob.status.LogicalBurnState;
import com.jar.jarstacker.stack.mob.status.LogicalBurnRecord;

/**
 * Handles runtime equipment transitions for stacked mobs.
 * When a stacked mob acquires damageable equipment, extracts exactly one logical mob
 * as a physical equipped singleton with its active health, leaving the remainder
 * stack unequipped at the anchor position.
 */
public class RuntimeCombatStateTransitionHandler {

	private static final ThreadLocal<Boolean> IS_TRANSITIONING = ThreadLocal.withInitial(() -> false);
	public static volatile boolean failNextExtractionForTesting = false;

	public static boolean isTransitioning() {
		return IS_TRANSITIONING.get();
	}

	public static void onEquipmentChanged(Mob mob, EquipmentSlot slot, ItemStack stack) {
		if (IS_TRANSITIONING.get() || mob == null || mob.level().isClientSide() || stack == null || stack.isEmpty()) {
			return;
		}

		if (!MobCompatibility.isDamageableItem(stack)) {
			return;
		}

		if (!(((Object) mob) instanceof StackableEntity stackable)) {
			return;
		}

		int count = stackable.jarstacker$getStackCount();
		if (count <= 1) {
			return;
		}

		if (!(mob.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		extractEquippedMob(mob, slot, stack);
	}

	public static Mob extractEquippedMob(Mob mob, EquipmentSlot slot, ItemStack stack) {
		IS_TRANSITIONING.set(true);
		try {
			StackableEntity stackable = (StackableEntity) mob;
			int sourceCount = stackable.jarstacker$getStackCount();
			if (sourceCount <= 1 || !(mob.level() instanceof ServerLevel serverLevel)) {
				return null;
			}

			if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
				JarStackerMod.LOGGER.debug("[JarStacker] RUNTIME_EQUIPMENT_UNSAFE type={} count={} slot={} item={}",
					BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()), sourceCount, slot, stack.getItem());
			}

			LogicalHealthState state = LogicalHealthManager.getOrCreateState(mob);
			float activeHp = state != null ? state.getActiveHealth() : mob.getHealth();
			if (activeHp <= 0.0f) {
				activeHp = mob.getMaxHealth();
			}

			if (failNextExtractionForTesting) {
				failNextExtractionForTesting = false;
				JarStackerMod.LOGGER.warn("[JarStacker] RUNTIME_EQUIPMENT_ROLLBACK simulated test failure");
				return null;
			}

			// Capture and extract active health record
			float extractedHp = state != null ? state.extractActive() : activeHp;
			if (extractedHp <= 0.0f) {
				extractedHp = mob.getMaxHealth();
			}

			int remainderCount = sourceCount - 1;
			stackable.jarstacker$setStackCount(remainderCount);

			float nextActiveHp = state != null ? state.getActiveHealth() : mob.getMaxHealth();
			if (nextActiveHp <= 0.0f) {
				nextActiveHp = mob.getMaxHealth();
			}
			mob.setHealth(nextActiveHp);

			// Clear acquired equipment from remainder stack representative
			mob.setItemSlot(slot, ItemStack.EMPTY);

			// Materialize extracted singleton mob
			Entity rawEntity = com.jar.jarstacker.adapter.EntityAdapter.create(mob.getType(), serverLevel);
			if (!(rawEntity instanceof Mob extractedMob)) {
				// Rollback
				if (state != null) {
					state.add(0, extractedHp);
				}
				stackable.jarstacker$setStackCount(sourceCount);
				mob.setHealth(extractedHp);
				mob.setItemSlot(slot, stack);
				JarStackerMod.LOGGER.error("[JarStacker] RUNTIME_EQUIPMENT_ROLLBACK reason=FAILED_CREATION type={}", mob.getType());
				return null;
			}

			// Safe split placement
			Vec3 spawnPos = SplitPlacementResolver.findSafeSplitPosition(serverLevel, mob, extractedMob, java.util.Set.of(mob.getBoundingBox()));
			if (spawnPos == null) {
				spawnPos = mob.position();
			}
			extractedMob.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, mob.getYRot(), mob.getXRot());

			// Assign transferred equipment, health, and stack count to extracted singleton
			extractedMob.setItemSlot(slot, stack.copy());
			extractedMob.setHealth(extractedHp);
			((StackableEntity) extractedMob).jarstacker$setStackCount(1);
			((StackableEntity) extractedMob).jarstacker$setManagedLabel(false);

			LogicalHealthState extractedHealthState = new LogicalHealthState();
			extractedHealthState.add(extractedHp);
			((StackableEntity) extractedMob).jarstacker$setLogicalHealthState(extractedHealthState);

			com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.extractState(mob, extractedMob);

			extractedMob.setDropChance(slot, 2.0f);

			if (mob instanceof Animal parentAnimal && extractedMob instanceof Animal childAnimal && stackable.jarstacker$getBabyGrowthState() != null) {
				long adultAt = stackable.jarstacker$getBabyGrowthState().extractEarliest();
				int age = (int) Math.min(-1, serverLevel.getGameTime() - adultAt);
				childAnimal.setAge(age);
			}

			// Commit both entities
			serverLevel.addFreshEntity(extractedMob);
			MobStackingManager.updateLabel(mob, remainderCount, ModConfig.getInstance().getMobStacking().isShowLabel());
			MobStackingManager.updateLabel(extractedMob, 1, ModConfig.getInstance().getMobStacking().isShowLabel());

			JarStackerMod.LOGGER.info("[JarStacker] RUNTIME_EQUIPMENT_COMMIT type={} sourceCount={} remainderCount={} extractedHp={} slot={} item={}",
				BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()), sourceCount, remainderCount, extractedHp, slot, stack.getItem());

			return extractedMob;
		} finally {
			IS_TRANSITIONING.set(false);
		}
	}

	public static Mob extractUnsupportedMob(Mob mob, MobEffectInstance triggerEffect) {
		IS_TRANSITIONING.set(true);
		try {
			StackableEntity stackable = (StackableEntity) mob;
			int sourceCount = stackable.jarstacker$getStackCount();
			if (sourceCount <= 1 || !(mob.level() instanceof ServerLevel serverLevel)) {
				return null;
			}

			if (failNextExtractionForTesting) {
				failNextExtractionForTesting = false;
				JarStackerMod.LOGGER.warn("[JarStacker] UNSUPPORTED_EFFECT_ROLLBACK simulated test failure");
				return null;
			}

			LogicalHealthState healthState = LogicalHealthManager.getOrCreateState(mob);
			float activeHp = healthState != null ? healthState.getActiveHealth() : mob.getHealth();
			if (activeHp <= 0.0f) {
				activeHp = mob.getMaxHealth();
			}

			// Capture and extract active health record
			float extractedHp = healthState != null ? healthState.extractActive() : activeHp;
			if (extractedHp <= 0.0f) {
				extractedHp = mob.getMaxHealth();
			}

			LogicalStatusEffectState sourceStatus = LogicalStatusEffectManager.getOrCreateStatusState(mob);
			LogicalStatusRecord extractedStatusRecord = sourceStatus != null ? sourceStatus.extractActive() : new LogicalStatusRecord();

			LogicalBurnState sourceBurn = LogicalStatusEffectManager.getOrCreateBurnState(mob);
			LogicalBurnRecord extractedBurnRecord = sourceBurn != null ? sourceBurn.extractActive() : new LogicalBurnRecord();

			int remainderCount = sourceCount - 1;
			stackable.jarstacker$setStackCount(remainderCount);

			float nextActiveHp = healthState != null ? healthState.getActiveHealth() : mob.getMaxHealth();
			if (nextActiveHp <= 0.0f) {
				nextActiveHp = mob.getMaxHealth();
			}
			mob.setHealth(nextActiveHp);

			// Materialize extracted singleton mob
			Entity rawEntity = com.jar.jarstacker.adapter.EntityAdapter.create(mob.getType(), serverLevel);
			if (!(rawEntity instanceof Mob extractedMob)) {
				// Rollback
				if (healthState != null) {
					healthState.add(0, extractedHp);
				}
				if (sourceStatus != null) {
					sourceStatus.add(0, extractedStatusRecord);
				}
				if (sourceBurn != null) {
					sourceBurn.add(0, extractedBurnRecord);
				}
				stackable.jarstacker$setStackCount(sourceCount);
				mob.setHealth(extractedHp);
				LogicalStatusEffectManager.projectActiveMember(mob);
				JarStackerMod.LOGGER.error("[JarStacker] UNSUPPORTED_EFFECT_ROLLBACK reason=FAILED_CREATION type={}", mob.getType());
				return null;
			}

			// Safe split placement
			Vec3 spawnPos = SplitPlacementResolver.findSafeSplitPosition(serverLevel, mob, extractedMob, java.util.Set.of(mob.getBoundingBox()));
			if (spawnPos == null) {
				spawnPos = mob.position();
			}
			extractedMob.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, mob.getYRot(), mob.getXRot());

			// Configure extracted singleton mob
			extractedMob.setHealth(extractedHp);
			((StackableEntity) extractedMob).jarstacker$setStackCount(1);
			((StackableEntity) extractedMob).jarstacker$setManagedLabel(false);
			((StackableEntity) extractedMob).jarstacker$setLogicalStatusEffectState(null); // Pure Vanilla native effect management

			LogicalHealthState extractedHealthState = new LogicalHealthState();
			extractedHealthState.add(extractedHp);
			((StackableEntity) extractedMob).jarstacker$setLogicalHealthState(extractedHealthState);

			// Transfer existing supported effects to Vanilla native effects map
			if (extractedStatusRecord != null) {
				for (MobEffectInstance instance : extractedStatusRecord.getEffectInstances()) {
					extractedMob.addEffect(new MobEffectInstance(instance));
				}
			}

			// Transfer burn ticks
			if (extractedBurnRecord != null && extractedBurnRecord.isBurning()) {
				extractedMob.setRemainingFireTicks(extractedBurnRecord.getRemainingFireTicks());
			}

			// Apply trigger unsupported effect natively
			if (triggerEffect != null) {
				extractedMob.addEffect(new MobEffectInstance(triggerEffect));
			}

			// Handle baby animal age if applicable
			if (mob instanceof Animal parentAnimal && extractedMob instanceof Animal childAnimal && stackable.jarstacker$getBabyGrowthState() != null) {
				long adultAt = stackable.jarstacker$getBabyGrowthState().extractEarliest();
				int age = (int) Math.min(-1, serverLevel.getGameTime() - adultAt);
				childAnimal.setAge(age);
			}

			// Project remainder stack
			if (sourceStatus != null && !sourceStatus.isEmpty()) {
				LogicalStatusEffectManager.onActiveMemberSwitched(mob, extractedStatusRecord, sourceStatus.get(0));
			}

			// Commit both entities
			serverLevel.addFreshEntity(extractedMob);
			MobStackingManager.updateLabel(mob, remainderCount, ModConfig.getInstance().getMobStacking().isShowLabel());
			MobStackingManager.updateLabel(extractedMob, 1, ModConfig.getInstance().getMobStacking().isShowLabel());

			JarStackerMod.LOGGER.info("[JarStacker] UNSUPPORTED_EFFECT_COMMIT type={} sourceCount={} remainderCount={} extractedHp={} effect={}",
				BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()), sourceCount, remainderCount, extractedHp,
				triggerEffect != null ? triggerEffect.getEffect().getRegisteredName() : "none");

			return extractedMob;
		} finally {
			IS_TRANSITIONING.set(false);
		}
	}
}
