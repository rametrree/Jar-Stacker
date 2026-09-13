package com.jar.jarstacker.stack.mob.status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds and manages the ordered list of LogicalStatusRecords for a mob stack (Jar Stacker V0.6.0).
 * Guarantees the invariant: logicalCount == statusRecordCount.
 */
public class LogicalStatusEffectState {

	public static final String NBT_KEY = "JarStackerLogicalStatus";

	private final List<LogicalStatusRecord> records = new ArrayList<>();

	public LogicalStatusEffectState() {
	}

	public int size() {
		return records.size();
	}

	public boolean isEmpty() {
		return records.isEmpty();
	}

	public LogicalStatusRecord get(int index) {
		if (index >= 0 && index < records.size()) {
			return records.get(index);
		}
		return null;
	}

	public void set(int index, LogicalStatusRecord record) {
		if (index >= 0 && index < records.size()) {
			records.set(index, record);
		}
	}

	public void add(LogicalStatusRecord record) {
		records.add(record != null ? record : new LogicalStatusRecord());
	}

	public void add(int index, LogicalStatusRecord record) {
		if (index >= 0 && index <= records.size()) {
			records.add(index, record != null ? record : new LogicalStatusRecord());
		} else {
			add(record);
		}
	}

	public void initializeDefault(int count) {
		records.clear();
		for (int i = 0; i < count; i++) {
			records.add(new LogicalStatusRecord());
		}
	}

	public LogicalStatusRecord getActiveRecord() {
		return !records.isEmpty() ? records.get(0) : null;
	}

	public LogicalStatusRecord extractActive() {
		if (!records.isEmpty()) {
			return records.remove(0);
		}
		return new LogicalStatusRecord();
	}

	public LogicalStatusEffectState extractBatch(int countToExtract) {
		LogicalStatusEffectState batch = new LogicalStatusEffectState();
		int actualToExtract = Math.min(countToExtract, records.size());
		for (int i = 0; i < actualToExtract; i++) {
			batch.add(records.remove(0));
		}
		return batch;
	}

	public void merge(LogicalStatusEffectState other) {
		if (other != null && !other.isEmpty()) {
			for (int i = 0; i < other.size(); i++) {
				this.records.add(other.get(i).copy());
			}
		}
	}

	public void removeIndices(List<Integer> descendingIndices) {
		if (descendingIndices == null || descendingIndices.isEmpty()) {
			return;
		}
		List<Integer> sorted = new ArrayList<>(descendingIndices);
		sorted.sort(Collections.reverseOrder());
		for (int idx : sorted) {
			if (idx >= 0 && idx < records.size()) {
				records.remove(idx);
			}
		}
	}

	public void validateAndRepair(int expectedCount) {
		if (expectedCount <= 0) {
			expectedCount = 1;
		}
		while (records.size() < expectedCount) {
			records.add(new LogicalStatusRecord());
		}
		while (records.size() > expectedCount) {
			records.remove(records.size() - 1);
		}
	}

	public CompoundTag saveToNbt() {
		CompoundTag root = new CompoundTag();
		ListTag list = new ListTag();
		for (LogicalStatusRecord record : records) {
			list.add(record.saveNbt());
		}
		root.put("Records", list);
		return root;
	}

	public static LogicalStatusEffectState loadFromNbt(CompoundTag tag) {
		if (tag == null || !tag.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
			return null;
		}
		CompoundTag root = tag.getCompound(NBT_KEY);
		LogicalStatusEffectState state = new LogicalStatusEffectState();
		if (root.contains("Records", Tag.TAG_LIST)) {
			ListTag list = root.getList("Records", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				state.add(LogicalStatusRecord.loadNbt(list.getCompound(i)));
			}
		}
		return state;
	}
}

