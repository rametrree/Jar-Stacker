package com.jar.jarstacker.stack.item;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.util.StackMetrics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ItemStackingManager {

	public static void updateLabel(ItemEntity entity, int logicalCount, boolean showLabel) {
		ItemStack stack = entity.getItem();
		ModConfig.DisplayConfig display = ModConfig.getInstance().getDisplay();
		boolean showItemLabels = display.isShowItemLabels() && showLabel;
		boolean onlyWhenStacked = display.isShowCountOnlyWhenStacked();

		boolean shouldDisplay = showItemLabels && (onlyWhenStacked ? logicalCount > 1 : logicalCount >= 1);

		if (shouldDisplay) {
			ChatFormatting rarityColor = stack.getRarity().color();
			MutableComponent name = Component.empty().append(stack.getHoverName()).withStyle(rarityColor);
			String formattedCount = String.format("%,d", logicalCount);
			MutableComponent count = Component.literal(" ×" + formattedCount).withStyle(ChatFormatting.YELLOW);
			Component label = Component.empty().append(name).append(count);
			entity.setCustomName(label);
			entity.setCustomNameVisible(true);
		} else {
			entity.setCustomName(null);
			entity.setCustomNameVisible(false);
		}
	}

	public static void scanAndStack(ServerLevel level, ModConfig config) {
		long start = System.nanoTime();
		ModConfig.ItemStackingConfig itemConfig = config.getItemStacking();
		double radius = itemConfig.getRadius();
		boolean showLabel = itemConfig.isShowLabel();

		List<? extends ItemEntity> allItems = level.getEntities(EntityTypeTest.forClass(ItemEntity.class), Entity::isAlive);
		if (allItems.isEmpty()) {
			StackMetrics.recordItemScan(System.nanoTime() - start);
			return;
		}

		if (allItems.size() == 1) {
			ItemEntity single = allItems.get(0);
			if (single.isAlive() && !single.isRemoved()) {
				int count = ((StackableEntity) single).jarstacker$getStackCount();
				if (count <= 0) count = single.getItem().getCount();
				updateLabel(single, count, showLabel);
			}
			StackMetrics.recordItemScan(System.nanoTime() - start);
			return;
		}

		Set<UUID> processed = new HashSet<>();

		for (ItemEntity itemEntity : allItems) {
			if (!itemEntity.isAlive() || itemEntity.isRemoved() || processed.contains(itemEntity.getUUID())) {
				continue;
			}

			int maxStackA = ItemCompatibility.getMaxStackSize(itemEntity.getItem(), itemConfig);
			int countA = ((StackableEntity) itemEntity).jarstacker$getStackCount();
			if (countA <= 0) countA = itemEntity.getItem().getCount();
			updateLabel(itemEntity, countA, showLabel);
			if (countA >= maxStackA) {
				processed.add(itemEntity.getUUID());
				continue;
			}

			processed.add(itemEntity.getUUID());

			AABB box = itemEntity.getBoundingBox().inflate(radius);
			List<ItemEntity> nearby = level.getEntitiesOfClass(ItemEntity.class, box, other ->
				other != itemEntity && other.isAlive() && !other.isRemoved() && !processed.contains(other.getUUID())
			);

			for (ItemEntity other : nearby) {
				if (!other.isAlive() || other.isRemoved() || processed.contains(other.getUUID())) {
					continue;
				}

				if (ItemCompatibility.canStack(itemEntity, other, itemConfig)) {
					int countB = ((StackableEntity) other).jarstacker$getStackCount();
					if (countB <= 0) countB = other.getItem().getCount();

					if (countA >= maxStackA) {
						break;
					}

					// Merge animation particles and sound
					level.sendParticles(ParticleTypes.POOF, other.getX(), other.getY() + 0.15, other.getZ(), 4, 0.1, 0.1, 0.1, 0.02);
					level.sendParticles(ParticleTypes.HAPPY_VILLAGER, itemEntity.getX(), itemEntity.getY() + 0.25, itemEntity.getZ(), 6, 0.15, 0.15, 0.15, 0.02);
					level.playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.AMBIENT, 0.35f, 1.8f);

					int total = countA + countB;
					if (total <= maxStackA) {
						countA = total;
						((StackableEntity) itemEntity).jarstacker$setStackCount(countA);
						ItemStack stackA = itemEntity.getItem();
						stackA.setCount(Math.min(stackA.getMaxStackSize(), countA));
						updateLabel(itemEntity, countA, showLabel);

						other.discard();
						processed.add(other.getUUID());
						StackMetrics.recordItemMerge(countB);

						if (config.getPerformance().isDebugLogging()) {
							JarStackerMod.LOGGER.info("Merged {} items into {} (total: {})", countB, itemEntity.getUUID(), countA);
						}
					} else {
						int absorbed = maxStackA - countA;
						countA = maxStackA;
						((StackableEntity) itemEntity).jarstacker$setStackCount(countA);
						ItemStack stackA = itemEntity.getItem();
						stackA.setCount(Math.min(stackA.getMaxStackSize(), countA));
						updateLabel(itemEntity, countA, showLabel);

						int remainder = countB - absorbed;
						((StackableEntity) other).jarstacker$setStackCount(remainder);
						ItemStack stackB = other.getItem();
						stackB.setCount(Math.min(stackB.getMaxStackSize(), remainder));
						updateLabel(other, remainder, showLabel);

						processed.add(other.getUUID());
						StackMetrics.recordItemMerge(absorbed);

						if (config.getPerformance().isDebugLogging()) {
							JarStackerMod.LOGGER.info("Partially merged {} items into {} (new: {}, remaining: {})", absorbed, itemEntity.getUUID(), countA, remainder);
						}
						break;
					}
				}
			}
		}

		StackMetrics.recordItemScan(System.nanoTime() - start);
	}
}