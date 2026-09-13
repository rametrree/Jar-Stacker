package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.AnimalInteractionHandler;
import com.jar.jarstacker.stack.mob.BreedingDiagnostics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreedGoal.class)
public class BreedGoalMixin {
	@Shadow @Final protected Animal animal;
	@Shadow protected Animal partner;

	@Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
	private void onCanUse(CallbackInfoReturnable<Boolean> cir) {
		if (this.animal.isInLove()) {
			BreedingDiagnostics.logEvent("BREED_PARTNER_SEARCH", this.animal,
				"canUseResult=" + cir.getReturnValue() + " partner=" + (this.partner != null ? this.partner.getUUID() : "none"));
		}

		if (!cir.getReturnValue() && this.animal.isInLove() && this.animal.level() instanceof ServerLevel serverLevel) {
			int count = ((StackableEntity) this.animal).jarstacker$getStackCount();
			if (count >= 2) {
				Animal extractedPartner = AnimalInteractionHandler.extractBreedingPartner(serverLevel, this.animal, count);
				if (extractedPartner != null) {
					this.partner = extractedPartner;
					cir.setReturnValue(true);
				}
			}
		}
	}


	@Inject(method = "breed", at = @At("HEAD"))
	private void onBreed(CallbackInfo ci) {
		BreedingDiagnostics.logEvent("BREED_COMPLETE", this.animal,
			"partner=" + (this.partner != null ? this.partner.getUUID() : "none"));
		((StackableEntity) this.animal).jarstacker$setBreedingLockTicks(0);
		if (this.partner != null) {
			((StackableEntity) this.partner).jarstacker$setBreedingLockTicks(0);
		}
	}
}
