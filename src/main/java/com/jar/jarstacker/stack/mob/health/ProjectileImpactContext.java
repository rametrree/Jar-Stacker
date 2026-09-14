package com.jar.jarstacker.stack.mob.health;

/**
 * Thread-local context tracking whether active execution is inside a projectile impact event.
 */
public class ProjectileImpactContext {

	private static final ThreadLocal<Integer> IMPACT_DEPTH = ThreadLocal.withInitial(() -> 0);

	public static void beginImpact() {
		IMPACT_DEPTH.set(IMPACT_DEPTH.get() + 1);
	}

	public static void endImpact() {
		int depth = IMPACT_DEPTH.get() - 1;
		if (depth <= 0) {
			IMPACT_DEPTH.remove();
		} else {
			IMPACT_DEPTH.set(depth);
		}
	}

	public static boolean isProjectileImpactActive() {
		return IMPACT_DEPTH.get() > 0;
	}
}

