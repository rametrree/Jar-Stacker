package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.status.LogicalEffectScopeResolver;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
//? if <1.21.5 {
import net.minecraft.world.entity.projectile.ThrownPotion;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.6 {
/*@Mixin(net.minecraft.world.entity.projectile.ThrownSplashPotion.class)
public abstract class ThrownPotionMixin {

	@Inject(method = "onHitAsPotion", at = @At("HEAD"))
	private void jarstacker$onApplySplashHead(net.minecraft.server.level.ServerLevel level, net.minecraft.world.item.ItemStack item, net.minecraft.world.phys.HitResult hitResult, CallbackInfo ci) {
		LogicalEffectScopeResolver.beginSplash(1.0);
	}

	@Inject(method = "onHitAsPotion", at = @At("RETURN"))
	private void jarstacker$onApplySplashReturn(net.minecraft.server.level.ServerLevel level, net.minecraft.world.item.ItemStack item, net.minecraft.world.phys.HitResult hitResult, CallbackInfo ci) {
		LogicalEffectScopeResolver.endSplash();
	}
}
*///?} else {
//? if >=1.21.5 {
/*@Mixin(net.minecraft.world.entity.projectile.ThrownSplashPotion.class)
public abstract class ThrownPotionMixin {

	@Inject(method = "onHitAsPotion", at = @At("HEAD"))
	private void jarstacker$onApplySplashHead(net.minecraft.server.level.ServerLevel level, net.minecraft.world.item.ItemStack item, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.beginSplash(1.0);
	}

	@Inject(method = "onHitAsPotion", at = @At("RETURN"))
	private void jarstacker$onApplySplashReturn(net.minecraft.server.level.ServerLevel level, net.minecraft.world.item.ItemStack item, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.endSplash();
	}
}
*///?} else {
//? if >=1.21.2 {
/*@Mixin(ThrownPotion.class)
public abstract class ThrownPotionMixin {

	@Inject(method = "applySplash", at = @At("HEAD"))
	private void jarstacker$onApplySplashHead(net.minecraft.server.level.ServerLevel level, Iterable<MobEffectInstance> effects, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.beginSplash(1.0);
	}

	@Inject(method = "applySplash", at = @At("RETURN"))
	private void jarstacker$onApplySplashReturn(net.minecraft.server.level.ServerLevel level, Iterable<MobEffectInstance> effects, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.endSplash();
	}
}
*///?} else {
@Mixin(ThrownPotion.class)
public abstract class ThrownPotionMixin {

	@Inject(method = "applySplash", at = @At("HEAD"))
	private void jarstacker$onApplySplashHead(Iterable<MobEffectInstance> effects, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.beginSplash(1.0);
	}

	@Inject(method = "applySplash", at = @At("RETURN"))
	private void jarstacker$onApplySplashReturn(Iterable<MobEffectInstance> effects, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.endSplash();
	}
}
//?}
//?}
//?}
