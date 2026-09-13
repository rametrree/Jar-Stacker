package com.jar.jarstacker.stack.mob.baby;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Logical per-entity baby growth state tracking for stacked mobs in Jar Stacker V0.3.1.
 * Logical per-entity baby growth state tracking for stacked mobs in Jar Stacker V0.3.2.
 * Stores monotonic adultAt timestamps (currentGameTime + remainingBabyTicks) in ascending sorted order.
 * Enforces ownership invariants: records are transferred, never duplicated.
 */
public class BabyGrowthState {

	public static final String NBT_KEY = "JarStackerBabyGrowth";
	public static final String NBT_VERSION_KEY = "JarStackerLogicalVersion";
	public static final int CURRENT_VERSION = 1;

	private final List<Long> adultAtTimes = new ArrayList<>();

	public BabyGrowthState() {
	}

	public synchronized void add(long adultAt) {
		insert(adultAt);
	}

	public synchronized void insert(long adultAt) {
		if (adultAt <= 0) {
			adultAt = 1;
		}
		int idx = Collections.binarySearch(adultAtTimes, adultAt);
		if (idx < 0) {
			idx = -idx - 1;
		}
		adultAtTimes.add(idx, adultAt);
	}

	public synchronized long getEarliestAdultAt() {
		return adultAtTimes.isEmpty() ? Long.MAX_VALUE : adultAtTimes.get(0);
	}

	public synchronized long getLatestAdultAt() {
		return adultAtTimes.isEmpty() ? Long.MIN_VALUE : adultAtTimes.get(adultAtTimes.size() - 1);
	}

	public synchronized long removeEarliest() {
		return extractEarliest();
	}

	public synchronized long extractEarliest() {
		return adultAtTimes.isEmpty() ? Long.MAX_VALUE : adultAtTimes.remove(0);
	}

	public synchronized BabyGrowthState extractBatch(int count) {
		BabyGrowthState batch = new BabyGrowthState();
		int toExtract = Math.min(count, adultAtTimes.size());
		for (int i = 0; i < toExtract; i++) {
			batch.adultAtTimes.add(adultAtTimes.remove(0));
		}
		return batch;
	}

	public synchronized int size() {
		return adultAtTimes.size();
	}

	public synchronized boolean isEmpty() {
		return adultAtTimes.isEmpty();
	}

	public synchronized List<Long> getEntries() {
		return Collections.unmodifiableList(new ArrayList<>(adultAtTimes));
	}

	public synchronized boolean isSorted() {
		for (int i = 0; i < adultAtTimes.size() - 1; i++) {
			if (adultAtTimes.get(i) > adultAtTimes.get(i + 1)) {
				return false;
			}
		}
		return true;
	}

	public synchronized boolean hasInvalidTimestamps() {
		for (long t : adultAtTimes) {
			if (t <= 0 || t == Long.MAX_VALUE) {
				return true;
			}
		}
		return false;
	}

	public synchronized void sortInPlace() {
		Collections.sort(adultAtTimes);
	}

	public synchronized int countMatured(long currentGameTime) {
		int count = 0;
		for (long adultAt : adultAtTimes) {
			if (adultAt <= currentGameTime) {
				count++;
			} else {
				break; // Sorted order allows early exit
			}
		}
		return count;
	}

	public synchronized int removeMatured(long currentGameTime) {
		int count = 0;
		while (!adultAtTimes.isEmpty() && adultAtTimes.get(0) <= currentGameTime) {
			adultAtTimes.remove(0);
			count++;
		}
		return count;
	}

	/**
	 * Transactionally transfers all records from source into this state.
	 * Clears the source to enforce strict single-owner semantics.
	 */
	public synchronized void mergeFrom(BabyGrowthState other) {
		if (other == null || other == this || other.isEmpty()) {
			return;
		}
		synchronized (other) {
			for (long val : other.adultAtTimes) {
				insert(val);
			}
			other.adultAtTimes.clear();
		}
	}

