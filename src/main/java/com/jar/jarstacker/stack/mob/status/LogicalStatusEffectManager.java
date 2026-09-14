package com.jar.jarstacker.stack.mob.status;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.health.LogicalHealthManager;
import com.jar.jarstacker.stack.mob.health.LogicalHealthState;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Core manager for authoritative logical status effects, burn timers,
 * single-tick deduplication, and representative projection (Jar Stacker V0.6.0).
 */
public class LogicalStatusEffectManager {

	private static final ThreadLocal<Boolean> IS_LOGICAL_TICKING = ThreadLocal.withInitial(() -> false);

	public static boolean isLogicalTicking() {
		return IS_LOGICAL_TICKING.get();
	}

	public static LogicalStatusEffectState getOrCreateStatusState(LivingEntity entity) {
		if (entity == null) {
			return null;
		}
		StackableEntity stackable = (StackableEntity) entity;
		LogicalStatusEffectState state = stackable.jarstacker$getLogicalStatusEffectState();
		int count = stackable.jarstacker$getStackCount();
		if (count <= 0) count = 1;

		if (state == null) {
			state = new LogicalStatusEffectState();
			state.initializeDefault(count);
			stackable.jarstacker$setLogicalStatusEffectState(state);
		} else if (state.size() < count) {
			while (state.size() < count) {
				state.add(new LogicalStatusRecord());
			}
		}
		return state;
	}

	public static LogicalBurnState getOrCreateBurnState(LivingEntity entity) {
		if (entity == null) {
			return null;
		}
		StackableEntity stackable = (StackableEntity) entity;
		LogicalBurnState state = stackable.jarstacker$getLogicalBurnState();
		int count = stackable.jarstacker$getStackCount();
		if (count <= 0) count = 1;

		if (state == null) {
			state = new LogicalBurnState();
			state.initializeDefault(count);
			stackable.jarstacker$setLogicalBurnState(state);
		} else if (state.size() < count) {
			while (state.size() < count) {
				state.add(new LogicalBurnRecord());
			}
		}
		return state;
	}

