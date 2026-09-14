package com.jar.jarstacker.mixin;

//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.cow.MushroomCow;
*///?} else {
import net.minecraft.world.entity.animal.MushroomCow;
//?}
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MushroomCow.class)
public interface MushroomCowAccessor {
	@Accessor("stewEffects")
	SuspiciousStewEffects jarstacker$getStewEffects();

	@Accessor("stewEffects")
	void jarstacker$setStewEffects(SuspiciousStewEffects effects);

	//? if >=1.21.5 {
	/*@Invoker("setVariant")
	void jarstacker$setVariant(MushroomCow.Variant variant);
	*///?}
}

