package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.equipment.RuntimeCombatStateTransitionHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class MobMixin {

	@Inject(method = "setItemSlot", at = @At("TAIL"))
	private void jarstacker$onSetItemSlot(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
		Mob mob = (Mob) (Object) this;
		RuntimeCombatStateTransitionHandler.onEquipmentChanged(mob, slot, stack);
	}
}

