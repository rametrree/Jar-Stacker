package com.jar.jarstacker.adapter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
//? if >=1.21.11 {
/*import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
*///?} else {
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
//?}
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

	public static boolean isEntityType(EntityType<?> type, net.minecraft.tags.TagKey<EntityType<?>> tag) {
		//? if >=26.1 {
		/*return type.builtInRegistryHolder().is(tag);
		*///?} else {
		return type.is(tag);
		//?}
	}

	public static net.minecraft.world.InteractionResult interactOn(net.minecraft.world.entity.player.Player player, Entity target, net.minecraft.world.InteractionHand hand) {
		//? if >=26.1 {
		/*return player.interactOn(target, hand, target.position());
		*///?} else {
		return player.interactOn(target, hand);
		//?}
	}

	//? if >=1.21.11 {
	/*public static void setMooshroomVariant(net.minecraft.world.entity.animal.cow.MushroomCow cow, boolean brown) {
		if (cow == null) return;
		((com.jar.jarstacker.mixin.MushroomCowAccessor) cow).jarstacker$setVariant(brown ? net.minecraft.world.entity.animal.cow.MushroomCow.Variant.BROWN : net.minecraft.world.entity.animal.cow.MushroomCow.Variant.RED);
	}

	public static void copyMooshroomVariant(net.minecraft.world.entity.animal.cow.MushroomCow src, net.minecraft.world.entity.animal.cow.MushroomCow dst) {
		if (src == null || dst == null) return;
		((com.jar.jarstacker.mixin.MushroomCowAccessor) dst).jarstacker$setVariant(src.getVariant());
	}
	*///? } else if >=1.21.5 {
	/*public static void setMooshroomVariant(net.minecraft.world.entity.animal.MushroomCow cow, boolean brown) {
		if (cow == null) return;
		((com.jar.jarstacker.mixin.MushroomCowAccessor) cow).jarstacker$setVariant(brown ? net.minecraft.world.entity.animal.MushroomCow.Variant.BROWN : net.minecraft.world.entity.animal.MushroomCow.Variant.RED);
	}

	public static void copyMooshroomVariant(net.minecraft.world.entity.animal.MushroomCow src, net.minecraft.world.entity.animal.MushroomCow dst) {
		if (src == null || dst == null) return;
		((com.jar.jarstacker.mixin.MushroomCowAccessor) dst).jarstacker$setVariant(src.getVariant());
	}
	*///? } else if >=1.21.2 {
	/*public static void setMooshroomVariant(net.minecraft.world.entity.animal.MushroomCow cow, boolean brown) {
		if (cow == null) return;
		cow.setVariant(brown ? net.minecraft.world.entity.animal.MushroomCow.Variant.BROWN : net.minecraft.world.entity.animal.MushroomCow.Variant.RED);
	}

	public static void copyMooshroomVariant(net.minecraft.world.entity.animal.MushroomCow src, net.minecraft.world.entity.animal.MushroomCow dst) {
		if (src == null || dst == null) return;
		dst.setVariant(src.getVariant());
	}
	*///? } else {
	public static void setMooshroomVariant(net.minecraft.world.entity.animal.MushroomCow cow, boolean brown) {
		if (cow == null) return;
		cow.setVariant(brown ? net.minecraft.world.entity.animal.MushroomCow.MushroomType.BROWN : net.minecraft.world.entity.animal.MushroomCow.MushroomType.RED);
	}

	public static void copyMooshroomVariant(net.minecraft.world.entity.animal.MushroomCow src, net.minecraft.world.entity.animal.MushroomCow dst) {
		if (src == null || dst == null) return;
		dst.setVariant(src.getVariant());
	}
	//? }

	public static ThrowableItemProjectile createThrownPotion(Level level, double x, double y, double z) {
		//? if >=26.2 {
		/*net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion potion = new net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion(net.minecraft.world.entity.EntityTypes.SPLASH_POTION, level);
		potion.setPos(x, y, z);
		return potion;
		*///? } else if >=1.21.11 {
		/*net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion potion = new net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion(EntityType.SPLASH_POTION, level);
		potion.setPos(x, y, z);
		return potion;
		*///? } else if >=1.21.5 {
		/*net.minecraft.world.entity.projectile.ThrownSplashPotion potion = new net.minecraft.world.entity.projectile.ThrownSplashPotion(EntityType.SPLASH_POTION, level);
		potion.setPos(x, y, z);
		return potion;
		*///? } else {
		net.minecraft.world.entity.projectile.ThrownPotion potion = new net.minecraft.world.entity.projectile.ThrownPotion(EntityType.POTION, level);
		potion.setPos(x, y, z);
		return potion;
		//? }
	}

	public static void knockback(net.minecraft.world.entity.LivingEntity entity, double strength, double x, double z) {
		if (entity == null) return;
		//? if >=26.2 {
		/*entity.knockback(strength, x, z, null, 0.0f);
		*///?} else {
		entity.knockback(strength, x, z);
		//?}
	}

	public static boolean isThrownPotion(Entity source) {
		//? if >=1.21.11 {
		/*return source instanceof net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion || source instanceof net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
		*///? } else if >=1.21.5 {
		/*return source instanceof net.minecraft.world.entity.projectile.ThrownSplashPotion || source instanceof net.minecraft.world.entity.projectile.ThrownLingeringPotion;
		*///? } else {
		return source instanceof net.minecraft.world.entity.projectile.ThrownPotion;
		//? }
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

	public enum VariantCompatibilityResult {
		MATCH,
		MISMATCH,
		NOT_APPLICABLE,
		UNKNOWN;

		public boolean isCompatible() {
			return this == MATCH || this == NOT_APPLICABLE;
		}
	}

	public static VariantCompatibilityResult evaluateVariantCompatibility(Entity a, Entity b) {
		if (a == null || b == null) {
			return VariantCompatibilityResult.UNKNOWN;
		}
		if (a.getType() != b.getType()) {
			return VariantCompatibilityResult.MISMATCH;
		}
		var key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(a.getType());
		boolean isVanillaNamespace = (key != null && "minecraft".equals(key.getNamespace()));
		boolean isVanillaPackage = a.getClass().getName().startsWith("net.minecraft.");
		if (!isVanillaNamespace || !isVanillaPackage) {
			return VariantCompatibilityResult.UNKNOWN;
		}
		//? if >=1.21.11 {
		/*if (a.getClass() != b.getClass()) {
			return VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.cow.MushroomCow mcA && b instanceof net.minecraft.world.entity.animal.cow.MushroomCow mcB) {
			return java.util.Objects.equals(mcA.getVariant(), mcB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.cow.Cow cowA && b instanceof net.minecraft.world.entity.animal.cow.Cow cowB) {
			return java.util.Objects.equals(cowA.getVariant(), cowB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.pig.Pig pigA && b instanceof net.minecraft.world.entity.animal.pig.Pig pigB) {
			return java.util.Objects.equals(pigA.getVariant(), pigB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.chicken.Chicken chickA && b instanceof net.minecraft.world.entity.animal.chicken.Chicken chickB) {
			return java.util.Objects.equals(chickA.getVariant(), chickB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.wolf.Wolf wolfA && b instanceof net.minecraft.world.entity.animal.wolf.Wolf wolfB) {
			return java.util.Objects.equals(((com.jar.jarstacker.mixin.WolfAccessor) wolfA).jarstacker$getVariant(), ((com.jar.jarstacker.mixin.WolfAccessor) wolfB).jarstacker$getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.feline.Cat catA && b instanceof net.minecraft.world.entity.animal.feline.Cat catB) {
			return java.util.Objects.equals(catA.getVariant(), catB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.frog.Frog frogA && b instanceof net.minecraft.world.entity.animal.frog.Frog frogB) {
			return java.util.Objects.equals(frogA.getVariant(), frogB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.equine.Horse horseA && b instanceof net.minecraft.world.entity.animal.equine.Horse horseB) {
			return horseA.getVariant() == horseB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.equine.Llama llamaA && b instanceof net.minecraft.world.entity.animal.equine.Llama llamaB) {
			return llamaA.getVariant() == llamaB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.rabbit.Rabbit rabbitA && b instanceof net.minecraft.world.entity.animal.rabbit.Rabbit rabbitB) {
			return rabbitA.getVariant() == rabbitB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.fox.Fox foxA && b instanceof net.minecraft.world.entity.animal.fox.Fox foxB) {
			return foxA.getVariant() == foxB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.axolotl.Axolotl axolA && b instanceof net.minecraft.world.entity.animal.axolotl.Axolotl axolB) {
			return axolA.getVariant() == axolB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.parrot.Parrot parrotA && b instanceof net.minecraft.world.entity.animal.parrot.Parrot parrotB) {
			return parrotA.getVariant() == parrotB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.fish.Salmon salmonA && b instanceof net.minecraft.world.entity.animal.fish.Salmon salmonB) {
			return salmonA.getVariant() == salmonB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.fish.TropicalFish tfA && b instanceof net.minecraft.world.entity.animal.fish.TropicalFish tfB) {
			return (tfA.getBaseColor() == tfB.getBaseColor() && tfA.getPatternColor() == tfB.getPatternColor() && tfA.getPattern() == tfB.getPattern()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.npc.villager.VillagerDataHolder vdhA && b instanceof net.minecraft.world.entity.npc.villager.VillagerDataHolder vdhB) {
			return java.util.Objects.equals(vdhA.getVillagerData(), vdhB.getVillagerData()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		return VariantCompatibilityResult.NOT_APPLICABLE;
		*///? } else if >=1.21.5 {
		/*if (a.getClass() != b.getClass()) {
			return VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.MushroomCow mcA && b instanceof net.minecraft.world.entity.animal.MushroomCow mcB) {
			return java.util.Objects.equals(mcA.getVariant(), mcB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.Cow cowA && b instanceof net.minecraft.world.entity.animal.Cow cowB) {
			return java.util.Objects.equals(cowA.getVariant(), cowB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.Pig pigA && b instanceof net.minecraft.world.entity.animal.Pig pigB) {
			return java.util.Objects.equals(pigA.getVariant(), pigB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.Chicken chickA && b instanceof net.minecraft.world.entity.animal.Chicken chickB) {
			return java.util.Objects.equals(chickA.getVariant(), chickB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.wolf.Wolf wolfA && b instanceof net.minecraft.world.entity.animal.wolf.Wolf wolfB) {
			return java.util.Objects.equals(((com.jar.jarstacker.mixin.WolfAccessor) wolfA).jarstacker$getVariant(), ((com.jar.jarstacker.mixin.WolfAccessor) wolfB).jarstacker$getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.Cat catA && b instanceof net.minecraft.world.entity.animal.Cat catB) {
			return java.util.Objects.equals(catA.getVariant(), catB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.frog.Frog frogA && b instanceof net.minecraft.world.entity.animal.frog.Frog frogB) {
			return java.util.Objects.equals(frogA.getVariant(), frogB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.horse.Horse horseA && b instanceof net.minecraft.world.entity.animal.horse.Horse horseB) {
			return horseA.getVariant() == horseB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.horse.Llama llamaA && b instanceof net.minecraft.world.entity.animal.horse.Llama llamaB) {
			return llamaA.getVariant() == llamaB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.Rabbit rabbitA && b instanceof net.minecraft.world.entity.animal.Rabbit rabbitB) {
			return rabbitA.getVariant() == rabbitB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.Fox foxA && b instanceof net.minecraft.world.entity.animal.Fox foxB) {
			return foxA.getVariant() == foxB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.axolotl.Axolotl axolA && b instanceof net.minecraft.world.entity.animal.axolotl.Axolotl axolB) {
			return axolA.getVariant() == axolB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.Parrot parrotA && b instanceof net.minecraft.world.entity.animal.Parrot parrotB) {
			return parrotA.getVariant() == parrotB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.Salmon salmonA && b instanceof net.minecraft.world.entity.animal.Salmon salmonB) {
			return salmonA.getVariant() == salmonB.getVariant() ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.TropicalFish tfA && b instanceof net.minecraft.world.entity.animal.TropicalFish tfB) {
			return (tfA.getBaseColor() == tfB.getBaseColor() && tfA.getPatternColor() == tfB.getPatternColor() && tfA.getPattern() == tfB.getPattern()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.npc.VillagerDataHolder vdhA && b instanceof net.minecraft.world.entity.npc.VillagerDataHolder vdhB) {
			return java.util.Objects.equals(vdhA.getVillagerData(), vdhB.getVillagerData()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		return VariantCompatibilityResult.NOT_APPLICABLE;
		*///? } else {
		if (a.getClass() != b.getClass()) {
			return VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.animal.MushroomCow mcA && b instanceof net.minecraft.world.entity.animal.MushroomCow mcB) {
			return java.util.Objects.equals(mcA.getVariant(), mcB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.VariantHolder<?> vA) {
			if (b instanceof net.minecraft.world.entity.VariantHolder<?> vB) {
				return java.util.Objects.equals(vA.getVariant(), vB.getVariant()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
			}
			return VariantCompatibilityResult.MISMATCH;
		}
		if (b instanceof net.minecraft.world.entity.VariantHolder<?>) {
			return VariantCompatibilityResult.MISMATCH;
		}
		if (a instanceof net.minecraft.world.entity.npc.VillagerDataHolder vdhA) {
			if (b instanceof net.minecraft.world.entity.npc.VillagerDataHolder vdhB) {
				return java.util.Objects.equals(vdhA.getVillagerData(), vdhB.getVillagerData()) ? VariantCompatibilityResult.MATCH : VariantCompatibilityResult.MISMATCH;
			}
			return VariantCompatibilityResult.MISMATCH;
		}
		if (b instanceof net.minecraft.world.entity.npc.VillagerDataHolder) {
			return VariantCompatibilityResult.MISMATCH;
		}
		return VariantCompatibilityResult.NOT_APPLICABLE;
		//? }
	}

	public static boolean variantsMatch(Entity a, Entity b) {
		return evaluateVariantCompatibility(a, b).isCompatible();
	}

	public static boolean copyVariant(Entity src, Entity dst) {
		if (src == null || dst == null) {
			return false;
		}
		if (src.getType() != dst.getType()) {
			return false;
		}
		var key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(src.getType());
		boolean isVanillaNamespace = (key != null && "minecraft".equals(key.getNamespace()));
		boolean isVanillaPackage = src.getClass().getName().startsWith("net.minecraft.");
		if (!isVanillaNamespace || !isVanillaPackage) {
			return false;
		}
		//? if >=1.21.11 {
		/*if (src instanceof net.minecraft.world.entity.animal.cow.MushroomCow mcSrc && dst instanceof net.minecraft.world.entity.animal.cow.MushroomCow mcDst) {
			copyMooshroomVariant(mcSrc, mcDst);
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.cow.Cow cowSrc && dst instanceof net.minecraft.world.entity.animal.cow.Cow cowDst) {
			cowDst.setVariant(cowSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.pig.Pig pigSrc && dst instanceof net.minecraft.world.entity.animal.pig.Pig pigDst) {
			((com.jar.jarstacker.mixin.PigAccessor) pigDst).jarstacker$setVariant(pigSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.chicken.Chicken chickSrc && dst instanceof net.minecraft.world.entity.animal.chicken.Chicken chickDst) {
			chickDst.setVariant(chickSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.wolf.Wolf wolfSrc && dst instanceof net.minecraft.world.entity.animal.wolf.Wolf wolfDst) {
			((com.jar.jarstacker.mixin.WolfAccessor) wolfDst).jarstacker$setVariant(((com.jar.jarstacker.mixin.WolfAccessor) wolfSrc).jarstacker$getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.feline.Cat catSrc && dst instanceof net.minecraft.world.entity.animal.feline.Cat catDst) {
			((com.jar.jarstacker.mixin.CatAccessor) catDst).jarstacker$setVariant(catSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.frog.Frog frogSrc && dst instanceof net.minecraft.world.entity.animal.frog.Frog frogDst) {
			((com.jar.jarstacker.mixin.FrogAccessor) frogDst).jarstacker$setVariant(frogSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.equine.Horse horseSrc && dst instanceof net.minecraft.world.entity.animal.equine.Horse horseDst) {
			((com.jar.jarstacker.mixin.HorseAccessor) horseDst).jarstacker$setVariant(horseSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.equine.Llama llamaSrc && dst instanceof net.minecraft.world.entity.animal.equine.Llama llamaDst) {
			((com.jar.jarstacker.mixin.LlamaAccessor) llamaDst).jarstacker$setVariant(llamaSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.rabbit.Rabbit rabbitSrc && dst instanceof net.minecraft.world.entity.animal.rabbit.Rabbit rabbitDst) {
			((com.jar.jarstacker.mixin.RabbitAccessor) rabbitDst).jarstacker$setVariant(rabbitSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.fox.Fox foxSrc && dst instanceof net.minecraft.world.entity.animal.fox.Fox foxDst) {
			((com.jar.jarstacker.mixin.FoxAccessor) foxDst).jarstacker$setVariant(foxSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.axolotl.Axolotl axolSrc && dst instanceof net.minecraft.world.entity.animal.axolotl.Axolotl axolDst) {
			((com.jar.jarstacker.mixin.AxolotlAccessor) axolDst).jarstacker$setVariant(axolSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.parrot.Parrot parrotSrc && dst instanceof net.minecraft.world.entity.animal.parrot.Parrot parrotDst) {
			((com.jar.jarstacker.mixin.ParrotAccessor) parrotDst).jarstacker$setVariant(parrotSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.fish.Salmon salmonSrc && dst instanceof net.minecraft.world.entity.animal.fish.Salmon salmonDst) {
			((com.jar.jarstacker.mixin.SalmonAccessor) salmonDst).jarstacker$setVariant(salmonSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.fish.TropicalFish tfSrc && dst instanceof net.minecraft.world.entity.animal.fish.TropicalFish tfDst) {
			((com.jar.jarstacker.mixin.TropicalFishAccessor) tfDst).jarstacker$setPackedVariant(((com.jar.jarstacker.mixin.TropicalFishAccessor) tfSrc).jarstacker$getPackedVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.npc.villager.VillagerDataHolder vdhSrc && dst instanceof net.minecraft.world.entity.npc.villager.VillagerDataHolder vdhDst) {
			vdhDst.setVillagerData(vdhSrc.getVillagerData());
			return true;
		}
		return true;
		*///? } else if >=1.21.5 {
		/*if (src instanceof net.minecraft.world.entity.animal.MushroomCow mcSrc && dst instanceof net.minecraft.world.entity.animal.MushroomCow mcDst) {
			copyMooshroomVariant(mcSrc, mcDst);
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.Cow cowSrc && dst instanceof net.minecraft.world.entity.animal.Cow cowDst) {
			cowDst.setVariant(cowSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.Pig pigSrc && dst instanceof net.minecraft.world.entity.animal.Pig pigDst) {
			((com.jar.jarstacker.mixin.PigAccessor) pigDst).jarstacker$setVariant(pigSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.Chicken chickSrc && dst instanceof net.minecraft.world.entity.animal.Chicken chickDst) {
			chickDst.setVariant(chickSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.wolf.Wolf wolfSrc && dst instanceof net.minecraft.world.entity.animal.wolf.Wolf wolfDst) {
			((com.jar.jarstacker.mixin.WolfAccessor) wolfDst).jarstacker$setVariant(((com.jar.jarstacker.mixin.WolfAccessor) wolfSrc).jarstacker$getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.Cat catSrc && dst instanceof net.minecraft.world.entity.animal.Cat catDst) {
			((com.jar.jarstacker.mixin.CatAccessor) catDst).jarstacker$setVariant(catSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.frog.Frog frogSrc && dst instanceof net.minecraft.world.entity.animal.frog.Frog frogDst) {
			((com.jar.jarstacker.mixin.FrogAccessor) frogDst).jarstacker$setVariant(frogSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.horse.Horse horseSrc && dst instanceof net.minecraft.world.entity.animal.horse.Horse horseDst) {
			((com.jar.jarstacker.mixin.HorseAccessor) horseDst).jarstacker$setVariant(horseSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.horse.Llama llamaSrc && dst instanceof net.minecraft.world.entity.animal.horse.Llama llamaDst) {
			((com.jar.jarstacker.mixin.LlamaAccessor) llamaDst).jarstacker$setVariant(llamaSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.Rabbit rabbitSrc && dst instanceof net.minecraft.world.entity.animal.Rabbit rabbitDst) {
			((com.jar.jarstacker.mixin.RabbitAccessor) rabbitDst).jarstacker$setVariant(rabbitSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.Fox foxSrc && dst instanceof net.minecraft.world.entity.animal.Fox foxDst) {
			((com.jar.jarstacker.mixin.FoxAccessor) foxDst).jarstacker$setVariant(foxSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.axolotl.Axolotl axolSrc && dst instanceof net.minecraft.world.entity.animal.axolotl.Axolotl axolDst) {
			((com.jar.jarstacker.mixin.AxolotlAccessor) axolDst).jarstacker$setVariant(axolSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.Parrot parrotSrc && dst instanceof net.minecraft.world.entity.animal.Parrot parrotDst) {
			((com.jar.jarstacker.mixin.ParrotAccessor) parrotDst).jarstacker$setVariant(parrotSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.Salmon salmonSrc && dst instanceof net.minecraft.world.entity.animal.Salmon salmonDst) {
			((com.jar.jarstacker.mixin.SalmonAccessor) salmonDst).jarstacker$setVariant(salmonSrc.getVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.animal.TropicalFish tfSrc && dst instanceof net.minecraft.world.entity.animal.TropicalFish tfDst) {
			((com.jar.jarstacker.mixin.TropicalFishAccessor) tfDst).jarstacker$setPackedVariant(((com.jar.jarstacker.mixin.TropicalFishAccessor) tfSrc).jarstacker$getPackedVariant());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.npc.VillagerDataHolder vdhSrc && dst instanceof net.minecraft.world.entity.npc.VillagerDataHolder vdhDst) {
			vdhDst.setVillagerData(vdhSrc.getVillagerData());
			return true;
		}
		return true;
		*///? } else {
		if (src instanceof net.minecraft.world.entity.animal.MushroomCow mcSrc && dst instanceof net.minecraft.world.entity.animal.MushroomCow mcDst) {
			copyMooshroomVariant(mcSrc, mcDst);
			return true;
		}
		if (src instanceof net.minecraft.world.entity.VariantHolder<?> vSrc && dst instanceof net.minecraft.world.entity.VariantHolder<?> vDst) {
			copyVariantInternal(vSrc, vDst);
			return true;
		}
		if (src instanceof net.minecraft.world.entity.VariantHolder<?> || dst instanceof net.minecraft.world.entity.VariantHolder<?>) {
			return false;
		}
		if (src instanceof net.minecraft.world.entity.npc.VillagerDataHolder vdhSrc && dst instanceof net.minecraft.world.entity.npc.VillagerDataHolder vdhDst) {
			vdhDst.setVillagerData(vdhSrc.getVillagerData());
			return true;
		}
		if (src instanceof net.minecraft.world.entity.npc.VillagerDataHolder || dst instanceof net.minecraft.world.entity.npc.VillagerDataHolder) {
			return false;
		}
		return true;
		//? }
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

	public static net.minecraft.world.phys.EntityHitResult getEntityHitResult(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.phys.Vec3 eyePos, net.minecraft.world.phys.Vec3 endPos, net.minecraft.world.phys.AABB aabb, java.util.function.Predicate<Entity> filter) {
		//? if >=1.21.6 {
		/*return net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player, eyePos, endPos, aabb, filter, 0.0);
		*///?} else {
		return net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player.level(), player, eyePos, endPos, aabb, filter);
		//?}
	}

	public static void saveWithoutId(Entity entity, CompoundTag tag) {
		//? if >=1.21.6 {
		/*net.minecraft.world.level.storage.TagValueOutput out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
		entity.saveWithoutId(out);
		CompoundTag res = out.buildResult();
		res.getCompound("JarStackerData").ifPresent(res::merge);
		tag.merge(res);
		*///?} else {
		entity.saveWithoutId(tag);
		//?}
	}

	public static void load(Entity entity, CompoundTag tag) {
		//? if >=1.21.6 {
		/*CompoundTag preparedTag = tag.copy();
		if (!preparedTag.contains("JarStackerData")) {
			preparedTag.put("JarStackerData", tag.copy());
		}
		net.minecraft.world.level.storage.ValueInput in = net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess(), preparedTag);
		entity.load(in);
		*///?} else {
		entity.load(tag);
		//?}
	}

	public static void addAdditionalSaveData(Entity entity, CompoundTag tag) {
		//? if >=1.21.6 {
		/*try {
			net.minecraft.world.level.storage.TagValueOutput out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
			java.lang.reflect.Method m = Entity.class.getDeclaredMethod("addAdditionalSaveData", net.minecraft.world.level.storage.ValueOutput.class);
			m.setAccessible(true);
			m.invoke(entity, out);
			CompoundTag res = out.buildResult();
			res.getCompound("JarStackerData").ifPresent(res::merge);
			tag.merge(res);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		*///?} else {
		try {
			java.lang.reflect.Method m = null;
			for (String name : new String[]{"addAdditionalSaveData", "writeCustomDataToNbt", "method_5652"}) {
				try {
					m = Entity.class.getDeclaredMethod(name, CompoundTag.class);
					break;
				} catch (NoSuchMethodException ignored) {}
			}
			if (m == null) throw new NoSuchMethodException("addAdditionalSaveData");
			m.setAccessible(true);
			m.invoke(entity, tag);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		//?}
	}

	public static void readAdditionalSaveData(Entity entity, CompoundTag tag) {
		//? if >=1.21.6 {
		/*try {
			CompoundTag preparedTag = tag.copy();
			if (!preparedTag.contains("JarStackerData")) {
				preparedTag.put("JarStackerData", tag.copy());
			}
			net.minecraft.world.level.storage.ValueInput in = net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess(), preparedTag);
			java.lang.reflect.Method m = Entity.class.getDeclaredMethod("readAdditionalSaveData", net.minecraft.world.level.storage.ValueInput.class);
			m.setAccessible(true);
			m.invoke(entity, in);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		*///?} else {
		try {
			java.lang.reflect.Method m = null;
			for (String name : new String[]{"readAdditionalSaveData", "readCustomDataFromNbt", "method_5749"}) {
				try {
					m = Entity.class.getDeclaredMethod(name, CompoundTag.class);
					break;
				} catch (NoSuchMethodException ignored) {}
			}
			if (m == null) throw new NoSuchMethodException("readAdditionalSaveData");
			m.setAccessible(true);
			m.invoke(entity, tag);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		//?}
	}

	public static net.minecraft.core.BlockPos getSharedSpawnPos(net.minecraft.server.level.ServerLevel level) {
		//? if >=1.21.9 {
		/*return level.getRespawnData().pos();
		*///?} else {
		return level.getSharedSpawnPos();
		//?}
	}
}
