package com.jar.jarstacker.mixin;

//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.fish.Salmon;
*///?} else {
import net.minecraft.world.entity.animal.Salmon;
//?}
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(Salmon.class)
public interface SalmonAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Salmon.Variant variant);
	*///?}
}

