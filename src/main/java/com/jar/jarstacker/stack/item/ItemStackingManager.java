package com.jar.jarstacker.stack.item;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.util.ItemMergeForensics;
import com.jar.jarstacker.util.StackMetrics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Production Item Stacking Manager for Jar Stacker.
 *
 * Architecture: LATEST ENTITY WINS
 *
 * When compatible item entities are found within the configured radius:
 * 1. The newest ItemEntity (determined deterministically by compareItemAge) becomes the survivor.
 * 2. Older piles transfer their logical counts into the newest entity.
 * 3. Older physical entities are removed (discarded).
 * 4. The newest entity preserves its natural Vanilla motion, position, velocity, and physics with zero teleportation.
 */
public class ItemStackingManager {

	public static void updateLabel(ItemEntity entity, int logicalCount, boolean showLabel) {
		if (entity == null) return;
		ItemStack stack = entity.getItem();
		ModConfig.DisplayConfig display = ModConfig.getInstance().getDisplay();
		boolean showItemLabels = display.isShowItemLabels() && showLabel;
		boolean onlyWhenStacked = display.isShowCountOnlyWhenStacked();

		// Single items (count == 1) hide their nametag when onlyWhenStacked is true (default)
		boolean shouldDisplay = showItemLabels && (onlyWhenStacked ? logicalCount >= 2 : logicalCount >= 1);

		String oldVal = entity.getCustomName() != null ? entity.getCustomName().getString() : "null";
		if (shouldDisplay) {
			ChatFormatting rarityColor = stack.getRarity().color();
			MutableComponent name = Component.empty().append(stack.getHoverName()).withStyle(rarityColor);
			String formattedCount = String.format("%,d", logicalCount);
			MutableComponent count = Component.literal(" ×" + formattedCount).withStyle(ChatFormatting.YELLOW);
			Component label = Component.empty().append(name).append(count);
			entity.setCustomName(label);
			entity.setCustomNameVisible(true);
			ItemMergeForensics.logLabelChange(entity.level().getGameTime(), entity, "updateLabel(display)", oldVal, label.getString());
		} else {
			entity.setCustomName(null);
			entity.setCustomNameVisible(false);
			ItemMergeForensics.logLabelChange(entity.level().getGameTime(), entity, "updateLabel(hidden)", oldVal, "null");
		}
	}

	/**
	 * Compares two ItemEntity instances to determine which is newer.
	 * Returns negative if 'a' is newer than 'b', positive if 'b' is newer than 'a', or 0 if identical.
	 *
	 * Deterministic ordering rules:
	 * 1. Lower age -> newer (item has existed for fewer ticks).
	 * 2. If same age / same game tick:
	 *    - Higher jarstacker$spawnSequence -> newer (atomic increment at entity instantiation).
	 *    - Higher entity runtime ID (getId()) -> newer (allocated sequentially by server).
	 * 3. Deterministic tie-break fallback: UUID comparison.
	 */
	public static int compareItemAge(ItemEntity a, ItemEntity b) {
		if (a == b) return 0;
		if (a == null) return 1;
		if (b == null) return -1;

		// 1. Lower age = newer (comes first)
		int ageCompare = Integer.compare(a.getAge(), b.getAge());
		if (ageCompare != 0) {
			return ageCompare;
		}

		// 2. Same age tie-break: higher spawn sequence = newer
		long seqA = ((StackableEntity) a).jarstacker$getSpawnSequence();
		long seqB = ((StackableEntity) b).jarstacker$getSpawnSequence();
		int seqCompare = Long.compare(seqB, seqA); // higher sequence first
		if (seqCompare != 0) {
			return seqCompare;
		}

		// 3. Higher runtime entity ID = newer
		int idCompare = Integer.compare(b.getId(), a.getId()); // higher id first
		if (idCompare != 0) {
			return idCompare;
		}

		// 4. Absolute deterministic fallback
		return b.getUUID().compareTo(a.getUUID());
	}

	public static boolean isNewer(ItemEntity candidate, ItemEntity current) {
		return compareItemAge(candidate, current) < 0;
	}

	/**
	 * Checks whether two ItemEntities satisfy Vanilla-like contact merge proximity.
	 * Audited against Minecraft Java 1.21.1 ItemEntity.mergeWithNeighbours():
	 *   this.getBoundingBox().inflate(0.5D, 0.0D, 0.5D)
	 */
	public static boolean isInContactRange(ItemEntity a, ItemEntity b) {
		if (a == null || b == null) return false;
		AABB contactBox = a.getBoundingBox().inflate(0.5D, 0.0D, 0.5D);
		return contactBox.intersects(b.getBoundingBox());
	}

