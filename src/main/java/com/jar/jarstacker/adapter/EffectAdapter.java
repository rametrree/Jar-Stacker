package com.jar.jarstacker.adapter;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
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
}

