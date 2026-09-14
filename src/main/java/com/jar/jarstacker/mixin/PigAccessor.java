package com.jar.jarstacker.mixin;

//? if >=1.21.11 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.pig.PigVariant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?} else if >=1.21.5 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PigVariant;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?} else {
import net.minecraft.world.entity.animal.Pig;
//?}
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Pig.class)
public interface PigAccessor {
	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(Holder<PigVariant> variant);
	*///?}
}