	public static ItemEntity mergePair(ItemEntity a, ItemEntity b, ServerLevel level, ModConfig.ItemStackingConfig itemConfig, boolean showLabel) {
		if (!ItemCompatibility.canStack(a, b, itemConfig)) {
			return null;
		}
		if (a.distanceTo(b) > itemConfig.getRadius()) {
			return null;
		}

		// Latest Entity Wins: newer entity survives and absorbs older entity's count
		ItemEntity destination = isNewer(b, a) ? b : a;
		ItemEntity source = (destination == a) ? b : a;

		int maxStack = ItemCompatibility.getMaxStackSize(destination.getItem(), itemConfig);
		int countDest = ((StackableEntity) destination).jarstacker$getStackCount();
		if (countDest <= 0) countDest = destination.getItem().getCount();
		int countSrc = ((StackableEntity) source).jarstacker$getStackCount();
		if (countSrc <= 0) countSrc = source.getItem().getCount();

		if (countDest >= maxStack) {
			return null;
		}

		int total = countDest + countSrc;
		Vec3 oldPos = destination.position();
		if (total <= maxStack) {
			((StackableEntity) destination).jarstacker$setStackCount(total);
			ItemStack stackDest = destination.getItem();
			stackDest.setCount(Math.min(stackDest.getMaxStackSize(), total));
			updateLabel(destination, total, showLabel);
			source.discard();
			ItemMergeForensics.logMergeAB(
				level.getGameTime(),
				"LATEST",
				destination.getUUID(),
				source.getUUID(),
				oldPos,
				destination.position(),
				0.0,
				source.getAge(),
				destination.getAge(),
				countDest,
				total
			);
			return destination;
		} else {
			int absorbed = maxStack - countDest;
			((StackableEntity) destination).jarstacker$setStackCount(maxStack);
			ItemStack stackDest = destination.getItem();
			stackDest.setCount(Math.min(stackDest.getMaxStackSize(), maxStack));
			updateLabel(destination, maxStack, showLabel);

			int remainder = countSrc - absorbed;
			((StackableEntity) source).jarstacker$setStackCount(remainder);
			ItemStack stackSrc = source.getItem();
			stackSrc.setCount(Math.min(stackSrc.getMaxStackSize(), remainder));
			updateLabel(source, remainder, showLabel);
			ItemMergeForensics.logMergeAB(
				level.getGameTime(),
				"LATEST",
				destination.getUUID(),
				source.getUUID(),
				oldPos,
				destination.position(),
				0.0,
				source.getAge(),
				destination.getAge(),
				countDest,
				maxStack
			);
			return destination;
		}
	}

	public static boolean isActive(ItemEntity item) {
		if (item == null || !item.isAlive() || item.isRemoved()) return false;
		return item.getAge() <= 40 || !item.onGround() || item.getDeltaMovement().lengthSqr() > 0.001;
	}

	/**
	 * Adaptive 2/10 scan scheduler:
	 * - Evaluates every 2 ticks if active items exist in the world.
	 * - Evaluates every 10 ticks (base scan interval) for settled items.
	 */
	public static void tick(ServerLevel level, ModConfig config, long gameTime) {
		int itemInterval = config.getItemStacking().getScanIntervalTicks();
		boolean isBaseInterval = (gameTime % itemInterval == 0);
		boolean isFastTick = (gameTime % 2 == 0);

		if (!isBaseInterval && !isFastTick) {
			return;
		}

		if (!isBaseInterval) {
			List<? extends ItemEntity> allItems = level.getEntities(EntityTypeTest.forClass(ItemEntity.class), Entity::isAlive);
			if (allItems.isEmpty()) {
				return;
			}
			boolean hasActive = false;
			for (ItemEntity item : allItems) {
				if (isActive(item)) {
					hasActive = true;
					break;
				}
			}
			if (!hasActive) {
				return;
			}
		}

		scanAndStack(level, config, gameTime);
	}

	public static void scanAndStack(ServerLevel level, ModConfig config) {
		scanAndStack(level, config, level.getGameTime());
	}

