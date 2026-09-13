package com.jar.jarstacker.stack.mob.status;

/**
 * Categorization of MobEffects for Jar Stacker V0.6.0.
 * Defines whether an effect operates via attributes, generic periodic ticking,
 * specialized logical health management, instant application, visual state,
 * passive status, or physical singleton fallback.
 */
public enum LogicalEffectBehaviorClass {
	ATTRIBUTE_ONLY,
	PERIODIC_VANILLA,
	SPECIAL_LOGICAL,
	INSTANT,
	VISUAL_STATE,
	PASSIVE_SAFE,
	EVENT_DRIVEN_LOGICAL,
	UNSUPPORTED
}

