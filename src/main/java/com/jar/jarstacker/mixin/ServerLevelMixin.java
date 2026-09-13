package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.transformation.LogicalEntityTransformer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(method = "addFreshEntity", at = @At("HEAD"))
	private void jarstacker$onAddFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		LogicalEntityTransformer.notifyEntityAdded(entity);
	}
}

