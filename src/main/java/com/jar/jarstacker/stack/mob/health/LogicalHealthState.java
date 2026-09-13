package com.jar.jarstacker.stack.mob.health;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Authoritative per-entity logical health state for stacked mobs in Jar Stacker V0.5.0.
 * Maintains invariant: logicalCount == healthRecordCount.
 * Single-ownership: records are transferred on merge/split, never duplicated.
 */
public class LogicalHealthState {

	public static final String NBT_KEY = "JarStackerHealth";
	public static final String NBT_VERSION_KEY = "JarStackerHealthVersion";
	public static final int CURRENT_VERSION = 1;

	private final List<Float> healthRecords = new ArrayList<>();

	public LogicalHealthState() {
	}

	public LogicalHealthState(List<Float> records) {
		if (records != null) {
			this.healthRecords.addAll(records);
		}
	}

	public synchronized int size() {
		return healthRecords.size();
	}

	public synchronized boolean isEmpty() {
		return healthRecords.isEmpty();
	}

	public synchronized List<Float> getRecords() {
		return new ArrayList<>(healthRecords);
	}

	public synchronized float get(int index) {
		if (index >= 0 && index < healthRecords.size()) {
			return healthRecords.get(index);
		}
		return 0.0f;
	}

	public synchronized void set(int index, float hp) {
		if (index >= 0 && index < healthRecords.size()) {
			healthRecords.set(index, hp);
		}
	}

	public synchronized float getActiveHealth() {
		return healthRecords.isEmpty() ? 0.0f : healthRecords.get(0);
	}

	public synchronized void setActiveHealth(float hp) {
		if (!healthRecords.isEmpty()) {
			healthRecords.set(0, hp);
		} else {
			healthRecords.add(hp);
		}
	}

	public synchronized void add(float hp) {
		healthRecords.add(hp);
	}

	public synchronized void add(int index, float hp) {
		if (index >= 0 && index <= healthRecords.size()) {
			healthRecords.add(index, hp);
		} else {
			healthRecords.add(hp);
		}
	}

	public synchronized void initializeDefault(int count, float health) {
		healthRecords.clear();
		float val = Math.max(0.1f, health);
		for (int i = 0; i < count; i++) {
			healthRecords.add(val);
		}
	}

	public synchronized float extractActive() {
		if (healthRecords.isEmpty()) {
			return 0.0f;
		}
		return healthRecords.remove(0);
	}

	public synchronized LogicalHealthState extractBatch(int count) {
		LogicalHealthState batch = new LogicalHealthState();
		int toExtract = Math.min(count, healthRecords.size());
		for (int i = 0; i < toExtract; i++) {
			batch.add(healthRecords.remove(0));
		}
		return batch;
	}

	public synchronized void merge(LogicalHealthState other) {
		if (other != null && !other.isEmpty()) {
			this.healthRecords.addAll(other.getRecords());
			other.healthRecords.clear();
		}
	}

	public synchronized void removeAt(int index) {
		if (index >= 0 && index < healthRecords.size()) {
			healthRecords.remove(index);
		}
	}

	public synchronized void removeIndices(List<Integer> descendingIndices) {
		for (int idx : descendingIndices) {
			if (idx >= 0 && idx < healthRecords.size()) {
				healthRecords.remove(idx);
			}
		}
	}

	public synchronized void validateAndRepair(int count, float representativeHp, float maxHealth) {
		float fallback = representativeHp > 0.0f ? representativeHp : maxHealth;
		if (fallback <= 0.0f) fallback = 20.0f;

		// 1. Pad missing records
		while (healthRecords.size() < count) {
			healthRecords.add(fallback);
		}

		// 2. Trim excess records
		while (healthRecords.size() > count) {
			healthRecords.remove(healthRecords.size() - 1);
		}

		// 3. Clamp health values
		for (int i = 0; i < healthRecords.size(); i++) {
			float hp = healthRecords.get(i);
			if (hp <= 0.0f) {
				healthRecords.set(i, fallback);
			} else if (maxHealth > 0.0f && hp > maxHealth) {
				healthRecords.set(i, maxHealth);
			}
		}
	}

	public synchronized CompoundTag saveToNbt(CompoundTag tag) {
		ListTag listTag = new ListTag();
		for (Float hp : healthRecords) {
			listTag.add(FloatTag.valueOf(hp));
		}
		tag.put(NBT_KEY, listTag);
		tag.putInt(NBT_VERSION_KEY, CURRENT_VERSION);
		return tag;
	}

	public static LogicalHealthState loadFromNbt(CompoundTag tag) {
		if (!tag.contains(NBT_KEY, Tag.TAG_LIST)) {
			return null;
		}
		ListTag listTag = tag.getList(NBT_KEY, Tag.TAG_FLOAT);
		LogicalHealthState state = new LogicalHealthState();
		for (int i = 0; i < listTag.size(); i++) {
			state.add(listTag.getFloat(i));
		}
		return state;
	}
}

