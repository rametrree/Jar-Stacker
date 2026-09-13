package com.jar.jarstacker.stack.mob.status;

import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;

/**
 * Resolves the logical effect application scope (SINGLE, AREA, SHARED_ENVIRONMENT, SELF)
 * based on the source of the effect application (Jar Stacker V0.6.0).
 */
public class LogicalEffectScopeResolver {

	public enum Scope {
		SINGLE,
		AREA,
		SHARED_ENVIRONMENT,
		SELF
	}

	private static final ThreadLocal<Boolean> IN_SPLASH = ThreadLocal.withInitial(() -> false);
	private static final ThreadLocal<Double> SPLASH_INTENSITY = ThreadLocal.withInitial(() -> 1.0);

	public static void beginSplash(double intensity) {
		IN_SPLASH.set(true);
		SPLASH_INTENSITY.set(intensity);
	}

	public static void endSplash() {
		IN_SPLASH.remove();
		SPLASH_INTENSITY.remove();
	}

	public static boolean isInSplash() {
		return IN_SPLASH.get();
	}

	public static double getSplashIntensity() {
		return SPLASH_INTENSITY.get();
	}

	public static Scope resolve(Entity source) {
		if (isInSplash()) {
			return Scope.AREA;
		}
		if (source instanceof ThrownPotion || source instanceof AreaEffectCloud) {
			return Scope.AREA;
		}
		if (source instanceof Projectile) {
			return Scope.SINGLE;
		}
		return Scope.SINGLE;
	}
}

