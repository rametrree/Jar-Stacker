package com.jar.jarstacker.adapter;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EffectAdapter {
	private EffectAdapter() {}

	public static void onMobHurt(Holder<MobEffect> effect, LivingEntity entity, int amplifier, DamageSource damageSource, float damageAmount) {
		if (effect == null || effect.value() == null || entity == null) return;
		ServerLevel serverLevel = entity.level() instanceof ServerLevel sl ? sl : null;
		//? if >=1.21.2 {
		/*effect.value().onMobHurt(serverLevel, entity, amplifier, damageSource, damageAmount);
		*///?} else {
		effect.value().onMobHurt(entity, amplifier, damageSource, damageAmount);
		//?}
	}

	public static void onMobRemoved(Holder<MobEffect> effect, LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
		if (effect == null || effect.value() == null || entity == null) return;
		ServerLevel serverLevel = entity.level() instanceof ServerLevel sl ? sl : null;
		//? if >=1.21.2 {
		/*effect.value().onMobRemoved(serverLevel, entity, amplifier, reason);
		*///?} else {
		effect.value().onMobRemoved(entity, amplifier, reason);
		//?}
	}

	public static boolean applyEffectTick(Holder<MobEffect> holder, LivingEntity entity, int amplifier) {
		if (holder == null || holder.value() == null || entity == null) return false;
		ServerLevel serverLevel = entity.level() instanceof ServerLevel sl ? sl : null;
		//? if >=1.21.2 {
		/*return holder.value().applyEffectTick(serverLevel, entity, amplifier);
		*///?} else {
		return holder.value().applyEffectTick(entity, amplifier);
		//?}
	}

	public static Tag saveMobEffectInstance(MobEffectInstance instance) {
		if (instance == null) return null;
		//? if >=1.21.5 {
		/*return MobEffectInstance.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, instance).result().orElse(null);
		*///?} else {
		return instance.save();
		//?}
	}

	public static MobEffectInstance loadMobEffectInstance(CompoundTag tag) {
		if (tag == null) return null;
		//? if >=1.21.5 {
		/*return MobEffectInstance.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).result().orElse(null);
		*///?} else {
		return MobEffectInstance.load(tag);
		//?}
	}

	public static Holder<MobEffect> getSpeed() {
		//? if >=1.21.5 {
		/*return net.minecraft.world.effect.MobEffects.SPEED;
		*///?} else {
		return net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED;
		//?}
	}

	public static Holder<MobEffect> getStrength() {
		//? if >=1.21.5 {
		/*return net.minecraft.world.effect.MobEffects.STRENGTH;
		*///?} else {
		return net.minecraft.world.effect.MobEffects.DAMAGE_BOOST;
		//?}
	}

	public static Holder<MobEffect> getResistance() {
		//? if >=1.21.5 {
		/*return net.minecraft.world.effect.MobEffects.RESISTANCE;
		*///?} else {
		return net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE;
		//?}
	}

	public static Holder<MobEffect> getInstantHealth() {
		//? if >=1.21.5 {
		/*return net.minecraft.world.effect.MobEffects.INSTANT_HEALTH;
		*///?} else {
		return net.minecraft.world.effect.MobEffects.HEAL;
		//?}
	}

	public static Holder<MobEffect> getInstantDamage() {
		//? if >=1.21.5 {
		/*return net.minecraft.world.effect.MobEffects.INSTANT_DAMAGE;
		*///?} else {
		return net.minecraft.world.effect.MobEffects.HARM;
		//?}
	}

	public static Holder<MobEffect> getSlowness() {
		//? if >=1.21.5 {
		/*return net.minecraft.world.effect.MobEffects.SLOWNESS;
		*///?} else {
		return net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN;
		//?}
	}

	public static Holder<MobEffect> getJumpBoost() {
		//? if >=1.21.5 {
		/*return net.minecraft.world.effect.MobEffects.JUMP_BOOST;
		*///?} else {
		return net.minecraft.world.effect.MobEffects.JUMP;
		//?}
	}
}

