package com.jar.jarstacker.stack.mob.health;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core manager for logical health, combat scopes, and multi-death processing in Jar Stacker V0.5.0.
 */
public class LogicalHealthManager {

	private static volatile DeathBatch lastDeathBatch = null;

	public static final ThreadLocal<Boolean> IN_ACTUALLY_HURT = ThreadLocal.withInitial(() -> false);
	public static volatile int testHurtCallbackCount = 0;
	public static volatile int testDeathCallbackCount = 0;

	public static void recordHurtCallback() {
		testHurtCallbackCount++;
	}

	public static void recordDeathCallback() {
		testDeathCallbackCount++;
	}

	public static void triggerLogicalHurtEffects(LivingEntity entity, int memberIndex, DamageSource damageSource, float damageAmount) {
		if (entity == null || damageAmount <= 0.0f) {
			return;
		}
		com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.getOrCreateStatusState(entity);
		if (statusState == null || memberIndex < 0 || memberIndex >= statusState.size()) {
			return;
		}
		com.jar.jarstacker.stack.mob.status.LogicalStatusRecord record = statusState.get(memberIndex);
		if (record == null) {
			return;
		}
		for (net.minecraft.world.effect.MobEffectInstance inst : record.getEffectInstances()) {
			net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect = inst.getEffect();
			if (com.jar.jarstacker.stack.mob.status.LogicalEffectClassifier.classify(effect) == com.jar.jarstacker.stack.mob.status.LogicalEffectBehaviorClass.EVENT_DRIVEN_LOGICAL) {
				testHurtCallbackCount++;
				effect.value().onMobHurt(entity, inst.getAmplifier(), damageSource, damageAmount);
			}
		}
	}

	public static void triggerLogicalDeathEffects(LivingEntity entity, com.jar.jarstacker.stack.mob.status.LogicalStatusRecord record) {
		if (entity == null || record == null) {
			return;
		}
		for (net.minecraft.world.effect.MobEffectInstance inst : record.getEffectInstances()) {
			net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect = inst.getEffect();
			if (com.jar.jarstacker.stack.mob.status.LogicalEffectClassifier.classify(effect) == com.jar.jarstacker.stack.mob.status.LogicalEffectBehaviorClass.EVENT_DRIVEN_LOGICAL) {
				testDeathCallbackCount++;
				effect.value().onMobRemoved(entity, inst.getAmplifier(), net.minecraft.world.entity.Entity.RemovalReason.KILLED);
			}
		}
	}

	public static DeathBatch getLastDeathBatch() {
		return lastDeathBatch;
	}

	public static void recordDeathBatch(DeathBatch batch) {
		lastDeathBatch = batch;
		if (!batch.isExact()) {
			JarStackerMod.LOGGER.error("[JarStacker] COMBAT_DEATH_ACCOUNTING_ERROR requested={} virtual={} representative={} committed={}",
				batch.getRequestedDeaths(), batch.getVirtualDeathsProcessed(), batch.isRepresentativeDeathOwned(), batch.getCommittedDeaths());
		}
	}

	public static LogicalHealthState getOrCreateState(LivingEntity entity) {
		if (entity == null) {
			return null;
		}
		StackableEntity stackable = (StackableEntity) entity;
		LogicalHealthState state = stackable.jarstacker$getLogicalHealthState();
		int count = stackable.jarstacker$getStackCount();
		if (count <= 0) count = 1;

		float maxHp = entity.getMaxHealth();
		if (maxHp <= 0.0f) maxHp = 20.0f;
		float repHp = entity.getHealth();
		float initialHp = Math.max(0.0f, Math.min(repHp, maxHp));

		if (state == null) {
			state = new LogicalHealthState();
			state.initializeDefault(count, initialHp);
			stackable.jarstacker$setLogicalHealthState(state);
		} else if (state.size() < count) {
			while (state.size() < count) {
				state.add(maxHp);
			}
		}
		return state;
	}