	public static void tick(LivingEntity entity) {
		if (entity == null || entity.level().isClientSide() || !entity.isAlive() || entity.isRemoved()) {
			return;
		}

		StackableEntity stackable = (StackableEntity) entity;
		int count = stackable.jarstacker$getStackCount();
		if (count <= 0) {
			return;
		}

		LogicalStatusEffectState statusState = getOrCreateStatusState(entity);
		LogicalBurnState burnState = getOrCreateBurnState(entity);
		LogicalHealthState healthState = LogicalHealthManager.getOrCreateState(entity);

		IS_LOGICAL_TICKING.set(true);
		try {
			// Check if physical entity fire was extinguished by water/rain/powder snow
			if (entity.isInWaterOrRain() || entity.wasInPowderSnow) {
				burnState.extinguishAll();
				stackable.jarstacker$setSharedIgnition(false);
			}

			// 1. Authoritative per-member status effect ticking
			int memberCount = Math.min(statusState.size(), healthState.size());
			for (int i = 0; i < memberCount; i++) {
				LogicalStatusRecord record = statusState.get(i);
				if (record == null) continue;

				Iterator<Map.Entry<Holder<MobEffect>, MobEffectInstance>> it = record.getActiveEffects().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<Holder<MobEffect>, MobEffectInstance> entry = it.next();
					Holder<MobEffect> holder = entry.getKey();
					MobEffectInstance instance = entry.getValue();

					int effectiveDuration = instance.isInfiniteDuration() ? entity.tickCount : instance.getDuration();
					if (holder.value().shouldApplyEffectTickThisTick(effectiveDuration, instance.getAmplifier())) {
						applyEffectTickToMember(entity, i, holder, instance);
					}

					((com.jar.jarstacker.mixin.MobEffectInstanceAccessor) instance).jarstacker$invokeTickDownDuration();
					if (instance.getDuration() <= 0) {
						it.remove();
						if (i == 0) {
							holder.value().removeAttributeModifiers(entity.getAttributes());
							float maxHp = entity.getMaxHealth();
							if (entity.getHealth() > maxHp) {
								entity.setHealth(maxHp);
							}
							if (healthState != null && !healthState.isEmpty() && healthState.get(0) > maxHp) {
								healthState.set(0, maxHp);
							}
						}
					}
				}
			}

			// 2. Authoritative per-member burn ticking
			int burnCount = Math.min(burnState.size(), healthState.size());
			for (int i = 0; i < burnCount; i++) {
				LogicalBurnRecord burnRecord = burnState.get(i);
				if (burnRecord == null || !burnRecord.isBurning()) continue;

				burnRecord.decrementFireTicks();
				// Deal fire damage every 20 ticks (1 second)
				if (burnRecord.getRemainingFireTicks() % 20 == 0) {
					boolean alreadyHandledByVanilla = (stackable.jarstacker$getLastFireDamageTick() == entity.level().getGameTime())
						&& (i == 0 || burnRecord.isSharedIgnition());
					if (!alreadyHandledByVanilla) {
						LogicalStatusRecord statusRecord = statusState.get(i);
						boolean hasFireResist = (statusRecord != null && statusRecord.hasEffect(MobEffects.FIRE_RESISTANCE)) || entity.fireImmune();
						if (!hasFireResist) {
							LogicalHealthManager.applyLogicalDirectDamage(entity, i, entity.damageSources().onFire(), 1.0f);
						}
					}
				}
			}

			// 3. Project active member 0 onto the representative physical entity
			projectActiveMember(entity);

		} finally {
			IS_LOGICAL_TICKING.set(false);
		}
	}

	private static void applyEffectTickToMember(LivingEntity entity, int memberIndex, Holder<MobEffect> holder, MobEffectInstance instance) {
		LogicalHealthState hState = LogicalHealthManager.getOrCreateState(entity);
		float currentHp = (hState != null && memberIndex < hState.size())
			? hState.get(memberIndex)
			: entity.getHealth();

		if (holder.is(MobEffects.POISON)) {
			// Vanilla rule: Poison cannot kill; reduces health until 1.0F
			if (currentHp > 1.0f) {
				LogicalHealthManager.applyLogicalDirectDamage(entity, memberIndex, entity.damageSources().magic(), 1.0f);
			}
		} else if (holder.is(MobEffects.WITHER)) {
			// Wither can deliver lethal damage
			LogicalHealthManager.applyLogicalDirectDamage(entity, memberIndex, entity.damageSources().wither(), 1.0f);
		} else if (holder.is(MobEffects.REGENERATION)) {
			// Regeneration heals affected member up to max HP
			if (currentHp < entity.getMaxHealth()) {
				LogicalHealthManager.healMember(entity, memberIndex, 1.0f);
			}
		} else {
			// Generic Vanilla effect execution via scoped temporary projection
			LogicalVanillaEffectExecutor.executeVanillaTick(entity, memberIndex, holder, instance);
		}
	}

	public static boolean applyEffect(LivingEntity entity, MobEffectInstance instance, Entity source) {
		if (entity == null || entity.level().isClientSide() || instance == null) {
			return false;
		}

		LogicalEffectBehaviorClass behaviorClass = LogicalEffectClassifier.classify(instance.getEffect());
		if (behaviorClass == LogicalEffectBehaviorClass.UNSUPPORTED) {
			return handleUnsupportedEffect(entity, instance, source);
		}

		LogicalStatusEffectState statusState = getOrCreateStatusState(entity);
		LogicalEffectScopeResolver.Scope scope = LogicalEffectScopeResolver.resolve(source);

		//? if >=26.2 {
		/*if (instance.getEffect().value().isInstantaneous()) {
		*///?} else {
		if (instance.getEffect().value().isInstantenous()) {
		//?}
			double intensity = LogicalEffectScopeResolver.isInSplash() ? LogicalEffectScopeResolver.getSplashIntensity() : 1.0;
			applyInstantEffect(entity, instance.getEffect(), instance.getAmplifier(), intensity, scope);
			return true;
		}

		if (scope == LogicalEffectScopeResolver.Scope.SINGLE) {
			LogicalStatusRecord activeRecord = statusState.get(0);
			if (activeRecord != null) {
				boolean added = activeRecord.addEffect(new MobEffectInstance(instance), entity);
				if (added) {
					projectActiveMember(entity);
				}
				return added;
			}
			return false;
		} else {
			// AREA or SHARED_ENVIRONMENT: apply to all represented logical members
			boolean anyAdded = false;
			for (int i = 0; i < statusState.size(); i++) {
				LogicalStatusRecord record = statusState.get(i);
				if (record != null) {
					if (record.addEffect(new MobEffectInstance(instance), entity)) {
						anyAdded = true;
					}
				}
			}
			if (anyAdded) {
				projectActiveMember(entity);
			}
			return anyAdded;
		}
	}

	public static final int MAX_UNSUPPORTED_AREA_FALLBACK = 5;

	private static boolean handleUnsupportedEffect(LivingEntity entity, MobEffectInstance instance, Entity source) {
		if (!(entity instanceof net.minecraft.world.entity.Mob mob)) {
			return false;
		}

		StackableEntity stackable = (StackableEntity) mob;
		int count = stackable.jarstacker$getStackCount();
		if (count <= 1) {
			// Physical singleton: clear logical virtualization, transfer existing logical effects to physical mob, and let Vanilla handle it natively
			LogicalStatusEffectState status = stackable.jarstacker$getLogicalStatusEffectState();
			if (status != null && !status.isEmpty()) {
				LogicalStatusRecord rec = status.get(0);
				if (rec != null) {
					for (MobEffectInstance existing : rec.getEffectInstances()) {
						mob.addEffect(new MobEffectInstance(existing));
					}
				}
			}
			stackable.jarstacker$setLogicalStatusEffectState(null);
			return mob.addEffect(new MobEffectInstance(instance), source);
		}

		// Stack with count > 1
		LogicalEffectScopeResolver.Scope scope = LogicalEffectScopeResolver.resolve(source);
		if (scope == LogicalEffectScopeResolver.Scope.SINGLE) {
			net.minecraft.world.entity.Mob extracted = com.jar.jarstacker.stack.mob.equipment.RuntimeCombatStateTransitionHandler.extractUnsupportedMob(mob, instance);
			return extracted != null;
		} else {
			// AREA effect (Splash / Lingering cloud)
			long gameTime = mob.level().getGameTime();
			long lastExtraction = stackable.jarstacker$getLastUnsupportedExtractionTime();
			if (gameTime - lastExtraction < 20) {
				// Within reapplication cooldown window (20 ticks): reject repeated extraction
				return false;
			}
			stackable.jarstacker$setLastUnsupportedExtractionTime(gameTime);

			// Extract up to bounded fallback limit (MAX_UNSUPPORTED_AREA_FALLBACK = 5)
			int toExtract = Math.min(MAX_UNSUPPORTED_AREA_FALLBACK, count - 1);
			boolean anyExtracted = false;
			for (int i = 0; i < toExtract; i++) {
				if (stackable.jarstacker$getStackCount() <= 1) break;
				net.minecraft.world.entity.Mob extracted = com.jar.jarstacker.stack.mob.equipment.RuntimeCombatStateTransitionHandler.extractUnsupportedMob(mob, instance);
				if (extracted != null) {
					anyExtracted = true;
				} else {
					break;
				}
			}
			return anyExtracted;
		}
	}

	public static void applyInstantEffect(LivingEntity entity, Holder<MobEffect> effect, int amplifier, double factor, LogicalEffectScopeResolver.Scope scope) {
		if (entity == null || entity.level().isClientSide() || effect == null) {
			return;
		}

		boolean isInverted = entity.isInvertedHealAndHarm();
		boolean isInstantHealth = effect.is(com.jar.jarstacker.adapter.EffectAdapter.getInstantHealth());
		boolean harms = isInstantHealth ? isInverted : !isInverted;

		float baseAmount = isInstantHealth ? (float) Math.max(4 << amplifier, 0) : (float) (6 << amplifier);
		float finalAmount = baseAmount * (float) factor;
		if (finalAmount <= 0.0f) {
			return;
		}

		LogicalHealthState healthState = LogicalHealthManager.getOrCreateState(entity);
		int targetCount = (scope == LogicalEffectScopeResolver.Scope.SINGLE) ? 1 : healthState.size();

		for (int i = 0; i < targetCount; i++) {
			if (harms) {
				DamageSource magic = entity.damageSources().magic();
				LogicalHealthManager.applyLogicalDirectDamage(entity, i, magic, finalAmount);
			} else {
				LogicalHealthManager.healMember(entity, i, finalAmount);
			}
		}
	}

	public static void projectActiveMember(LivingEntity entity) {
		if (entity == null || entity.level().isClientSide()) {
			return;
		}

		LogicalStatusEffectState statusState = getOrCreateStatusState(entity);
		LogicalBurnState burnState = getOrCreateBurnState(entity);

		LogicalStatusRecord activeRecord = statusState.get(0);
		AttributeMap attributes = entity.getAttributes();

		if (activeRecord != null) {
			// Synchronize attribute modifiers for active member
			for (MobEffectInstance instance : activeRecord.getEffectInstances()) {
				Holder<MobEffect> holder = instance.getEffect();
				// Ensure active attribute modifiers are in place
				holder.value().removeAttributeModifiers(attributes);
				holder.value().addAttributeModifiers(attributes, instance.getAmplifier());
			}

			// Invisibility & Glowing
			entity.setInvisible(activeRecord.hasEffect(MobEffects.INVISIBILITY));
			entity.setGlowingTag(activeRecord.hasEffect(MobEffects.GLOWING));
		}

		// Synchronize representative burn timer and shared ignition
		if (burnState != null && !burnState.isEmpty()) {
			LogicalBurnRecord rec0 = burnState.get(0);
			int fireTicks = rec0 != null ? rec0.getRemainingFireTicks() : 0;
			entity.setRemainingFireTicks(fireTicks);
			if (entity instanceof StackableEntity stackable) {
				stackable.jarstacker$setSharedIgnition(rec0 != null && rec0.isSharedIgnition());
			}
		}
	}

	public static void handleIgnite(LivingEntity entity, int ticks) {
		if (entity == null || entity.level().isClientSide() || ticks <= 0) {
			return;
		}
		StackableEntity stackable = (StackableEntity) entity;
		LogicalBurnState burnState = getOrCreateBurnState(entity);
		if (burnState == null) {
			return;
		}

		boolean isDirect = com.jar.jarstacker.stack.mob.health.CombatContext.isDirectAttackActive()
			|| com.jar.jarstacker.stack.mob.health.ProjectileImpactContext.isProjectileImpactActive();
		if (isDirect) {
			// Direct single-target attack (flaming arrow, Fire Aspect melee)
			burnState.ignite(0, ticks);
			LogicalBurnRecord record0 = burnState.get(0);
			if (record0 != null) {
				record0.setSharedIgnition(false);
			}
			stackable.jarstacker$setSharedIgnition(false);
		} else {
			// Position-wide / environmental hazard (sunlight, lava, fire block, campfire, lightning)
			burnState.igniteAll(ticks, true);
			stackable.jarstacker$setSharedIgnition(true);
		}
	}

	public static void onActiveMemberSwitched(LivingEntity entity, LogicalStatusRecord oldRecord, LogicalStatusRecord newRecord) {
		if (entity == null || entity.level().isClientSide()) {
			return;
		}

		AttributeMap attributes = entity.getAttributes();

		// Cleanly remove stale attribute modifiers from old active member
		if (oldRecord != null) {
			for (MobEffectInstance instance : oldRecord.getEffectInstances()) {
				instance.getEffect().value().removeAttributeModifiers(attributes);
			}
		}

		// Apply attribute modifiers for new active member
		if (newRecord != null) {
			for (MobEffectInstance instance : newRecord.getEffectInstances()) {
				instance.getEffect().value().addAttributeModifiers(attributes, instance.getAmplifier());
			}
			entity.setInvisible(newRecord.hasEffect(MobEffects.INVISIBILITY));
			entity.setGlowingTag(newRecord.hasEffect(MobEffects.GLOWING));
		} else {
			entity.setInvisible(false);
			entity.setGlowingTag(false);
		}

		// Clamp physical and logical health if max health decreased (e.g. Health Boost removal)
		float maxHp = entity.getMaxHealth();
		if (entity.getHealth() > maxHp) {
			entity.setHealth(maxHp);
		}
		LogicalHealthState hState = ((StackableEntity) entity).jarstacker$getLogicalHealthState();
		if (hState != null && !hState.isEmpty() && hState.get(0) > maxHp) {
			hState.set(0, maxHp);
		}
	}

	public static void mergeStates(LivingEntity target, LivingEntity source, int countToAdd) {
		if (target == null || source == null || countToAdd <= 0) {
			return;
		}

		LogicalStatusEffectState targetStatus = getOrCreateStatusState(target);
		LogicalStatusEffectState sourceStatus = getOrCreateStatusState(source);
		if (targetStatus != null && sourceStatus != null) {
			if (countToAdd >= sourceStatus.size()) {
				targetStatus.merge(sourceStatus);
			} else {
				targetStatus.merge(sourceStatus.extractBatch(countToAdd));
			}
		}

		LogicalBurnState targetBurn = getOrCreateBurnState(target);
		LogicalBurnState sourceBurn = getOrCreateBurnState(source);
		if (targetBurn != null && sourceBurn != null) {
			if (countToAdd >= sourceBurn.size()) {
				targetBurn.merge(sourceBurn);
			} else {
				targetBurn.merge(sourceBurn.extractBatch(countToAdd));
			}
		}
	}

	public static void extractState(LivingEntity source, LivingEntity extracted) {
		if (source == null || extracted == null) {
			return;
		}

		LogicalStatusEffectState sourceStatus = getOrCreateStatusState(source);
		LogicalStatusRecord extractedRecord = sourceStatus != null ? sourceStatus.extractActive() : new LogicalStatusRecord();

		LogicalStatusEffectState extractedStatus = new LogicalStatusEffectState();
		extractedStatus.add(extractedRecord.copy());
		((StackableEntity) extracted).jarstacker$setLogicalStatusEffectState(extractedStatus);

		LogicalBurnState sourceBurn = getOrCreateBurnState(source);
		LogicalBurnRecord extractedBurn = sourceBurn != null ? sourceBurn.extractActive() : new LogicalBurnRecord();

		LogicalBurnState extractedBurnState = new LogicalBurnState();
		extractedBurnState.add(extractedBurn.copy());
		((StackableEntity) extracted).jarstacker$setLogicalBurnState(extractedBurnState);

		// Project to both source (remainder) and extracted singleton
		if (sourceStatus != null && !sourceStatus.isEmpty()) {
			onActiveMemberSwitched(source, extractedRecord, sourceStatus.get(0));
		}
		projectActiveMember(extracted);
	}
}
