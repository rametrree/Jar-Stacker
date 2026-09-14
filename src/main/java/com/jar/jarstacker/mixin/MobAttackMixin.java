package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.health.CombatContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobAttackMixin {

	//? if >=1.21.2 {
	/*@Inject(method = "doHurtTarget(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"))
	private void jarstacker$onDoHurtTargetHead(net.minecraft.server.level.ServerLevel serverLevel, Entity target, CallbackInfoReturnable<Boolean> cir) {
		CombatContext.beginDirectAttack();
	}

	@Inject(method = "doHurtTarget(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
	private void jarstacker$onDoHurtTargetReturn(net.minecraft.server.level.ServerLevel serverLevel, Entity target, CallbackInfoReturnable<Boolean> cir) {
		CombatContext.endDirectAttack();
	}
	*///?} else {
	@Inject(method = "doHurtTarget(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"))
	private void jarstacker$onDoHurtTargetHead(Entity target, CallbackInfoReturnable<Boolean> cir) {
		CombatContext.beginDirectAttack();
	}

	@Inject(method = "doHurtTarget(Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
	private void jarstacker$onDoHurtTargetReturn(Entity target, CallbackInfoReturnable<Boolean> cir) {
		CombatContext.endDirectAttack();
	}
	//?}
}

