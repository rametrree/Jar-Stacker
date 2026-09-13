package com.jar.jarstacker.mixin;

import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import net.minecraft.world.entity.animal.horse.Variant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(Horse.class)
public interface HorseAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Variant variant);
	*///?}
}

