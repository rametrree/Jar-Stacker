package com.jar.jarstacker.stack.mob.status;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.UUID;

/**
 * Scoped ThreadLocal execution context for temporary Vanilla effect execution.
 * Prevents nested context leakage and guarantees target ownership.
 */
public class LogicalEffectExecutionContext {

	private static final ThreadLocal<LogicalEffectExecutionContext> CURRENT = new ThreadLocal<>();

	private final UUID stackUuid;
	private final int memberIndex;
	private final Holder<MobEffect> effect;
	private final int depth;

	public LogicalEffectExecutionContext(UUID stackUuid, int memberIndex, Holder<MobEffect> effect, int depth) {
		this.stackUuid = stackUuid;
		this.memberIndex = memberIndex;
		this.effect = effect;
		this.depth = depth;
	}

	public static LogicalEffectExecutionContext getCurrent() {
		return CURRENT.get();
	}

	public static int currentDepth() {
		LogicalEffectExecutionContext current = CURRENT.get();
		return current != null ? current.depth : 0;
	}

	public static Scope open(UUID stackUuid, int memberIndex, Holder<MobEffect> effect) {
		LogicalEffectExecutionContext parent = CURRENT.get();
		int nextDepth = parent != null ? parent.depth + 1 : 1;
		LogicalEffectExecutionContext next = new LogicalEffectExecutionContext(stackUuid, memberIndex, effect, nextDepth);
		CURRENT.set(next);
		return new Scope(parent);
	}

	public UUID getStackUuid() {
		return stackUuid;
	}

	public int getMemberIndex() {
		return memberIndex;
	}

	public Holder<MobEffect> getEffect() {
		return effect;
	}

	public int getDepth() {
		return depth;
	}

	public static class Scope implements AutoCloseable {
		private final LogicalEffectExecutionContext previous;

		public Scope(LogicalEffectExecutionContext previous) {
			this.previous = previous;
		}

		@Override
		public void close() {
			if (previous != null) {
				CURRENT.set(previous);
			} else {
				CURRENT.remove();
			}
		}
	}
}
