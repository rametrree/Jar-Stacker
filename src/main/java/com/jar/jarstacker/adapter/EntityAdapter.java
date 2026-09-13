package com.jar.jarstacker.adapter;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class EntityAdapter {
	private EntityAdapter() {}

	public static <T extends Entity> T create(EntityType<T> type, Level level) {
		//? if >=1.21.2 {
		/*return type.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
		*///?} else {
		return type.create(level);
		//?}
	}

	public static void setMooshroomVariant(net.minecraft.world.entity.animal.MushroomCow cow, boolean brown) {
		if (cow == null) return;
		//? if >=1.21.2 {
		/*cow.setVariant(brown ? net.minecraft.world.entity.animal.MushroomCow.Variant.BROWN : net.minecraft.world.entity.animal.MushroomCow.Variant.RED);
		*///?} else {
		cow.setVariant(brown ? net.minecraft.world.entity.animal.MushroomCow.MushroomType.BROWN : net.minecraft.world.entity.animal.MushroomCow.MushroomType.RED);
		//?}
	}

	public static net.minecraft.world.entity.projectile.ThrownPotion createThrownPotion(Level level, double x, double y, double z) {
		net.minecraft.world.entity.projectile.ThrownPotion potion = new net.minecraft.world.entity.projectile.ThrownPotion(EntityType.POTION, level);
		potion.setPos(x, y, z);
		return potion;
	}
}
