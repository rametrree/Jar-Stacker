package com.jar.jarstacker.stack.mob.health;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Thread-local context tracking active combat events and authorized Vanilla sweep mechanics.
 */
public class CombatContext {

	private static final ThreadLocal<CombatState> ACTIVE_STATE = new ThreadLocal<>();

	public static class CombatState {
		public Player player;
		public Entity primaryTarget;
		public float sweepDamage = 0.0f;
		public boolean sweepAuthorized = false;
		public boolean isSecondarySweep = false;
	}

	public static void beginAttack(Player player, Entity target) {
		CombatState state = new CombatState();
		state.player = player;
		state.primaryTarget = target;
		ACTIVE_STATE.set(state);
	}

	public static void setSweepAuthorized(float damage) {
		CombatState state = ACTIVE_STATE.get();
		if (state != null) {
			state.sweepAuthorized = true;
			state.sweepDamage = damage;
		}
	}

	public static void setSecondarySweep(boolean secondary) {
		CombatState state = ACTIVE_STATE.get();
		if (state != null) {
			state.isSecondarySweep = secondary;
		}
	}

	public static boolean isSecondarySweep() {
		CombatState state = ACTIVE_STATE.get();
		return state != null && state.isSecondarySweep;
	}

	public static boolean isSweepAuthorized() {
		CombatState state = ACTIVE_STATE.get();
		return state != null && state.sweepAuthorized;
	}

	public static boolean isSweepActive() {
		CombatState state = ACTIVE_STATE.get();
		return state != null && (state.sweepAuthorized || state.isSecondarySweep);
	}

	public static float getSweepDamage() {
		CombatState state = ACTIVE_STATE.get();
		return state != null ? state.sweepDamage : 0.0f;
	}

	public static Entity getPrimaryTarget() {
		CombatState state = ACTIVE_STATE.get();
		return state != null ? state.primaryTarget : null;
	}

	public static Player getAttackingPlayer() {
		CombatState state = ACTIVE_STATE.get();
		return state != null ? state.player : null;
	}

	public static void endAttack() {
		ACTIVE_STATE.remove();
	}

	private static final ThreadLocal<Integer> DIRECT_ATTACK_DEPTH = ThreadLocal.withInitial(() -> 0);

	public static void beginDirectAttack() {
		DIRECT_ATTACK_DEPTH.set(DIRECT_ATTACK_DEPTH.get() + 1);
	}

	public static void endDirectAttack() {
		int depth = DIRECT_ATTACK_DEPTH.get() - 1;
		if (depth <= 0) {
			DIRECT_ATTACK_DEPTH.remove();
		} else {
			DIRECT_ATTACK_DEPTH.set(depth);
		}
	}

	public static boolean isDirectAttackActive() {
		return ACTIVE_STATE.get() != null || DIRECT_ATTACK_DEPTH.get() > 0;
	}
}
