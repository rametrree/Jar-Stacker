package com.jar.jarstacker.mixin;

//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Variant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?} else if >=1.21.5 {
/*import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?} else {
import net.minecraft.world.entity.animal.horse.Horse;
//?}
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Horse.class)
public interface HorseAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Variant variant);
	*///?}
}