	public static void mergeHealthStates(LivingEntity target, LivingEntity source, int countToAdd) {
		if (target == null || source == null || countToAdd <= 0) {
			return;
		}
		LogicalHealthState targetState = getOrCreateState(target);
		LogicalHealthState sourceState = getOrCreateState(source);

		if (targetState != null && sourceState != null) {
			if (countToAdd >= sourceState.size()) {
				targetState.merge(sourceState);
			} else {
				LogicalHealthState batch = sourceState.extractBatch(countToAdd);
				targetState.merge(batch);
			}
		}
		com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.mergeStates(target, source, countToAdd);
	}

	public static void splitAndMergeHealthStates(LivingEntity target, LivingEntity source, int countToTransfer) {
		mergeHealthStates(target, source, countToTransfer);
	}

	public static void extractHealthState(LivingEntity source, LivingEntity extracted) {
		if (source == null || extracted == null) {
			return;
		}
		LogicalHealthState sourceState = getOrCreateState(source);
		float hp = sourceState != null ? sourceState.extractActive() : source.getHealth();
		if (hp <= 0.0f) hp = source.getMaxHealth();

		extracted.setHealth(hp);
		LogicalHealthState extractedState = new LogicalHealthState();
		extractedState.add(hp);
		((StackableEntity) extracted).jarstacker$setLogicalHealthState(extractedState);

		com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.extractState(source, extracted);
	}

	public static void onDamageApplied(LivingEntity entity, DamageSource damageSource, float damageAmount) {
		if (entity.level().isClientSide || !entity.isAlive() || entity.isRemoved() || damageAmount <= 0.0f) {
			return;
		}

		StackableEntity stackable = (StackableEntity) entity;
		int count = stackable.jarstacker$getStackCount();

		// Track shared ignition for environmental fire/lava
		if (damageSource.is(DamageTypes.LAVA) || damageSource.is(DamageTypes.IN_FIRE)
			|| damageSource.is(DamageTypes.CAMPFIRE) || damageSource.is(DamageTypes.HOT_FLOOR)) {
			stackable.jarstacker$setSharedIgnition(true);
		}

		LogicalHealthState state = getOrCreateState(entity);
		LogicalDamageScope scope = LogicalDamageScopeResolver.resolve(entity, damageSource);

		if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
			JarStackerMod.LOGGER.debug("[JarStackerCombat] DAMAGE_SCOPE type={} scope={} source={} damage={} count={}",
				entity.getType(), scope, damageSource.type().msgId(), damageAmount, count);
		}

		if (entity instanceof Mob mob && com.jar.jarstacker.stack.mob.MobCompatibility.hasDamageableEquipment(mob)) {
			// Unsafe equipment state: stop further logical combat virtualization across shared durability
			float currentHp = state.getActiveHealth();
			float newHp = Math.max(0.0f, currentHp - damageAmount);
			state.setActiveHealth(newHp);
			return;
		}

		if (scope == LogicalDamageScope.SINGLE) {
			// Affects only active member (index 0)
			com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.getOrCreateStatusState(entity);
			float finalDmg = resolveMemberDamage(entity, 0, damageSource, damageAmount, statusState);
			float currentHp = state.getActiveHealth();
			float newHp = Math.max(0.0f, currentHp - finalDmg);
			state.setActiveHealth(newHp);
			if (!IN_ACTUALLY_HURT.get()) {
				triggerLogicalHurtEffects(entity, 0, damageSource, finalDmg);
			}
			return;
		}

