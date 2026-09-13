package com.jar.jarstacker.mixin;

import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to support logical stack counts on Mobs, persistence, and SINGLE death mode.
 * Reason: Vanilla LivingEntity immediately removes the entity on death. To support
 * mob stacking where killing one logical mob decrements the stack count by 1, drops
 * normal loot/XP for that single mob, and leaves the remaining stack alive, we intercept die().
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements StackableEntity {

	@Shadow public abstract float getHealth();
	@Shadow public abstract float getMaxHealth();
	@Shadow public abstract void setHealth(float health);
	@Shadow protected abstract SoundEvent getDeathSound();
	@Shadow protected abstract float getSoundVolume();
	@Shadow public abstract float getVoicePitch();
	//? if >=1.21.5 {
	/*@Shadow protected net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.player.Player> lastHurtByPlayer;
	@Shadow protected int lastHurtByPlayerMemoryTime;
	*///?} else {
	@Shadow protected int lastHurtByPlayerTime;
	@Shadow protected net.minecraft.world.entity.player.Player lastHurtByPlayer;
	//?}
	@Shadow protected int attackStrengthTicker;
	//? if >=1.21.2 {
	/*@Shadow protected abstract void dropFromLootTable(ServerLevel serverLevel, DamageSource damageSource, boolean hitByPlayer);
	*///?} else {
	@Shadow protected abstract void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer);
	//?}
	@Shadow protected abstract void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean hitByPlayer);
	@Shadow public abstract boolean isAlwaysExperienceDropper();
	@Shadow protected abstract int getExperienceReward(ServerLevel serverLevel, Entity entity);

	@Unique
	private int jarstacker$stackCount = 0;

	@Unique
	private int jarstacker$breedingLockTicks = 0;

	@Unique
	private int jarstacker$interactionLockTicks = 0;

	@Unique
	private com.jar.jarstacker.stack.mob.baby.BabyGrowthState jarstacker$babyGrowthState = null;

	@Unique
	private com.jar.jarstacker.stack.mob.health.LogicalHealthState jarstacker$logicalHealthState = null;

	@Unique
	private com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState jarstacker$logicalStatusEffectState = null;

	@Unique
	private com.jar.jarstacker.stack.mob.status.LogicalBurnState jarstacker$logicalBurnState = null;

	@Unique
	private boolean jarstacker$managedLabel = false;

	@Unique
	private boolean jarstacker$sharedIgnition = false;

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public int jarstacker$getStackCount() {
		return Math.max(1, this.jarstacker$stackCount);
	}

	@Override
	public void jarstacker$setStackCount(int count) {
		this.jarstacker$stackCount = count;
	}

	@Override
	public com.jar.jarstacker.stack.mob.baby.BabyGrowthState jarstacker$getBabyGrowthState() {
		return this.jarstacker$babyGrowthState;
	}

	@Override
	public void jarstacker$setBabyGrowthState(com.jar.jarstacker.stack.mob.baby.BabyGrowthState state) {
		this.jarstacker$babyGrowthState = state;
	}

	@Override
	public com.jar.jarstacker.stack.mob.health.LogicalHealthState jarstacker$getLogicalHealthState() {
		return this.jarstacker$logicalHealthState;
	}

	@Override
	public void jarstacker$setLogicalHealthState(com.jar.jarstacker.stack.mob.health.LogicalHealthState state) {
		this.jarstacker$logicalHealthState = state;
	}

	@Override
	public com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState jarstacker$getLogicalStatusEffectState() {
		return this.jarstacker$logicalStatusEffectState;
	}

	@Override
	public void jarstacker$setLogicalStatusEffectState(com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState state) {
		this.jarstacker$logicalStatusEffectState = state;
	}

	@Override
	public com.jar.jarstacker.stack.mob.status.LogicalBurnState jarstacker$getLogicalBurnState() {
		return this.jarstacker$logicalBurnState;
	}

	@Override
	public void jarstacker$setLogicalBurnState(com.jar.jarstacker.stack.mob.status.LogicalBurnState state) {
		this.jarstacker$logicalBurnState = state;
	}

	@Override
	public boolean jarstacker$isSharedIgnition() {
		return this.jarstacker$sharedIgnition;
	}

	@Override
	public void jarstacker$setSharedIgnition(boolean shared) {
		this.jarstacker$sharedIgnition = shared;
	}

	@Override
	public void jarstacker$dropLootAndExperience(ServerLevel serverLevel, DamageSource damageSource) {
		com.jar.jarstacker.stack.mob.death.CombatDeathContext ctx = com.jar.jarstacker.stack.mob.death.CombatDeathContext.getCurrentContext();
		boolean hitByPlayer = false;
		net.minecraft.world.entity.player.Player playerCause = null;

		if (ctx != null) {
			hitByPlayer = ctx.hasPlayerCredit();
			if (ctx.getCausingEntity() instanceof net.minecraft.world.entity.player.Player p) {
				playerCause = p;
			}
		} else {
			if (damageSource != null && damageSource.getEntity() instanceof net.minecraft.world.entity.player.Player p) {
				hitByPlayer = true;
				playerCause = p;
		//? if >=1.21.5 {
		/*} else if (this.lastHurtByPlayerMemoryTime > 0 && ((LivingEntity) (Object) this).getLastHurtByPlayer() != null) {
				hitByPlayer = true;
				playerCause = ((LivingEntity) (Object) this).getLastHurtByPlayer();
			}
		}
		if (hitByPlayer) {
			if (playerCause != null) {
				((LivingEntity) (Object) this).setLastHurtByPlayer(playerCause, this.lastHurtByPlayerMemoryTime <= 0 ? 100 : this.lastHurtByPlayerMemoryTime);
			}
			if (this.lastHurtByPlayerMemoryTime <= 0) {
				this.lastHurtByPlayerMemoryTime = 100;
			}
		} else {
			this.lastHurtByPlayerMemoryTime = 0;
			this.lastHurtByPlayer = null;
		}
		*///?} else {
			} else if (this.lastHurtByPlayerTime > 0 && this.lastHurtByPlayer != null) {
				hitByPlayer = true;
				playerCause = this.lastHurtByPlayer;
			}
		}
		if (hitByPlayer) {
			if (playerCause != null) {
				this.lastHurtByPlayer = playerCause;
			}
			if (this.lastHurtByPlayerTime <= 0) {
				this.lastHurtByPlayerTime = 100;
			}
		} else {
			this.lastHurtByPlayerTime = 0;
			this.lastHurtByPlayer = null;
		}
		//?}

		//? if >=1.21.2 {
		/*this.dropFromLootTable(serverLevel, damageSource, hitByPlayer);
		*///?} else {
		this.dropFromLootTable(damageSource, hitByPlayer);
		//?}
		this.dropCustomDeathLoot(serverLevel, damageSource, hitByPlayer);

		if (hitByPlayer || this.isAlwaysExperienceDropper()) {
			int xp = this.getExperienceReward(serverLevel, playerCause != null ? playerCause : (damageSource != null ? damageSource.getEntity() : null));
			if (xp <= 0 && (Object) this instanceof Mob) {
				xp = 5;
			}
			if (xp > 0) {
				net.minecraft.world.entity.ExperienceOrb.award(serverLevel, this.position(), xp);
			}
		}
	}

	@Override
	public int jarstacker$getLastHurtByPlayerTime() {
		//? if >=1.21.5 {
		/*return this.lastHurtByPlayerMemoryTime;
		*///?} else {
		return this.lastHurtByPlayerTime;
		//?}
	}

	@Override
	public void jarstacker$setLastHurtByPlayerTime(int time) {
		//? if >=1.21.5 {
		/*this.lastHurtByPlayerMemoryTime = time;
		*///?} else {
		this.lastHurtByPlayerTime = time;
		//?}
	}

	@Override
	public net.minecraft.world.entity.player.Player jarstacker$getLastHurtByPlayer() {
		//? if >=1.21.5 {
		/*return ((LivingEntity) (Object) this).getLastHurtByPlayer();
		*///?} else {
		return this.lastHurtByPlayer;
		//?}
	}

	@Override
	public void jarstacker$setLastHurtByPlayer(net.minecraft.world.entity.player.Player player) {
		//? if >=1.21.5 {
		/*if (player != null) {
			((LivingEntity) (Object) this).setLastHurtByPlayer(player, this.lastHurtByPlayerMemoryTime > 0 ? this.lastHurtByPlayerMemoryTime : 100);
		} else {
			this.lastHurtByPlayer = null;
			this.lastHurtByPlayerMemoryTime = 0;
		}
		*///?} else {
		this.lastHurtByPlayer = player;
		//?}
	}

	@Override
	public void jarstacker$playDeathEffects() {
		SoundEvent deathSound = this.getDeathSound();
		if (deathSound != null) {
			this.playSound(deathSound, this.getSoundVolume(), this.getVoicePitch());
		}
		this.level().broadcastEntityEvent(this, (byte) 2);
	}

	@Override
	public void jarstacker$setAttackStrengthTicker(int ticks) {
		this.attackStrengthTicker = ticks;
	}

	@Override
	public int jarstacker$getBreedingLockTicks() {
		return this.jarstacker$breedingLockTicks;
	}

	@Override
	public void jarstacker$setBreedingLockTicks(int ticks) {
		this.jarstacker$breedingLockTicks = Math.max(0, ticks);
	}

	@Override
	public int jarstacker$getInteractionLockTicks() {
		return this.jarstacker$interactionLockTicks;
	}

	@Override
	public void jarstacker$setInteractionLockTicks(int ticks) {
		this.jarstacker$interactionLockTicks = Math.max(0, ticks);
	}

	@Override
	public boolean jarstacker$hasManagedLabel() {
		return this.jarstacker$managedLabel;
	}

	@Override
	public void jarstacker$setManagedLabel(boolean managed) {
		this.jarstacker$managedLabel = managed;
	}

	@Unique private net.minecraft.world.phys.Vec3 jarstacker$lastPos = null;
	@Unique private int jarstacker$diagTicks = 0;

	@Inject(method = "tick", at = @At("HEAD"))
	private void jarstacker$onTick(CallbackInfo ci) {
		if (this.jarstacker$breedingLockTicks > 0) {
			this.jarstacker$breedingLockTicks--;
			if ((Object) this instanceof net.minecraft.world.entity.animal.Animal animal) {
				if (animal.getAge() > 0 || !animal.isInLove()) {
					this.jarstacker$breedingLockTicks = 0;
				}
			}
		}
		if (this.jarstacker$interactionLockTicks > 0) {
			this.jarstacker$interactionLockTicks--;
		}

		if (this.jarstacker$sharedIgnition && this.getRemainingFireTicks() <= 0) {
			this.jarstacker$sharedIgnition = false;
		}

		if ((Object) this instanceof net.minecraft.world.entity.animal.Animal animal && com.jar.jarstacker.stack.mob.MovementDiagnostics.isLoggingEnabled()) {
			if (this.jarstacker$diagTicks == 0) {
				com.jar.jarstacker.stack.mob.MovementDiagnostics.logMovementEvent("FIRST_TICK", animal, null, null);
			} else if (this.jarstacker$diagTicks <= 30) {
				if (this.jarstacker$lastPos != null && !this.jarstacker$lastPos.equals(animal.position())) {
					com.jar.jarstacker.stack.mob.MovementDiagnostics.logMovementEvent("POSITION_CHANGED", animal, this.jarstacker$lastPos, "tickAge=" + this.jarstacker$diagTicks);
				}
			}
			this.jarstacker$lastPos = animal.position();
			this.jarstacker$diagTicks++;
		}
	}

	@Inject(method = "doPush", at = @At("HEAD"))
	private void jarstacker$onDoPush(Entity entity, CallbackInfo ci) {
		if (com.jar.jarstacker.stack.mob.MovementDiagnostics.isLoggingEnabled() && (Object) this instanceof net.minecraft.world.entity.animal.Animal animal) {
			com.jar.jarstacker.stack.mob.MovementDiagnostics.logMovementEvent("COLLISION_PUSH", animal, null, "pushedBy=" + entity.getUUID());
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void jarstacker$saveData(CompoundTag tag, CallbackInfo ci) {
		if (this.jarstacker$stackCount > 1) {
			tag.putInt("JarStackerCount", this.jarstacker$stackCount);
		}
		if (this.jarstacker$breedingLockTicks > 0) {
			tag.putInt("JarStackerBreedingLock", this.jarstacker$breedingLockTicks);
		}
		if (this.jarstacker$interactionLockTicks > 0) {
			tag.putInt("JarStackerInteractionLock", this.jarstacker$interactionLockTicks);
		}
		if (this.jarstacker$babyGrowthState != null && !this.jarstacker$babyGrowthState.isEmpty()) {
			this.jarstacker$babyGrowthState.saveToNbt(tag);
		}
		if (this.jarstacker$logicalHealthState != null && !this.jarstacker$logicalHealthState.isEmpty()) {
			this.jarstacker$logicalHealthState.saveToNbt(tag);
		}
		if (this.jarstacker$logicalStatusEffectState != null && !this.jarstacker$logicalStatusEffectState.isEmpty()) {
			tag.put(com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState.NBT_KEY, this.jarstacker$logicalStatusEffectState.saveToNbt());
		}
		if (this.jarstacker$logicalBurnState != null && !this.jarstacker$logicalBurnState.isEmpty()) {
			tag.put(com.jar.jarstacker.stack.mob.status.LogicalBurnState.NBT_KEY, this.jarstacker$logicalBurnState.saveToNbt());
		}
		if (this.jarstacker$managedLabel) {
			tag.putBoolean("JarStackerManagedLabel", true);
		}
		if (this.jarstacker$sharedIgnition) {
			tag.putBoolean("JarStackerSharedIgnition", true);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void jarstacker$loadData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains("JarStackerCount")) {
			this.jarstacker$stackCount = com.jar.jarstacker.adapter.NbtAdapter.getInt(tag, "JarStackerCount");
			if ((Object) this instanceof Mob mob) {
				MobStackingManager.updateLabel(mob, this.jarstacker$stackCount, ModConfig.getInstance().getMobStacking().isShowLabel());
			}
		} else {
			this.jarstacker$stackCount = 1;
		}
		if (tag.contains("JarStackerBreedingLock")) {
			this.jarstacker$breedingLockTicks = com.jar.jarstacker.adapter.NbtAdapter.getInt(tag, "JarStackerBreedingLock");
		}
		if (tag.contains("JarStackerInteractionLock")) {
			this.jarstacker$interactionLockTicks = com.jar.jarstacker.adapter.NbtAdapter.getInt(tag, "JarStackerInteractionLock");
		}
		if (tag.contains("JarStackerManagedLabel")) {
			this.jarstacker$managedLabel = com.jar.jarstacker.adapter.NbtAdapter.getBoolean(tag, "JarStackerManagedLabel");
		}
		if (tag.contains("JarStackerSharedIgnition")) {
			this.jarstacker$sharedIgnition = com.jar.jarstacker.adapter.NbtAdapter.getBoolean(tag, "JarStackerSharedIgnition");
		}
		com.jar.jarstacker.stack.mob.baby.BabyGrowthState loadedGrowth = com.jar.jarstacker.stack.mob.baby.BabyGrowthState.loadFromNbt(tag);
		if (loadedGrowth != null) {
			this.jarstacker$babyGrowthState = loadedGrowth;
		}

		com.jar.jarstacker.stack.mob.health.LogicalHealthState loadedHealth = com.jar.jarstacker.stack.mob.health.LogicalHealthState.loadFromNbt(tag);
		if (loadedHealth != null) {
			this.jarstacker$logicalHealthState = loadedHealth;
		} else if (this.jarstacker$stackCount > 1) {
			this.jarstacker$logicalHealthState = new com.jar.jarstacker.stack.mob.health.LogicalHealthState();
			float maxHp = this.getMaxHealth() > 0.0f ? this.getMaxHealth() : 20.0f;
			float curHp = this.getHealth();
			float initialHp = Math.max(0.0f, Math.min(curHp, maxHp));
			this.jarstacker$logicalHealthState.initializeDefault(this.jarstacker$stackCount, initialHp);
			com.jar.jarstacker.JarStackerMod.LOGGER.info("[JarStacker] HEALTH_MIGRATION uuid={} type={} count={} initialHp={}",
				this.getUUID(), net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()), this.jarstacker$stackCount, initialHp);
		}

		com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState loadedStatus = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState.loadFromNbt(tag);
		if (loadedStatus != null) {
			this.jarstacker$logicalStatusEffectState = loadedStatus;
		} else if (this.jarstacker$stackCount > 1) {
			// Conservative migration: active member 0 gets representative effects, remaining members clean
			com.jar.jarstacker.stack.mob.status.LogicalStatusRecord old0 = (this.jarstacker$logicalStatusEffectState != null && !this.jarstacker$logicalStatusEffectState.isEmpty())
				? this.jarstacker$logicalStatusEffectState.get(0) : null;
			this.jarstacker$logicalStatusEffectState = new com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState();
			this.jarstacker$logicalStatusEffectState.initializeDefault(this.jarstacker$stackCount);
			com.jar.jarstacker.stack.mob.status.LogicalStatusRecord record0 = this.jarstacker$logicalStatusEffectState.get(0);
			if (old0 != null) {
				for (net.minecraft.world.effect.MobEffectInstance instance : old0.getEffectInstances()) {
					record0.addEffect(new net.minecraft.world.effect.MobEffectInstance(instance), (LivingEntity) (Object) this);
				}
			} else if ((Object) this instanceof LivingEntity living) {
				for (net.minecraft.world.effect.MobEffectInstance instance : living.getActiveEffects()) {
					record0.addEffect(new net.minecraft.world.effect.MobEffectInstance(instance), living);
				}
			}
		}

		com.jar.jarstacker.stack.mob.status.LogicalBurnState loadedBurn = com.jar.jarstacker.stack.mob.status.LogicalBurnState.loadFromNbt(tag);
		if (loadedBurn != null) {
			this.jarstacker$logicalBurnState = loadedBurn;
		} else if (this.jarstacker$stackCount > 1) {
			this.jarstacker$logicalBurnState = new com.jar.jarstacker.stack.mob.status.LogicalBurnState();
			this.jarstacker$logicalBurnState.initializeDefault(this.jarstacker$stackCount);
			int fireTicks = this.getRemainingFireTicks();
			if (this.jarstacker$sharedIgnition) {
				this.jarstacker$logicalBurnState.igniteAll(fireTicks, true);
			} else if (fireTicks > 0) {
				this.jarstacker$logicalBurnState.ignite(0, fireTicks);
			}
		}

		if ((Object) this instanceof LivingEntity living) {
			com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.repairLogicalState(living);
		}
	}

	@Unique private com.jar.jarstacker.stack.mob.death.CombatDeathContext.Scope jarstacker$dieScope = null;

	//? if >=1.21.2 {
	/*@Inject(
		method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"
		)
	)
	private void jarstacker$onActuallyHurt(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, float amount, CallbackInfo ci) {
		jarstacker$handleActuallyHurt(damageSource, amount);
	}
	*///?} else {
	@Inject(
		method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"
		)
	)
	private void jarstacker$onActuallyHurt(DamageSource damageSource, float amount, CallbackInfo ci) {
		jarstacker$handleActuallyHurt(damageSource, amount);
	}
	//?}

	@Unique
	private void jarstacker$handleActuallyHurt(DamageSource damageSource, float amount) {
		if ((Object) this instanceof LivingEntity living) {
			com.jar.jarstacker.stack.mob.health.LogicalHealthManager.IN_ACTUALLY_HURT.set(true);
			try {
				com.jar.jarstacker.stack.mob.death.CombatDeathContext context = com.jar.jarstacker.stack.mob.death.CombatDeathContext.capture(living, damageSource);
				try (com.jar.jarstacker.stack.mob.death.CombatDeathContext.Scope scope = context.openScope()) {
					com.jar.jarstacker.stack.mob.health.LogicalHealthManager.onDamageApplied(living, damageSource, amount);
				}
			} finally {
				com.jar.jarstacker.stack.mob.health.LogicalHealthManager.IN_ACTUALLY_HURT.set(false);
			}
		}
	}

	@Inject(method = "die", at = @At("HEAD"), cancellable = true)
	private void jarstacker$onDie(DamageSource damageSource, CallbackInfo ci) {
		if (!((Object) this instanceof Mob mob)) {
			return;
		}
		if (mob.level().isClientSide) {
			return;
		}

		com.jar.jarstacker.stack.mob.death.CombatDeathContext context = com.jar.jarstacker.stack.mob.death.CombatDeathContext.capture(mob, damageSource);
		this.jarstacker$dieScope = context.openScope();

		if (com.jar.jarstacker.stack.mob.health.LogicalHealthManager.handleDie(mob, damageSource)) {
			if (this.jarstacker$dieScope != null) {
				this.jarstacker$dieScope.close();
				this.jarstacker$dieScope = null;
			}
			ci.cancel();
		}
	}

	@Inject(method = "die", at = @At("TAIL"))
	private void jarstacker$onDieTail(DamageSource damageSource, CallbackInfo ci) {
		if (this.jarstacker$dieScope != null) {
			this.jarstacker$dieScope.close();
			this.jarstacker$dieScope = null;
		}
	}

	@Inject(method = "tickEffects", at = @At("HEAD"), cancellable = true)
	private void jarstacker$tickEffects(CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (this.jarstacker$stackCount > 1) {
			com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.tick(entity);
			ci.cancel();
		}
	}

	@Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
	private void jarstacker$addEffect(net.minecraft.world.effect.MobEffectInstance effectInstance, Entity source, CallbackInfoReturnable<Boolean> cir) {
		if (com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.isLogicalTicking()) {
			return;
		}
		LivingEntity entity = (LivingEntity) (Object) this;
		if (this.jarstacker$stackCount > 1) {
			boolean result = com.jar.jarstacker.stack.mob.status.LogicalStatusEffectManager.applyEffect(entity, effectInstance, source);
			cir.setReturnValue(result);
		}
	}

	@Unique
	private long jarstacker$lastUnsupportedExtractionTime = 0L;

	@Override
	public long jarstacker$getLastUnsupportedExtractionTime() {
		return this.jarstacker$lastUnsupportedExtractionTime;
	}

	@Override
	public void jarstacker$setLastUnsupportedExtractionTime(long time) {
		this.jarstacker$lastUnsupportedExtractionTime = time;
	}

	@Inject(method = "hasEffect(Lnet/minecraft/core/Holder;)Z", at = @At("HEAD"), cancellable = true)
	private void jarstacker$hasEffect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
		if (this.jarstacker$stackCount > 1 && this.jarstacker$logicalStatusEffectState != null && !this.jarstacker$logicalStatusEffectState.isEmpty()) {
			com.jar.jarstacker.stack.mob.status.LogicalStatusRecord active = this.jarstacker$logicalStatusEffectState.get(0);
			if (active != null) {
				cir.setReturnValue(active.hasEffect(effect));
			}
		}
	}

	@Inject(method = "getEffect(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/effect/MobEffectInstance;", at = @At("HEAD"), cancellable = true)
	private void jarstacker$getEffect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, CallbackInfoReturnable<net.minecraft.world.effect.MobEffectInstance> cir) {
		if (this.jarstacker$stackCount > 1 && this.jarstacker$logicalStatusEffectState != null && !this.jarstacker$logicalStatusEffectState.isEmpty()) {
			com.jar.jarstacker.stack.mob.status.LogicalStatusRecord active = this.jarstacker$logicalStatusEffectState.get(0);
			if (active != null) {
				cir.setReturnValue(active.getEffect(effect));
			}
		}
	}

	@Inject(method = "getActiveEffects()Ljava/util/Collection;", at = @At("HEAD"), cancellable = true)
	private void jarstacker$getActiveEffects(CallbackInfoReturnable<java.util.Collection<net.minecraft.world.effect.MobEffectInstance>> cir) {
		if (this.jarstacker$stackCount > 1 && this.jarstacker$logicalStatusEffectState != null && !this.jarstacker$logicalStatusEffectState.isEmpty()) {
			com.jar.jarstacker.stack.mob.status.LogicalStatusRecord active = this.jarstacker$logicalStatusEffectState.get(0);
			if (active != null) {
				cir.setReturnValue(active.getEffectInstances());
			}
		}
	}

	@Inject(method = "getActiveEffectsMap()Ljava/util/Map;", at = @At("HEAD"), cancellable = true)
	private void jarstacker$getActiveEffectsMap(CallbackInfoReturnable<java.util.Map<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>, net.minecraft.world.effect.MobEffectInstance>> cir) {
		if (this.jarstacker$stackCount > 1 && this.jarstacker$logicalStatusEffectState != null && !this.jarstacker$logicalStatusEffectState.isEmpty()) {
			com.jar.jarstacker.stack.mob.status.LogicalStatusRecord active = this.jarstacker$logicalStatusEffectState.get(0);
			if (active != null) {
				cir.setReturnValue(active.getActiveEffects());
			}
		}
	}
}