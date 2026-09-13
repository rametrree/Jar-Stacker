package com.jar.jarstacker.stack.mob.interaction;

/**
 * Result of resolving a player interaction with an entity stack.
 */
public record StackInteractionDecision(StackInteractionMode mode, String reason, int interactionLockTicks) {

	public static final StackInteractionDecision PASS_THROUGH =
		new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Pass through to Vanilla", 0);

	public static final StackInteractionDecision DIRECT =
		new StackInteractionDecision(StackInteractionMode.DIRECT, "Direct interaction on representative", 0);

	public static final StackInteractionDecision UNSUPPORTED =
		new StackInteractionDecision(StackInteractionMode.UNSUPPORTED, "Unsupported interaction", 0);

	public static StackInteractionDecision extractOne(String reason) {
		return new StackInteractionDecision(StackInteractionMode.EXTRACT_ONE, reason, 0);
	}

	public static StackInteractionDecision extractOne(String reason, int lockTicks) {
		return new StackInteractionDecision(StackInteractionMode.EXTRACT_ONE, reason, lockTicks);
	}

	public static StackInteractionDecision transformOne(String reason) {
		return new StackInteractionDecision(StackInteractionMode.TRANSFORM_ONE, reason, 0);
	}

	public boolean requiresExtraction() {
		return this.mode == StackInteractionMode.EXTRACT_ONE;
	}

	public boolean isTransform() {
		return this.mode == StackInteractionMode.TRANSFORM_ONE;
	}

	public boolean isDirect() {
		return this.mode == StackInteractionMode.DIRECT;
	}
}

