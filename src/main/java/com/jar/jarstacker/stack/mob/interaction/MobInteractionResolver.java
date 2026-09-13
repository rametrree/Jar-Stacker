package com.jar.jarstacker.stack.mob.interaction;

import com.jar.jarstacker.stack.StackableEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Cow;
//? if >=1.21.5 {
/*import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
*///?} else {
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
//?}
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Centralized interaction resolver for stacked mobs in Jar Stacker V0.3.0.
 * Classifies interactions into PASS_THROUGH, DIRECT, EXTRACT_ONE, or UNSUPPORTED.
 */
public class MobInteractionResolver {

	public static StackInteractionDecision resolve(Player player, Level level, InteractionHand hand, Entity entity) {
		if (!(entity instanceof Mob mob) || !mob.isAlive() || mob.isRemoved()) {
			return StackInteractionDecision.PASS_THROUGH;
		}

		if (player.isSpectator()) {
			return StackInteractionDecision.PASS_THROUGH;
		}

		ItemStack heldItem = player.getItemInHand(hand);

		// Mooshroom shearing transforms the entity type (MushroomCow -> Cow)
		// and requires Jar Stacker transformation management (normalization & destination merging)
		// even when count == 1.
		if (mob instanceof net.minecraft.world.entity.animal.MushroomCow mc && heldItem.is(Items.SHEARS)) {
			if (mc.readyForShearing()) {
				return StackInteractionDecision.transformOne("Mooshroom shearing to cow");
			} else {
				return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Mooshroom not ready for shearing", 0);
			}
		}

		int count = ((StackableEntity) mob).jarstacker$getStackCount();
		if (count <= 1) {
			return StackInteractionDecision.PASS_THROUGH;
		}

		// Defer complex mount and inventory interactions
		if (mob instanceof AbstractHorse || mob instanceof Camel || mob instanceof Llama) {
			return StackInteractionDecision.UNSUPPORTED;
		}

		if (heldItem.isEmpty()) {
			return StackInteractionDecision.PASS_THROUGH;
		}

		// 0. Mooshroom interactions (Bowl, Flower, and Shears)
		if (mob instanceof net.minecraft.world.entity.animal.MushroomCow mc) {
			if (heldItem.is(Items.BOWL)) {
				if (mc.isBaby()) {
					return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Baby mooshroom cannot be milked for stew", 0);
				}
				net.minecraft.world.item.component.SuspiciousStewEffects stew =
					((com.jar.jarstacker.mixin.MushroomCowAccessor) mc).jarstacker$getStewEffects();
				if (stew == null) {
					// Normal mushroom stew: state-neutral DIRECT interaction
					return StackInteractionDecision.DIRECT;
				} else {
					// Prepared suspicious stew: state-changing EXTRACT_ONE
					return StackInteractionDecision.extractOne("Mooshroom suspicious stew milking", 0);
				}
			}

			if (heldItem.is(Items.SHEARS)) {
				if (mc.readyForShearing()) {
					return StackInteractionDecision.transformOne("Mooshroom shearing to cow");
				} else {
					return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Mooshroom not ready for shearing", 0);
				}
			}

			if ("brown".equals(mc.getVariant().getSerializedName()) && heldItem.is(net.minecraft.tags.ItemTags.SMALL_FLOWERS)) {
				return StackInteractionDecision.extractOne("Brown mooshroom flower feeding", 0);
			}
		}

		// 1. Cow Milking (DIRECT)
		if (mob instanceof Cow cow) {
			if (heldItem.is(Items.BUCKET)) {
				if (cow.isBaby()) {
					return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Baby cow cannot be milked", 0);
				}
				return StackInteractionDecision.DIRECT;
			}
		}

		// Snow Golem Shearing (EXTRACT_ONE)
		if (mob instanceof net.minecraft.world.entity.animal.SnowGolem snowGolem) {
			if (heldItem.is(Items.SHEARS)) {
				if (snowGolem.readyForShearing()) {
					return StackInteractionDecision.extractOne("Snow Golem pumpkin shearing", 0);
				} else {
					return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Snow Golem has no pumpkin to shear", 0);
				}
			}
		}

		// Pig Saddling (EXTRACT_ONE)
		if (mob instanceof net.minecraft.world.entity.animal.Pig pig) {
			if (heldItem.is(Items.SADDLE)) {
				if (com.jar.jarstacker.adapter.EntityAdapter.isSaddleable(pig) && !com.jar.jarstacker.adapter.EntityAdapter.isSaddled(pig)) {
					return StackInteractionDecision.extractOne("Pig saddling", 0);
				} else {
					return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Pig cannot be saddled", 0);
				}
			}
		}

		// Strider Saddling (EXTRACT_ONE)
		if (mob instanceof net.minecraft.world.entity.monster.Strider strider) {
			if (heldItem.is(Items.SADDLE)) {
				if (com.jar.jarstacker.adapter.EntityAdapter.isSaddleable(strider) && !com.jar.jarstacker.adapter.EntityAdapter.isSaddled(strider)) {
					return StackInteractionDecision.extractOne("Strider saddling", 0);
				} else {
					return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Strider cannot be saddled", 0);
				}
			}
		}

		// 2. Sheep Shearing (EXTRACT_ONE)
		if (mob instanceof Sheep sheep) {
			if (heldItem.is(Items.SHEARS)) {
				if (sheep.readyForShearing()) {
					return StackInteractionDecision.extractOne("Sheep shearing with shears", 0);
				} else {
					return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Sheep not ready for shearing", 0);
				}
			}

			// 3. Sheep Dyeing (EXTRACT_ONE)
			if (heldItem.getItem() instanceof DyeItem dyeItem) {
				if (!sheep.isSheared() && !sheep.isBaby()) {
					if (sheep.getColor() != dyeItem.getDyeColor()) {
						return StackInteractionDecision.extractOne("Sheep dyeing to " + dyeItem.getDyeColor(), 0);
					} else {
						// Same color dye is a Vanilla no-op; avoid unnecessary split
						return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Same color dye is no-op", 0);
					}
				} else {
					return new StackInteractionDecision(StackInteractionMode.PASS_THROUGH, "Sheared or baby sheep cannot be dyed", 0);
				}
			}
		}

		// 4. Baby Animal Growth Feeding (EXTRACT_ONE)
		if (mob instanceof Animal animal && animal.isBaby()) {
			if (animal.isFood(heldItem)) {
				// V0.3.1: Logical per-entity state tracks individual ages; no 400-tick lock required
				return StackInteractionDecision.extractOne("Baby animal growth feeding", 0);
			}
		}

		// 5. Tameable Animal Taming Attempts (EXTRACT_ONE)
		if (mob instanceof TamableAnimal tamable && !tamable.isTame()) {
			if (tamable instanceof Wolf wolf) {
				if (heldItem.is(Items.BONE) && !wolf.isAngry()) {
					return StackInteractionDecision.extractOne("Wolf taming attempt with bone", 100);
				}
			} else if (tamable instanceof Cat cat) {
				if (cat.isFood(heldItem) && player.distanceToSqr(cat) < 36.0) {
					return StackInteractionDecision.extractOne("Cat taming attempt with fish", 100);
				}
			} else if (tamable.isFood(heldItem)) {
				return StackInteractionDecision.extractOne("Tamable animal taming attempt", 100);
			}
		}

		// 6. Adult Animal Breeding Feeding (EXTRACT_ONE)
		if (mob instanceof Animal animal && !animal.isBaby()) {
			if (animal.getAge() == 0 && !animal.isInLove() && animal.isFood(heldItem)) {
				return StackInteractionDecision.extractOne("Adult animal breeding feed", 300);
			}
		}

		return StackInteractionDecision.PASS_THROUGH;
	}
}

