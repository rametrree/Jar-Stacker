package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.equipment.RuntimeCombatStateTransitionHandler;
import net.minecraft.world.entity.EquipmentSlot;
//? if >=1.21.5 {
/*import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
*///?} else {
import net.minecraft.world.entity.Mob;
//?}
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.5 {
/*@Mixin(LivingEntity.class)
public class MobMixin {

	@Inject(method = "setItemSlot", at = @At("TAIL"))
	private void jarstacker$onSetItemSlot(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
		if ((Object) this instanceof Mob mob) {
			RuntimeCombatStateTransitionHandler.onEquipmentChanged(mob, slot, stack);
		}
	}
}
*///?} else {
@Mixin(Mob.class)
public class MobMixin {

	@Inject(method = "setItemSlot", at = @At("TAIL"))
	private void jarstacker$onSetItemSlot(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
		Mob mob = (Mob) (Object) this;
		RuntimeCombatStateTransitionHandler.onEquipmentChanged(mob, slot, stack);
	}
}

//?}
