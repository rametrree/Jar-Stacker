package com.jar.jarstacker.stack.mob.health;

/**
 * Explicit transaction accounting for logical deaths in Jar Stacker V0.5.1.
 * Enforces the permanent invariant: requestedDeaths == committedDeaths.
 * Every logical death is strictly owned by either VIRTUAL_DEATH or REPRESENTATIVE_VANILLA_DEATH, never both.
 */
public class DeathBatch {
	private final int requestedDeaths;
	private int virtualDeathsProcessed;
	private boolean representativeDeathOwned;
	private final long timestamp;

	public DeathBatch(int requestedDeaths) {
		this.requestedDeaths = requestedDeaths;
		this.virtualDeathsProcessed = 0;
		this.representativeDeathOwned = false;
		this.timestamp = System.currentTimeMillis();
	}

	public synchronized void addVirtualDeaths(int count) {
		if (count > 0) {
			this.virtualDeathsProcessed += count;
		}
	}

	public synchronized void setRepresentativeDeathOwned(boolean owned) {
		this.representativeDeathOwned = owned;
	}

	public int getRequestedDeaths() {
		return this.requestedDeaths;
	}

	public synchronized int getVirtualDeathsProcessed() {
		return this.virtualDeathsProcessed;
	}

	public synchronized boolean isRepresentativeDeathOwned() {
		return this.representativeDeathOwned;
	}

	public synchronized int getCommittedDeaths() {
		return this.virtualDeathsProcessed + (this.representativeDeathOwned ? 1 : 0);
	}

	public synchronized boolean isExact() {
		return getCommittedDeaths() == this.requestedDeaths;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	@Override
	public synchronized String toString() {
		return "DeathBatch{" +
			"requested=" + requestedDeaths +
			", virtual=" + virtualDeathsProcessed +
			", representative=" + representativeDeathOwned +
			", committed=" + getCommittedDeaths() +
			", exact=" + isExact() +
			'}';
	}
}

