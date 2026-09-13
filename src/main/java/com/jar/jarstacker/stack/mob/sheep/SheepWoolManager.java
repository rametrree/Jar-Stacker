package com.jar.jarstacker.stack.mob.sheep;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.MobCompatibility;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import com.jar.jarstacker.stack.mob.SplitPlacementResolver;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

public class SheepWoolManager {

	public static void handleSheepRegrowth(Sheep sheep) {
		if (!(sheep.level() instanceof ServerLevel level) || !sheep.isAlive() || sheep.isRemoved()) {
			return;
		}

		int count = ((StackableEntity) sheep).jarstacker$getStackCount();
		if (count <= 1) {
			// Single sheep: Vanilla ate() has set sheared=false; nothing to split or re-shear
			return;
		}

		// Stack of Sheared Sheep ×N:
		// Exactly 1 logical sheep regrows into unsheared state.
		// The representative must remain sheared=true with count = N - 1.
		int remainderCount = count - 1;
		sheep.setSheared(true);
		((StackableEntity) sheep).jarstacker$setStackCount(remainderCount);
		MobStackingManager.updateLabel(sheep, remainderCount, ModConfig.getInstance().getMobStacking().isShowLabel());

		// Transfer 1 regrown unsheared sheep: merge into existing unsheared stack or spawn singleton
		ModConfig config = ModConfig.getInstance();
		double radius = MobCompatibility.getRadius(sheep, config.getMobStacking());
		AABB searchBox = sheep.getBoundingBox().inflate(radius);

		List<Sheep> nearbyUnsheared = level.getEntitiesOfClass(Sheep.class, searchBox, other ->
			other != sheep && other.isAlive() && !other.isRemoved() && !other.isSheared()
			&& other.getColor() == sheep.getColor()
			&& other.isBaby() == sheep.isBaby()
			&& !MobCompatibility.isExcluded(other)
			&& !((StackableEntity) other).jarstacker$isInteractionLocked()
		);

		boolean merged = false;
		for (Sheep target : nearbyUnsheared) {
			int targetCount = ((StackableEntity) target).jarstacker$getStackCount();
			int maxStack = MobCompatibility.getMaxStackSize(target, config.getMobStacking());
			if (targetCount + 1 <= maxStack) {
				int newTotal = targetCount + 1;
				((StackableEntity) target).jarstacker$setStackCount(newTotal);
				MobStackingManager.updateLabel(target, newTotal, config.getMobStacking().isShowLabel());
				merged = true;
				if (config.getPerformance().isDebugLogging()) {
					JarStackerMod.LOGGER.info("[JarStackerSheep] SHEEP_REGROW_MERGE shearedRem={} mergedInto={}",
						remainderCount, target.getUUID());
				}
				break;
			}
		}

		if (!merged) {
			Sheep regrown = com.jar.jarstacker.adapter.EntityAdapter.create(EntityType.SHEEP, level);
			if (regrown != null) {
				Vec3 safePos = SplitPlacementResolver.findSafeSplitPosition(level, sheep, regrown, Collections.singleton(sheep.getBoundingBox()));
				regrown.moveTo(safePos.x, safePos.y, safePos.z, sheep.getYRot(), sheep.getXRot());
				regrown.setColor(sheep.getColor());
				regrown.setSheared(false);
				regrown.setAge(sheep.getAge());
				regrown.setHealth(sheep.getHealth());

				for (EquipmentSlot slot : EquipmentSlot.values()) {
					regrown.setItemSlot(slot, sheep.getItemBySlot(slot).copy());
				}

				((StackableEntity) regrown).jarstacker$setStackCount(1);
				MobStackingManager.updateLabel(regrown, 1, config.getMobStacking().isShowLabel());
				level.addFreshEntity(regrown);

				if (config.getPerformance().isDebugLogging()) {
					JarStackerMod.LOGGER.info("[JarStackerSheep] SHEEP_REGROW_ONE shearedRem={} regrownUUID={}",
						remainderCount, regrown.getUUID());
				}
			}
		}
	}
}

