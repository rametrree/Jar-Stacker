package com.jar.jarstacker.stack.mob.interaction;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import net.minecraft.world.entity.Mob;

public class MobInteractionDiagnostics {
	public static boolean enabled = false;

	public static void logEvent(String event, Mob mob, String details) {
		if (!enabled && !ModConfig.getInstance().getPerformance().isDebugLogging()) {
			return;
		}
		int count = ((StackableEntity) mob).jarstacker$getStackCount();
		boolean locked = ((StackableEntity) mob).jarstacker$isInteractionLocked();
		int lockTicks = ((StackableEntity) mob).jarstacker$getInteractionLockTicks();
		int breedingTicks = ((StackableEntity) mob).jarstacker$getBreedingLockTicks();

		JarStackerMod.LOGGER.info("[JarStackerInteraction] event={} uuid={} type={} count={} lock={} lockTicks={} breedingLockTicks={} pos=({}, {}, {}) {}",
			event,
			mob.getUUID(),
			mob.getType().getDescription().getString(),
			count,
			locked,
			lockTicks,
			breedingTicks,
			String.format("%.2f", mob.getX()),
			String.format("%.2f", mob.getY()),
			String.format("%.2f", mob.getZ()),
			details != null ? details : ""
		);
	}
}

