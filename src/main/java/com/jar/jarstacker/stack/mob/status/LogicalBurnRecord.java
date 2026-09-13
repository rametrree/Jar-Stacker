package com.jar.jarstacker.stack.mob.status;

import net.minecraft.nbt.CompoundTag;

/**
 * Tracks the burn timer and ignition provenance for a single logical member (Jar Stacker V0.6.0).
 */
public class LogicalBurnRecord {

	private int remainingFireTicks = 0;
	private boolean sharedIgnition = false;

	public LogicalBurnRecord() {
	}

	public LogicalBurnRecord(int remainingFireTicks, boolean sharedIgnition) {
		this.remainingFireTicks = Math.max(0, remainingFireTicks);
		this.sharedIgnition = sharedIgnition;
	}

	public int getRemainingFireTicks() {
		return remainingFireTicks;
	}

	public void setRemainingFireTicks(int remainingFireTicks) {
		this.remainingFireTicks = Math.max(0, remainingFireTicks);
		if (this.remainingFireTicks == 0) {
			this.sharedIgnition = false;
		}
	}

	public boolean isBurning() {
		return remainingFireTicks > 0;
	}

	public boolean isSharedIgnition() {
		return sharedIgnition;
	}

	public void setSharedIgnition(boolean sharedIgnition) {
		this.sharedIgnition = sharedIgnition;
	}

	public void decrementFireTicks() {
		if (remainingFireTicks > 0) {
			remainingFireTicks--;
			if (remainingFireTicks == 0) {
				sharedIgnition = false;
			}
		}
	}

	public LogicalBurnRecord copy() {
		return new LogicalBurnRecord(remainingFireTicks, sharedIgnition);
	}

	public CompoundTag saveNbt() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("FireTicks", remainingFireTicks);
		tag.putBoolean("Shared", sharedIgnition);
		return tag;
	}

	public static LogicalBurnRecord loadNbt(CompoundTag tag) {
		if (tag == null) {
			return new LogicalBurnRecord();
		}
		int ticks = com.jar.jarstacker.adapter.NbtAdapter.getInt(tag, "FireTicks");
		boolean shared = com.jar.jarstacker.adapter.NbtAdapter.getBoolean(tag, "Shared");
		return new LogicalBurnRecord(ticks, shared);
	}
}

