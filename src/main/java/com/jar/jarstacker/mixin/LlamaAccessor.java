package com.jar.jarstacker.mixin;

//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.equine.Llama;
*///?} else {
import net.minecraft.world.entity.animal.horse.Llama;
//?}
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(Llama.class)
public interface LlamaAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Llama.Variant variant);
	*///?}
}