		if (scope == LogicalDamageScope.AREA || scope == LogicalDamageScope.SHARED_ENVIRONMENT || scope == LogicalDamageScope.SWEEP) {
			// All members represented take damage
			com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.getOrCreateStatusState(entity);
			com.jar.jarstacker.stack.mob.status.LogicalBurnState burnState = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.getOrCreateBurnState(entity);

			// Active member 0 damage resolution
			float activeDamage = resolveMemberDamage(entity, 0, damageSource, damageAmount, statusState);
			float activeHp = state.getActiveHealth();
			state.setActiveHealth(Math.max(0.0f, activeHp - activeDamage));
			if (!IN_ACTUALLY_HURT.get()) {
				triggerLogicalHurtEffects(entity, 0, damageSource, activeDamage);
			}

			List<Integer> deadVirtualIndices = new ArrayList<>();
			for (int i = 1; i < state.size(); i++) {
				float memberDamage = resolveMemberDamage(entity, i, damageSource, damageAmount, statusState);
				triggerLogicalHurtEffects(entity, i, damageSource, memberDamage);
				float memberHp = state.get(i);
				float newMemberHp = memberHp - memberDamage;
				if (newMemberHp <= 0.0f) {
					state.set(i, 0.0f);
					deadVirtualIndices.add(i);
				} else {
					state.set(i, newMemberHp);
				}
			}

			// If representative member 0 did NOT die, but some virtual members did:
			if (state.getActiveHealth() > 0.0f && !deadVirtualIndices.isEmpty()) {
				Collections.sort(deadVirtualIndices, Collections.reverseOrder());
				for (int idx : deadVirtualIndices) {
					com.jar.jarstacker.stack.mob.status.LogicalStatusRecord rec = (statusState != null && idx < statusState.size()) ? statusState.get(idx) : null;
					triggerLogicalDeathEffects(entity, rec);
				}
				state.removeIndices(deadVirtualIndices);
				if (statusState != null) {
					statusState.removeIndices(deadVirtualIndices);
				}
				if (burnState != null) {
					burnState.removeIndices(deadVirtualIndices);
				}

				int virtualDeaths = deadVirtualIndices.size();
				int newCount = count - virtualDeaths;
				stackable.jarstacker$setStackCount(newCount);
				if (entity instanceof Mob mob) {
					MobStackingManager.updateLabel(mob, newCount, ModConfig.getInstance().getMobStacking().isShowLabel());
				}

				DeathBatch batch = new DeathBatch(virtualDeaths);
				processDeaths(entity, damageSource, virtualDeaths);
				batch.addVirtualDeaths(virtualDeaths);
				batch.setRepresentativeDeathOwned(false);
				recordDeathBatch(batch);

				if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
					JarStackerMod.LOGGER.info("[JarStackerCombat] LOGICAL_DAMAGE_{} type={} damage={} deaths={} survivors={}",
						scope, entity.getType(), damageAmount, virtualDeaths, newCount);
				}
			}
		}
	}

	public static void applySweepToPrimary(LivingEntity primaryEntity, float sweepDamage) {
		if (primaryEntity == null || primaryEntity.level().isClientSide || !primaryEntity.isAlive() || primaryEntity.isRemoved() || sweepDamage <= 0.0f) {
			return;
		}

		StackableEntity stackable = (StackableEntity) primaryEntity;
		int count = stackable.jarstacker$getStackCount();
		if (count <= 1) {
			return;
		}

		LogicalHealthState state = getOrCreateState(primaryEntity);
		List<Integer> deadIndices = new ArrayList<>();

		// Remaining members (1 to N-1) receive sweep damage
		for (int i = 1; i < state.size(); i++) {
			triggerLogicalHurtEffects(primaryEntity, i, primaryEntity.damageSources().generic(), sweepDamage);
			float hp = state.get(i);
			float newHp = hp - sweepDamage;
			if (newHp <= 0.0f) {
				deadIndices.add(i);
			} else {
				state.set(i, newHp);
			}
		}

		if (!deadIndices.isEmpty()) {
			Collections.sort(deadIndices, Collections.reverseOrder());
			com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.getOrCreateStatusState(primaryEntity);
			for (int idx : deadIndices) {
				com.jar.jarstacker.stack.mob.status.LogicalStatusRecord rec = (statusState != null && idx < statusState.size()) ? statusState.get(idx) : null;
				triggerLogicalDeathEffects(primaryEntity, rec);
			}
			state.removeIndices(deadIndices);
			if (statusState != null) {
				statusState.removeIndices(deadIndices);
			}

			int deaths = deadIndices.size();
			int newCount = count - deaths;
			stackable.jarstacker$setStackCount(newCount);
			if (primaryEntity instanceof Mob mob) {
				MobStackingManager.updateLabel(mob, newCount, ModConfig.getInstance().getMobStacking().isShowLabel());
			}

			DamageSource sweepSource = primaryEntity.damageSources().playerAttack(CombatContext.getAttackingPlayer());
			com.jar.jarstacker.stack.mob.death.CombatDeathContext sweepCtx = com.jar.jarstacker.stack.mob.death.CombatDeathContext.capture(primaryEntity, sweepSource);
			try (com.jar.jarstacker.stack.mob.death.CombatDeathContext.Scope scope = sweepCtx.openScope()) {
				DeathBatch batch = new DeathBatch(deaths);
				processDeaths(primaryEntity, sweepSource, deaths);
				batch.addVirtualDeaths(deaths);
				batch.setRepresentativeDeathOwned(false);
				recordDeathBatch(batch);
			}

			if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
				JarStackerMod.LOGGER.info("[JarStackerCombat] SWEEP_PRIMARY_MULTI_DEATH type={} sweepDamage={} deaths={} survivors={}",
					primaryEntity.getType(), sweepDamage, deaths, newCount);
			}
		}
	}

	public static void processDeaths(LivingEntity entity, DamageSource damageSource, int deathCount) {
		if (deathCount <= 0 || !(entity.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		StackableEntity stackable = (StackableEntity) entity;
		com.jar.jarstacker.stack.mob.death.CombatDeathContext activeCtx = com.jar.jarstacker.stack.mob.death.CombatDeathContext.getCurrentContext();
		if (activeCtx == null) {
			com.jar.jarstacker.stack.mob.death.CombatDeathContext ctx = com.jar.jarstacker.stack.mob.death.CombatDeathContext.capture(entity, damageSource);
			try (com.jar.jarstacker.stack.mob.death.CombatDeathContext.Scope scope = ctx.openScope()) {
				for (int i = 0; i < deathCount; i++) {
					stackable.jarstacker$dropLootAndExperience(serverLevel, damageSource);
					if (stackable.jarstacker$getBabyGrowthState() != null && !stackable.jarstacker$getBabyGrowthState().isEmpty()) {
						stackable.jarstacker$getBabyGrowthState().extractEarliest();
					}
				}
				stackable.jarstacker$playDeathEffects();
				com.jar.jarstacker.stack.mob.death.CombatDeathContext.logAttribution(ctx, deathCount);
			}
		} else {
			for (int i = 0; i < deathCount; i++) {
				stackable.jarstacker$dropLootAndExperience(serverLevel, damageSource);
				if (stackable.jarstacker$getBabyGrowthState() != null && !stackable.jarstacker$getBabyGrowthState().isEmpty()) {
					stackable.jarstacker$getBabyGrowthState().extractEarliest();
				}
			}
			stackable.jarstacker$playDeathEffects();
			com.jar.jarstacker.stack.mob.death.CombatDeathContext.logAttribution(activeCtx, deathCount);
		}
	}

	public static boolean handleDie(LivingEntity entity, DamageSource damageSource) {
		if (entity.level().isClientSide) {
			return false;
		}

		if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD) || damageSource.is(DamageTypes.GENERIC_KILL)) {
			return false; // Let Vanilla remove the entire entity
		}

		if (entity instanceof Mob mob && com.jar.jarstacker.stack.mob.MobCompatibility.hasDamageableEquipment(mob)) {
			DeathBatch batch = new DeathBatch(1);
			batch.setRepresentativeDeathOwned(true);
			recordDeathBatch(batch);
			return false; // Stop logical virtualization; let Vanilla die() handle the physical entity
		}

		StackableEntity stackable = (StackableEntity) entity;
		int count = stackable.jarstacker$getStackCount();
		if (count <= 1) {
			DeathBatch batch = new DeathBatch(1);
			batch.setRepresentativeDeathOwned(true);
			recordDeathBatch(batch);
			return false; // Last logical mob dying naturally
		}

		LogicalHealthState state = getOrCreateState(entity);
		com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.getOrCreateStatusState(entity);
		com.jar.jarstacker.stack.mob.status.LogicalBurnState burnState = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.getOrCreateBurnState(entity);
		LogicalDamageScope scope = LogicalDamageScopeResolver.resolve(entity, damageSource);

		int totalDeaths = 1; // At least representative index 0 died
		if (scope == LogicalDamageScope.AREA || scope == LogicalDamageScope.SHARED_ENVIRONMENT || scope == LogicalDamageScope.SWEEP) {
			// Check if other members also died
			List<Integer> additionalDead = new ArrayList<>();
			for (int i = 1; i < state.size(); i++) {
				if (state.get(i) <= 0.0f) {
					additionalDead.add(i);
				}
			}
			if (!additionalDead.isEmpty()) {
				Collections.sort(additionalDead, Collections.reverseOrder());
				for (int idx : additionalDead) {
					com.jar.jarstacker.stack.mob.status.LogicalStatusRecord rec = (statusState != null && idx < statusState.size()) ? statusState.get(idx) : null;
					triggerLogicalDeathEffects(entity, rec);
				}
				state.removeIndices(additionalDead);
				if (statusState != null) {
					statusState.removeIndices(additionalDead);
				}
				if (burnState != null) {
					burnState.removeIndices(additionalDead);
				}
				totalDeaths += additionalDead.size();
			}
		}

		// Remove the active member index 0
		state.extractActive();
		com.jar.jarstacker.stack.mob.status.LogicalStatusRecord oldStatus = statusState != null ? statusState.extractActive() : null;
		triggerLogicalDeathEffects(entity, oldStatus);
		if (burnState != null) {
			burnState.extractActive();
		}

		int remainingCount = count - totalDeaths;
		if (remainingCount <= 0) {
			// All members died! Process loot/XP for virtual deaths, let Vanilla remove the representative
			DeathBatch batch = new DeathBatch(totalDeaths);
			int virtualDeaths = totalDeaths - 1;
			if (virtualDeaths > 0) {
				processDeaths(entity, damageSource, virtualDeaths);
				batch.addVirtualDeaths(virtualDeaths);
			}
			batch.setRepresentativeDeathOwned(true);
			recordDeathBatch(batch);
			return false;
		}

		// Some members survived: update stack count and project surviving active health
		stackable.jarstacker$setStackCount(remainingCount);
		if (entity instanceof Mob mob) {
			MobStackingManager.updateLabel(mob, remainingCount, ModConfig.getInstance().getMobStacking().isShowLabel());
		}

		DeathBatch batch = new DeathBatch(totalDeaths);
		processDeaths(entity, damageSource, totalDeaths);
		batch.addVirtualDeaths(totalDeaths);
		batch.setRepresentativeDeathOwned(false);
		recordDeathBatch(batch);

		float nextHp = state.getActiveHealth();
		if (nextHp <= 0.0f) {
			nextHp = entity.getMaxHealth();
			state.setActiveHealth(nextHp);
		}
		entity.setHealth(nextHp);

		// Project new active member status and clean up stale attributes
		if (statusState != null && !statusState.isEmpty()) {
			com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.onActiveMemberSwitched(entity, oldStatus, statusState.get(0));
		} else {
			com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.onActiveMemberSwitched(entity, oldStatus, null);
		}

		if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
			JarStackerMod.LOGGER.info("[JarStackerCombat] LOGICAL_DEATH_HANDLED type={} deaths={} remaining={}",
				entity.getType(), totalDeaths, remainingCount);
		}

		return true; // Cancel Vanilla die() so entity is preserved
	}

	public static boolean applyLogicalDirectDamage(LivingEntity entity, int memberIndex, DamageSource damageSource, float amount) {
		if (entity == null || entity.level().isClientSide || !entity.isAlive() || entity.isRemoved() || amount <= 0.0f) {
			return false;
		}

		StackableEntity stackable = (StackableEntity) entity;
		int count = stackable.jarstacker$getStackCount();
		LogicalHealthState healthState = getOrCreateState(entity);
		com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.getOrCreateStatusState(entity);
		com.jar.jarstacker.stack.mob.status.LogicalBurnState burnState = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.getOrCreateBurnState(entity);

		if (memberIndex == 0) {
			float curHp = healthState.getActiveHealth();
			float newHp = curHp - amount;
			if (newHp <= 0.0f) {
				// Representative / active member dies
				entity.hurt(damageSource, amount);
				return true;
			} else {
				healthState.setActiveHealth(newHp);
				entity.setHealth(newHp);
				return true;
			}
		} else if (memberIndex > 0 && memberIndex < healthState.size()) {
			float memberHp = healthState.get(memberIndex);
			float newMemberHp = memberHp - amount;
			if (newMemberHp <= 0.0f) {
				// Virtual member death
				com.jar.jarstacker.stack.mob.status.LogicalStatusRecord rec = (statusState != null && memberIndex < statusState.size()) ? statusState.get(memberIndex) : null;
				triggerLogicalDeathEffects(entity, rec);
				healthState.removeIndices(List.of(memberIndex));
				if (statusState != null) {
					statusState.removeIndices(List.of(memberIndex));
				}
				if (burnState != null) {
					burnState.removeIndices(List.of(memberIndex));
				}
				int newCount = count - 1;
				stackable.jarstacker$setStackCount(newCount);
				if (entity instanceof Mob mob) {
					MobStackingManager.updateLabel(mob, newCount, ModConfig.getInstance().getMobStacking().isShowLabel());
				}
				DeathBatch batch = new DeathBatch(1);
				processDeaths(entity, damageSource, 1);
				batch.addVirtualDeaths(1);
				batch.setRepresentativeDeathOwned(false);
				recordDeathBatch(batch);
				return true;
			} else {
				triggerLogicalHurtEffects(entity, memberIndex, damageSource, amount);
				healthState.set(memberIndex, newMemberHp);
				return true;
			}
		}
		return false;
	}

	public static void healMember(LivingEntity entity, int memberIndex, float amount) {
		if (entity == null || entity.level().isClientSide || amount <= 0.0f) {
			return;
		}
		LogicalHealthState state = getOrCreateState(entity);
		if (state != null && memberIndex >= 0 && memberIndex < state.size()) {
			float maxHp = entity.getMaxHealth();
			float currentHp = state.get(memberIndex);
			float newHp = Math.min(maxHp, currentHp + amount);
			state.set(memberIndex, newHp);
			if (memberIndex == 0) {
				entity.setHealth(newHp);
			}
		}
	}

	private static float resolveMemberDamage(LivingEntity entity, int memberIndex, DamageSource damageSource, float baseDamage, com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState) {
		if (baseDamage <= 0.0f || statusState == null || memberIndex >= statusState.size()) {
			return baseDamage;
		}

		com.jar.jarstacker.stack.mob.status.LogicalStatusRecord record = statusState.get(memberIndex);
		if (record == null) {
			return baseDamage;
		}

		float damage = baseDamage;

		// 1. Fire Resistance
		if (damageSource.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
			if (record.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE) || entity.fireImmune()) {
				return 0.0f;
			}
		}

		// 2. Resistance
		if (damage > 0.0f && !damageSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_RESISTANCE)) {
			if (record.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE)) {
				int amp = record.getEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE).getAmplifier();
				int reduction = Math.min(25, (amp + 1) * 5);
				damage = Math.max(0.0f, damage * (25 - reduction) / 25.0f);
			}
		}

		// 3. Absorption
		if (damage > 0.0f && record.getAbsorptionAmount() > 0.0f) {
			float abs = record.getAbsorptionAmount();
			float absorbed = Math.min(abs, damage);
			record.setAbsorptionAmount(abs - absorbed);
			damage -= absorbed;
		}

		return damage;
	}
}
