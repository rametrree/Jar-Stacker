package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.sheep.SheepWoolManager;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public abstract class SheepMixin {

	@Unique
	private boolean jarstacker$wasShearedBeforeAte = false;

	@Inject(method = "ate", at = @At("HEAD"))
	private void jarstacker$onAteHead(CallbackInfo ci) {
		Sheep self = (Sheep) (Object) this;
		this.jarstacker$wasShearedBeforeAte = self.isSheared();
	}

	@Inject(method = "ate", at = @At("TAIL"))
	private void jarstacker$onAteTail(CallbackInfo ci) {
		Sheep self = (Sheep) (Object) this;
		if (this.jarstacker$wasShearedBeforeAte && !self.level().isClientSide()) {
			SheepWoolManager.handleSheepRegrowth(self);
		}
	}
}

