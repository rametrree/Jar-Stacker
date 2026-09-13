package com.jar.jarstacker.util;

import java.util.concurrent.atomic.AtomicLong;

public class StackMetrics {
	public static final AtomicLong TOTAL_ITEMS_MERGED = new AtomicLong(0);
	public static final AtomicLong TOTAL_MOBS_MERGED = new AtomicLong(0);

	public static volatile long lastItemScanDurationNs = 0;
	public static volatile long lastMobScanDurationNs = 0;

	public static volatile long maxItemScanDurationNs = 0;
	public static volatile long maxMobScanDurationNs = 0;

	private static final int WINDOW_SIZE = 10;
	private static final long[] itemScanHistory = new long[WINDOW_SIZE];
	private static int itemHistoryIndex = 0;
	private static int itemHistoryCount = 0;

	private static final long[] mobScanHistory = new long[WINDOW_SIZE];
	private static int mobHistoryIndex = 0;
	private static int mobHistoryCount = 0;

	public static synchronized void recordItemScan(long durationNs) {
		lastItemScanDurationNs = durationNs;
		if (durationNs > maxItemScanDurationNs) {
			maxItemScanDurationNs = durationNs;
		}
		itemScanHistory[itemHistoryIndex] = durationNs;
		itemHistoryIndex = (itemHistoryIndex + 1) % WINDOW_SIZE;
		if (itemHistoryCount < WINDOW_SIZE) {
			itemHistoryCount++;
		}
	}

	public static synchronized void recordMobScan(long durationNs) {
		lastMobScanDurationNs = durationNs;
		if (durationNs > maxMobScanDurationNs) {
			maxMobScanDurationNs = durationNs;
		}
		mobScanHistory[mobHistoryIndex] = durationNs;
		mobHistoryIndex = (mobHistoryIndex + 1) % WINDOW_SIZE;
		if (mobHistoryCount < WINDOW_SIZE) {
			mobHistoryCount++;
		}
	}

	public static synchronized double getAverageItemScanMs() {
		if (itemHistoryCount == 0) return 0.0;
		long sum = 0;
		for (int i = 0; i < itemHistoryCount; i++) {
			sum += itemScanHistory[i];
		}
		return (sum / (double) itemHistoryCount) / 1_000_000.0;
	}

	public static synchronized double getAverageMobScanMs() {
		if (mobHistoryCount == 0) return 0.0;
		long sum = 0;
		for (int i = 0; i < mobHistoryCount; i++) {
			sum += mobScanHistory[i];
		}
		return (sum / (double) mobHistoryCount) / 1_000_000.0;
	}

	public static void recordItemMerge(long count) {
		TOTAL_ITEMS_MERGED.addAndGet(count);
	}

	public static void recordMobMerge(long count) {
		TOTAL_MOBS_MERGED.addAndGet(count);
	}
}