	/**
	 * Extracts countToExtract records from this state into a new state.
	 * Deterministically takes the youngest entries (end of sorted list).
	 */
	public synchronized BabyGrowthState split(int countToExtract) {
		BabyGrowthState extracted = new BabyGrowthState();
		if (countToExtract <= 0 || adultAtTimes.isEmpty()) {
			return extracted;
		}
		// Extract youngest entries (from the end of the sorted list)
		int extractActual = Math.min(countToExtract, adultAtTimes.size());
		for (int i = 0; i < extractActual; i++) {
			extracted.insert(adultAtTimes.remove(adultAtTimes.size() - 1));
		}
		return extracted;
	}

	public synchronized void initializeDefault(int count, int vanillaAge, long currentGameTime) {
		adultAtTimes.clear();
		if (count <= 0) {
			return;
		}
		long remaining = Math.max(1L, -(long) vanillaAge);
		long adultAt = currentGameTime + remaining;
		for (int i = 0; i < count; i++) {
			adultAtTimes.add(adultAt);
		}
	}

	/**
	 * Deterministic repair policy:
	 * - If unsorted: sort.
	 * - If invalid timestamps: reset to fallback.
	 * - If missing entries (size < stackCount): pad with representative age.
	 * - If extra entries (size > stackCount): retain stackCount earliest/closest-to-adult entries.
	 */
	public synchronized void validateAndRepair(int stackCount, int vanillaAge, long currentGameTime) {
		if (stackCount <= 0) {
			adultAtTimes.clear();
			return;
		}

		long fallbackAdultAt = currentGameTime + Math.max(1L, -(long) vanillaAge);

		// 1. Sanitize invalid timestamps
		for (int i = 0; i < adultAtTimes.size(); i++) {
			long t = adultAtTimes.get(i);
			if (t <= 0 || t == Long.MAX_VALUE) {
				adultAtTimes.set(i, fallbackAdultAt);
			}
		}

		// 2. Sort
		sortInPlace();

		// 3. Reconcile count mismatch
		if (adultAtTimes.size() < stackCount) {
			while (adultAtTimes.size() < stackCount) {
				insert(fallbackAdultAt);
			}
		} else if (adultAtTimes.size() > stackCount) {
			// Deterministically keep the stackCount entries closest to adulthood (indices 0 .. stackCount - 1)
			while (adultAtTimes.size() > stackCount) {
				adultAtTimes.remove(adultAtTimes.size() - 1);
			}
		}
	}

	public synchronized void saveToNbt(CompoundTag tag) {
		long[] arr = new long[adultAtTimes.size()];
		for (int i = 0; i < adultAtTimes.size(); i++) {
			arr[i] = adultAtTimes.get(i);
		}
		tag.putLongArray(NBT_KEY, arr);
		tag.putInt(NBT_VERSION_KEY, CURRENT_VERSION);
	}

	public static BabyGrowthState loadFromNbt(CompoundTag tag) {
		if (com.jar.jarstacker.adapter.NbtAdapter.contains(tag, NBT_KEY, Tag.TAG_LONG_ARRAY)) {
			long[] arr = com.jar.jarstacker.adapter.NbtAdapter.getLongArray(tag, NBT_KEY);
			BabyGrowthState state = new BabyGrowthState();
			for (long v : arr) {
				state.adultAtTimes.add(v);
			}
			Collections.sort(state.adultAtTimes);
			return state;
		}
		return null;
	}

	public synchronized BabyGrowthState copy() {
		BabyGrowthState copy = new BabyGrowthState();
		copy.adultAtTimes.addAll(this.adultAtTimes);
		return copy;
	}

	// -------------------------------------------------------------
	// TEST CORRUPTION HOOKS (DO NOT USE IN NORMAL GAMEPLAY)
	// -------------------------------------------------------------

	public synchronized void injectCorruptMissing(int countToRemove) {
		for (int i = 0; i < countToRemove && !adultAtTimes.isEmpty(); i++) {
			adultAtTimes.remove(adultAtTimes.size() - 1);
		}
	}

	public synchronized void injectCorruptExtra(long[] recordsToAdd) {
		for (long v : recordsToAdd) {
			adultAtTimes.add(v);
		}
	}

	public synchronized void injectCorruptUnsorted(long[] records) {
		adultAtTimes.clear();
		for (long v : records) {
			adultAtTimes.add(v);
		}
	}
}

