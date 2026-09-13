package com.jar.jarstacker.util;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.stack.StackableEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public class ItemMergeForensics {
	private static final boolean DEBUG = Boolean.getBoolean("jarstacker.debugItemMerge");

	public static boolean isEnabled() {
		return DEBUG;
	}

	public static String formatEntity(ItemEntity e) {
		if (e == null) return "null";
		int logical = (e instanceof StackableEntity s) ? s.jarstacker$getStackCount() : -1;
		long seq = (e instanceof StackableEntity s) ? s.jarstacker$getSpawnSequence() : -1;
		Vec3 pos = e.position();
		Vec3 vel = e.getDeltaMovement();
		Component cName = e.getCustomName();
		String nameStr = cName != null ? "\"" + cName.getString() + "\"" : "none";
		return String.format("[ID:%d UUID:%s Item:%s Phys:%d Logi:%d Age:%d Seq:%d Pos:(%.2f,%.2f,%.2f) Vel:(%.2f,%.2f,%.2f) onGround:%b Delay:%b Name:%s Vis:%b]",
			e.getId(),
			e.getUUID().toString().substring(0, 8),
			e.getItem().getItem().toString(),
			e.getItem().getCount(),
			logical,
			e.getAge(),
			seq,
			pos.x, pos.y, pos.z,
			vel.x, vel.y, vel.z,
			e.onGround(),
			e.hasPickUpDelay(),
			nameStr,
			e.isCustomNameVisible()
		);
	}

	public static void logScan(long gameTime, ItemEntity scanner, ItemEntity candidate, double dist, boolean compatible, boolean inContact, String rejectionReason) {
		if (!DEBUG) return;
		JarStackerMod.LOGGER.info("[FORENSIC SCAN] GT:{} Scanner:{} Candidate:{} Dist:{.2f} Compatible:{} Contact:{} Rejected:{}",
			gameTime,
			formatEntity(scanner),
			formatEntity(candidate),
			dist,
			compatible,
			inContact,
			rejectionReason
		);
	}

	public static void logMergeBegin(long gameTime, ItemEntity source, ItemEntity destination, int srcCount, int destCount, String reason) {
		if (!DEBUG) return;
		JarStackerMod.LOGGER.info("[FORENSIC MERGE_BEGIN] GT:{} Reason:{} Source:{} (count:{}) -> Dest:{} (count:{})",
			gameTime, reason, formatEntity(source), srcCount, formatEntity(destination), destCount);
	}

	public static void logMergeCommit(long gameTime, ItemEntity survivor, ItemEntity removed, int finalCount, Vec3 finalPos, Vec3 finalVel, String finalLabel) {
		if (!DEBUG) return;
		JarStackerMod.LOGGER.info("[FORENSIC MERGE_COMMIT] GT:{} Survivor:{} Removed:{} FinalCount:{} FinalPos:(%.2f,%.2f,%.2f) FinalVel:(%.2f,%.2f,%.2f) Label:{}",
			gameTime,
			formatEntity(survivor),
			removed != null ? removed.getId() : -1,
			finalCount,
			finalPos.x, finalPos.y, finalPos.z,
			finalVel.x, finalVel.y, finalVel.z,
			finalLabel
		);
	}

	public static void logLabelChange(long gameTime, ItemEntity entity, String caller, String oldVal, String newVal) {
		if (!DEBUG) return;
		JarStackerMod.LOGGER.info("[FORENSIC LABEL] GT:{} Caller:{} Entity:{} Old:{} -> New:{}",
			gameTime, caller, formatEntity(entity), oldVal, newVal);
	}

	public static void logEntityRemove(long gameTime, ItemEntity entity, String reason) {
		if (!DEBUG) return;
		JarStackerMod.LOGGER.info("[FORENSIC REMOVE] GT:{} Reason:{} Entity:{}",
			gameTime, reason, formatEntity(entity));
	}

	public static void logDropLatency(String dropLabel, long spawnTick, long firstScanTick, long mergeTick, int ageAtMerge, double distance) {
		if (!DEBUG) return;
		long latency = mergeTick - spawnTick;
		JarStackerMod.LOGGER.info("[DROP LATENCY] {}: spawn tick = {}, first scan = {}, merge = {}, latency = {} ticks, age = {}, distance = {}",
			dropLabel, spawnTick, firstScanTick, mergeTick, latency, ageAtMerge, String.format("%.2f", distance));
	}

	public static void logMergeAB(long tick, String mode, java.util.UUID survivorUUID, java.util.UUID removedUUID, Vec3 oldPos, Vec3 newPos, double distMoved, int srcAge, int destAge, int countBefore, int countAfter) {
		if (!DEBUG) return;
		JarStackerMod.LOGGER.info("[MERGE_AB:{}] tick:{} survivor:{} removed:{} oldPos:(%.2f,%.2f,%.2f) newPos:(%.2f,%.2f,%.2f) distMoved:{} srcAge:{} destAge:{} count:{}->{}",
			mode, tick,
			survivorUUID != null ? survivorUUID.toString().substring(0, 8) : "null",
			removedUUID != null ? removedUUID.toString().substring(0, 8) : "null",
			oldPos.x, oldPos.y, oldPos.z,
			newPos.x, newPos.y, newPos.z,
			String.format("%.2f", distMoved),
			srcAge, destAge, countBefore, countAfter);
	}
}
