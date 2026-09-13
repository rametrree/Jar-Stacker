package com.jar.jarstacker.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PigVariant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?} else {
import net.minecraft.world.entity.animal.Pig;
//?}

@Mixin(Pig.class)
public interface PigAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Holder<PigVariant> variant);
	*///?}
}

