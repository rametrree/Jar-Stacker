package com.jar.jarstacker.stack.mob.status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the ordered list of LogicalBurnRecords for a mob stack (Jar Stacker V0.6.0).
 * Guarantees the invariant: logicalCount == burnRecordCount.
 */
public class LogicalBurnState {

	public static final String NBT_KEY = "JarStackerLogicalBurn";

	private final List<LogicalBurnRecord> records = new ArrayList<>();

	public LogicalBurnState() {
	}

	public int size() {
		return records.size();
	}

	public boolean isEmpty() {
		return records.isEmpty();
	}

	public LogicalBurnRecord get(int index) {
		if (index >= 0 && index < records.size()) {
			return records.get(index);
		}
		return null;
	}

	public void set(int index, LogicalBurnRecord record) {
		if (index >= 0 && index < records.size()) {
			records.set(index, record);
		}
	}

	public void add(LogicalBurnRecord record) {
		records.add(record != null ? record : new LogicalBurnRecord());
	}

	public void add(int index, LogicalBurnRecord record) {
		if (index >= 0 && index <= records.size()) {
			records.add(index, record != null ? record : new LogicalBurnRecord());
		} else {
			add(record);
		}
	}

	public void initializeDefault(int count) {
		records.clear();
		for (int i = 0; i < count; i++) {
			records.add(new LogicalBurnRecord());
		}
	}

	public LogicalBurnRecord getActiveRecord() {
		return !records.isEmpty() ? records.get(0) : null;
	}

	public void ignite(int index, int ticks) {
		LogicalBurnRecord record = get(index);
		if (record != null) {
			record.setRemainingFireTicks(Math.max(record.getRemainingFireTicks(), ticks));
		}
	}

	public void igniteAll(int ticks, boolean shared) {
		for (LogicalBurnRecord record : records) {
			record.setRemainingFireTicks(Math.max(record.getRemainingFireTicks(), ticks));
			if (shared) {
				record.setSharedIgnition(true);
			}
		}
	}

	public void extinguish(int index) {
		LogicalBurnRecord record = get(index);
		if (record != null) {
			record.setRemainingFireTicks(0);
		}
	}

	public void extinguishAll() {
		for (LogicalBurnRecord record : records) {
			record.setRemainingFireTicks(0);
		}
	}

	public boolean hasAnyBurning() {
		for (LogicalBurnRecord record : records) {
			if (record.isBurning()) {
				return true;
			}
		}
		return false;
	}

	public LogicalBurnRecord extractActive() {
		if (!records.isEmpty()) {
			return records.remove(0);
		}
		return new LogicalBurnRecord();
	}

	public LogicalBurnState extractBatch(int countToExtract) {
		LogicalBurnState batch = new LogicalBurnState();
		int actualToExtract = Math.min(countToExtract, records.size());
		for (int i = 0; i < actualToExtract; i++) {
			batch.add(records.remove(0));
		}
		return batch;
	}

	public void merge(LogicalBurnState other) {
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
			records.add(new LogicalBurnRecord());
		}
		while (records.size() > expectedCount) {
			records.remove(records.size() - 1);
		}
	}

	public CompoundTag saveToNbt() {
		CompoundTag root = new CompoundTag();
		ListTag list = new ListTag();
		for (LogicalBurnRecord record : records) {
			list.add(record.saveNbt());
		}
		root.put("Records", list);
		return root;
	}

	public static LogicalBurnState loadFromNbt(CompoundTag tag) {
		if (tag == null || !tag.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
			return null;
		}
		CompoundTag root = tag.getCompound(NBT_KEY);
		LogicalBurnState state = new LogicalBurnState();
		if (root.contains("Records", Tag.TAG_LIST)) {
			ListTag list = root.getList("Records", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				state.add(LogicalBurnRecord.loadNbt(list.getCompound(i)));
			}
		}
		return state;
	}
}

