package com.jar.jarstacker.mixin;

import com.jar.jarstacker.stack.mob.transformation.LogicalEntityTransformer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.MushroomCow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Mixin into MushroomCow to capture direct source -> destination association
 * during authentic Vanilla shearing transformations.
 */
@Mixin(MushroomCow.class)
public class MushroomCowMixin {

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
}

