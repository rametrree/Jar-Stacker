package com.jar.jarstacker.stack.mob.interaction;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.mob.BreedingDiagnostics;
import com.jar.jarstacker.stack.mob.MobCompatibility;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import com.jar.jarstacker.stack.mob.MovementDiagnostics;
import com.jar.jarstacker.stack.mob.SplitPlacementResolver;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.golem.SnowGolem;
*///?} else {
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.SnowGolem;
//?}
//? if >=1.21.5 {
/*import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
*///?} else {
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
//?}
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Set;

/**
 * Generalized mob interaction handler for Jar Stacker V0.3.0.
 * Implements DIRECT execution (e.g. milking) and EXTRACT_ONE handoff (e.g. shearing, dyeing, growth, taming).
 */
public class MobInteractionHandler {

	private static final ThreadLocal<Boolean> HANDOFF_ACTIVE = ThreadLocal.withInitial(() -> false);

	public static boolean isHandoffActive() {
		return HANDOFF_ACTIVE.get();
	}

	public static void register() {
		UseEntityCallback.EVENT.register((Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) -> {
			if (HANDOFF_ACTIVE.get()) {
				return InteractionResult.PASS;
			}

			if (!ModConfig.getInstance().getMobStacking().isEnabled()) {
				return InteractionResult.PASS;
			}

			if (!(entity instanceof Mob mob) || !mob.isAlive() || mob.isRemoved()) {
				return InteractionResult.PASS;
			}

			// Client-side prediction handling
			if (level.isClientSide()) {
				StackInteractionDecision decision = MobInteractionResolver.resolve(player, level, hand, mob);
				if (decision.requiresExtraction()) {
					return InteractionResult.SUCCESS;
				}
				return InteractionResult.PASS;
			}

			if (!(level instanceof ServerLevel serverLevel)) {
				return InteractionResult.PASS;
			}

			StackInteractionDecision decision = MobInteractionResolver.resolve(player, level, hand, mob);
			int count = ((StackableEntity) mob).jarstacker$getStackCount();

			if (decision.mode() == StackInteractionMode.PASS_THROUGH || decision.mode() == StackInteractionMode.UNSUPPORTED) {
				// Handle singleton lock application when interacted with directly
				if (count == 1) {
					ItemStack heldItem = player.getItemInHand(hand);
					if (!heldItem.isEmpty()) {
						if (mob instanceof Animal animal) {
							if (animal.isBaby() && animal.isFood(heldItem)) {
								((StackableEntity) animal).jarstacker$setInteractionLockTicks(400);
								return com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.handleBabyFeeding(player, serverLevel, hand, animal, heldItem);
							} else if (animal.getAge() == 0 && !animal.isInLove() && animal.isFood(heldItem)) {
								((StackableEntity) animal).jarstacker$setBreedingLockTicks(300);
								BreedingDiagnostics.logEvent("FEED_INTERACTION", animal, "player=" + player.getScoreboardName() + " lock=300");
							}
						} else if (mob instanceof TamableAnimal tamable && !tamable.isTame()) {
							if (tamable instanceof Wolf wolf && heldItem.is(Items.BONE) && !wolf.isAngry()) {
								((StackableEntity) tamable).jarstacker$setInteractionLockTicks(100);
							} else if (tamable instanceof Cat cat && cat.isFood(heldItem) && player.distanceToSqr(cat) < 36.0) {
								((StackableEntity) tamable).jarstacker$setInteractionLockTicks(100);
							}
						}
					}
				}
				return InteractionResult.PASS;
			}

			if (decision.isTransform()) {
				return com.jar.jarstacker.stack.mob.transformation.LogicalEntityTransformer.handleTransformation(player, serverLevel, hand, mob);
			}

			if (decision.isDirect()) {
				MobInteractionDiagnostics.logEvent("DIRECT_INTERACTION", mob,
					"player=" + player.getScoreboardName() + " reason=" + decision.reason() + " count=" + count);
				return InteractionResult.PASS;
			}

			if (decision.requiresExtraction()) {
				if (mob instanceof Animal animal && animal.isBaby() && animal.isFood(player.getItemInHand(hand))) {
					return com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.handleBabyFeeding(player, serverLevel, hand, animal, player.getItemInHand(hand));
				}

				if (count > 1) {
					int remainderCount = count - 1;

					// 1. Decrement anchor stack count
					((StackableEntity) mob).jarstacker$setStackCount(remainderCount);
					MobStackingManager.updateLabel(mob, remainderCount, ModConfig.getInstance().getMobStacking().isShowLabel());

					// 2. Spawn extracted singleton
					@SuppressWarnings("unchecked")
					EntityType<Mob> type = (EntityType<Mob>) mob.getType();
					Mob extracted = com.jar.jarstacker.adapter.EntityAdapter.create(type, serverLevel);
					if (extracted == null) {
						// Restore count if creation failed
						((StackableEntity) mob).jarstacker$setStackCount(count);
						MobStackingManager.updateLabel(mob, count, ModConfig.getInstance().getMobStacking().isShowLabel());
						return InteractionResult.FAIL;
					}

					MovementDiagnostics.logMovementEvent("ENTITY_CREATED", extracted, null, "role=extracted_singleton");

					Set<AABB> reserved = Collections.singleton(mob.getBoundingBox());
					Vec3 safePos = SplitPlacementResolver.findSafeSplitPosition(serverLevel, mob, extracted, reserved, player);
					MovementDiagnostics.logMovementEvent("SPLIT_POSITION_SELECTED", extracted, null, "target=" + safePos);

					com.jar.jarstacker.adapter.EntityAdapter.moveTo(
						extracted,
						safePos.x,
						safePos.y,
						safePos.z,
						mob.getYRot(),
						mob.getXRot()
					);
					extracted.setDeltaMovement(Vec3.ZERO);
					extracted.setHealth(mob.getHealth());

					if (mob instanceof AgeableMob ageableSrc && extracted instanceof AgeableMob ageableDst) {
						ageableDst.setAge(ageableSrc.getAge());
					}

					for (EquipmentSlot slot : EquipmentSlot.values()) {
						extracted.setItemSlot(slot, mob.getItemBySlot(slot).copy());
					}

					com.jar.jarstacker.adapter.EntityAdapter.copyVariant(mob, extracted);

					if (mob instanceof Sheep sSrc && extracted instanceof Sheep sDst) {
						sDst.setColor(sSrc.getColor());
						sDst.setSheared(sSrc.isSheared());
					}

					if (mob instanceof SnowGolem sgSrc && extracted instanceof SnowGolem sgDst) {
						sgDst.setPumpkin(sgSrc.hasPumpkin());
					}

					if (mob instanceof MushroomCow mcSrc && extracted instanceof MushroomCow mcDst) {
						com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(mcDst, "brown".equals(mcSrc.getVariant().getSerializedName()));
						net.minecraft.world.item.component.SuspiciousStewEffects effects =
							((com.jar.jarstacker.mixin.MushroomCowAccessor) mcSrc).jarstacker$getStewEffects();
						if (effects != null) {
							((com.jar.jarstacker.mixin.MushroomCowAccessor) mcDst).jarstacker$setStewEffects(effects);
						}
					}

					if (com.jar.jarstacker.adapter.EntityAdapter.isSaddled(mob)) {
						com.jar.jarstacker.adapter.EntityAdapter.equipSaddle(extracted, ItemStack.EMPTY);
					}

					((StackableEntity) extracted).jarstacker$setStackCount(1);
					if (decision.interactionLockTicks() > 0) {
						((StackableEntity) extracted).jarstacker$setInteractionLockTicks(decision.interactionLockTicks());
					}
					if (decision.interactionLockTicks() == 300 && mob instanceof Animal) {
						((StackableEntity) extracted).jarstacker$setBreedingLockTicks(300);
					}
					MobStackingManager.updateLabel(extracted, 1, ModConfig.getInstance().getMobStacking().isShowLabel());

					serverLevel.addFreshEntity(extracted);
					MovementDiagnostics.logMovementEvent("ENTITY_ADDED_TO_WORLD", extracted, null, "role=extracted_singleton");
					MobInteractionDiagnostics.logEvent("SPLIT_HANDOFF_SPAWN", extracted,
						"anchor=" + mob.getUUID() + " remainderCount=" + remainderCount + " reason=" + decision.reason());

					// 3. Interaction handoff
					HANDOFF_ACTIVE.set(true);
					InteractionResult handoffResult;
					try {
						handoffResult = com.jar.jarstacker.adapter.EntityAdapter.interactOn(player, extracted, hand);
					} finally {
						HANDOFF_ACTIVE.remove();
					}

					MobInteractionDiagnostics.logEvent("HANDOFF_COMPLETE", extracted,
						"player=" + player.getScoreboardName() + " result=" + handoffResult + " reason=" + decision.reason());

					if (ModConfig.getInstance().getPerformance().isDebugLogging()) {
						JarStackerMod.LOGGER.info("Stable anchor interaction: remainder {} at anchor, extracted {} handled with result {}",
							remainderCount, extracted.getUUID(), handoffResult);
					}

					if (handoffResult.consumesAction()) {
						return handoffResult;
					}

					return InteractionResult.SUCCESS;
				}
			}

			return InteractionResult.PASS;
		});
	}
}
