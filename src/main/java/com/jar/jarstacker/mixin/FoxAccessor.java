package com.jar.jarstacker.mixin;

//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.fox.Fox;
*///?} else {
import net.minecraft.world.entity.animal.Fox;
//?}
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(Fox.class)
public interface FoxAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Fox.Variant variant);
	*///?}
}

