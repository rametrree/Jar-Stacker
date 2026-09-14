package com.jar.jarstacker.mixin;

//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.fish.TropicalFish;
*///?} else {
import net.minecraft.world.entity.animal.TropicalFish;
//?}
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(TropicalFish.class)
public interface TropicalFishAccessor {
	//? if >=1.21.5 {
	/*@Invoker("getPackedVariant")
	int jarstacker$getPackedVariant();

	@Invoker("setPackedVariant")
	void jarstacker$setPackedVariant(int packedVariant);
	*///?}
}

