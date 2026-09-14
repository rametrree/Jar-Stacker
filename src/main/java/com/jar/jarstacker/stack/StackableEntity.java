package com.jar.jarstacker.stack;

public interface StackableEntity {
	int jarstacker$getStackCount();
	void jarstacker$setStackCount(int count);

	default int jarstacker$getEffectiveCount() {
		int count = jarstacker$getStackCount();
		return count > 0 ? count : 1;
	}

	int jarstacker$getBreedingLockTicks();
	void jarstacker$setBreedingLockTicks(int ticks);

	default boolean jarstacker$isBreedingLocked() {
		return jarstacker$getBreedingLockTicks() > 0;
	}

	int jarstacker$getInteractionLockTicks();
	void jarstacker$setInteractionLockTicks(int ticks);

	default boolean jarstacker$isInteractionLocked() {
		return jarstacker$getInteractionLockTicks() > 0 || jarstacker$isBreedingLocked();
	}

	default com.jar.jarstacker.stack.mob.baby.BabyGrowthState jarstacker$getBabyGrowthState() {
		return null;
	}

	default void jarstacker$setBabyGrowthState(com.jar.jarstacker.stack.mob.baby.BabyGrowthState state) {
	}

	default boolean jarstacker$hasManagedLabel() {
		return false;
	}

	default void jarstacker$setManagedLabel(boolean managed) {
	}

	default com.jar.jarstacker.stack.mob.health.LogicalHealthState jarstacker$getLogicalHealthState() {
		return null;
	}

	default void jarstacker$setLogicalHealthState(com.jar.jarstacker.stack.mob.health.LogicalHealthState state) {
	}

	default com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState jarstacker$getLogicalStatusEffectState() {
		return null;
	}

	default void jarstacker$setLogicalStatusEffectState(com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState state) {
	}

	default com.jar.jarstacker.stack.mob.status.LogicalBurnState jarstacker$getLogicalBurnState() {
		return null;
	}

	default void jarstacker$setLogicalBurnState(com.jar.jarstacker.stack.mob.status.LogicalBurnState state) {
	}

	default boolean jarstacker$isSharedIgnition() {
		return false;
	}

	default void jarstacker$setSharedIgnition(boolean shared) {
	}

	default void jarstacker$dropLootAndExperience(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource damageSource) {
	}

	default void jarstacker$playDeathEffects() {
	}

	default void jarstacker$setAttackStrengthTicker(int ticks) {
	}

	default int jarstacker$getLastHurtByPlayerTime() {
		return 0;
	}

	default void jarstacker$setLastHurtByPlayerTime(int time) {
	}

	default net.minecraft.world.entity.player.Player jarstacker$getLastHurtByPlayer() {
		return null;
	}

	default void jarstacker$setLastHurtByPlayer(net.minecraft.world.entity.player.Player player) {
	}

	default long jarstacker$getLastUnsupportedExtractionTime() {
		return 0L;
	}

	default void jarstacker$setLastUnsupportedExtractionTime(long time) {
	}

	default long jarstacker$getSpawnSequence() {
		return 0L;
	}

	default int jarstacker$getAge() {
		return 0;
	}

	default void jarstacker$setAge(int age) {
	}

	default long jarstacker$getSpawnGameTime() {
		return 0L;
	}

	default void jarstacker$setSpawnGameTime(long time) {
	}

	default long jarstacker$getFirstScanGameTime() {
		return 0L;
	}

	default void jarstacker$setFirstScanGameTime(long time) {
	}

	default long jarstacker$getLastFireDamageTick() {
		return -1L;
	}

	default void jarstacker$setLastFireDamageTick(long time) {
	}
}