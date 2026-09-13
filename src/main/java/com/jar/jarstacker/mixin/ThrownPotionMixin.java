package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.status.LogicalEffectScopeResolver;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownPotion.class)
public abstract class ThrownPotionMixin {

	//? if >=1.21.2 {
	/*@Inject(method = "applySplash", at = @At("HEAD"))
	private void jarstacker$onApplySplashHead(net.minecraft.server.level.ServerLevel level, Iterable<MobEffectInstance> effects, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.beginSplash(1.0);
	}

	@Inject(method = "applySplash", at = @At("RETURN"))
	private void jarstacker$onApplySplashReturn(net.minecraft.server.level.ServerLevel level, Iterable<MobEffectInstance> effects, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.endSplash();
	}
	*///?} else {
	@Inject(method = "applySplash", at = @At("HEAD"))
	private void jarstacker$onApplySplashHead(Iterable<MobEffectInstance> effects, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.beginSplash(1.0);
	}

	@Inject(method = "applySplash", at = @At("RETURN"))
	private void jarstacker$onApplySplashReturn(Iterable<MobEffectInstance> effects, Entity entity, CallbackInfo ci) {
		LogicalEffectScopeResolver.endSplash();
	}
	//?}
}