	public static void scanAndStack(ServerLevel level, ModConfig config, long gameTime) {
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
				if (single instanceof StackableEntity sSingle && sSingle.jarstacker$getFirstScanGameTime() == 0L) {
					sSingle.jarstacker$setFirstScanGameTime(gameTime);
				}
				int count = ((StackableEntity) single).jarstacker$getStackCount();
				if (count <= 0) count = single.getItem().getCount();
				updateLabel(single, count, showLabel);
			}
			StackMetrics.recordItemScan(System.nanoTime() - start);
			return;
		}

		// LATEST ENTITY WINS:
		// Sort items by age (newest first) so newest dropped entities absorb older items without moving.
		List<ItemEntity> sortedItems = new java.util.ArrayList<>(allItems);
		sortedItems.sort(ItemStackingManager::compareItemAge);

		Set<UUID> processed = new HashSet<>();

		for (ItemEntity survivor : sortedItems) {
			if (!survivor.isAlive() || survivor.isRemoved() || processed.contains(survivor.getUUID())) {
				continue;
			}

			int maxStack = ItemCompatibility.getMaxStackSize(survivor.getItem(), itemConfig);
			int countSurv = ((StackableEntity) survivor).jarstacker$getStackCount();
			if (countSurv <= 0) countSurv = survivor.getItem().getCount();

			if (countSurv >= maxStack) {
				processed.add(survivor.getUUID());
				updateLabel(survivor, countSurv, showLabel);
				continue;
			}

			if (survivor instanceof StackableEntity sSurv && sSurv.jarstacker$getFirstScanGameTime() == 0L) {
				sSurv.jarstacker$setFirstScanGameTime(gameTime);
			}

			AABB box = survivor.getBoundingBox().inflate(radius);
			List<ItemEntity> nearby = level.getEntitiesOfClass(ItemEntity.class, box, other ->
				other != survivor && other.isAlive() && !other.isRemoved() && !processed.contains(other.getUUID())
			);

			if (nearby.isEmpty()) {
				processed.add(survivor.getUUID());
				updateLabel(survivor, countSurv, showLabel);
				continue;
			}

			// Sort nearby candidates by age (newest first)
			nearby.sort(ItemStackingManager::compareItemAge);

			boolean mergedAny = false;
			int initialCount = countSurv;

			for (ItemEntity other : nearby) {
				if (!other.isAlive() || other.isRemoved() || processed.contains(other.getUUID())) {
					continue;
				}

				if (other instanceof StackableEntity sOther && sOther.jarstacker$getFirstScanGameTime() == 0L) {
					sOther.jarstacker$setFirstScanGameTime(gameTime);
				}

				double dist = survivor.distanceTo(other);
				boolean compatible = ItemCompatibility.canStack(survivor, other, itemConfig);

				if (!compatible) {
					ItemMergeForensics.logScan(gameTime, survivor, other, dist, false, true, "INCOMPATIBLE");
					continue;
				}

				ItemMergeForensics.logScan(gameTime, survivor, other, dist, compatible, true, "ELIGIBLE");

				int countOther = ((StackableEntity) other).jarstacker$getStackCount();
				if (countOther <= 0) countOther = other.getItem().getCount();

				if (countSurv >= maxStack) {
					break;
				}

				// Track latency metrics
				if (other instanceof StackableEntity sOther) {
					long srcSpawn = sOther.jarstacker$getSpawnGameTime();
					long srcFirstScan = sOther.jarstacker$getFirstScanGameTime();
					if (srcSpawn > 0) {
						ItemMergeForensics.logDropLatency("Drop " + other.getId(), srcSpawn, srcFirstScan, gameTime, other.getAge(), dist);
					}
				}

				int total = countSurv + countOther;
				if (total <= maxStack) {
					ItemMergeForensics.logMergeBegin(gameTime, other, survivor, countOther, countSurv, "FULL_MERGE");
					countSurv = total;
					ItemMergeForensics.logEntityRemove(gameTime, other, "MERGED_INTO_LATEST");
					other.discard();
					processed.add(other.getUUID());
					StackMetrics.recordItemMerge(countOther);
					mergedAny = true;

					ItemMergeForensics.logMergeAB(
						gameTime, "LATEST", survivor.getUUID(), other.getUUID(),
						survivor.position(), survivor.position(), 0.0,
						other.getAge(), survivor.getAge(), initialCount, total
					);

					if (config.getPerformance().isDebugLogging()) {
						JarStackerMod.LOGGER.info("Merged {} items into latest drop {} (total: {})", countOther, survivor.getUUID(), countSurv);
					}
				} else {
					ItemMergeForensics.logMergeBegin(gameTime, other, survivor, countOther, countSurv, "PARTIAL_MERGE");
					int absorbed = maxStack - countSurv;
					countSurv = maxStack;

					int remainder = countOther - absorbed;
					((StackableEntity) other).jarstacker$setStackCount(remainder);
					ItemStack stackOther = other.getItem();
					stackOther.setCount(Math.min(stackOther.getMaxStackSize(), remainder));
					updateLabel(other, remainder, showLabel);

					processed.add(other.getUUID());
					StackMetrics.recordItemMerge(absorbed);
					mergedAny = true;

					ItemMergeForensics.logMergeAB(
						gameTime, "LATEST", survivor.getUUID(), other.getUUID(),
						survivor.position(), survivor.position(), 0.0,
						other.getAge(), survivor.getAge(), initialCount, maxStack
					);

					if (config.getPerformance().isDebugLogging()) {
						JarStackerMod.LOGGER.info("Partially merged {} items into latest drop {} (new: {}, remaining: {})", absorbed, survivor.getUUID(), countSurv, remainder);
					}
					break;
				}
			}

			if (mergedAny) {
				((StackableEntity) survivor).jarstacker$setStackCount(countSurv);
				ItemStack stackSurv = survivor.getItem();
				stackSurv.setCount(Math.min(stackSurv.getMaxStackSize(), countSurv));
				ItemMergeForensics.logMergeCommit(
					gameTime,
					survivor,
					null,
					countSurv,
					survivor.position(),
					survivor.getDeltaMovement(),
					survivor.getCustomName() != null ? survivor.getCustomName().getString() : "none"
				);
			}

			processed.add(survivor.getUUID());
			updateLabel(survivor, countSurv, showLabel);
		}

		StackMetrics.recordItemScan(System.nanoTime() - start);
	}
}