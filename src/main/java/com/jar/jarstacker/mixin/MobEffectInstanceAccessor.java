package com.jar.jarstacker.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {

	//? if >=1.21.5 {
	/*@Invoker("tickDownDuration")
	void jarstacker$invokeTickDownDuration();
	*///?} else {
	@Invoker("tickDownDuration")
	int jarstacker$invokeTickDownDuration();
	//?}

	@Accessor("duration")
	void jarstacker$setDuration(int duration);
}

