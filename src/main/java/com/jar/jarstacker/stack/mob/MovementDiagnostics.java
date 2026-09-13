package com.jar.jarstacker.stack.mob;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MovementDiagnostics {
	public static boolean enabled = false;

	public static boolean isLoggingEnabled() {
		return enabled || (ModConfig.getInstance() != null && ModConfig.getInstance().getPerformance().isDebugLogging());
	}

	public static void logMovementEvent(String event, Mob animal, Vec3 prevPos, String extra) {
		if (!isLoggingEnabled() || animal == null) {
			return;
		}

		long tick = animal.level() != null ? animal.level().getGameTime() : -1;
		Vec3 pos = animal.position();
		Vec3 vel = animal.getDeltaMovement();
		AABB aabb = animal.getBoundingBox();
		int count = (animal instanceof StackableEntity se) ? se.jarstacker$getStackCount() : 1;
		boolean locked = (animal instanceof StackableEntity se) && se.jarstacker$isBreedingLocked();
		AnimalStackState state = (animal instanceof Animal a) ? MobCompatibility.getStackState(a) : AnimalStackState.ADULT;

		// Find nearest physical animal
		double minDistance = Double.MAX_VALUE;
		Entity nearestMob = null;
		boolean overlap = false;

		if (animal.level() != null) {
			List<Mob> nearby = animal.level().getEntitiesOfClass(Mob.class, aabb.inflate(4.0), e -> e != animal && e.isAlive());
			for (Mob other : nearby) {
				double d = animal.distanceTo(other);
				if (d < minDistance) {
					minDistance = d;
					nearestMob = other;
				}
				if (aabb.intersects(other.getBoundingBox())) {
					overlap = true;
				}
			}
		}

		String deltaStr = "none";
		if (prevPos != null) {
			deltaStr = String.format("(%.3f, %.3f, %.3f)", pos.x - prevPos.x, pos.y - prevPos.y, pos.z - prevPos.z);
		}

		String navTarget = "none";
		if (animal.getNavigation() != null && animal.getNavigation().getTargetPos() != null) {
			navTarget = animal.getNavigation().getTargetPos().toShortString();
		}

		String posStr = String.format("(%.3f, %.3f, %.3f)", pos.x, pos.y, pos.z);
		String velStr = String.format("(%.3f, %.3f, %.3f)", vel.x, vel.y, vel.z);
		String aabbStr = String.format("[%.2f,%.2f,%.2f -> %.2f,%.2f,%.2f]", aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);

		JarStackerMod.LOGGER.info("[JarStackerMovementDebug] tick={} uuid={} event={} count={} state={} lock={} pos={} prevDelta={} vel={} aabb={} overlap={} nearestDist={} nav={} {}",
			tick,
			animal.getUUID(),
			event,
			count,
			state,
			locked,
			posStr,
			deltaStr,
			velStr,
			aabbStr,
			overlap,
			String.format("%.3f", (nearestMob != null ? minDistance : -1.0)),
			navTarget,
			extra != null ? extra : ""
		);
	}
}

