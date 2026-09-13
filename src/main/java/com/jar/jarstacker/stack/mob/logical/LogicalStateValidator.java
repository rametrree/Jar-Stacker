package com.jar.jarstacker.stack.mob.logical;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.baby.BabyGrowthState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;

import java.util.List;

public class LogicalStateValidator {

	public static boolean strictMode = false;

	public enum ValidationStatus {
		VALID,
		REPAIRABLE,
		CORRUPT
	}

	public record ValidationReport(
		ValidationStatus status,
		String reason,
		int logicalCount,
		int recordCount
	) {}

	public static ValidationReport validateLogicalState(LivingEntity entity) {
		if (entity == null || entity.isRemoved()) {
			return new ValidationReport(ValidationStatus.CORRUPT, "Entity is null or removed", 0, 0);
		}

		int count = ((StackableEntity) entity).jarstacker$getStackCount();
		if (count <= 0) {
			return new ValidationReport(ValidationStatus.CORRUPT, "Stack count <= 0 (" + count + ")", count, 0);
		}

		if (entity instanceof Animal animal && animal.isBaby()) {
			BabyGrowthState state = ((StackableEntity) animal).jarstacker$getBabyGrowthState();
			if (state == null) {
				return new ValidationReport(ValidationStatus.REPAIRABLE, "Missing BabyGrowthState metadata", count, 0);
			}

			int recordCount = state.size();
			if (recordCount != count) {
				String reason = recordCount < count ? "MISSING_LOGICAL_STATE" : "EXTRA_LOGICAL_STATE";
				return new ValidationReport(ValidationStatus.REPAIRABLE, reason, count, recordCount);
			}

			if (!state.isSorted()) {
				return new ValidationReport(ValidationStatus.REPAIRABLE, "UNSORTED_LOGICAL_STATE", count, recordCount);
			}

			if (state.hasInvalidTimestamps()) {
				return new ValidationReport(ValidationStatus.REPAIRABLE, "INVALID_TIMESTAMPS", count, recordCount);
			}
		}

		if (count > 1) {
			com.jar.jarstacker.stack.mob.health.LogicalHealthState healthState = ((StackableEntity) entity).jarstacker$getLogicalHealthState();
			if (healthState != null) {
				int hCount = healthState.size();
				if (hCount != count) {
					String reason = hCount < count ? "MISSING_HEALTH_STATE" : "EXTRA_HEALTH_STATE";
					return new ValidationReport(ValidationStatus.REPAIRABLE, reason, count, hCount);
				}
			}

			com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState = ((StackableEntity) entity).jarstacker$getLogicalStatusEffectState();
			if (statusState != null) {
				int sCount = statusState.size();
				if (sCount != count) {
					String reason = sCount < count ? "MISSING_STATUS_STATE" : "EXTRA_STATUS_STATE";
					return new ValidationReport(ValidationStatus.REPAIRABLE, reason, count, sCount);
				}
			}

			com.jar.jarstacker.stack.mob.status.LogicalBurnState burnState = ((StackableEntity) entity).jarstacker$getLogicalBurnState();
			if (burnState != null) {
				int bCount = burnState.size();
				if (bCount != count) {
					String reason = bCount < count ? "MISSING_BURN_STATE" : "EXTRA_BURN_STATE";
					return new ValidationReport(ValidationStatus.REPAIRABLE, reason, count, bCount);
				}
			}
		}

		return new ValidationReport(ValidationStatus.VALID, "State is valid", count, count);
	}

	public static boolean repairLogicalState(LivingEntity entity) {
		ValidationReport report = validateLogicalState(entity);
		int count = ((StackableEntity) entity).jarstacker$getStackCount();
		com.jar.jarstacker.stack.mob.health.LogicalHealthState healthState = ((StackableEntity) entity).jarstacker$getLogicalHealthState();
		com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState = ((StackableEntity) entity).jarstacker$getLogicalStatusEffectState();
		com.jar.jarstacker.stack.mob.status.LogicalBurnState burnState = ((StackableEntity) entity).jarstacker$getLogicalBurnState();

		if (report.status() == ValidationStatus.VALID && (healthState != null || count <= 1) && (statusState != null || count <= 1) && (burnState != null || count <= 1)) {
			return true;
		}

		if (report.status() == ValidationStatus.CORRUPT) {
			JarStackerMod.LOGGER.error("[JarStacker] CORRUPT_STATE uuid={} type={} reason={}",
				entity.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()), report.reason());
			return false;
		}

		if (count <= 0) count = 1;

		if (entity instanceof Animal animal && animal.isBaby()) {
			BabyGrowthState state = ((StackableEntity) animal).jarstacker$getBabyGrowthState();
			int beforeSize = state != null ? state.size() : 0;
			long gameTime = animal.level() instanceof ServerLevel sl ? sl.getGameTime() : 0L;

			if (state == null) {
				state = new BabyGrowthState();
				state.initializeDefault(count, animal.getAge(), gameTime);
				((StackableEntity) animal).jarstacker$setBabyGrowthState(state);
			} else {
				state.validateAndRepair(count, animal.getAge(), gameTime);
			}

			int afterSize = state.size();

			JarStackerMod.LOGGER.warn("[JarStacker] STATE_REPAIR uuid={} type={} count={} growthEntriesBefore={} growthEntriesAfter={} reason={}",
				animal.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()), count, beforeSize, afterSize, report.reason());
		}

		int hBefore = healthState != null ? healthState.size() : 0;
		float maxHp = entity.getMaxHealth() > 0.0f ? entity.getMaxHealth() : 20.0f;
		float repHp = entity.getHealth();
		float initialHp = Math.max(0.0f, Math.min(repHp, maxHp));

		if (healthState == null) {
			healthState = new com.jar.jarstacker.stack.mob.health.LogicalHealthState();
			healthState.initializeDefault(count, initialHp);
			((StackableEntity) entity).jarstacker$setLogicalHealthState(healthState);
			JarStackerMod.LOGGER.info("[JarStacker] HEALTH_MIGRATION uuid={} type={} count={} initialHp={}",
				entity.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()), count, initialHp);
		} else {
			healthState.validateAndRepair(count, initialHp, maxHp);
			int hAfter = healthState.size();
			if (hBefore != hAfter) {
				JarStackerMod.LOGGER.warn("[JarStacker] COMBAT_STATE_REPAIR uuid={} type={} count={} healthEntriesBefore={} healthEntriesAfter={} reason={}",
					entity.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()), count, hBefore, hAfter, report.reason());
			}
		}

		if (statusState == null) {
			statusState = new com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState();
			statusState.initializeDefault(count);
			((StackableEntity) entity).jarstacker$setLogicalStatusEffectState(statusState);
		} else {
			statusState.validateAndRepair(count);
		}

		if (burnState == null) {
			burnState = new com.jar.jarstacker.stack.mob.status.LogicalBurnState();
			burnState.initializeDefault(count);
			((StackableEntity) entity).jarstacker$setLogicalBurnState(burnState);
		} else {
			burnState.validateAndRepair(count);
		}

		return true;
	}
}

