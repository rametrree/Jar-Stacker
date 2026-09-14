package com.jar.jarstacker.mixin;

//? if >=1.21.11 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.CatVariant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?} else if >=1.21.5 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?} else {
import net.minecraft.world.entity.animal.Cat;
//?}
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Cat.class)
public interface CatAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Holder<CatVariant> variant);
	*///?}
}

