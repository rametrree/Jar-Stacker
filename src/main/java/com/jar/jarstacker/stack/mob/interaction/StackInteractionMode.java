package com.jar.jarstacker.stack.mob.interaction;

/**
 * Reusable interaction classification modes for stacked mobs.
 */
public enum StackInteractionMode {
	/**
	 * Jar Stacker does nothing. Vanilla handles the representative entity normally.
	 */
	PASS_THROUGH,

	/**
	 * Interaction executes directly against the representative stack without splitting,
	 * because the interaction does not alter individual mob state (e.g. cow milking).
	 */
	DIRECT,

	/**
	 * State-changing interaction requiring extraction of exactly one logical mob into
	 * a physical singleton nearby, forwarding the vanilla interaction to the singleton
	 * while keeping the remainder stack at the interaction anchor.
	 */
	EXTRACT_ONE,

	/**
	 * Transformation interaction where one logical mob undergoes Vanilla transformation
	 * into a different entity type (e.g. Mooshroom -> Cow), keeping the remainder stack
	 * at the interaction anchor and optionally merging the transformed entity into a
	 * nearby compatible stack.
	 */
	TRANSFORM_ONE,

	/**
	 * Jar Stacker intentionally does not virtualize or intercept this interaction
	 * (e.g. riding, complex mount inventory, entity GUI).
	 */
	UNSUPPORTED
}

