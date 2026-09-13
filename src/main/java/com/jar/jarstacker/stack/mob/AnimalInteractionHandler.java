package com.jar.jarstacker.stack.mob;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AnimalInteractionHandler {

	private static final ThreadLocal<Boolean> HANDOFF_ACTIVE = ThreadLocal.withInitial(() -> false);

	public static void register() {
		// Handled by MobInteractionHandler in V0.3.0
	}

	public static Animal splitOneForInteraction(ServerLevel level, Animal clickedAnimal, int currentCount) {
		BreedingDiagnostics.logEvent("SPLIT_BEGIN", clickedAnimal, "currentCount=" + currentCount);
		int remainderCount = currentCount - 1;

		// 1. Clicked animal becomes a physical singleton (count = 1)
		((StackableEntity) clickedAnimal).jarstacker$setStackCount(1);
		MobStackingManager.updateLabel(clickedAnimal, 1, ModConfig.getInstance().getMobStacking().isShowLabel());

		// 2. Spawn a remainder animal of identical type with count = remainderCount
		@SuppressWarnings("unchecked")
		EntityType<Animal> type = (EntityType<Animal>) clickedAnimal.getType();
		Animal remainder = type.create(level);
		if (remainder != null) {
			MovementDiagnostics.logMovementEvent("ENTITY_CREATED", remainder, null, "role=remainder");

			java.util.Set<net.minecraft.world.phys.AABB> reserved = java.util.Collections.singleton(clickedAnimal.getBoundingBox());
			Vec3 safePos = SplitPlacementResolver.findSafeSplitPosition(level, clickedAnimal, remainder, reserved);
			MovementDiagnostics.logMovementEvent("SPLIT_POSITION_SELECTED", remainder, null, "target=" + safePos);

			remainder.moveTo(
				safePos.x,
				safePos.y,
				safePos.z,
				clickedAnimal.getYRot(),
				clickedAnimal.getXRot()
			);
			remainder.setDeltaMovement(Vec3.ZERO);

			remainder.setHealth(clickedAnimal.getHealth());
			remainder.setAge(clickedAnimal.getAge());

			for (EquipmentSlot slot : EquipmentSlot.values()) {
				remainder.setItemSlot(slot, clickedAnimal.getItemBySlot(slot).copy());
			}

			if (clickedAnimal instanceof VariantHolder<?> vSrc && remainder instanceof VariantHolder<?> vRem) {
				copyVariant(vSrc, vRem);
			}
			if (clickedAnimal instanceof Sheep sSrc && remainder instanceof Sheep sRem) {
				sRem.setColor(sSrc.getColor());
				sRem.setSheared(sSrc.isSheared());
			}

			((StackableEntity) remainder).jarstacker$setStackCount(remainderCount);
			MobStackingManager.updateLabel(remainder, remainderCount, ModConfig.getInstance().getMobStacking().isShowLabel());

			level.addFreshEntity(remainder);
			MovementDiagnostics.logMovementEvent("ENTITY_ADDED_TO_WORLD", remainder, null, "role=remainder");
			BreedingDiagnostics.logEvent("SPLIT_COMPLETE", remainder, "source=" + clickedAnimal.getUUID());
			return remainder;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T> void copyVariant(VariantHolder<T> src, VariantHolder<?> dst) {
		((VariantHolder<T>) dst).setVariant(src.getVariant());
	}

	public static Animal extractBreedingPartner(ServerLevel level, Animal parentA, int currentCount) {
		if (currentCount < 2) return null;

		BreedingDiagnostics.logEvent("PAIR_EXTRACTION_BEGIN", parentA, "currentCount=" + currentCount);

		// 1. parentA becomes locked physical singleton (count = 1)
		((StackableEntity) parentA).jarstacker$setStackCount(1);
		((StackableEntity) parentA).jarstacker$setBreedingLockTicks(300);
		MobStackingManager.updateLabel(parentA, 1, ModConfig.getInstance().getMobStacking().isShowLabel());

		// 2. Spawn parentB with count = 1, locked for 300 ticks, in love
		@SuppressWarnings("unchecked")
		EntityType<Animal> type = (EntityType<Animal>) parentA.getType();
		Animal parentB = type.create(level);
		if (parentB == null) return null;

		MovementDiagnostics.logMovementEvent("ENTITY_CREATED", parentB, null, "role=parentB");

		java.util.Set<net.minecraft.world.phys.AABB> reserved = new java.util.HashSet<>();
		reserved.add(parentA.getBoundingBox());

		Vec3 posB = SplitPlacementResolver.findSafeSplitPosition(level, parentA, parentB, reserved);
		MovementDiagnostics.logMovementEvent("SPLIT_POSITION_SELECTED", parentB, null, "target=" + posB);

		parentB.moveTo(
			posB.x,
			posB.y,
			posB.z,
			parentA.getYRot(),
			parentA.getXRot()
		);
		parentB.setDeltaMovement(Vec3.ZERO);
		reserved.add(parentB.getBoundingBox());

		parentB.setHealth(parentA.getHealth());
		parentB.setAge(parentA.getAge());
		parentB.setInLove(null);

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			parentB.setItemSlot(slot, parentA.getItemBySlot(slot).copy());
		}
		if (parentA instanceof VariantHolder<?> vSrc && parentB instanceof VariantHolder<?> vDst) {
			copyVariant(vSrc, vDst);
		}
		if (parentA instanceof Sheep sSrc && parentB instanceof Sheep sDst) {
			sDst.setColor(sSrc.getColor());
			sDst.setSheared(sSrc.isSheared());
		}

		((StackableEntity) parentB).jarstacker$setStackCount(1);
		((StackableEntity) parentB).jarstacker$setBreedingLockTicks(300);
		MobStackingManager.updateLabel(parentB, 1, ModConfig.getInstance().getMobStacking().isShowLabel());
		level.addFreshEntity(parentB);
		MovementDiagnostics.logMovementEvent("ENTITY_ADDED_TO_WORLD", parentB, null, "role=parentB");

		// 3. If currentCount > 2, spawn remainder stack with count = currentCount - 2, in love
		if (currentCount > 2) {
			int remainderCount = currentCount - 2;
			Animal remainder = type.create(level);
			if (remainder != null) {
				MovementDiagnostics.logMovementEvent("ENTITY_CREATED", remainder, null, "role=remainder");

				Vec3 posRem = SplitPlacementResolver.findSafeSplitPosition(level, parentA, remainder, reserved);
				MovementDiagnostics.logMovementEvent("SPLIT_POSITION_SELECTED", remainder, null, "target=" + posRem);

				remainder.moveTo(
					posRem.x,
					posRem.y,
					posRem.z,
					parentA.getYRot(),
					parentA.getXRot()
				);
				remainder.setDeltaMovement(Vec3.ZERO);

				remainder.setHealth(parentA.getHealth());
				remainder.setAge(parentA.getAge());
				remainder.setInLove(null);

				for (EquipmentSlot slot : EquipmentSlot.values()) {
					remainder.setItemSlot(slot, parentA.getItemBySlot(slot).copy());
				}
				if (parentA instanceof VariantHolder<?> vSrc && remainder instanceof VariantHolder<?> vDst) {
					copyVariant(vSrc, vDst);
				}
				if (parentA instanceof Sheep sSrc && remainder instanceof Sheep sDst) {
					sDst.setColor(sSrc.getColor());
					sDst.setSheared(sSrc.isSheared());
				}

				((StackableEntity) remainder).jarstacker$setStackCount(remainderCount);
				MobStackingManager.updateLabel(remainder, remainderCount, ModConfig.getInstance().getMobStacking().isShowLabel());
				level.addFreshEntity(remainder);
				MovementDiagnostics.logMovementEvent("ENTITY_ADDED_TO_WORLD", remainder, null, "role=remainder");
				BreedingDiagnostics.logEvent("PAIR_EXTRACTION_REMAINDER", remainder, "remainderCount=" + remainderCount);
			}
		}

		BreedingDiagnostics.logEvent("PAIR_EXTRACTION_COMPLETE", parentB, "partnerOf=" + parentA.getUUID());
		return parentB;
	}
}

