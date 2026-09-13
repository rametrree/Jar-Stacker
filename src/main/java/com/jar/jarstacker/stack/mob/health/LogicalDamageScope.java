package com.jar.jarstacker.stack.mob.health;

/**
 * Conceptual combat scopes for damage distribution across logical stacks in Jar Stacker V0.5.0.
 */
public enum LogicalDamageScope {
	/** Direct single-target hit affecting exactly one active logical mob */
	SINGLE,
	/** Authorized Vanilla sword sweep affecting primary target and surrounding entities */
	SWEEP,
	/** True area event (Explosion, Lightning) affecting all logical mobs at the affected position */
	AREA,
	/** Shared position hazard (Fire, Lava, Fall, Drown, etc.) affecting all logical mobs sharing the position */
	SHARED_ENVIRONMENT,
	/** Unsupported / fallback */
	UNSUPPORTED
}

