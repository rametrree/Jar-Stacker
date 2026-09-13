package com.jar.jarstacker.stack.mob;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import net.minecraft.world.entity.animal.Animal;

public class BreedingDiagnostics {
	public static boolean enabled = false;

	public static void logEvent(String event, Animal animal, String extra) {
		if (!enabled && !ModConfig.getInstance().getPerformance().isDebugLogging()) {
			return;
		}
		int count = ((StackableEntity) animal).jarstacker$getStackCount();
		AnimalStackState state = MobCompatibility.getStackState(animal);
		boolean locked = ((StackableEntity) animal).jarstacker$isBreedingLocked();
		int lockTicks = ((StackableEntity) animal).jarstacker$getBreedingLockTicks();
		JarStackerMod.LOGGER.info("[JarStackerDebug] event={} uuid={} type={} count={} state={} age={} baby={} love={} lock={} lockTicks={} pos=({}, {}, {}) {}",
			event,
			animal.getUUID(),
			animal.getType().getDescription().getString(),
			count,
			state,
			animal.getAge(),
			animal.isBaby(),
			animal.isInLove(),
			locked,
			lockTicks,
			String.format("%.2f", animal.getX()),
			String.format("%.2f", animal.getY()),
			String.format("%.2f", animal.getZ()),
			extra != null ? extra : ""
		);
	}
}

