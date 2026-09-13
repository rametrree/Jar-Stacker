package com.jar.jarstacker.stack.mob;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.util.StackMetrics;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MobStackingManager {

	public static void updateLabel(Mob mob, int count, boolean showLabel) {
		ModConfig.DisplayConfig display = ModConfig.getInstance().getDisplay();
		boolean showMobLabels = display.isShowMobLabels() && showLabel;
		boolean onlyWhenStacked = display.isShowCountOnlyWhenStacked();

		boolean shouldDisplay = showMobLabels && (onlyWhenStacked ? count > 1 : count >= 1);

		if (shouldDisplay) {
			Component typeName = mob.getType().getDescription();
			String formattedCount = String.format("%,d", count);
			MutableComponent countComponent = Component.literal(" ×" + formattedCount).withStyle(ChatFormatting.YELLOW);
			Component label = Component.empty().append(typeName).append(countComponent);
			mob.setCustomName(label);
			mob.setCustomNameVisible(true);
			((StackableEntity) mob).jarstacker$setManagedLabel(true);
		} else {
			// Only clear custom name if it was managed by Jar Stacker; preserve player Name Tags
			if (((StackableEntity) mob).jarstacker$hasManagedLabel()) {
				mob.setCustomName(null);
				mob.setCustomNameVisible(false);
				((StackableEntity) mob).jarstacker$setManagedLabel(false);
			}
		}
	}

	public static void scanAndStack(ServerLevel level, ModConfig config) {
		long start = System.nanoTime();
		ModConfig.MobStackingConfig mobConfig = config.getMobStacking();
		boolean showLabel = mobConfig.isShowLabel();

		List<? extends Mob> allMobs = level.getEntities(EntityTypeTest.forClass(Mob.class), Entity::isAlive);
		if (allMobs.isEmpty()) {
			StackMetrics.recordMobScan(System.nanoTime() - start);
			return;
		}

		if (allMobs.size() == 1) {
			Mob single = allMobs.get(0);
			if (single.isAlive() && !single.isRemoved() && !MobCompatibility.isExcluded(single)) {
				int count = ((StackableEntity) single).jarstacker$getStackCount();
				if (count <= 0) count = 1;
				updateLabel(single, count, showLabel);
			}
			StackMetrics.recordMobScan(System.nanoTime() - start);
			return;
		}

		for (Mob mob : allMobs) {
			if (mob instanceof net.minecraft.world.entity.animal.Animal animal && animal.isBaby() && animal.isAlive() && !animal.isRemoved()) {
				com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, animal);
			}
		}

		Set<UUID> processed = new HashSet<>();

		for (Mob mob : allMobs) {
			if (!mob.isAlive() || mob.isRemoved() || processed.contains(mob.getUUID()) || MobCompatibility.isExcluded(mob)) {
				continue;
			}

			int maxStackA = MobCompatibility.getMaxStackSize(mob, mobConfig);
			double radiusA = MobCompatibility.getRadius(mob, mobConfig);

			int countA = ((StackableEntity) mob).jarstacker$getStackCount();
			if (countA <= 0) countA = 1;
			updateLabel(mob, countA, showLabel);

			if (countA >= maxStackA) {
				processed.add(mob.getUUID());
				continue;
			}

			processed.add(mob.getUUID());

			AABB box = mob.getBoundingBox().inflate(radiusA);
			List<Mob> nearby = level.getEntitiesOfClass(Mob.class, box, other ->
				other != mob && other.isAlive() && !other.isRemoved() && !processed.contains(other.getUUID()) && !MobCompatibility.isExcluded(other)
			);

			for (Mob other : nearby) {
				if (!other.isAlive() || other.isRemoved() || processed.contains(other.getUUID())) {
					continue;
				}

				if (MobCompatibility.canStack(mob, other, mobConfig)) {
					int countB = ((StackableEntity) other).jarstacker$getStackCount();
					if (countB <= 0) countB = 1;

					if (countA >= maxStackA) {
						break;
					}

					// Merge animation particles and sound
					level.sendParticles(ParticleTypes.POOF, other.getX(), other.getY() + other.getBbHeight() * 0.5, other.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
					level.sendParticles(ParticleTypes.PORTAL, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 10, 0.25, 0.25, 0.25, 0.08);
					level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.CHICKEN_EGG, SoundSource.NEUTRAL, 0.6f, 1.2f);

					int total = countA + countB;
					if (mob instanceof net.minecraft.world.entity.animal.Animal aAnim) {
						BreedingDiagnostics.logEvent("STACK_MERGE", aAnim, "mergedFrom=" + other.getUUID() + " countAdded=" + countB);
					}
					if (total <= maxStackA) {
						if (mob instanceof net.minecraft.world.entity.animal.Animal && mob.isBaby()) {
							com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.mergeGrowthStates(mob, other, countB);
						}
						com.jar.jarstacker.stack.mob.health.LogicalHealthManager.mergeHealthStates(mob, other, countB);

						countA = total;
						((StackableEntity) mob).jarstacker$setStackCount(countA);
						updateLabel(mob, countA, showLabel);

						other.discard();
						processed.add(other.getUUID());
						StackMetrics.recordMobMerge(countB);

						if (config.getPerformance().isDebugLogging()) {
							JarStackerMod.LOGGER.info("Merged mob {} into {} (total: {})", other.getUUID(), mob.getUUID(), countA);
						}
					} else {
						int absorbed = maxStackA - countA;
						int remainder = countB - absorbed;

						if (mob instanceof net.minecraft.world.entity.animal.Animal && mob.isBaby()) {
							com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.splitAndMergeGrowthStates(mob, other, absorbed);
						}
						com.jar.jarstacker.stack.mob.health.LogicalHealthManager.splitAndMergeHealthStates(mob, other, absorbed);

						countA = maxStackA;
						((StackableEntity) mob).jarstacker$setStackCount(countA);
						updateLabel(mob, countA, showLabel);

						((StackableEntity) other).jarstacker$setStackCount(remainder);
						updateLabel(other, remainder, showLabel);

						processed.add(other.getUUID());
						StackMetrics.recordMobMerge(absorbed);

						if (config.getPerformance().isDebugLogging()) {
							JarStackerMod.LOGGER.info("Partially merged mob {} into {} (new: {}, remaining: {})", other.getUUID(), mob.getUUID(), countA, remainder);
						}
						break;
					}
				}
			}
		}

		StackMetrics.recordMobScan(System.nanoTime() - start);
	}
}