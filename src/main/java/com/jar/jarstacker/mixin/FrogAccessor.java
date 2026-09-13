package com.jar.jarstacker.mixin;

import net.minecraft.world.entity.animal.frog.Frog;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(Frog.class)
public interface FrogAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Holder<FrogVariant> variant);
	*///?}
}

