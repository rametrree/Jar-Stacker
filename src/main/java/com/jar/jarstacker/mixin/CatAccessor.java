package com.jar.jarstacker.mixin;

import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.CatVariant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(Cat.class)
public interface CatAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Holder<CatVariant> variant);
	*///?}
}

