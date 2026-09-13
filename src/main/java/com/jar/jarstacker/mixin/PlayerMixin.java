package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.health.CombatContext;
import com.jar.jarstacker.stack.mob.health.LogicalHealthManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

	@Inject(method = "attack", at = @At("HEAD"))
	private void jarstacker$onAttackStart(Entity target, CallbackInfo ci) {
		Player player = (Player) (Object) this;
		CombatContext.beginAttack(player, target);
	}

	@Inject(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
		)
	)
	private void jarstacker$onSweepStart(Entity target, CallbackInfo ci) {
		Player player = (Player) (Object) this;
		float ratio = (float) player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO);
		float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
		float sweepDamage = 1.0f + ratio * baseDamage;

		CombatContext.setSweepAuthorized(sweepDamage);
		CombatContext.setSecondarySweep(true);

		if (target instanceof LivingEntity livingTarget) {
			LogicalHealthManager.applySweepToPrimary(livingTarget, sweepDamage);
		}
	}

	@Inject(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;sweepAttack()V"
		)
	)
	private void jarstacker$onSweepEnd(Entity target, CallbackInfo ci) {
		CombatContext.setSecondarySweep(false);
	}

	@Inject(method = "attack", at = @At("RETURN"))
	private void jarstacker$onAttackEnd(Entity target, CallbackInfo ci) {
		CombatContext.endAttack();
	}
}

