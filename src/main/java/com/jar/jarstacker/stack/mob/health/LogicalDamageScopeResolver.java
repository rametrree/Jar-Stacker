package com.jar.jarstacker.stack.mob.health;

import com.jar.jarstacker.stack.StackableEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * Centralized resolver mapping Minecraft 1.21.1 DamageSources to LogicalDamageScope in Jar Stacker V0.5.0.
 */
public class LogicalDamageScopeResolver {

	public static LogicalDamageScope resolve(LivingEntity entity, DamageSource source) {
		if (source == null) {
			return LogicalDamageScope.SINGLE;
		}

		// 1. True Area Damage
		if (source.is(DamageTypes.EXPLOSION)
			|| source.is(DamageTypes.PLAYER_EXPLOSION)
			|| source.is(DamageTypes.BAD_RESPAWN_POINT)
			|| source.is(DamageTypes.LIGHTNING_BOLT)) {
			return LogicalDamageScope.AREA;
		}

		// 2. Shared Environmental Hazards (Position-wide)
		if (source.is(DamageTypes.LAVA)
			|| source.is(DamageTypes.IN_FIRE)
			|| source.is(DamageTypes.CAMPFIRE)
			|| source.is(DamageTypes.HOT_FLOOR)
			|| source.is(DamageTypes.DROWN)
			|| source.is(DamageTypes.IN_WALL)
			|| source.is(DamageTypes.FREEZE)
			|| source.is(DamageTypes.FALL)
			|| source.is(DamageTypes.FLY_INTO_WALL)
			|| source.is(DamageTypes.CACTUS)
			|| source.is(DamageTypes.SWEET_BERRY_BUSH)
			|| source.is(DamageTypes.STALAGMITE)
			|| source.is(DamageTypes.FALLING_BLOCK)
			|| source.is(DamageTypes.FALLING_ANVIL)
			|| source.is(DamageTypes.FALLING_STALACTITE)) {
			return LogicalDamageScope.SHARED_ENVIRONMENT;
		}

		// 3. Continuous Burning (ON_FIRE) - depends on ignition source
		if (source.is(DamageTypes.ON_FIRE)) {
			if (entity instanceof StackableEntity stackable && stackable.jarstacker$isSharedIgnition()) {
				return LogicalDamageScope.SHARED_ENVIRONMENT;
			}
			return LogicalDamageScope.SINGLE;
		}

		// 4. Player Attack (Sweep vs Single)
		if (source.is(DamageTypes.PLAYER_ATTACK)) {
			if (CombatContext.isSecondarySweep()) {
				return LogicalDamageScope.SWEEP;
			}
			return LogicalDamageScope.SINGLE;
		}

		// 5. Direct Projectile Attacks
		if (source.is(DamageTypes.ARROW)
			|| source.is(DamageTypes.TRIDENT)
			|| source.is(DamageTypes.MOB_PROJECTILE)
			|| source.is(DamageTypes.SPIT)
			|| source.is(DamageTypes.FIREWORKS)
			|| source.is(DamageTypes.THROWN)) {
			return LogicalDamageScope.SINGLE;
		}

		// 6. Direct Mob Melee
		if (source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)) {
			return LogicalDamageScope.SINGLE;
		}

		// Default fallback
		return LogicalDamageScope.SINGLE;
	}
}

