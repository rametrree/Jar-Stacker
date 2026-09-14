package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.health.ProjectileImpactContext;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {

	@Inject(method = "onHit", at = @At("HEAD"))
	private void jarstacker$onHitHead(HitResult hitResult, CallbackInfo ci) {
		if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
			ProjectileImpactContext.beginImpact();
		}
	}

	@Inject(method = "onHit", at = @At("RETURN"))
	private void jarstacker$onHitReturn(HitResult hitResult, CallbackInfo ci) {
		if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
			ProjectileImpactContext.endImpact();
		}
	}
}

