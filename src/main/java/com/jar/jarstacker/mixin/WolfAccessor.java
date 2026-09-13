package com.jar.jarstacker.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?} else {
import net.minecraft.world.entity.animal.Wolf;
//?}

@Mixin(Wolf.class)
public interface WolfAccessor {
	//? if >=1.21.5 {
	/*@Invoker("getVariant")
	Holder<WolfVariant> jarstacker$getVariant();

	@Invoker("setVariant")
	void jarstacker$setVariant(Holder<WolfVariant> variant);
	*///?}
}

