package com.jar.jarstacker.mixin;

//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.parrot.Parrot;
*///?} else {
import net.minecraft.world.entity.animal.Parrot;
//?}
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.5 {
/*import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(Parrot.class)
public interface ParrotAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Parrot.Variant variant);
	*///?}
}

