package com.jar.jarstacker.adapter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
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
		//? if >=1.21.5 {
		/*((com.jar.jarstacker.mixin.MushroomCowAccessor) cow).jarstacker$setVariant(brown ? net.minecraft.world.entity.animal.MushroomCow.Variant.BROWN : net.minecraft.world.entity.animal.MushroomCow.Variant.RED);
		*///?} else {
		//? if >=1.21.2 {
		/*cow.setVariant(brown ? net.minecraft.world.entity.animal.MushroomCow.Variant.BROWN : net.minecraft.world.entity.animal.MushroomCow.Variant.RED);
		*///?} else {
		cow.setVariant(brown ? net.minecraft.world.entity.animal.MushroomCow.MushroomType.BROWN : net.minecraft.world.entity.animal.MushroomCow.MushroomType.RED);
		//?}
		//?}
	}

	public static void copyMooshroomVariant(net.minecraft.world.entity.animal.MushroomCow src, net.minecraft.world.entity.animal.MushroomCow dst) {
		if (src == null || dst == null) return;
		//? if >=1.21.5 {
		/*((com.jar.jarstacker.mixin.MushroomCowAccessor) dst).jarstacker$setVariant(src.getVariant());
		*///?} else {
		//? if >=1.21.2 {
		/*dst.setVariant(src.getVariant());
		*///?} else {
		dst.setVariant(src.getVariant());
		//?}
		//?}
	}

	public static ThrowableItemProjectile createThrownPotion(Level level, double x, double y, double z) {
		//? if >=1.21.5 {
		/*net.minecraft.world.entity.projectile.ThrownSplashPotion potion = new net.minecraft.world.entity.projectile.ThrownSplashPotion(EntityType.SPLASH_POTION, level);
		potion.setPos(x, y, z);
		return potion;
		*///?} else {
		net.minecraft.world.entity.projectile.ThrownPotion potion = new net.minecraft.world.entity.projectile.ThrownPotion(EntityType.POTION, level);
		potion.setPos(x, y, z);
		return potion;
		//?}
	}

	public static boolean isThrownPotion(Entity source) {
		//? if >=1.21.5 {
		/*return source instanceof net.minecraft.world.entity.projectile.ThrownSplashPotion || source instanceof net.minecraft.world.entity.projectile.ThrownLingeringPotion;
		*///?} else {
		return source instanceof net.minecraft.world.entity.projectile.ThrownPotion;
		//?}
	}


	public static void moveTo(Entity entity, double x, double y, double z, float yRot, float xRot) {
		//? if >=1.21.5 {
		/*entity.snapTo(x, y, z, yRot, xRot);
		*///?} else {
		entity.moveTo(x, y, z, yRot, xRot);
		//?}
	}

	public static java.util.UUID getOwnerUUID(TamableAnimal tamable) {
		//? if >=1.21.5 {
		/*return tamable.getOwnerReference() != null ? tamable.getOwnerReference().getUUID() : null;
		*///?} else {
		return tamable.getOwnerUUID();
		//?}
	}

	public static boolean isSaddleable(Mob mob) {
		//? if >=1.21.5 {
		/*return mob.canUseSlot(net.minecraft.world.entity.EquipmentSlot.SADDLE);
		*///?} else {
		if (mob instanceof net.minecraft.world.entity.Saddleable saddleable) {
			return saddleable.isSaddleable();
		}
		return false;
		//?}
	}

	public static boolean isSaddled(Mob mob) {
		//? if >=1.21.5 {
		/*return mob.isSaddled();
		*///?} else {
		if (mob instanceof net.minecraft.world.entity.Saddleable saddleable) {
			return saddleable.isSaddled();
		}
		return false;
		//?}
	}

	public static boolean variantsMatch(Entity a, Entity b) {
		//? if >=1.21.5 {
		/*if (a instanceof net.minecraft.world.entity.animal.MushroomCow mcA && b instanceof net.minecraft.world.entity.animal.MushroomCow mcB) {
			return java.util.Objects.equals(mcA.getVariant(), mcB.getVariant());
		}
		try {
			java.lang.reflect.Method getA = a.getClass().getMethod("getVariant");
			java.lang.reflect.Method getB = b.getClass().getMethod("getVariant");
			return java.util.Objects.equals(getA.invoke(a), getB.invoke(b));
		} catch (Throwable ignored) {
			return true;
		}
		*///?} else {
		if (a instanceof net.minecraft.world.entity.VariantHolder<?> vA && b instanceof net.minecraft.world.entity.VariantHolder<?> vB) {
			return vA.getVariant().equals(vB.getVariant());
		}
		return true;
		//?}
	}

	public static void copyVariant(Entity src, Entity dst) {
		//? if >=1.21.5 {
		/*if (src instanceof net.minecraft.world.entity.animal.MushroomCow mcSrc && dst instanceof net.minecraft.world.entity.animal.MushroomCow mcDst) {
			copyMooshroomVariant(mcSrc, mcDst);
			return;
		}
		try {
			java.lang.reflect.Method getM = src.getClass().getMethod("getVariant");
			Object variant = getM.invoke(src);
			java.lang.reflect.Method setM = dst.getClass().getMethod("setVariant", getM.getReturnType());
			setM.invoke(dst, variant);
		} catch (Throwable ignored) {}
		*///?} else {
		if (src instanceof net.minecraft.world.entity.VariantHolder<?> vSrc && dst instanceof net.minecraft.world.entity.VariantHolder<?> vDst) {
			copyVariantInternal(vSrc, vDst);
		}
		//?}
	}

	public static void equipSaddle(Mob mob, net.minecraft.world.item.ItemStack saddle) {
		//? if >=1.21.5 {
		/*mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.SADDLE, saddle);
		*///?} else {
		if (mob instanceof net.minecraft.world.entity.Saddleable s) {
			s.equipSaddle(saddle, net.minecraft.sounds.SoundSource.NEUTRAL);
		}
		//?}
	}

	//? if <1.21.5 {
	@SuppressWarnings("unchecked")
	private static <T> void copyVariantInternal(net.minecraft.world.entity.VariantHolder<T> src, net.minecraft.world.entity.VariantHolder<?> dst) {
		((net.minecraft.world.entity.VariantHolder<T>) dst).setVariant(src.getVariant());
	}
	//?}

	public static boolean isSheep(Entity entity) {
		//? if >=1.21.5 {
		/*return entity instanceof net.minecraft.world.entity.animal.sheep.Sheep;
		*///?} else {
		return entity instanceof net.minecraft.world.entity.animal.Sheep;
		//?}
	}

	public static String getSheepColorName(Entity entity) {
		//? if >=1.21.5 {
		/*if (entity instanceof net.minecraft.world.entity.animal.sheep.Sheep sheep) {
			return sheep.getColor().getName().toUpperCase();
		}
		return "";
		*///?} else {
		if (entity instanceof net.minecraft.world.entity.animal.Sheep sheep) {
			return sheep.getColor().getName().toUpperCase();
		}
		return "";
		//?}
	}

	public static boolean isSheepSheared(Entity entity) {
		//? if >=1.21.5 {
		/*if (entity instanceof net.minecraft.world.entity.animal.sheep.Sheep sheep) {
			return sheep.isSheared();
		}
		return false;
		*///?} else {
		if (entity instanceof net.minecraft.world.entity.animal.Sheep sheep) {
			return sheep.isSheared();
		}
		return false;
		//?}
	}
}
