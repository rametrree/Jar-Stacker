package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.transformation.LogicalEntityTransformer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.cow.MushroomCow;
*///?} else {
import net.minecraft.world.entity.animal.MushroomCow;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Mixin into MushroomCow to capture direct source -> destination association
 * during authentic Vanilla shearing transformations.
 */
@Mixin(MushroomCow.class)
public class MushroomCowMixin {

	//? if >=1.21.11 {
	/*@ModifyArg(
		method = "shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/animal/cow/MushroomCow;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"
		),
		index = 2
	)
	@SuppressWarnings("unchecked")
	private net.minecraft.world.entity.ConversionParams.AfterConversion<?> jarstacker$onShearConvertTo(net.minecraft.world.entity.ConversionParams.AfterConversion<?> original) {
		return cow -> {
			if (original != null) {
				((net.minecraft.world.entity.ConversionParams.AfterConversion<net.minecraft.world.entity.animal.cow.Cow>) original).finalizeConversion((net.minecraft.world.entity.animal.cow.Cow) cow);
			}
			LogicalEntityTransformer.recordDirectTransformation((MushroomCow) (Object) this, (Mob) cow);
		};
	}
	*///?} else if >=1.21.2 {
	/*@ModifyArg(
		method = "shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/animal/MushroomCow;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"
		),
		index = 2
	)
	@SuppressWarnings("unchecked")
	private net.minecraft.world.entity.ConversionParams.AfterConversion<?> jarstacker$onShearConvertTo(net.minecraft.world.entity.ConversionParams.AfterConversion<?> original) {
		return cow -> {
			if (original != null) {
				((net.minecraft.world.entity.ConversionParams.AfterConversion<net.minecraft.world.entity.animal.Cow>) original).finalizeConversion((net.minecraft.world.entity.animal.Cow) cow);
			}
			LogicalEntityTransformer.recordDirectTransformation((MushroomCow) (Object) this, (Mob) cow);
		};
	}
	*///?} else {
	@ModifyArg(
		method = "shear",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
		),
		index = 0
	)
	private Entity jarstacker$onShearSpawnCow(Entity entity) {
		if (entity instanceof Mob mob) {
			LogicalEntityTransformer.recordDirectTransformation((MushroomCow) (Object) this, mob);
		}
		return entity;
	}
	//?}
}

