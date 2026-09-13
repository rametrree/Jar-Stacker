package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.death.CombatDeathContext;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

	@Inject(
		method = "getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void jarstacker$onGetEnchantmentLevel(Holder<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
		CombatDeathContext ctx = CombatDeathContext.getCurrentContext();
		if (ctx != null && ctx.hasLootingOverride()) {
			if (enchantment.is(Enchantments.LOOTING)) {
				cir.setReturnValue(ctx.getLootingLevel());
			}
		}
	}

	@Inject(
		method = "getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I",
		at = @At("RETURN")
	)
	private static void jarstacker$onGetEnchantmentLevelReturn(Holder<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
		if (enchantment.is(Enchantments.LOOTING)) {
			CombatDeathContext ctx = CombatDeathContext.getCurrentContext();
			if (ctx != null) {
				ctx.recordEvaluatedLooting(cir.getReturnValue());
			}
		}
	}
}

