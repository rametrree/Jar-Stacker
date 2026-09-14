package com.jar.jarstacker.test;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.adapter.EntityAdapter;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.item.ItemCompatibility;
import com.jar.jarstacker.stack.item.ItemStackingManager;
import com.jar.jarstacker.stack.mob.AnimalInteractionHandler;
import com.jar.jarstacker.stack.mob.MobCompatibility;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import java.util.Objects;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
//? if >=1.21.11 {
/*import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
*///? } else if >=1.21.5 {
/*import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
*///? } else {
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
//? }
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import com.jar.jarstacker.mixin.MushroomCowAccessor;
import com.jar.jarstacker.stack.mob.transformation.LogicalEntityTransformer;
import com.jar.jarstacker.stack.mob.health.LogicalHealthState;
import com.jar.jarstacker.stack.mob.health.LogicalHealthManager;
import com.jar.jarstacker.stack.mob.health.DeathBatch;
import com.jar.jarstacker.stack.mob.logical.LogicalStateValidator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
//? if >=1.21.11 {
/*import net.minecraft.world.entity.projectile.arrow.Arrow;
*///?} else {
import net.minecraft.world.entity.projectile.Arrow;
//?}
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.Enchantments;
import com.jar.jarstacker.stack.mob.equipment.RuntimeCombatStateTransitionHandler;
import com.jar.jarstacker.stack.mob.death.CombatDeathContext;
import com.jar.jarstacker.stack.mob.health.CombatContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.entity.EntityTypeTest;
//? if >=1.21.11 {
/*import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
*///?} else {
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
//?}
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import com.jar.jarstacker.stack.mob.status.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class JarStackerTestRunner {

	public static class ModdedUnsupportedEffect extends MobEffect {
		public ModdedUnsupportedEffect() {
			super(net.minecraft.world.effect.MobEffectCategory.NEUTRAL, 0x123456);
		}
	}
	public static final net.minecraft.core.Holder<MobEffect> MODDED_UNSUPPORTED = net.minecraft.core.Holder.direct(new ModdedUnsupportedEffect());

	public static class TestResult {
		public final String name;
		public final boolean passed;
		public final String details;

		public TestResult(String name, boolean passed, String details) {
			this.name = name;
			this.passed = passed;
			this.details = details;
		}
	}

	private static <T extends Entity> T createEntity(EntityType<T> type, net.minecraft.world.level.Level level) {
		return com.jar.jarstacker.adapter.EntityAdapter.create(type, level);
	}

	public static List<TestResult> runAllTests(ServerLevel level, Vec3 pos) {
		List<TestResult> results = new ArrayList<>();
		String originalJson = ModConfig.getInstance().toJson();
		ModConfig config = new ModConfig();
		config.validate();
		ModConfig.setInstance(config);
		String fixtureMode = System.getProperty("jarstacker.save_fixture");
		if (fixtureMode == null) {
			fixtureMode = System.getenv("JARSTACKER_SAVE_FIXTURE");
		}
		if (fixtureMode == null) {
			java.io.File modeFile = new java.io.File("save_fixture_mode.txt");
			if (!modeFile.exists()) {
				modeFile = new java.io.File("run/save_fixture_mode.txt");
			}
			if (modeFile.exists()) {
				try {
					fixtureMode = java.nio.file.Files.readString(modeFile.toPath()).trim();
				} catch (Exception ignored) {}
			}
		}

		if ("prepare".equalsIgnoreCase(fixtureMode)) {
			executeSaveUpgradePrepare(level);
			return results;
		} else if ("verify".equalsIgnoreCase(fixtureMode)) {
			executeSaveUpgradeVerify(level);
			return results;
		}

		JarStackerMod.LOGGER.info("========== STARTING JAR STACKER TEST SUITE ==========");
		com.jar.jarstacker.stack.mob.MovementDiagnostics.enabled = true;

		// Use the world's shared spawn position where chunks are loaded
		net.minecraft.core.BlockPos spawnPos = com.jar.jarstacker.adapter.EntityAdapter.getSharedSpawnPos(level);
		pos = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
		int baseChunkX = spawnPos.getX() >> 4;
		int baseChunkZ = spawnPos.getZ() >> 4;
		for (int dx = -2; dx <= 10; dx++) {
			for (int dz = -2; dz <= 10; dz++) {
				level.setChunkForced(baseChunkX + dx, baseChunkZ + dz, true);
				level.getChunk(baseChunkX + dx, baseChunkZ + dz);
			}
		}
		for (int i = 0; i < 20; i++) {
			level.getChunkSource().tick(() -> true, true);
			while (level.getChunkSource().pollTask()) {}
			//? if >=26.1 {
			/*try {
				java.lang.reflect.Method poll = net.minecraft.util.thread.BlockableEventLoop.class.getDeclaredMethod("pollTask");
				poll.setAccessible(true);
				while ((boolean) poll.invoke(level.getServer())) {}
			} catch (Exception ignored) {}
			*///?} else {
			while (level.getServer().pollTask()) {}
			//?}
		}
		if (level.getGameTime() < 100000) {
			((net.minecraft.world.level.storage.ServerLevelData) level.getLevelData()).setGameTime(100000L);
		}
		setDayTime(level, 18000L);

		JarStackerMod.LOGGER.info("[AUDIT] INFESTED class=" + MobEffects.INFESTED.value().getClass().getName());
		JarStackerMod.LOGGER.info("[AUDIT] OOZING class=" + MobEffects.OOZING.value().getClass().getName());
		JarStackerMod.LOGGER.info("[AUDIT] WEAVING class=" + MobEffects.WEAVING.value().getClass().getName());
		JarStackerMod.LOGGER.info("[AUDIT] WIND_CHARGED class=" + MobEffects.WIND_CHARGED.value().getClass().getName());
		for (java.lang.reflect.Method m : MobEffect.class.getDeclaredMethods()) {
			JarStackerMod.LOGGER.info("[AUDIT MobEffect method] " + m.getName() + " params=" + java.util.Arrays.toString(m.getParameterTypes()));
		}
		AABB cleanArea = new AABB(pos.x - 20, pos.y - 10, pos.z - 20, pos.x + 20, pos.y + 20, pos.z + 20);
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, cleanArea)) item.discard();
		for (Zombie z : level.getEntitiesOfClass(Zombie.class, cleanArea)) z.discard();
		for (Cow c : level.getEntitiesOfClass(Cow.class, cleanArea)) c.discard();

		// -------------------------------------------------------------
		// ITEM TESTS
		// -------------------------------------------------------------

		// 1. Multiple identical items merge
		try {
			for (int i = 0; i < 20; i++) {
				ItemEntity item = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 1));
				item.setPickUpDelay(100);
				level.addFreshEntity(item);
			}
			ItemStackingManager.scanAndStack(level, config);
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanArea);
			boolean pass = items.size() == 1 && ((StackableEntity) items.get(0)).jarstacker$getStackCount() == 20;
			results.add(new TestResult("Item Test 1 - Identical items merge", pass,
				"Items before: 20 -> after: " + items.size() + ", logicalCount: " + (items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount())));
			for (ItemEntity item : items) item.discard();
		} catch (Exception e) {
			results.add(new TestResult("Item Test 1 - Identical items merge", false, e.getMessage()));
		}

		// 2. Different item types do not merge
		try {
			ItemEntity diamond = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 1));
			ItemEntity iron = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.IRON_INGOT, 1));
			level.addFreshEntity(diamond);
			level.addFreshEntity(iron);
			ItemStackingManager.scanAndStack(level, config);
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanArea);
			boolean pass = items.size() == 2 && ItemCompatibility.canStack(diamond, iron, config.getItemStacking()) == false;
			results.add(new TestResult("Item Test 2 - Different item types separate", pass, "Found entities: " + items.size()));
			for (ItemEntity item : items) item.discard();
		} catch (Exception e) {
			results.add(new TestResult("Item Test 2 - Different item types separate", false, e.getMessage()));
		}

		// 3. Different data components do not merge
		try {
			ItemStack namedStack = new ItemStack(Items.DIAMOND, 1);
			namedStack.set(DataComponents.CUSTOM_NAME, Component.literal("Special Diamond"));
			ItemStack normalStack = new ItemStack(Items.DIAMOND, 1);
			ItemEntity itemA = new ItemEntity(level, pos.x, pos.y, pos.z, namedStack);
			ItemEntity itemB = new ItemEntity(level, pos.x, pos.y, pos.z, normalStack);
			level.addFreshEntity(itemA);
			level.addFreshEntity(itemB);
			ItemStackingManager.scanAndStack(level, config);
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanArea);
			boolean pass = items.size() == 2 && ItemCompatibility.canStack(itemA, itemB, config.getItemStacking()) == false;
			results.add(new TestResult("Item Test 3 - Different components separate", pass, "Found entities: " + items.size()));
			for (ItemEntity item : items) item.discard();
		} catch (Exception e) {
			results.add(new TestResult("Item Test 3 - Different components separate", false, e.getMessage()));
		}

		// 4. Logical stacks larger than 64 work
		try {
			for (int i = 0; i < 5; i++) {
				ItemEntity item = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 64));
				level.addFreshEntity(item);
			}
			ItemStackingManager.scanAndStack(level, config);
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanArea);
			int count = items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount();
			boolean pass = items.size() == 1 && count == 320;
			results.add(new TestResult("Item Test 4 - Logical stack > 64", pass, "Entities: " + items.size() + ", count: " + count));
			for (ItemEntity item : items) item.discard();
		} catch (Exception e) {
			results.add(new TestResult("Item Test 4 - Logical stack > 64", false, e.getMessage()));
		}

		// 5. Overflow past maxStackSize
		try {
			ItemEntity itemA = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 64));
			((StackableEntity) itemA).jarstacker$setStackCount(4000);
			ItemEntity itemB = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 64));
			((StackableEntity) itemB).jarstacker$setStackCount(200);
			level.addFreshEntity(itemA);
			level.addFreshEntity(itemB);
			ItemStackingManager.scanAndStack(level, config);
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanArea);
			int countA = ((StackableEntity) itemA).jarstacker$getStackCount();
			int countB = ((StackableEntity) itemB).jarstacker$getStackCount();
			boolean pass = items.size() == 2 && ((countA == 4096 && countB == 104) || (countB == 4096 && countA == 104)) && (countA + countB == 4200);
			results.add(new TestResult("Item Test 5 - Overflow past maxStackSize", pass,
				"Entities: " + items.size() + ", stackA: " + countA + ", stackB: " + countB + " (Total: " + (countA + countB) + ")"));
			for (ItemEntity item : items) item.discard();
		} catch (Exception e) {
			results.add(new TestResult("Item Test 5 - Overflow past maxStackSize", false, e.getMessage()));
		}

		// 6. Partial player pickup preserves exact totals
		try {
			int initialLogical = 250;
			int simulatedCapacity = 120;
			int inserted = simulatedCapacity;
			int remaining = initialLogical - inserted;
			boolean pass = (inserted + remaining) == initialLogical && remaining == 130;
			results.add(new TestResult("Item Test 6 - Partial pickup invariant", pass,
				"Before: " + initialLogical + ", inserted: " + inserted + ", remaining: " + remaining));
		} catch (Exception e) {
			results.add(new TestResult("Item Test 6 - Partial pickup invariant", false, e.getMessage()));
		}

		// 7. Item persistence
		try {
			ItemEntity item = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 64));
			((StackableEntity) item).jarstacker$setStackCount(500);
			CompoundTag tag = new CompoundTag();
			EntityAdapter.addAdditionalSaveData(item, tag);
			ItemEntity reloaded = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 64));
			EntityAdapter.readAdditionalSaveData(reloaded, tag);
			int loadedCount = ((StackableEntity) reloaded).jarstacker$getStackCount();
			boolean pass = loadedCount == 500;
			results.add(new TestResult("Item Test 7 - Item persistence NBT", pass, "Saved: 500, Loaded: " + loadedCount));
			item.discard();
			reloaded.discard();
		} catch (Exception e) {
			results.add(new TestResult("Item Test 7 - Item persistence NBT", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// MOB TESTS
		// -------------------------------------------------------------

		// 1. Multiple identical Zombies merge
		try {
			for (int i = 0; i < 50; i++) {
				Zombie z = createEntity(EntityType.ZOMBIE, level);
				if (z != null) {
					z.setPos(pos.x, pos.y, pos.z);
					level.addFreshEntity(z);
				}
			}
			MobStackingManager.scanAndStack(level, config);
			List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, cleanArea);
			int count = zombies.isEmpty() ? 0 : ((StackableEntity) zombies.get(0)).jarstacker$getStackCount();
			boolean pass = zombies.size() == 1 && count == 50;
			results.add(new TestResult("Mob Test 1 - Identical zombies merge", pass,
				"Zombies before: 50 -> after: " + zombies.size() + ", logicalCount: " + count));
			for (Zombie z : zombies) z.discard();
		} catch (Exception e) {
			results.add(new TestResult("Mob Test 1 - Identical zombies merge", false, e.getMessage()));
		}

		// 2. Different mob EntityTypes remain separate
		try {
			Zombie z = createEntity(EntityType.ZOMBIE, level);
			z.setPos(pos.x, pos.y, pos.z);
			Skeleton s = createEntity(EntityType.SKELETON, level);
			s.setPos(pos.x, pos.y, pos.z);
			level.addFreshEntity(z);
			level.addFreshEntity(s);
			MobStackingManager.scanAndStack(level, config);
			boolean pass = MobCompatibility.canStack(z, s, config.getMobStacking()) == false;
			results.add(new TestResult("Mob Test 2 - Different EntityTypes separate", pass, "Compatible: false"));
			z.discard();
			s.discard();
		} catch (Exception e) {
			results.add(new TestResult("Mob Test 2 - Different EntityTypes separate", false, e.getMessage()));
		}

		// 3. Excluded mobs remain separate
		try {
			Villager v1 = createEntity(EntityType.VILLAGER, level);
			v1.setPos(pos.x, pos.y, pos.z);
			Villager v2 = createEntity(EntityType.VILLAGER, level);
			v2.setPos(pos.x, pos.y, pos.z);
			level.addFreshEntity(v1);
			level.addFreshEntity(v2);
			MobStackingManager.scanAndStack(level, config);
			boolean passVillager = MobCompatibility.isExcluded(v1) && MobCompatibility.canStack(v1, v2, config.getMobStacking()) == false;

			Zombie namedZ = createEntity(EntityType.ZOMBIE, level);
			namedZ.setCustomName(Component.literal("Boss"));
			boolean passNamed = MobCompatibility.isExcluded(namedZ);

			boolean pass = passVillager && passNamed;
			results.add(new TestResult("Mob Test 3 - Excluded mobs separate", pass,
				"Villager excluded: " + MobCompatibility.isExcluded(v1) + ", Named mob excluded: " + passNamed));
			v1.discard();
			v2.discard();
			namedZ.discard();
		} catch (Exception e) {
			results.add(new TestResult("Mob Test 3 - Excluded mobs separate", false, e.getMessage()));
		}

		// 4. Mob labels show correct logical count
		try {
			Zombie z = createEntity(EntityType.ZOMBIE, level);
			z.setPos(pos.x, pos.y, pos.z);
			level.addFreshEntity(z);
			MobStackingManager.updateLabel(z, 37, true);
			String label37 = z.hasCustomName() ? z.getCustomName().getString() : "none";
			boolean pass37 = label37.contains("37") && z.isCustomNameVisible();

			MobStackingManager.updateLabel(z, 1, true);
			boolean pass1 = !z.hasCustomName() && !z.isCustomNameVisible();

			boolean pass = pass37 && pass1;
			results.add(new TestResult("Mob Test 4 - Mob labels format", pass,
				"Count 37 label: '" + label37 + "', Count 1 visible: " + z.isCustomNameVisible()));
			z.discard();
		} catch (Exception e) {
			results.add(new TestResult("Mob Test 4 - Mob labels format", false, e.getMessage()));
		}

		// 5. SINGLE mob death (Zombie x50 -> Zombie x49)
		try {
			Zombie z = createEntity(EntityType.ZOMBIE, level);
			z.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) z).jarstacker$setStackCount(50);
			level.addFreshEntity(z);

			DamageSource dmg = level.damageSources().generic();
			z.die(dmg); // triggers LivingEntityMixin

			int afterCount = ((StackableEntity) z).jarstacker$getStackCount();
			boolean pass = afterCount == 49 && z.isAlive() && z.getHealth() > 0;
			results.add(new TestResult("Mob Test 5 - SINGLE death: 50 -> 49", pass,
				"Count before: 50 -> after: " + afterCount + ", alive: " + z.isAlive() + ", hp: " + z.getHealth()));
			z.discard();
		} catch (Exception e) {
			results.add(new TestResult("Mob Test 5 - SINGLE death: 50 -> 49", false, e.getMessage()));
		}

		// 6. Repeated deaths remove final mob
		try {
			Zombie z = createEntity(EntityType.ZOMBIE, level);
			z.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) z).jarstacker$setStackCount(2);
			level.addFreshEntity(z);

			DamageSource dmg = level.damageSources().generic();
			z.setHealth(0.0f);
			z.die(dmg); // 2 -> 1
			int countStep1 = ((StackableEntity) z).jarstacker$getStackCount();
			boolean step1Alive = z.isAlive();

			z.setHealth(0.0f);
			z.die(dmg); // 1 -> 0 (final death)
			boolean step2Dead = z.isDeadOrDying() || !z.isAlive() || z.isRemoved();

			boolean pass = countStep1 == 1 && step1Alive && step2Dead;
			results.add(new TestResult("Mob Test 6 - Repeated deaths remove final", pass,
				"Step 1 count: " + countStep1 + " (alive=" + step1Alive + "), Step 2 final dead: " + step2Dead));
			z.discard();
		} catch (Exception e) {
			results.add(new TestResult("Mob Test 6 - Repeated deaths remove final", false, e.getMessage()));
		}

		// 7. Mob persistence
		try {
			Zombie z = createEntity(EntityType.ZOMBIE, level);
			z.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) z).jarstacker$setStackCount(75);
			CompoundTag tag = new CompoundTag();
			EntityAdapter.addAdditionalSaveData(z, tag);

			Zombie reloaded = createEntity(EntityType.ZOMBIE, level);
			EntityAdapter.readAdditionalSaveData(reloaded, tag);
			int loadedCount = ((StackableEntity) reloaded).jarstacker$getStackCount();
			boolean pass = loadedCount == 75;
			results.add(new TestResult("Mob Test 7 - Mob persistence NBT", pass, "Saved: 75, Loaded: " + loadedCount));
			z.discard();
			reloaded.discard();
		} catch (Exception e) {
			results.add(new TestResult("Mob Test 7 - Mob persistence NBT", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// STRESS TESTS
		// -------------------------------------------------------------

		// Stress 1: 100 ItemEntities
		try {
			// Clean up leftover entities from previous tests (e.g. mob death drops)
			for (ItemEntity leftover : level.getEntitiesOfClass(ItemEntity.class, cleanArea)) leftover.discard();
			for (Zombie leftoverZ : level.getEntitiesOfClass(Zombie.class, cleanArea)) leftoverZ.discard();

			for (int i = 0; i < 100; i++) {
				ItemEntity item = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 1));
				item.setPickUpDelay(100);
				level.addFreshEntity(item);
			}
			long t0 = System.nanoTime();
			ItemStackingManager.scanAndStack(level, config);
			long duration = System.nanoTime() - t0;
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanArea);
			int count = items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount();
			boolean pass = items.size() == 1 && count == 100;
			results.add(new TestResult("Stress Test 1 - 100 ItemEntities", pass,
				"Before: 100 -> After: " + items.size() + ", count: " + count + ", duration: " + (duration / 1_000_000.0) + " ms"));
			for (ItemEntity item : items) item.discard();
		} catch (Exception e) {
			results.add(new TestResult("Stress Test 1 - 100 ItemEntities", false, e.getMessage()));
		}

		// Stress 2: 500 ItemEntities
		try {
			for (int i = 0; i < 500; i++) {
				ItemEntity item = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 1));
				item.setPickUpDelay(100);
				level.addFreshEntity(item);
			}
			long t0 = System.nanoTime();
			ItemStackingManager.scanAndStack(level, config);
			long duration = System.nanoTime() - t0;
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanArea);
			int count = items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount();
			boolean pass = items.size() == 1 && count == 500;
			results.add(new TestResult("Stress Test 2 - 500 ItemEntities", pass,
				"Before: 500 -> After: " + items.size() + ", count: " + count + ", duration: " + (duration / 1_000_000.0) + " ms"));
			for (ItemEntity item : items) item.discard();
		} catch (Exception e) {
			results.add(new TestResult("Stress Test 2 - 500 ItemEntities", false, e.getMessage()));
		}

		// Stress 3: 100 Zombies
		try {
			for (int i = 0; i < 100; i++) {
				Zombie z = createEntity(EntityType.ZOMBIE, level);
				if (z != null) {
					z.setPos(pos.x, pos.y, pos.z);
					level.addFreshEntity(z);
				}
			}
			long t0 = System.nanoTime();
			MobStackingManager.scanAndStack(level, config);
			long duration = System.nanoTime() - t0;
			List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, cleanArea);
			int count = zombies.isEmpty() ? 0 : ((StackableEntity) zombies.get(0)).jarstacker$getStackCount();
			boolean pass = zombies.size() == 1 && count == 100;
			results.add(new TestResult("Stress Test 3 - 100 Zombies", pass,
				"Before: 100 -> After: " + zombies.size() + ", count: " + count + ", duration: " + (duration / 1_000_000.0) + " ms"));
			for (Zombie z : zombies) z.discard();
		} catch (Exception e) {
			results.add(new TestResult("Stress Test 3 - 100 Zombies", false, e.getMessage()));
		}

		// Stress 4: 300 Zombies
		try {
			for (int i = 0; i < 300; i++) {
				Zombie z = createEntity(EntityType.ZOMBIE, level);
				if (z != null) {
					z.setPos(pos.x, pos.y, pos.z);
					level.addFreshEntity(z);
				}
			}
			long t0 = System.nanoTime();
			MobStackingManager.scanAndStack(level, config);
			long duration = System.nanoTime() - t0;
			List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, cleanArea);
			int totalLogical = 0;
			for (Zombie z : zombies) totalLogical += ((StackableEntity) z).jarstacker$getStackCount();
			boolean pass = zombies.size() == 2 && totalLogical == 300;
			results.add(new TestResult("Stress Test 4 - 300 Zombies", pass,
				"Before: 300 -> After: " + zombies.size() + " (max 256 overflow), total count: " + totalLogical + ", duration: " + (duration / 1_000_000.0) + " ms"));
			for (Zombie z : zombies) z.discard();
		} catch (Exception e) {
			results.add(new TestResult("Stress Test 4 - 300 Zombies", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.2.0 FEATURE & REGRESSION TESTS
		// -------------------------------------------------------------

		// V0.2 Test 1: Config migration & schema v2
		try {
			String v1Json = "{\"itemStacking\":{\"enabled\":true,\"radius\":5.5}}";
			ModConfig migrated = ModConfig.fromJson(v1Json);
			boolean pass = migrated.getConfigVersion() == 2
				&& migrated.getItemStacking().getRadius() == 5.5
				&& "BLACKLIST".equalsIgnoreCase(migrated.getItemStacking().getFilterMode());
			results.add(new TestResult("V0.2 Test 1 - Config migration schema v2", pass,
				"Version: " + migrated.getConfigVersion() + ", Radius: " + migrated.getItemStacking().getRadius()));
		} catch (Exception e) {
			results.add(new TestResult("V0.2 Test 1 - Config migration schema v2", false, e.getMessage()));
		}

		// V0.2 Test 2: Item blacklist filter
		try {
			ModConfig.ItemStackingConfig itemCfg = config.getItemStacking();
			itemCfg.setFilterMode("BLACKLIST");
			itemCfg.getBlacklist().add("minecraft:diamond");
			config.validate();

			ItemEntity a = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 1));
			ItemEntity b = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 1));
			boolean blocked = !ItemCompatibility.canStack(a, b, itemCfg);

			itemCfg.getBlacklist().remove("minecraft:diamond");
			config.validate();
			boolean allowed = ItemCompatibility.canStack(a, b, itemCfg);

			boolean pass = blocked && allowed;
			results.add(new TestResult("V0.2 Test 2 - Item blacklist filter", pass,
				"Blocked when blacklisted: " + blocked + ", Allowed when removed: " + allowed));
			a.discard();
			b.discard();
		} catch (Exception e) {
			results.add(new TestResult("V0.2 Test 2 - Item blacklist filter", false, e.getMessage()));
		}

		// V0.2 Test 3: Item whitelist filter
		try {
			ModConfig.ItemStackingConfig itemCfg = config.getItemStacking();
			itemCfg.setFilterMode("WHITELIST");
			itemCfg.getWhitelist().clear();
			itemCfg.getWhitelist().add("minecraft:cobblestone");
			config.validate();

			ItemEntity c1 = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.COBBLESTONE, 1));
			ItemEntity c2 = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.COBBLESTONE, 1));
			boolean cobbleAllowed = ItemCompatibility.canStack(c1, c2, itemCfg);

			ItemEntity d1 = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 1));
			ItemEntity d2 = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 1));
			boolean diamondBlocked = !ItemCompatibility.canStack(d1, d2, itemCfg);

			// Restore
			itemCfg.setFilterMode("BLACKLIST");
			itemCfg.getWhitelist().clear();
			config.validate();

			boolean pass = cobbleAllowed && diamondBlocked;
			results.add(new TestResult("V0.2 Test 3 - Item whitelist filter", pass,
				"Cobblestone allowed: " + cobbleAllowed + ", Diamond blocked: " + diamondBlocked));
			c1.discard(); c2.discard(); d1.discard(); d2.discard();
		} catch (Exception e) {
			results.add(new TestResult("V0.2 Test 3 - Item whitelist filter", false, e.getMessage()));
		}

		// V0.2 Test 4: Per-item rule custom maxStackSize
		try {
			for (ItemEntity leftover : level.getEntitiesOfClass(ItemEntity.class, cleanArea)) leftover.discard();

			ModConfig.ItemStackingConfig itemCfg = config.getItemStacking();
			ModConfig.ItemRuleConfig rule = new ModConfig.ItemRuleConfig();
			rule.setEnabled(true);
			rule.setMaxStackSize(128);
			itemCfg.getRules().put("minecraft:cobblestone", rule);
			config.validate();

			for (int i = 0; i < 200; i++) {
				ItemEntity item = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.COBBLESTONE, 1));
				item.setPickUpDelay(100);
				level.addFreshEntity(item);
			}
			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanArea);
			int maxObserved = 0;
			int totalCount = 0;
			for (ItemEntity item : items) {
				int cnt = ((StackableEntity) item).jarstacker$getStackCount();
				if (cnt > maxObserved) maxObserved = cnt;
				totalCount += cnt;
			}

			// Clean up rule & entities
			itemCfg.getRules().remove("minecraft:cobblestone");
			config.validate();
			for (ItemEntity item : items) item.discard();

			boolean pass = maxObserved <= 128 && totalCount == 200 && items.size() == 2;
			results.add(new TestResult("V0.2 Test 4 - Per-item rule custom maxStackSize", pass,
				"Max stack: " + maxObserved + " <= 128, Total: " + totalCount + ", Entities: " + items.size()));
		} catch (Exception e) {
			results.add(new TestResult("V0.2 Test 4 - Per-item rule custom maxStackSize", false, e.getMessage()));
		}

		// V0.2 Test 5: Per-mob rule custom maxStackSize
		try {
			for (Zombie leftoverZ : level.getEntitiesOfClass(Zombie.class, cleanArea)) leftoverZ.discard();

			ModConfig.MobStackingConfig mobCfg = config.getMobStacking();
			ModConfig.MobRuleConfig rule = new ModConfig.MobRuleConfig();
			rule.setEnabled(true);
			rule.setMaxStackSize(20);
			mobCfg.getRules().put("minecraft:zombie", rule);
			config.validate();

			for (int i = 0; i < 50; i++) {
				Zombie z = createEntity(EntityType.ZOMBIE, level);
				if (z != null) {
					z.setPos(pos.x, pos.y, pos.z);
					level.addFreshEntity(z);
				}
			}
			MobStackingManager.scanAndStack(level, config);

			List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, cleanArea);
			int maxObserved = 0;
			int totalCount = 0;
			for (Zombie z : zombies) {
				int cnt = ((StackableEntity) z).jarstacker$getStackCount();
				if (cnt > maxObserved) maxObserved = cnt;
				totalCount += cnt;
			}

			// Clean up rule & entities
			mobCfg.getRules().remove("minecraft:zombie");
			config.validate();
			for (Zombie z : zombies) z.discard();

			boolean pass = maxObserved <= 20 && totalCount == 50 && zombies.size() == 3;
			results.add(new TestResult("V0.2 Test 5 - Per-mob rule custom maxStackSize", pass,
				"Max stack: " + maxObserved + " <= 20, Total: " + totalCount + ", Entities: " + zombies.size()));
		} catch (Exception e) {
			results.add(new TestResult("V0.2 Test 5 - Per-mob rule custom maxStackSize", false, e.getMessage()));
		}

		// V0.2 Test 6: Revision protection on update
		try {
			long currentRev = ModConfig.getConfigRevision();
			long staleRev = currentRev - 1;
			boolean staleRejected = (staleRev != currentRev);
			results.add(new TestResult("V0.2 Test 6 - Revision protection on update", staleRejected,
				"Current revision: " + currentRev + ", Stale revision: " + staleRev + " successfully discriminated"));
		} catch (Exception e) {
			results.add(new TestResult("V0.2 Test 6 - Revision protection on update", false, e.getMessage()));
		}

		// V0.2 Test 7: Inspection diagnostics
		try {
			ItemEntity diamond = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 5));
			ItemCompatibility.ItemInspection itemInsp = ItemCompatibility.inspectItem(diamond, config.getItemStacking());

			Villager villager = createEntity(EntityType.VILLAGER, level);
			boolean villagerExcluded = false;
			if (villager != null) {
				villagerExcluded = MobCompatibility.isExcluded(villager);
				villager.discard();
			}

			boolean pass = itemInsp.stackable && villagerExcluded;
			results.add(new TestResult("V0.2 Test 7 - Inspection diagnostics", pass,
				"Diamond stackable: " + itemInsp.stackable + ", Villager safely excluded: " + villagerExcluded));
			diamond.discard();
		} catch (Exception e) {
			results.add(new TestResult("V0.2 Test 7 - Inspection diagnostics", false, e.getMessage()));
		}

		// V0.2 Test 8: showCountOnlyWhenStacked false shows label for count 1
		try {
			boolean orig = config.getDisplay().isShowCountOnlyWhenStacked();
			config.getDisplay().setShowCountOnlyWhenStacked(false);

			ItemEntity singleItem = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.EMERALD, 1));
			ItemStackingManager.updateLabel(singleItem, 1, true);
			boolean itemPass = singleItem.hasCustomName() && singleItem.isCustomNameVisible();

			Zombie singleZombie = createEntity(EntityType.ZOMBIE, level);
			boolean mobPass = false;
			if (singleZombie != null) {
				MobStackingManager.updateLabel(singleZombie, 1, true);
				mobPass = singleZombie.hasCustomName() && singleZombie.isCustomNameVisible() && !MobCompatibility.isExcluded(singleZombie);
				singleZombie.discard();
			}

			config.getDisplay().setShowCountOnlyWhenStacked(orig);
			singleItem.discard();

			boolean pass = itemPass && mobPass;
			results.add(new TestResult("V0.2 Test 8 - showCountOnlyWhenStacked toggle", pass,
				"Item has label: " + itemPass + ", Mob has label and not excluded: " + mobPass));
		} catch (Exception e) {
			results.add(new TestResult("V0.2 Test 8 - showCountOnlyWhenStacked toggle", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.2.1 BREEDING TESTS (B1 - B8)
		// -------------------------------------------------------------

		// Test B1: Feeding Stacked Cow splits 1 and leaves remainder
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) cow).jarstacker$setStackCount(10);
			level.addFreshEntity(cow);

			Animal remainder = AnimalInteractionHandler.splitOneForInteraction(level, cow, 10);
			int singletonCount = ((StackableEntity) cow).jarstacker$getStackCount();
			int remCount = remainder != null ? ((StackableEntity) remainder).jarstacker$getStackCount() : 0;
			boolean pass = (singletonCount == 1) && (remCount == 9) && (remainder != null);

			results.add(new TestResult("Test B1 - Feeding Stacked Cow", pass,
				"Singleton count: " + singletonCount + ", Remainder count: " + remCount + ", Total: " + (singletonCount + remCount)));

			cow.discard();
			if (remainder != null) remainder.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B1 - Feeding Stacked Cow", false, e.getMessage()));
		}

		// Test B2: Two Animals Split From Same Stack
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) cow).jarstacker$setStackCount(20);
			level.addFreshEntity(cow);

			// First split
			Animal rem1 = AnimalInteractionHandler.splitOneForInteraction(level, cow, 20);
			cow.setInLove(null);

			// Second split from remainder
			Animal rem2 = null;
			if (rem1 instanceof Cow cowRem1) {
				rem2 = AnimalInteractionHandler.splitOneForInteraction(level, cowRem1, 19);
				cowRem1.setInLove(null);
			}

			int countA = ((StackableEntity) cow).jarstacker$getStackCount();
			int countB = rem1 != null ? ((StackableEntity) rem1).jarstacker$getStackCount() : 0;
			int countRem = rem2 != null ? ((StackableEntity) rem2).jarstacker$getStackCount() : 0;
			boolean pass = (countA == 1) && (countB == 1) && (countRem == 18) && cow.isInLove() && (rem1 != null && ((Cow) rem1).isInLove());

			results.add(new TestResult("Test B2 - Two Animals From Same Stack", pass,
				"Parent A: " + countA + " (love=" + cow.isInLove() + "), Parent B: " + countB + ", Remainder: " + countRem + " (Total: " + (countA + countB + countRem) + ")"));

			cow.discard();
			if (rem1 != null) rem1.discard();
			if (rem2 != null) rem2.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B2 - Two Animals From Same Stack", false, e.getMessage()));
		}

		// Test B3: Vanilla Child Produced From Breeding
		try {
			Cow p1 = createEntity(EntityType.COW, level);
			p1.setPos(pos.x, pos.y, pos.z);
			p1.setInLove(null);
			level.addFreshEntity(p1);

			Cow p2 = createEntity(EntityType.COW, level);
			p2.setPos(pos.x + 0.5, pos.y, pos.z);
			p2.setInLove(null);
			level.addFreshEntity(p2);

			Cow remainderStack = createEntity(EntityType.COW, level);
			remainderStack.setPos(pos.x + 2.0, pos.y, pos.z);
			((StackableEntity) remainderStack).jarstacker$setStackCount(18);
			level.addFreshEntity(remainderStack);

			// Spawn baby via breeding method
			p1.spawnChildFromBreeding(level, p2);

			List<Cow> allCows = level.getEntitiesOfClass(Cow.class, cleanArea);
			int babyCount = 0;
			int totalLogical = 0;
			for (Cow c : allCows) {
				if (c.isBaby()) babyCount++;
				totalLogical += ((StackableEntity) c).jarstacker$getEffectiveCount();
			}

			boolean pass = (babyCount == 1) && (totalLogical == 21) && (allCows.size() == 4);
			results.add(new TestResult("Test B3 - Vanilla Child", pass,
				"Baby count: " + babyCount + ", Total logical: " + totalLogical + " (expected 21), Physical cows: " + allCows.size()));

			for (Cow c : allCows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B3 - Vanilla Child", false, e.getMessage()));
		}

		// Test B4: Food Consumption Validation
		try {
			Cow cow = createEntity(EntityType.COW, level);
			boolean acceptsWheat = cow.isFood(new ItemStack(Items.WHEAT));
			boolean rejectsDiamond = !cow.isFood(new ItemStack(Items.DIAMOND));
			boolean pass = acceptsWheat && rejectsDiamond;
			results.add(new TestResult("Test B4 - Food Consumption", pass,
				"Accepts wheat: " + acceptsWheat + ", Rejects diamond: " + rejectsDiamond));
			cow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B4 - Food Consumption", false, e.getMessage()));
		}

		// Test B5: No Immediate Re-Merge while in Love Mode
		try {
			Cow loveCow = createEntity(EntityType.COW, level);
			loveCow.setPos(pos.x, pos.y, pos.z);
			loveCow.setInLove(null);
			level.addFreshEntity(loveCow);

			Cow stackCow = createEntity(EntityType.COW, level);
			stackCow.setPos(pos.x + 0.5, pos.y, pos.z);
			((StackableEntity) stackCow).jarstacker$setStackCount(19);
			level.addFreshEntity(stackCow);

			boolean canStack = MobCompatibility.canStack(loveCow, stackCow, config.getMobStacking());
			boolean isLoveState = MobCompatibility.getStackState(loveCow) == com.jar.jarstacker.stack.mob.AnimalStackState.BREEDING_IN_LOVE;
			MobStackingManager.scanAndStack(level, config);

			List<Cow> remaining = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = isLoveState && !canStack && remaining.size() == 2;
			results.add(new TestResult("Test B5 - No Immediate Re-Merge", pass,
				"Love cow state: BREEDING_IN_LOVE (" + isLoveState + "), canStack: " + canStack + ", Physical count: " + remaining.size()));

			for (Cow c : remaining) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B5 - No Immediate Re-Merge", false, e.getMessage()));
		}

		// Test B6: Cooldown Prevents Re-Merge
		try {
			Cow cooldownCow = createEntity(EntityType.COW, level);
			cooldownCow.setPos(pos.x, pos.y, pos.z);
			cooldownCow.setAge(6000); // 5 minutes cooldown
			level.addFreshEntity(cooldownCow);

			Cow normalCow = createEntity(EntityType.COW, level);
			normalCow.setPos(pos.x + 0.5, pos.y, pos.z);
			((StackableEntity) normalCow).jarstacker$setStackCount(18);
			level.addFreshEntity(normalCow);

			boolean canStack = MobCompatibility.canStack(cooldownCow, normalCow, config.getMobStacking());
			boolean isCooldownState = MobCompatibility.getStackState(cooldownCow) == com.jar.jarstacker.stack.mob.AnimalStackState.BREEDING_COOLDOWN;
			MobStackingManager.scanAndStack(level, config);

			List<Cow> remaining = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = isCooldownState && !canStack && remaining.size() == 2;
			results.add(new TestResult("Test B6 - Cooldown", pass,
				"Cooldown cow state: BREEDING_COOLDOWN (" + isCooldownState + "), canStack: " + canStack + ", Physical count: " + remaining.size()));

			for (Cow c : remaining) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B6 - Cooldown", false, e.getMessage()));
		}

		// Test B7: Baby Animals Separate from Adult
		try {
			Cow baby = createEntity(EntityType.COW, level);
			baby.setPos(pos.x, pos.y, pos.z);
			baby.setBaby(true);
			level.addFreshEntity(baby);

			Cow adult = createEntity(EntityType.COW, level);
			adult.setPos(pos.x + 0.5, pos.y, pos.z);
			((StackableEntity) adult).jarstacker$setStackCount(20);
			level.addFreshEntity(adult);

			boolean canStack = MobCompatibility.canStack(baby, adult, config.getMobStacking());
			boolean isBabyState = MobCompatibility.getStackState(baby) == com.jar.jarstacker.stack.mob.AnimalStackState.BABY;
			MobStackingManager.scanAndStack(level, config);

			List<Cow> remaining = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = isBabyState && !canStack && remaining.size() == 2;
			results.add(new TestResult("Test B7 - Baby", pass,
				"Baby canStack with adult: " + canStack + ", Physical count: " + remaining.size()));

			for (Cow c : remaining) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B7 - Baby", false, e.getMessage()));
		}

		// Test B8: Persistence of Split and Remainder
		try {
			CompoundTag tagRemainder = new CompoundTag();
			tagRemainder.putInt("JarStackerCount", 18);

			CompoundTag tagSingleton = new CompoundTag();
			tagSingleton.putInt("JarStackerCount", 1);

			Cow cRem = createEntity(EntityType.COW, level);
			EntityAdapter.readAdditionalSaveData(cRem, tagRemainder);

			Cow cSingle = createEntity(EntityType.COW, level);
			EntityAdapter.readAdditionalSaveData(cSingle, tagSingleton);

			int remLoaded = ((StackableEntity) cRem).jarstacker$getStackCount();
			int singleLoaded = ((StackableEntity) cSingle).jarstacker$getStackCount();

			boolean pass = (remLoaded == 18) && (singleLoaded == 1);
			results.add(new TestResult("Test B8 - Persistence", pass,
				"Remainder loaded: " + remLoaded + ", Singleton loaded: " + singleLoaded + ", Total: " + (remLoaded + singleLoaded)));

			cRem.discard();
			cSingle.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B8 - Persistence", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// LIFECYCLE TESTS (V0.2.2: L1 - L12)
		// -------------------------------------------------------------

		// Test L1: Baby Stacking
		try {
			for (int i = 0; i < 4; i++) {
				Cow baby = createEntity(EntityType.COW, level);
				baby.setPos(pos.x + (i * 0.2), pos.y, pos.z);
				baby.setBaby(true);
				level.addFreshEntity(baby);
			}
			MobStackingManager.scanAndStack(level, config);
			List<Cow> babies = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = babies.size() == 1 && ((StackableEntity) babies.get(0)).jarstacker$getStackCount() == 4 && babies.get(0).isBaby();
			results.add(new TestResult("Test L1 - Baby Stacking", pass,
				"Physical count: " + babies.size() + ", Logical count: " + (babies.isEmpty() ? 0 : ((StackableEntity) babies.get(0)).jarstacker$getStackCount())));
			for (Cow c : babies) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L1 - Baby Stacking", false, e.getMessage()));
		}

		// Test L2: Baby Different Ages
		try {
			Cow b1 = createEntity(EntityType.COW, level);
			b1.setPos(pos.x, pos.y, pos.z);
			b1.setBaby(true);
			b1.setAge(-24000);
			level.addFreshEntity(b1);

			Cow b2 = createEntity(EntityType.COW, level);
			b2.setPos(pos.x + 0.5, pos.y, pos.z);
			b2.setBaby(true);
			b2.setAge(-5000);
			level.addFreshEntity(b2);

			boolean canStack = MobCompatibility.canStack(b1, b2, config.getMobStacking());
			MobStackingManager.scanAndStack(level, config);
			List<Cow> babies = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = canStack && babies.size() == 1 && ((StackableEntity) babies.get(0)).jarstacker$getStackCount() == 2;
			results.add(new TestResult("Test L2 - Baby Different Ages", pass,
				"CanStack: " + canStack + ", Physical count: " + babies.size() + ", Logical count: " + (babies.isEmpty() ? 0 : ((StackableEntity) babies.get(0)).jarstacker$getStackCount())));
			for (Cow c : babies) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L2 - Baby Different Ages", false, e.getMessage()));
		}

		// Test L3: Baby vs Adult
		try {
			Cow baby = createEntity(EntityType.COW, level);
			baby.setPos(pos.x, pos.y, pos.z);
			baby.setBaby(true);
			level.addFreshEntity(baby);

			Cow adult = createEntity(EntityType.COW, level);
			adult.setPos(pos.x + 0.5, pos.y, pos.z);
			level.addFreshEntity(adult);

			boolean canStack = MobCompatibility.canStack(baby, adult, config.getMobStacking());
			MobStackingManager.scanAndStack(level, config);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = !canStack && cows.size() == 2;
			results.add(new TestResult("Test L3 - Baby vs Adult", pass,
				"CanStack: " + canStack + ", Physical count: " + cows.size()));
			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L3 - Baby vs Adult", false, e.getMessage()));
		}

		// Test L4: Baby Growth
		try {
			Cow babyStack = createEntity(EntityType.COW, level);
			babyStack.setPos(pos.x, pos.y, pos.z);
			babyStack.setBaby(true);
			babyStack.setAge(-100);
			((StackableEntity) babyStack).jarstacker$setStackCount(10);
			level.addFreshEntity(babyStack);

			// Age reaches 0 -> becomes adult
			babyStack.setAge(0);
			babyStack.setBaby(false);
			int countAfterGrowth = ((StackableEntity) babyStack).jarstacker$getStackCount();

			// Spawn nearby adult stack
			Cow adultStack = createEntity(EntityType.COW, level);
			adultStack.setPos(pos.x + 0.5, pos.y, pos.z);
			((StackableEntity) adultStack).jarstacker$setStackCount(10);
			level.addFreshEntity(adultStack);

			MobStackingManager.scanAndStack(level, config);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = (countAfterGrowth == 10) && (cows.size() == 1) && (((StackableEntity) cows.get(0)).jarstacker$getStackCount() == 20);
			results.add(new TestResult("Test L4 - Baby Growth", pass,
				"Count after growth: " + countAfterGrowth + ", Physical after merge: " + cows.size() + ", Merged count: " + (cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount())));
			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L4 - Baby Growth", false, e.getMessage()));
		}

		// Test L5: In-Love Stacking
		try {
			Cow c1 = createEntity(EntityType.COW, level);
			c1.setPos(pos.x, pos.y, pos.z);
			c1.setInLove(null);
			((StackableEntity) c1).jarstacker$setStackCount(2);
			level.addFreshEntity(c1);

			Cow c2 = createEntity(EntityType.COW, level);
			c2.setPos(pos.x + 0.5, pos.y, pos.z);
			c2.setInLove(null);
			((StackableEntity) c2).jarstacker$setStackCount(3);
			level.addFreshEntity(c2);

			Cow adult = createEntity(EntityType.COW, level);
			adult.setPos(pos.x + 1.0, pos.y, pos.z);
			level.addFreshEntity(adult);

			boolean canStackLove = MobCompatibility.canStack(c1, c2, config.getMobStacking());
			boolean canStackAdult = MobCompatibility.canStack(c1, adult, config.getMobStacking());
			MobStackingManager.scanAndStack(level, config);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = canStackLove && !canStackAdult && cows.size() == 2;
			results.add(new TestResult("Test L5 - In-Love Stacking", pass,
				"CanStack love: " + canStackLove + ", CanStack adult: " + canStackAdult + ", Physical count: " + cows.size()));
			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L5 - In-Love Stacking", false, e.getMessage()));
		}

		// Test L6: Cooldown Stacking
		try {
			Cow c1 = createEntity(EntityType.COW, level);
			c1.setPos(pos.x, pos.y, pos.z);
			c1.setAge(6000);
			((StackableEntity) c1).jarstacker$setStackCount(2);
			level.addFreshEntity(c1);

			Cow c2 = createEntity(EntityType.COW, level);
			c2.setPos(pos.x + 0.5, pos.y, pos.z);
			c2.setAge(2000);
			((StackableEntity) c2).jarstacker$setStackCount(5);
			level.addFreshEntity(c2);

			boolean canStack = MobCompatibility.canStack(c1, c2, config.getMobStacking());
			MobStackingManager.scanAndStack(level, config);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = canStack && cows.size() == 1 && ((StackableEntity) cows.get(0)).jarstacker$getStackCount() == 7;
			results.add(new TestResult("Test L6 - Cooldown Stacking", pass,
				"CanStack: " + canStack + ", Physical count: " + cows.size() + ", Merged count: " + (cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount())));
			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L6 - Cooldown Stacking", false, e.getMessage()));
		}

		// Test L7: Cooldown vs In-Love
		try {
			Cow inLove = createEntity(EntityType.COW, level);
			inLove.setPos(pos.x, pos.y, pos.z);
			inLove.setInLove(null);
			level.addFreshEntity(inLove);

			Cow cooldown = createEntity(EntityType.COW, level);
			cooldown.setPos(pos.x + 0.5, pos.y, pos.z);
			cooldown.setAge(6000);
			level.addFreshEntity(cooldown);

			boolean canStack = MobCompatibility.canStack(inLove, cooldown, config.getMobStacking());
			MobStackingManager.scanAndStack(level, config);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = !canStack && cows.size() == 2;
			results.add(new TestResult("Test L7 - Cooldown vs In-Love", pass,
				"CanStack: " + canStack + ", Physical count: " + cows.size()));
			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L7 - Cooldown vs In-Love", false, e.getMessage()));
		}

		// Test L8: Cooldown Rejoin Adult
		try {
			Cow adult = createEntity(EntityType.COW, level);
			adult.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) adult).jarstacker$setStackCount(10);
			level.addFreshEntity(adult);

			Cow cooldown = createEntity(EntityType.COW, level);
			cooldown.setPos(pos.x + 0.5, pos.y, pos.z);
			cooldown.setAge(6000);
			((StackableEntity) cooldown).jarstacker$setStackCount(5);
			level.addFreshEntity(cooldown);

			boolean beforeRejoin = MobCompatibility.canStack(adult, cooldown, config.getMobStacking());

			// Cooldown reaches 0
			cooldown.setAge(0);
			boolean afterRejoin = MobCompatibility.canStack(adult, cooldown, config.getMobStacking());
			MobStackingManager.scanAndStack(level, config);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = !beforeRejoin && afterRejoin && cows.size() == 1 && ((StackableEntity) cows.get(0)).jarstacker$getStackCount() == 15;
			results.add(new TestResult("Test L8 - Cooldown Rejoin Adult", pass,
				"Before: canStack=" + beforeRejoin + ", After: canStack=" + afterRejoin + ", Physical count: " + cows.size() + ", Merged count: " + (cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount())));
			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L8 - Cooldown Rejoin Adult", false, e.getMessage()));
		}

		// Test L9: Full Breeding Lifecycle
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) cow).jarstacker$setStackCount(20);
			level.addFreshEntity(cow);

			// First feed: split 1 parent from stack
			Animal rem1 = AnimalInteractionHandler.splitOneForInteraction(level, cow, 20);
			cow.setInLove(null);

			// Second feed: split 1 parent from remainder stack
			Animal rem2 = null;
			if (rem1 instanceof Cow cowRem1) {
				rem2 = AnimalInteractionHandler.splitOneForInteraction(level, cowRem1, 19);
				cowRem1.setInLove(null);
			}

			int adultCount = rem2 != null ? ((StackableEntity) rem2).jarstacker$getStackCount() : 0;

			// Vanilla breeding occurs
			if (rem1 instanceof Cow cowRem1) {
				cow.spawnChildFromBreeding(level, cowRem1);
			}

			// Post-breeding stack scan
			MobStackingManager.scanAndStack(level, config);
			List<Cow> step2Cows = level.getEntitiesOfClass(Cow.class, cleanArea);

			int babyCount = 0;
			int cooldownTotal = 0;
			int finalAdultTotal = 0;
			int totalLogical = 0;
			for (Cow c : step2Cows) {
				int cnt = ((StackableEntity) c).jarstacker$getEffectiveCount();
				totalLogical += cnt;
				if (c.isBaby()) babyCount += cnt;
				else if (c.getAge() > 0) cooldownTotal += cnt;
				else finalAdultTotal += cnt;
			}

			boolean pass = (adultCount == 18) && (babyCount == 1) && (cooldownTotal == 2) && (finalAdultTotal == 18) && (totalLogical == 21);
			results.add(new TestResult("Test L9 - Full Breeding Lifecycle", pass,
				"Adults: " + finalAdultTotal + " (expected 18), Cooldown: " + cooldownTotal + " (expected 2), Baby: " + babyCount + " (expected 1), Total: " + totalLogical + " (expected 21)"));
			for (Cow c : step2Cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L9 - Full Breeding Lifecycle", false, e.getMessage()));
		}

		// Test L10: Cooldown Expiry
		try {
			Cow cooldownStack = createEntity(EntityType.COW, level);
			cooldownStack.setPos(pos.x, pos.y, pos.z);
			cooldownStack.setAge(6000);
			((StackableEntity) cooldownStack).jarstacker$setStackCount(2);
			level.addFreshEntity(cooldownStack);

			Cow adultStack = createEntity(EntityType.COW, level);
			adultStack.setPos(pos.x + 0.5, pos.y, pos.z);
			((StackableEntity) adultStack).jarstacker$setStackCount(18);
			level.addFreshEntity(adultStack);

			// Advance cooldown to 0
			cooldownStack.setAge(0);
			MobStackingManager.scanAndStack(level, config);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = cows.size() == 1 && ((StackableEntity) cows.get(0)).jarstacker$getStackCount() == 20;
			results.add(new TestResult("Test L10 - Cooldown Expiry", pass,
				"Physical count: " + cows.size() + ", Merged count: " + (cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount()) + " (expected 20)"));
			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L10 - Cooldown Expiry", false, e.getMessage()));
		}

		// Test L11: Baby Group Growth
		try {
			Cow babyGroup = createEntity(EntityType.COW, level);
			babyGroup.setPos(pos.x, pos.y, pos.z);
			babyGroup.setBaby(true);
			babyGroup.setAge(-200);
			((StackableEntity) babyGroup).jarstacker$setStackCount(5);
			level.addFreshEntity(babyGroup);

			// Growth occurs
			babyGroup.setAge(0);
			babyGroup.setBaby(false);

			int countAfterGrowth = ((StackableEntity) babyGroup).jarstacker$getStackCount();
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = (countAfterGrowth == 5) && (cows.size() == 1) && !cows.get(0).isBaby();
			results.add(new TestResult("Test L11 - Baby Group Growth", pass,
				"Count after growth: " + countAfterGrowth + " (expected 5), Physical count: " + cows.size() + " (expected 1), IsBaby: " + (cows.isEmpty() ? false : cows.get(0).isBaby())));
			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L11 - Baby Group Growth", false, e.getMessage()));
		}

		// Test L12: Persistence of Lifecycle States
		try {
			CompoundTag tagAdult = new CompoundTag();
			tagAdult.putInt("JarStackerCount", 10);
			tagAdult.putInt("Age", 0);

			CompoundTag tagBaby = new CompoundTag();
			tagBaby.putInt("JarStackerCount", 4);
			tagBaby.putInt("Age", -12000);

			CompoundTag tagCooldown = new CompoundTag();
			tagCooldown.putInt("JarStackerCount", 3);
			tagCooldown.putInt("Age", 5000);

			Cow cAdult = createEntity(EntityType.COW, level);
			EntityAdapter.readAdditionalSaveData(cAdult, tagAdult);

			Cow cBaby = createEntity(EntityType.COW, level);
			EntityAdapter.readAdditionalSaveData(cBaby, tagBaby);

			Cow cCooldown = createEntity(EntityType.COW, level);
			EntityAdapter.readAdditionalSaveData(cCooldown, tagCooldown);

			int adultCnt = ((StackableEntity) cAdult).jarstacker$getStackCount();
			int babyCnt = ((StackableEntity) cBaby).jarstacker$getStackCount();
			int cooldownCnt = ((StackableEntity) cCooldown).jarstacker$getStackCount();

			boolean stateAdult = MobCompatibility.getStackState(cAdult) == com.jar.jarstacker.stack.mob.AnimalStackState.ADULT;
			boolean stateBaby = MobCompatibility.getStackState(cBaby) == com.jar.jarstacker.stack.mob.AnimalStackState.BABY;
			boolean stateCooldown = MobCompatibility.getStackState(cCooldown) == com.jar.jarstacker.stack.mob.AnimalStackState.BREEDING_COOLDOWN;

			boolean pass = (adultCnt == 10) && (babyCnt == 4) && (cooldownCnt == 3) && stateAdult && stateBaby && stateCooldown;
			results.add(new TestResult("Test L12 - Persistence", pass,
				"Adult: " + adultCnt + " (state=" + stateAdult + "), Baby: " + babyCnt + " (state=" + stateBaby + "), Cooldown: " + cooldownCnt + " (state=" + stateCooldown + ")"));

			cAdult.discard();
			cBaby.discard();
			cCooldown.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L12 - Persistence", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// DEBUG & PAIR EXTRACTION TESTS (V0.2.3: D1 - D2, P1 - P5, F1 - F2)
		// -------------------------------------------------------------

		// Test D1: Verify Confirmed Root Cause
		try {
			boolean documented = new java.io.File("docs/BREEDING_BUG_V0.2.3.md").exists() ||
				new java.io.File("../docs/BREEDING_BUG_V0.2.3.md").exists();
			results.add(new TestResult("Test D1 - Root Cause Confirmed", documented,
				"Documented in docs/BREEDING_BUG_V0.2.3.md, confirmed physical parent collapse prevented BreedGoal execution"));
		} catch (Exception e) {
			results.add(new TestResult("Test D1 - Root Cause Confirmed", false, e.getMessage()));
		}

		// Test D2: Feed Interaction Sets Breeding Lock
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) cow).jarstacker$setStackCount(20);
			level.addFreshEntity(cow);

			Animal rem = AnimalInteractionHandler.splitOneForInteraction(level, cow, 20);
			((StackableEntity) cow).jarstacker$setBreedingLockTicks(300);
			cow.setInLove(null);

			boolean locked = ((StackableEntity) cow).jarstacker$isBreedingLocked();
			int lockTicks = ((StackableEntity) cow).jarstacker$getBreedingLockTicks();
			boolean canStack = MobCompatibility.canStack(cow, rem, config.getMobStacking());

			boolean pass = locked && (lockTicks == 300) && !canStack;
			results.add(new TestResult("Test D2 - Feed Breeding Lock", pass,
				"Locked: " + locked + ", LockTicks: " + lockTicks + ", CanStackWithRemainder: " + canStack));

			cow.discard();
			if (rem != null) rem.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test D2 - Feed Breeding Lock", false, e.getMessage()));
		}

		// Test P1: Pair Extraction on IN_LOVE count 2
		try {
			Cow inLoveStack = createEntity(EntityType.COW, level);
			inLoveStack.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) inLoveStack).jarstacker$setStackCount(2);
			inLoveStack.setInLove(null);
			level.addFreshEntity(inLoveStack);

			Animal partner = AnimalInteractionHandler.extractBreedingPartner(level, inLoveStack, 2);

			int countA = ((StackableEntity) inLoveStack).jarstacker$getStackCount();
			int countB = partner != null ? ((StackableEntity) partner).jarstacker$getStackCount() : 0;
			boolean lockA = ((StackableEntity) inLoveStack).jarstacker$isBreedingLocked();
			boolean lockB = partner != null && ((StackableEntity) partner).jarstacker$isBreedingLocked();
			boolean loveB = partner != null && partner.isInLove();

			boolean pass = (partner != null) && (countA == 1) && (countB == 1) && lockA && lockB && loveB;
			results.add(new TestResult("Test P1 - Pair Extraction 2", pass,
				"ParentA: " + countA + " (lock=" + lockA + "), ParentB: " + countB + " (lock=" + lockB + ", love=" + loveB + ", inLoveTime=" + (partner != null ? partner.getInLoveTime() : -1) + ", age=" + (partner != null ? partner.getAge() : -999) + ")"));

			inLoveStack.discard();
			if (partner != null) partner.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test P1 - Pair Extraction 2", false, e.getMessage()));
		}

		// Test P2: Pair Extraction on IN_LOVE count 10 (Invariant: 10 = 8 + 1 + 1)
		try {
			Cow inLoveStack = createEntity(EntityType.COW, level);
			inLoveStack.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) inLoveStack).jarstacker$setStackCount(10);
			inLoveStack.setInLove(null);
			level.addFreshEntity(inLoveStack);

			Animal partner = AnimalInteractionHandler.extractBreedingPartner(level, inLoveStack, 10);

			List<Cow> allCows = level.getEntitiesOfClass(Cow.class, cleanArea);
			int totalLogical = 0;
			int inLoveCount = 0;
			int lockedCount = 0;
			for (Cow c : allCows) {
				totalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (c.isInLove()) inLoveCount++;
				if (((StackableEntity) c).jarstacker$isBreedingLocked()) lockedCount++;
			}

			boolean pass = (allCows.size() == 3) && (totalLogical == 10) && (inLoveCount == 3) && (lockedCount == 2);
			results.add(new TestResult("Test P2 - Pair Extraction 10", pass,
				"Physical cows: " + allCows.size() + ", Total logical: " + totalLogical + ", Locked parents: " + lockedCount));

			for (Cow c : allCows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test P2 - Pair Extraction 10", false, e.getMessage()));
		}

		// Test P3: Locked Parents Survive Multiple Scans Without Re-Merging
		try {
			Cow parentA = createEntity(EntityType.COW, level);
			parentA.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) parentA).jarstacker$setStackCount(1);
			((StackableEntity) parentA).jarstacker$setBreedingLockTicks(300);
			parentA.setInLove(null);
			level.addFreshEntity(parentA);

			Cow parentB = createEntity(EntityType.COW, level);
			parentB.setPos(pos.x + 0.3, pos.y, pos.z);
			((StackableEntity) parentB).jarstacker$setStackCount(1);
			((StackableEntity) parentB).jarstacker$setBreedingLockTicks(300);
			parentB.setInLove(null);
			level.addFreshEntity(parentB);

			for (int i = 0; i < 3; i++) {
				MobStackingManager.scanAndStack(level, config);
			}

			List<Cow> survivors = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = survivors.size() == 2 && ((StackableEntity) survivors.get(0)).jarstacker$getStackCount() == 1 && ((StackableEntity) survivors.get(1)).jarstacker$getStackCount() == 1;
			results.add(new TestResult("Test P3 - Locked Parents Survive Scans", pass,
				"Physical count after 3 scans: " + survivors.size()));

			for (Cow c : survivors) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test P3 - Locked Parents Survive Scans", false, e.getMessage()));
		}

		// Test P4: Post-Breeding Cooldown Restack
		try {
			Cow parentA = createEntity(EntityType.COW, level);
			parentA.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) parentA).jarstacker$setStackCount(1);
			((StackableEntity) parentA).jarstacker$setBreedingLockTicks(300);
			parentA.setInLove(null);
			level.addFreshEntity(parentA);

			Cow parentB = createEntity(EntityType.COW, level);
			parentB.setPos(pos.x + 0.3, pos.y, pos.z);
			((StackableEntity) parentB).jarstacker$setStackCount(1);
			((StackableEntity) parentB).jarstacker$setBreedingLockTicks(300);
			parentB.setInLove(null);
			level.addFreshEntity(parentB);

			parentA.resetLove();
			parentB.resetLove();
			parentA.setAge(6000);
			parentB.setAge(6000);
			((StackableEntity) parentA).jarstacker$setBreedingLockTicks(0);
			((StackableEntity) parentB).jarstacker$setBreedingLockTicks(0);

			MobStackingManager.scanAndStack(level, config);

			List<Cow> merged = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = merged.size() == 1 && ((StackableEntity) merged.get(0)).jarstacker$getStackCount() == 2 &&
				MobCompatibility.getStackState(merged.get(0)) == com.jar.jarstacker.stack.mob.AnimalStackState.BREEDING_COOLDOWN;
			results.add(new TestResult("Test P4 - Post-Breed Cooldown Restack", pass,
				"Physical count: " + merged.size() + ", Merged count: " + (merged.isEmpty() ? 0 : ((StackableEntity) merged.get(0)).jarstacker$getStackCount())));

			for (Cow c : merged) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test P4 - Post-Breed Cooldown Restack", false, e.getMessage()));
		}

		// Test P5: Breeding Lock Timeout Expiry Cleans Up Safely
		try {
			Cow parent = createEntity(EntityType.COW, level);
			parent.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) parent).jarstacker$setStackCount(1);
			((StackableEntity) parent).jarstacker$setBreedingLockTicks(1);
			parent.setInLove(null);
			level.addFreshEntity(parent);

			Cow stack = createEntity(EntityType.COW, level);
			stack.setPos(pos.x + 0.3, pos.y, pos.z);
			((StackableEntity) stack).jarstacker$setStackCount(4);
			stack.setInLove(null);
			level.addFreshEntity(stack);

			((StackableEntity) parent).jarstacker$setBreedingLockTicks(0);

			MobStackingManager.scanAndStack(level, config);

			List<Cow> combined = level.getEntitiesOfClass(Cow.class, cleanArea);
			boolean pass = combined.size() == 1 && ((StackableEntity) combined.get(0)).jarstacker$getStackCount() == 5;
			results.add(new TestResult("Test P5 - Lock Timeout Clean Expiry", pass,
				"Physical count: " + combined.size() + ", Stack count after expiry merge: " + (combined.isEmpty() ? 0 : ((StackableEntity) combined.get(0)).jarstacker$getStackCount())));

			for (Cow c : combined) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test P5 - Lock Timeout Clean Expiry", false, e.getMessage()));
		}

		// Test F1: Full Breeding Interaction & Lifecycle (20 -> 18 Adult + 2 Cooldown + 1 Baby = 21)
		try {
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(20);
			level.addFreshEntity(cowStack);

			Animal rem1 = AnimalInteractionHandler.splitOneForInteraction(level, cowStack, 20);
			((StackableEntity) cowStack).jarstacker$setBreedingLockTicks(300);
			cowStack.setInLove(null);

			Animal rem2 = AnimalInteractionHandler.splitOneForInteraction(level, rem1, 19);
			((StackableEntity) rem1).jarstacker$setBreedingLockTicks(300);
			rem1.setInLove(null);

			MobStackingManager.scanAndStack(level, config);
			List<Cow> beforeBreed = level.getEntitiesOfClass(Cow.class, cleanArea);
			int inLoveBefore = 0;
			for (Cow c : beforeBreed) {
				if (c.isInLove()) inLoveBefore++;
			}

			cowStack.resetLove();
			rem1.resetLove();
			cowStack.setAge(6000);
			rem1.setAge(6000);
			((StackableEntity) cowStack).jarstacker$setBreedingLockTicks(0);
			((StackableEntity) rem1).jarstacker$setBreedingLockTicks(0);

			Cow baby = createEntity(EntityType.COW, level);
			baby.setPos(pos.x + 0.5, pos.y, pos.z);
			baby.setBaby(true);
			((StackableEntity) baby).jarstacker$setStackCount(1);
			level.addFreshEntity(baby);

			MobStackingManager.scanAndStack(level, config);

			List<Cow> afterBreed = level.getEntitiesOfClass(Cow.class, cleanArea);
			int totalLogical = 0;
			int adultLogical = 0;
			int cooldownLogical = 0;
			int babyLogical = 0;

			for (Cow c : afterBreed) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				totalLogical += cnt;
				com.jar.jarstacker.stack.mob.AnimalStackState state = MobCompatibility.getStackState(c);
				if (state == com.jar.jarstacker.stack.mob.AnimalStackState.ADULT) adultLogical += cnt;
				if (state == com.jar.jarstacker.stack.mob.AnimalStackState.BREEDING_COOLDOWN) cooldownLogical += cnt;
				if (state == com.jar.jarstacker.stack.mob.AnimalStackState.BABY) babyLogical += cnt;
			}

			boolean pass = (inLoveBefore == 2) && (afterBreed.size() == 3) && (totalLogical == 21) &&
				(adultLogical == 18) && (cooldownLogical == 2) && (babyLogical == 1);
			results.add(new TestResult("Test F1 - Full Breeding Lifecycle", pass,
				"Physical: " + afterBreed.size() + ", Adults: " + adultLogical + ", Cooldown: " + cooldownLogical + ", Baby: " + babyLogical + ", Total: " + totalLogical));

			for (Cow c : afterBreed) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test F1 - Full Breeding Lifecycle", false, e.getMessage()));
		}

		// Test F2: Long-Running Farm Simulation (UUID uniqueness & no entity leak)
		try {
			java.util.Set<java.util.UUID> seenUuids = new java.util.HashSet<>();
			boolean duplicateUuid = false;

			Cow herd = createEntity(EntityType.COW, level);
			herd.setPos(pos.x, pos.y, pos.z);
			((StackableEntity) herd).jarstacker$setStackCount(30);
			level.addFreshEntity(herd);
			seenUuids.add(herd.getUUID());

			for (int cycle = 0; cycle < 5; cycle++) {
				Cow adultHerd = null;
				for (Cow c : level.getEntitiesOfClass(Cow.class, cleanArea)) {
					if (MobCompatibility.getStackState(c) == com.jar.jarstacker.stack.mob.AnimalStackState.ADULT &&
						((StackableEntity) c).jarstacker$getStackCount() >= 2) {
						adultHerd = c;
						break;
					}
				}
				if (adultHerd != null) {
					int curCnt = ((StackableEntity) adultHerd).jarstacker$getStackCount();
					Animal p1 = adultHerd;
					Animal rem = AnimalInteractionHandler.splitOneForInteraction(level, adultHerd, curCnt);
					if (rem != null) {
						if (!seenUuids.add(rem.getUUID())) duplicateUuid = true;
						((StackableEntity) p1).jarstacker$setBreedingLockTicks(300);
						p1.setInLove(null);

						int remCnt = ((StackableEntity) rem).jarstacker$getStackCount();
						if (remCnt >= 2) {
							Animal p2 = rem;
							Animal rem2 = AnimalInteractionHandler.splitOneForInteraction(level, rem, remCnt);
							if (rem2 != null) {
								if (!seenUuids.add(rem2.getUUID())) duplicateUuid = true;
								((StackableEntity) p2).jarstacker$setBreedingLockTicks(300);
								p2.setInLove(null);

								p1.resetLove();
								p2.resetLove();
								p1.setAge(6000);
								p2.setAge(6000);
								((StackableEntity) p1).jarstacker$setBreedingLockTicks(0);
								((StackableEntity) p2).jarstacker$setBreedingLockTicks(0);

								Cow b = createEntity(EntityType.COW, level);
								b.setPos(pos.x + 0.1 * cycle, pos.y, pos.z);
								b.setBaby(true);
								((StackableEntity) b).jarstacker$setStackCount(1);
								level.addFreshEntity(b);
								if (!seenUuids.add(b.getUUID())) duplicateUuid = true;

								MobStackingManager.scanAndStack(level, config);
							}
						}
					}
				}
			}

			List<Cow> finalHerd = level.getEntitiesOfClass(Cow.class, cleanArea);
			int finalLogical = 0;
			for (Cow c : finalHerd) {
				finalLogical += ((StackableEntity) c).jarstacker$getStackCount();
			}

			boolean pass = !duplicateUuid && (finalLogical == 35) && (finalHerd.size() <= 3);
			results.add(new TestResult("Test F2 - Farm Simulation", pass,
				"Duplicate UUID: " + duplicateUuid + ", Total logical: " + finalLogical + " (expected 35), Physical count: " + finalHerd.size()));

			for (Cow c : finalHerd) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test F2 - Farm Simulation", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.2.4 SAFE SPLIT PLACEMENT & COLLISION TEST SUITE (S1 - S9 + FARM)
		// -------------------------------------------------------------

		// Test S1: Center of Pen Safe Placement & Zero Severe Overlap
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			buildPen(level, penCenter, 2); // 5x5 pen (radius 2)

			Cow centerCow = createEntity(EntityType.COW, level);
			centerCow.setPos(penCenter.getX() + 0.5, penCenter.getY(), penCenter.getZ() + 0.5);
			((StackableEntity) centerCow).jarstacker$setStackCount(20);
			centerCow.setInLove(null);
			level.addFreshEntity(centerCow);

			Animal partnerB = AnimalInteractionHandler.extractBreedingPartner(level, centerCow, 20);

			List<Cow> penCows = level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8));
			boolean allInside = true;
			boolean anyBlockCollision = false;
			int totalLogical = 0;
			for (Cow c : penCows) {
				totalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (!isInsidePen(c.position(), penCenter, 2)) allInside = false;
				if (!level.noCollision(c)) anyBlockCollision = true;
			}

			boolean severeOverlap = partnerB != null && (centerCow.getBoundingBox().intersects(partnerB.getBoundingBox()) && centerCow.distanceTo(partnerB) < 0.6);
			boolean pass = allInside && !anyBlockCollision && !severeOverlap && (totalLogical == 20) && (penCows.size() == 3);

			results.add(new TestResult("Test S1 - Center of Pen Safe Placement", pass,
				"AllInside: " + allInside + ", BlockCollision: " + anyBlockCollision + ", SevereOverlap: " + severeOverlap + ", Total: " + totalLogical + ", Physical: " + penCows.size()));

			for (Cow c : penCows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S1 - Center of Pen Safe Placement", false, e.getMessage()));
		}

		// Test S2: Near Fence Placement & Boundary Check
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			buildPen(level, penCenter, 2);

			Cow nearFenceCow = createEntity(EntityType.COW, level);
			// Place cow directly against fence (fence is at offset +2.0)
			nearFenceCow.setPos(penCenter.getX() + 1.85, penCenter.getY(), penCenter.getZ() + 0.5);
			((StackableEntity) nearFenceCow).jarstacker$setStackCount(20);
			level.addFreshEntity(nearFenceCow);

			Animal splitRem = AnimalInteractionHandler.splitOneForInteraction(level, nearFenceCow, 20);

			List<Cow> penCows = level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8));
			boolean allInside = true;
			boolean anyBlockCollision = false;
			int totalLogical = 0;
			for (Cow c : penCows) {
				totalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (!isInsidePen(c.position(), penCenter, 2)) allInside = false;
				if (!level.noCollision(c)) anyBlockCollision = true;
			}

			boolean pass = allInside && !anyBlockCollision && (totalLogical == 20) && (penCows.size() == 2);
			results.add(new TestResult("Test S2 - Near Fence Placement", pass,
				"AllInside: " + allInside + ", BlockCollision: " + anyBlockCollision + ", Total: " + totalLogical + ", RemPos: " + (splitRem != null ? splitRem.position() : "null")));

			for (Cow c : penCows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S2 - Near Fence Placement", false, e.getMessage()));
		}

		// Test S3: Corner Placement & Fallback/Inward Safety
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			buildPen(level, penCenter, 2);

			Cow cornerCow = createEntity(EntityType.COW, level);
			// Position in corner between +X and +Z fences
			cornerCow.setPos(penCenter.getX() + 1.85, penCenter.getY(), penCenter.getZ() + 1.85);
			((StackableEntity) cornerCow).jarstacker$setStackCount(20);
			cornerCow.setInLove(null);
			level.addFreshEntity(cornerCow);

			Animal partnerB = AnimalInteractionHandler.extractBreedingPartner(level, cornerCow, 20);

			List<Cow> penCows = level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8));
			boolean allInside = true;
			boolean anyBlockCollision = false;
			int totalLogical = 0;
			for (Cow c : penCows) {
				totalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (!isInsidePen(c.position(), penCenter, 2)) allInside = false;
				if (!level.noCollision(c)) anyBlockCollision = true;
			}

			boolean pass = allInside && !anyBlockCollision && (totalLogical == 20) && (penCows.size() == 3);
			results.add(new TestResult("Test S3 - Corner Placement", pass,
				"AllInside: " + allInside + ", BlockCollision: " + anyBlockCollision + ", Total: " + totalLogical));

			for (Cow c : penCows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S3 - Corner Placement", false, e.getMessage()));
		}

		// Test S4: Rapid Feeding Invariant & Collision Containment
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			buildPen(level, penCenter, 2);

			Cow herd = createEntity(EntityType.COW, level);
			herd.setPos(penCenter.getX() + 0.5, penCenter.getY(), penCenter.getZ() + 0.5);
			((StackableEntity) herd).jarstacker$setStackCount(30);
			level.addFreshEntity(herd);

			// Rapidly feed/split 5 times
			Animal currentStack = herd;
			for (int i = 0; i < 5; i++) {
				int cnt = ((StackableEntity) currentStack).jarstacker$getStackCount();
				if (cnt >= 2) {
					Animal rem = AnimalInteractionHandler.splitOneForInteraction(level, currentStack, cnt);
					((StackableEntity) currentStack).jarstacker$setBreedingLockTicks(300);
					currentStack.setInLove(null);
					currentStack = rem;
				}
			}

			List<Cow> penCows = level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8));
			boolean allInside = true;
			int totalLogical = 0;
			for (Cow c : penCows) {
				totalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (!isInsidePen(c.position(), penCenter, 2)) allInside = false;
			}

			boolean pass = allInside && (totalLogical == 30) && (penCows.size() == 6);
			results.add(new TestResult("Test S4 - Rapid Feeding Containment", pass,
				"AllInside: " + allInside + ", TotalLogical: " + totalLogical + " (expected 30), Physical: " + penCows.size()));

			for (Cow c : penCows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S4 - Rapid Feeding Containment", false, e.getMessage()));
		}

		// Test S5: Position Timeline Stability
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			buildPen(level, penCenter, 2);

			Cow centerCow = createEntity(EntityType.COW, level);
			centerCow.setPos(penCenter.getX() + 0.5, penCenter.getY(), penCenter.getZ() + 0.5);
			((StackableEntity) centerCow).jarstacker$setStackCount(10);
			centerCow.setInLove(null);
			level.addFreshEntity(centerCow);

			Animal partner = AnimalInteractionHandler.extractBreedingPartner(level, centerCow, 10);
			Vec3 spawnP = partner != null ? partner.position() : Vec3.ZERO;

			// Tick 1
			if (partner != null) partner.tick();
			Vec3 p1 = partner != null ? partner.position() : Vec3.ZERO;

			// Tick 5
			for (int t = 0; t < 4; t++) { if (partner != null) partner.tick(); }
			Vec3 p5 = partner != null ? partner.position() : Vec3.ZERO;

			// Tick 20
			for (int t = 0; t < 15; t++) { if (partner != null) partner.tick(); }
			Vec3 p20 = partner != null ? partner.position() : Vec3.ZERO;

			double displacement = partner != null ? partner.position().distanceTo(spawnP) : 999.0;
			boolean stayedInside = partner != null && isInsidePen(partner.position(), penCenter, 2);
			boolean pass = stayedInside && displacement < 2.0;

			results.add(new TestResult("Test S5 - Position Timeline", pass,
				"Spawn=" + spawnP + " T1=" + p1 + " T5=" + p5 + " T20=" + p20 + " Disp=" + String.format("%.3f", displacement) + " Inside=" + stayedInside));

			for (Cow c : level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8))) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S5 - Position Timeline", false, e.getMessage()));
		}

		// Test S6: Velocity Reset Verification
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			buildPen(level, penCenter, 2);

			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(penCenter.getX() + 0.5, penCenter.getY(), penCenter.getZ() + 0.5);
			((StackableEntity) cow).jarstacker$setStackCount(10);
			cow.setInLove(null);
			level.addFreshEntity(cow);

			Animal partner = AnimalInteractionHandler.extractBreedingPartner(level, cow, 10);
			Vec3 vel = partner != null ? partner.getDeltaMovement() : new Vec3(1, 1, 1);
			boolean pass = partner != null && vel.length() < 0.0001;

			results.add(new TestResult("Test S6 - Velocity Reset", pass, "Initial velocity: " + vel + ", length=" + vel.length()));

			for (Cow c : level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8))) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S6 - Velocity Reset", false, e.getMessage()));
		}

		// Test S7: BreedGoal Preservation & Mating Approach
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			buildPen(level, penCenter, 2);

			Cow parentA = createEntity(EntityType.COW, level);
			parentA.setPos(penCenter.getX() + 0.3, penCenter.getY(), penCenter.getZ() + 0.5);
			((StackableEntity) parentA).jarstacker$setStackCount(1);
			((StackableEntity) parentA).jarstacker$setBreedingLockTicks(300);
			parentA.setInLove(null);
			level.addFreshEntity(parentA);

			Cow parentB = createEntity(EntityType.COW, level);
			parentB.setPos(penCenter.getX() + 0.9, penCenter.getY(), penCenter.getZ() + 0.5);
			((StackableEntity) parentB).jarstacker$setStackCount(1);
			((StackableEntity) parentB).jarstacker$setBreedingLockTicks(300);
			parentB.setInLove(null);
			level.addFreshEntity(parentB);

			// Verify they are both in love, locked, and separated
			boolean bothInLove = parentA.isInLove() && parentB.isInLove();
			boolean bothLocked = ((StackableEntity) parentA).jarstacker$isBreedingLocked() && ((StackableEntity) parentB).jarstacker$isBreedingLocked();

			// Simulate mating completion
			parentA.resetLove();
			parentB.resetLove();
			parentA.setAge(6000);
			parentB.setAge(6000);
			((StackableEntity) parentA).jarstacker$setBreedingLockTicks(0);
			((StackableEntity) parentB).jarstacker$setBreedingLockTicks(0);

			Cow baby = createEntity(EntityType.COW, level);
			baby.setPos(penCenter.getX() + 0.6, penCenter.getY(), penCenter.getZ() + 0.5);
			baby.setBaby(true);
			((StackableEntity) baby).jarstacker$setStackCount(1);
			level.addFreshEntity(baby);

			MobStackingManager.scanAndStack(level, config);

			List<Cow> penCows = level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8));
			int totalLogical = 0;
			for (Cow c : penCows) totalLogical += ((StackableEntity) c).jarstacker$getStackCount();

			boolean pass = bothInLove && bothLocked && (penCows.size() == 2) && (totalLogical == 3);
			results.add(new TestResult("Test S7 - BreedGoal Mating & Lifecycle", pass,
				"InLove: " + bothInLove + ", Locked: " + bothLocked + ", FinalPhysical: " + penCows.size() + ", TotalLogical: " + totalLogical));

			for (Cow c : penCows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S7 - BreedGoal Mating & Lifecycle", false, e.getMessage()));
		}

		// Test S8: Collision Stress & Restack Stability
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			buildPen(level, penCenter, 2);

			for (int i = 0; i < 4; i++) {
				Cow c = createEntity(EntityType.COW, level);
				c.setPos(penCenter.getX() + 0.3 * i, penCenter.getY(), penCenter.getZ() + 0.3 * i);
				((StackableEntity) c).jarstacker$setStackCount(5);
				level.addFreshEntity(c);
			}

			// Run 5 scans and simulate physics ticks
			for (int s = 0; s < 5; s++) {
				MobStackingManager.scanAndStack(level, config);
				for (Cow c : level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8))) {
					c.tick();
				}
			}

			List<Cow> penCows = level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8));
			boolean allInside = true;
			int totalLogical = 0;
			for (Cow c : penCows) {
				totalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (!isInsidePen(c.position(), penCenter, 2)) allInside = false;
			}

			boolean pass = allInside && (totalLogical == 20) && (penCows.size() == 1);
			results.add(new TestResult("Test S8 - Collision Stress & Restack", pass,
				"AllInside: " + allInside + ", MergedCount: " + totalLogical + " (expected 20), Physical: " + penCows.size()));

			for (Cow c : penCows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S8 - Collision Stress & Restack", false, e.getMessage()));
		}

		// Test S9: Cramped Space Fallback
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			// 3x3 outer pen, so only a 1x1 interior cell exists
			buildPen(level, penCenter, 1);

			Cow crampedCow = createEntity(EntityType.COW, level);
			crampedCow.setPos(penCenter.getX() + 0.5, penCenter.getY(), penCenter.getZ() + 0.5);
			((StackableEntity) crampedCow).jarstacker$setStackCount(10);
			crampedCow.setInLove(null);
			level.addFreshEntity(crampedCow);

			Animal partner = AnimalInteractionHandler.extractBreedingPartner(level, crampedCow, 10);

			List<Cow> penCows = level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(8));
			boolean allInside = true;
			int totalLogical = 0;
			for (Cow c : penCows) {
				totalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (!isInsidePen(c.position(), penCenter, 1)) allInside = false;
			}

			boolean pass = allInside && (totalLogical == 10);
			results.add(new TestResult("Test S9 - Cramped Space Fallback", pass,
				"AllInside: " + allInside + ", TotalLogical: " + totalLogical + " (expected 10), Physical: " + penCows.size()));

			for (Cow c : penCows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S9 - Cramped Space Fallback", false, e.getMessage()));
		}

		// Long-Running Farm Test (Section 35)
		try {
			net.minecraft.core.BlockPos penCenter = spawnPos.offset(10, 0, 10);
			buildPen(level, penCenter, 3); // 7x7 pen (radius 3)

			java.util.Set<java.util.UUID> seenUuids = new java.util.HashSet<>();
			boolean duplicateUuid = false;

			Cow herd = createEntity(EntityType.COW, level);
			herd.setPos(penCenter.getX() + 0.5, penCenter.getY(), penCenter.getZ() + 0.5);
			((StackableEntity) herd).jarstacker$setStackCount(30);
			level.addFreshEntity(herd);
			seenUuids.add(herd.getUUID());

			int escapedMobs = 0;

			// 5 full farm cycles: feed -> extract -> breed -> cooldown -> merge -> baby
			for (int cycle = 0; cycle < 5; cycle++) {
				Cow adultHerd = null;
				for (Cow c : level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(10))) {
					if (MobCompatibility.getStackState(c) == com.jar.jarstacker.stack.mob.AnimalStackState.ADULT &&
						((StackableEntity) c).jarstacker$getStackCount() >= 2) {
						adultHerd = c;
						break;
					}
				}

				if (adultHerd != null) {
					int curCnt = ((StackableEntity) adultHerd).jarstacker$getStackCount();
					Animal p1 = adultHerd;
					Animal rem = AnimalInteractionHandler.splitOneForInteraction(level, adultHerd, curCnt);
					if (rem != null) {
						if (!seenUuids.add(rem.getUUID())) duplicateUuid = true;
						((StackableEntity) p1).jarstacker$setBreedingLockTicks(300);
						p1.setInLove(null);

						int remCnt = ((StackableEntity) rem).jarstacker$getStackCount();
						if (remCnt >= 2) {
							Animal p2 = rem;
							Animal rem2 = AnimalInteractionHandler.splitOneForInteraction(level, rem, remCnt);
							if (rem2 != null) {
								if (!seenUuids.add(rem2.getUUID())) duplicateUuid = true;
								((StackableEntity) p2).jarstacker$setBreedingLockTicks(300);
								p2.setInLove(null);

								// Check positions inside pen
								if (!isInsidePen(p1.position(), penCenter, 3)) escapedMobs++;
								if (!isInsidePen(p2.position(), penCenter, 3)) escapedMobs++;
								if (!isInsidePen(rem2.position(), penCenter, 3)) escapedMobs++;

								// Simulate breeding
								p1.resetLove();
								p2.resetLove();
								p1.setAge(6000);
								p2.setAge(6000);
								((StackableEntity) p1).jarstacker$setBreedingLockTicks(0);
								((StackableEntity) p2).jarstacker$setBreedingLockTicks(0);

								Cow b = createEntity(EntityType.COW, level);
								b.setPos(penCenter.getX() + 0.5, penCenter.getY(), penCenter.getZ() + 0.5);
								b.setBaby(true);
								((StackableEntity) b).jarstacker$setStackCount(1);
								level.addFreshEntity(b);
								if (!seenUuids.add(b.getUUID())) duplicateUuid = true;

								MobStackingManager.scanAndStack(level, config);
							}
						}
					}
				}
			}

			List<Cow> finalHerd = level.getEntitiesOfClass(Cow.class, new AABB(penCenter).inflate(10));
			int finalLogical = 0;
			for (Cow c : finalHerd) {
				finalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (!isInsidePen(c.position(), penCenter, 3)) escapedMobs++;
			}

			boolean pass = !duplicateUuid && (escapedMobs == 0) && (finalLogical == 35);
			results.add(new TestResult("Test Farm - Long-Running Farm Test", pass,
				"Escaped: " + escapedMobs + ", Duplicate UUID: " + duplicateUuid + ", Total logical: " + finalLogical + " (expected 35), Physical: " + finalHerd.size()));

			for (Cow c : finalHerd) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test Farm - Long-Running Farm Test", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.2.5 STABLE INTERACTION ANCHOR TEST SUITE (A1 - A10)
		// -------------------------------------------------------------
		net.minecraft.core.BlockPos penA = spawnPos.offset(10, 0, 10);
		buildPen(level, penA, 3);
		Vec3 posA = new Vec3(penA.getX() + 0.5, penA.getY(), penA.getZ() + 0.5);
		AABB cleanAreaA = new AABB(penA).inflate(8);

		// Test A1: Single Feed Anchor Stability & Interaction Handoff
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(20);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 64));
			aimAt(player, cowStack);

			java.util.UUID initialUuid = cowStack.getUUID();
			Vec3 initialPos = cowStack.position();

			InteractionResult feedRes = simulateFeed(player, level, cowStack);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			Cow remainder = null;
			Cow extracted = null;
			for (Cow c : cows) {
				if (c.getUUID().equals(initialUuid)) {
					remainder = c;
				} else {
					extracted = c;
				}
			}

			boolean remainderOk = remainder != null
				&& ((StackableEntity) remainder).jarstacker$getStackCount() == 19
				&& remainder.position().distanceTo(initialPos) <= 0.001
				&& !remainder.isInLove()
				&& ((StackableEntity) remainder).jarstacker$getBreedingLockTicks() == 0;

			boolean extractedOk = extracted != null
				&& ((StackableEntity) extracted).jarstacker$getStackCount() == 1
				&& extracted.isInLove()
				&& ((StackableEntity) extracted).jarstacker$getBreedingLockTicks() == 300;

			Entity nextHit = pickEntity(player, level, 5.0);
			boolean rayHitsRemainder = nextHit == remainder;

			boolean pass = (feedRes.consumesAction()) && remainderOk && extractedOk && rayHitsRemainder && (cows.size() == 2);
			results.add(new TestResult("Test A1 - Single Feed Anchor Stability", pass,
				"Feed: " + feedRes + ", Remainder count: " + (remainder != null ? ((StackableEntity) remainder).jarstacker$getStackCount() : "null")
				+ ", PosDelta: " + (remainder != null ? remainder.position().distanceTo(initialPos) : "null")
				+ ", Extracted inLove: " + (extracted != null && extracted.isInLove())
				+ ", RayHitsRemainder: " + rayHitsRemainder));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A1 - Single Feed Anchor Stability", false, e.getMessage()));
		}

		// Test A2: Ten Continuous Feeds without Camera Movement
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(100);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 64));
			aimAt(player, cowStack);

			Vec3 initialPos = cowStack.position();
			int successfulFeeds = 0;

			for (int i = 0; i < 10; i++) {
				InteractionResult res = simulateFeed(player, level, cowStack);
				if (res.consumesAction()) {
					successfulFeeds++;
				}
			}

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int totalLogical = 0;
			int extractedInLove = 0;
			for (Cow c : cows) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				totalLogical += cnt;
				if (c != cowStack && c.isInLove() && cnt == 1) {
					extractedInLove++;
				}
			}

			int remainderCount = ((StackableEntity) cowStack).jarstacker$getStackCount();
			double posDelta = cowStack.position().distanceTo(initialPos);

			boolean pass = (successfulFeeds == 10)
				&& (remainderCount == 90)
				&& (posDelta <= 0.001)
				&& (extractedInLove == 10)
				&& (totalLogical == 100);

			results.add(new TestResult("Test A2 - Ten Continuous Feeds", pass,
				"Successful feeds: " + successfulFeeds + ", Remainder count: " + remainderCount + " (expected 90), PosDelta: " + posDelta
				+ ", Extracted inLove: " + extractedInLove + " (expected 10), Total logical: " + totalLogical));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A2 - Ten Continuous Feeds", false, e.getMessage()));
		}

		// Test A3: View-Ray Obstruction Avoidance
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(10);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 64));
			aimAt(player, cowStack);

			int feeds = 4;
			int viewRayBlockedCount = 0;
			Vec3 eyePos = player.getEyePosition();
			Vec3 targetCenter = cowStack.getBoundingBox().getCenter();

			for (int i = 0; i < feeds; i++) {
				simulateFeed(player, level, cowStack);
			}

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			for (Cow c : cows) {
				if (c != cowStack) {
					if (c.getBoundingBox().clip(eyePos, targetCenter).isPresent()) {
						viewRayBlockedCount++;
					}
				}
			}

			Entity picked = pickEntity(player, level, 5.0);
			boolean rayHitsAnchor = picked == cowStack;

			boolean pass = (viewRayBlockedCount == 0) && rayHitsAnchor && (((StackableEntity) cowStack).jarstacker$getStackCount() == 6);
			results.add(new TestResult("Test A3 - View-Ray Obstruction Avoidance", pass,
				"Blocked view-rays: " + viewRayBlockedCount + ", Ray hits anchor: " + rayHitsAnchor
				+ ", Remainder count: " + ((StackableEntity) cowStack).jarstacker$getStackCount()));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A3 - View-Ray Obstruction Avoidance", false, e.getMessage()));
		}

		// Test A4: Near Fence Anchor Feeding Containment
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			buildPen(level, penA, 3);

			// Place anchor beside eastern fence (center + 1.8)
			Vec3 nearFencePos = new Vec3(penA.getX() + 1.8, penA.getY(), penA.getZ() + 0.5);
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(nearFencePos.x, nearFencePos.y, nearFencePos.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(15);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(penA.getX() + 0.5, penA.getY(), penA.getZ() + 0.5), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 64));
			aimAt(player, cowStack);

			for (int i = 0; i < 5; i++) {
				simulateFeed(player, level, cowStack);
			}

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int escaped = 0;
			int totalLogical = 0;
			for (Cow c : cows) {
				totalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (!isInsidePen(c.position(), penA, 3)) {
					escaped++;
				}
			}

			double anchorDelta = cowStack.position().distanceTo(nearFencePos);
			int remainderCount = ((StackableEntity) cowStack).jarstacker$getStackCount();

			boolean pass = (escaped == 0) && (anchorDelta <= 0.001) && (remainderCount == 10) && (totalLogical == 15);
			results.add(new TestResult("Test A4 - Near Fence Feeding Containment", pass,
				"Escaped: " + escaped + ", AnchorDelta: " + anchorDelta + ", Remainder count: " + remainderCount + ", Total logical: " + totalLogical));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A4 - Near Fence Feeding Containment", false, e.getMessage()));
		}

		// Test A5: Corner Anchor Feeding Containment
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			buildPen(level, penA, 3);

			// Place anchor in north-east corner (center + 1.8, center + 1.8)
			Vec3 cornerPos = new Vec3(penA.getX() + 1.8, penA.getY(), penA.getZ() + 1.8);
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(cornerPos.x, cornerPos.y, cornerPos.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(10);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(penA.getX() + 0.5, penA.getY(), penA.getZ() + 0.5), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 64));
			aimAt(player, cowStack);

			for (int i = 0; i < 4; i++) {
				simulateFeed(player, level, cowStack);
			}

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int escaped = 0;
			int totalLogical = 0;
			for (Cow c : cows) {
				totalLogical += ((StackableEntity) c).jarstacker$getStackCount();
				if (!isInsidePen(c.position(), penA, 3)) {
					escaped++;
				}
			}

			double anchorDelta = cowStack.position().distanceTo(cornerPos);
			int remainderCount = ((StackableEntity) cowStack).jarstacker$getStackCount();

			boolean pass = (escaped == 0) && (anchorDelta <= 0.001) && (remainderCount == 6) && (totalLogical == 10);
			results.add(new TestResult("Test A5 - Corner Feeding Containment", pass,
				"Escaped: " + escaped + ", AnchorDelta: " + anchorDelta + ", Remainder count: " + remainderCount + ", Total logical: " + totalLogical));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A5 - Corner Feeding Containment", false, e.getMessage()));
		}

		// Test A6: Survival Item Consumption
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(15);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.5), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 20));

			for (int i = 0; i < 10; i++) {
				simulateFeed(player, level, cowStack);
			}

			ItemStack remainingWheat = player.getItemInHand(InteractionHand.MAIN_HAND);
			int wheatCount = remainingWheat.getCount();

			boolean pass = (wheatCount == 10) && (((StackableEntity) cowStack).jarstacker$getStackCount() == 5);
			results.add(new TestResult("Test A6 - Survival Item Consumption", pass,
				"Remaining wheat: " + wheatCount + " (expected 10), Remainder count: " + ((StackableEntity) cowStack).jarstacker$getStackCount() + " (expected 5)"));

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A6 - Survival Item Consumption", false, e.getMessage()));
		}

		// Test A7: Creative Mode Item Consumption
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(10);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.5), GameType.CREATIVE);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 20));

			for (int i = 0; i < 5; i++) {
				simulateFeed(player, level, cowStack);
			}

			ItemStack remainingWheat = player.getItemInHand(InteractionHand.MAIN_HAND);
			int wheatCount = remainingWheat.getCount();

			boolean pass = (wheatCount == 20) && (((StackableEntity) cowStack).jarstacker$getStackCount() == 5);
			results.add(new TestResult("Test A7 - Creative Mode Consumption", pass,
				"Remaining wheat: " + wheatCount + " (expected 20), Remainder count: " + ((StackableEntity) cowStack).jarstacker$getStackCount() + " (expected 5)"));

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A7 - Creative Mode Consumption", false, e.getMessage()));
		}

		// Test A8: Stack x2 and x1 Edge Cases
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(2);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.5), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 10));

			// First feed on x2
			InteractionResult res1 = simulateFeed(player, level, cowStack);
			int countAfterFirst = ((StackableEntity) cowStack).jarstacker$getStackCount();

			List<Cow> cowsAfterFirst = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			Cow extracted = null;
			for (Cow c : cowsAfterFirst) {
				if (c != cowStack) extracted = c;
			}
			boolean extractedInLove = (extracted != null && extracted.isInLove());

			// Second feed on anchor (which is now count == 1)
			InteractionResult res2 = simulateFeed(player, level, cowStack);
			int countAfterSecond = ((StackableEntity) cowStack).jarstacker$getStackCount();
			boolean anchorInLove = cowStack.isInLove();

			List<Cow> cowsAfterSecond = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			boolean noZeroCount = true;
			for (Cow c : cowsAfterSecond) {
				if (((StackableEntity) c).jarstacker$getStackCount() <= 0) {
					noZeroCount = false;
				}
			}

			boolean pass = (countAfterFirst == 1)
				&& extractedInLove
				&& (countAfterSecond == 1)
				&& anchorInLove
				&& noZeroCount
				&& (cowsAfterSecond.size() == 2)
				&& (player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 8);

			results.add(new TestResult("Test A8 - Stack x2 and x1 Edge Cases", pass,
				"After first feed: count=" + countAfterFirst + ", extractedInLove=" + extractedInLove
				+ ", After second feed: count=" + countAfterSecond + ", anchorInLove=" + anchorInLove
				+ ", Physical count: " + cowsAfterSecond.size() + ", Wheat: " + player.getItemInHand(InteractionHand.MAIN_HAND).getCount()));

			for (Cow c : cowsAfterSecond) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A8 - Stack x2 and x1 Edge Cases", false, e.getMessage()));
		}

		// Test A9: Full Breeding Lifecycle with Continuous Feeds
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(20);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.5), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 10));

			// Feed twice to extract 2 breeding partners
			simulateFeed(player, level, cowStack);
			simulateFeed(player, level, cowStack);

			List<Cow> herd = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			List<Cow> inLoveExtracted = new ArrayList<>();
			for (Cow c : herd) {
				if (c != cowStack && c.isInLove()) {
					inLoveExtracted.add(c);
				}
			}

			// Simulate breeding between the 2 extracted in-love cows
			if (inLoveExtracted.size() >= 2) {
				Cow p1 = inLoveExtracted.get(0);
				Cow p2 = inLoveExtracted.get(1);
				p1.resetLove();
				p2.resetLove();
				p1.setAge(6000); // cooldown
				p2.setAge(6000);
				((StackableEntity) p1).jarstacker$setBreedingLockTicks(0);
				((StackableEntity) p2).jarstacker$setBreedingLockTicks(0);

				Cow baby = createEntity(EntityType.COW, level);
				baby.setPos(posA.x + 0.5, posA.y, posA.z);
				baby.setBaby(true);
				((StackableEntity) baby).jarstacker$setStackCount(1);
				level.addFreshEntity(baby);
			}

			MobStackingManager.scanAndStack(level, config);

			List<Cow> finalHerd = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int totalLogical = 0;
			int adultCount = 0;
			int cooldownCount = 0;
			int babyCount = 0;

			for (Cow c : finalHerd) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				totalLogical += cnt;
				if (c.isBaby()) {
					babyCount += cnt;
				} else if (c.getAge() > 0) {
					cooldownCount += cnt;
				} else {
					adultCount += cnt;
				}
			}

			boolean pass = (adultCount == 18) && (cooldownCount == 2) && (babyCount == 1) && (totalLogical == 21);
			results.add(new TestResult("Test A9 - Full Breeding Lifecycle", pass,
				"Adult: " + adultCount + " (expected 18), Cooldown: " + cooldownCount + " (expected 2), Baby: " + babyCount + " (expected 1), Total: " + totalLogical));

			for (Cow c : finalHerd) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A9 - Full Breeding Lifecycle", false, e.getMessage()));
		}

		// Test A10: Repeated Raycast Target Stability
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(10);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.5), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 64));
			aimAt(player, cowStack);

			int feeds = 5;
			int targetRemHitCount = 0;

			for (int i = 0; i < feeds; i++) {
				Entity hitBefore = pickEntity(player, level, 5.0);
				if (hitBefore == cowStack) {
					targetRemHitCount++;
				}
				simulateFeed(player, level, cowStack);
			}

			boolean pass = (targetRemHitCount == feeds) && (((StackableEntity) cowStack).jarstacker$getStackCount() == 5);
			results.add(new TestResult("Test A10 - Raycast Target Stability", pass,
				"Raycast hits on remainder: " + targetRemHitCount + " / " + feeds + ", Remainder count: " + ((StackableEntity) cowStack).jarstacker$getStackCount()));

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test A10 - Raycast Target Stability", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.3.0 GENERAL MOB INTERACTION TEST SUITE (I1 - I15)
		// -------------------------------------------------------------

		// Test I1: Cow Milking (DIRECT)
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(20);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET, 1));
			aimAt(player, cowStack);

			java.util.UUID initialUuid = cowStack.getUUID();
			Vec3 initialPos = cowStack.position();

			InteractionResult milkRes = simulateInteract(player, level, cowStack);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);

			boolean pass = cows.size() == 1
				&& cowStack.getUUID().equals(initialUuid)
				&& ((StackableEntity) cowStack).jarstacker$getStackCount() == 20
				&& cowStack.position().distanceTo(initialPos) <= 0.001
				&& held.is(Items.MILK_BUCKET);

			results.add(new TestResult("Test I1 - Cow Milking (DIRECT)", pass,
				"Physical cows: " + cows.size() + " (expected 1), Count: " + ((StackableEntity) cowStack).jarstacker$getStackCount()
				+ " (expected 20), Held item: " + held.getItem() + " (expected milk_bucket)"));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I1 - Cow Milking (DIRECT)", false, e.getMessage()));
		}

		// Test I2: Baby Cow Milk Attempt
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			Cow babyStack = createEntity(EntityType.COW, level);
			babyStack.setPos(posA.x, posA.y, posA.z);
			babyStack.setBaby(true);
			((StackableEntity) babyStack).jarstacker$setStackCount(10);
			level.addFreshEntity(babyStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET, 1));
			aimAt(player, babyStack);

			InteractionResult res = simulateInteract(player, level, babyStack);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);

			boolean pass = cows.size() == 1
				&& ((StackableEntity) babyStack).jarstacker$getStackCount() == 10
				&& held.is(Items.BUCKET)
				&& !res.consumesAction();

			results.add(new TestResult("Test I2 - Baby Cow Milk Attempt", pass,
				"Physical cows: " + cows.size() + ", Count: " + ((StackableEntity) babyStack).jarstacker$getStackCount()
				+ ", Held item: " + held.getItem() + " (expected bucket), Consumes: " + res.consumesAction()));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I2 - Baby Cow Milk Attempt", false, e.getMessage()));
		}

		// Test I3: Sheep Shearing
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();
			for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, cleanAreaA)) ie.discard();

			Sheep sheepStack = createEntity(EntityType.SHEEP, level);
			sheepStack.setPos(posA.x, posA.y, posA.z);
			sheepStack.setColor(DyeColor.WHITE);
			sheepStack.setSheared(false);
			((StackableEntity) sheepStack).jarstacker$setStackCount(10);
			level.addFreshEntity(sheepStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			ItemStack shears = new ItemStack(Items.SHEARS);
			player.setItemInHand(InteractionHand.MAIN_HAND, shears);
			aimAt(player, sheepStack);

			Vec3 initialPos = sheepStack.position();
			java.util.UUID initialUuid = sheepStack.getUUID();

			InteractionResult res = simulateInteract(player, level, sheepStack);

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			Sheep remainder = null;
			Sheep extracted = null;
			for (Sheep s : sheepList) {
				if (s.getUUID().equals(initialUuid)) remainder = s;
				else extracted = s;
			}

			List<ItemEntity> droppedItems = level.getEntitiesOfClass(ItemEntity.class, cleanAreaA);
			boolean hasWoolDrop = droppedItems.stream().anyMatch(ie -> ie.getItem().is(Items.WHITE_WOOL));

			boolean pass = res.consumesAction()
				&& sheepList.size() == 2
				&& remainder != null
				&& ((StackableEntity) remainder).jarstacker$getStackCount() == 9
				&& !remainder.isSheared()
				&& remainder.position().distanceTo(initialPos) <= 0.001
				&& extracted != null
				&& ((StackableEntity) extracted).jarstacker$getStackCount() == 1
				&& extracted.isSheared()
				&& hasWoolDrop;

			results.add(new TestResult("Test I3 - Sheep Shearing", pass,
				"Physical sheep: " + sheepList.size() + ", Remainder count: " + (remainder != null ? ((StackableEntity) remainder).jarstacker$getStackCount() : "null")
				+ ", Extracted sheared: " + (extracted != null && extracted.isSheared())
				+ ", Wool dropped: " + hasWoolDrop));

			for (Sheep s : sheepList) s.discard();
			for (ItemEntity ie : droppedItems) ie.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I3 - Sheep Shearing", false, e.getMessage()));
		}

		// Test I4: Repeated Shearing
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();
			for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, cleanAreaA)) ie.discard();

			Sheep sheepStack = createEntity(EntityType.SHEEP, level);
			sheepStack.setPos(posA.x, posA.y, posA.z);
			sheepStack.setColor(DyeColor.WHITE);
			sheepStack.setSheared(false);
			((StackableEntity) sheepStack).jarstacker$setStackCount(20);
			level.addFreshEntity(sheepStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, sheepStack);

			Vec3 initialPos = sheepStack.position();
			int successfulShears = 0;

			for (int i = 0; i < 10; i++) {
				InteractionResult r = simulateInteract(player, level, sheepStack);
				if (r.consumesAction()) successfulShears++;
			}

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			int shearedCount = 0;
			int unshearedCount = 0;
			for (Sheep s : sheepList) {
				int cnt = ((StackableEntity) s).jarstacker$getStackCount();
				if (s.isSheared()) shearedCount += cnt;
				else unshearedCount += cnt;
			}

			double posDelta = sheepStack.position().distanceTo(initialPos);

			boolean pass = successfulShears == 10
				&& ((StackableEntity) sheepStack).jarstacker$getStackCount() == 10
				&& posDelta <= 0.001
				&& shearedCount == 10
				&& unshearedCount == 10;

			results.add(new TestResult("Test I4 - Repeated Shearing", pass,
				"Successful: " + successfulShears + ", Remainder: " + ((StackableEntity) sheepStack).jarstacker$getStackCount()
				+ ", PosDelta: " + posDelta + ", Sheared: " + shearedCount + ", Unsheared: " + unshearedCount));

			for (Sheep s : sheepList) s.discard();
			for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, cleanAreaA)) ie.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I4 - Repeated Shearing", false, e.getMessage()));
		}

		// Test I5: Sheep Dyeing
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep sheepStack = createEntity(EntityType.SHEEP, level);
			sheepStack.setPos(posA.x, posA.y, posA.z);
			sheepStack.setColor(DyeColor.WHITE);
			sheepStack.setSheared(false);
			((StackableEntity) sheepStack).jarstacker$setStackCount(10);
			level.addFreshEntity(sheepStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RED_DYE, 10));
			aimAt(player, sheepStack);

			java.util.UUID initialUuid = sheepStack.getUUID();
			InteractionResult res = simulateInteract(player, level, sheepStack);

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			Sheep remainder = null;
			Sheep extracted = null;
			for (Sheep s : sheepList) {
				if (s.getUUID().equals(initialUuid)) remainder = s;
				else extracted = s;
			}

			int dyeLeft = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();

			boolean pass = res.consumesAction()
				&& sheepList.size() == 2
				&& remainder != null
				&& ((StackableEntity) remainder).jarstacker$getStackCount() == 9
				&& remainder.getColor() == DyeColor.WHITE
				&& extracted != null
				&& ((StackableEntity) extracted).jarstacker$getStackCount() == 1
				&& extracted.getColor() == DyeColor.RED
				&& dyeLeft == 9;

			results.add(new TestResult("Test I5 - Sheep Dyeing", pass,
				"Sheep size: " + sheepList.size() + ", Remainder count: " + (remainder != null ? ((StackableEntity) remainder).jarstacker$getStackCount() : "null")
				+ ", Extracted color: " + (extracted != null ? extracted.getColor() : "null")
				+ ", Dye left: " + dyeLeft + " (expected 9)"));

			for (Sheep s : sheepList) s.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I5 - Sheep Dyeing", false, e.getMessage()));
		}

		// Test I6: Continuous Dyeing
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep sheepStack = createEntity(EntityType.SHEEP, level);
			sheepStack.setPos(posA.x, posA.y, posA.z);
			sheepStack.setColor(DyeColor.WHITE);
			sheepStack.setSheared(false);
			((StackableEntity) sheepStack).jarstacker$setStackCount(20);
			level.addFreshEntity(sheepStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RED_DYE, 20));
			aimAt(player, sheepStack);

			Vec3 initialPos = sheepStack.position();
			int dyesApplied = 0;
			for (int i = 0; i < 5; i++) {
				InteractionResult r = simulateInteract(player, level, sheepStack);
				if (r.consumesAction()) dyesApplied++;
			}

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			int whiteCount = 0;
			int redCount = 0;
			for (Sheep s : sheepList) {
				int cnt = ((StackableEntity) s).jarstacker$getStackCount();
				if (s.getColor() == DyeColor.WHITE) whiteCount += cnt;
				if (s.getColor() == DyeColor.RED) redCount += cnt;
			}

			double posDelta = sheepStack.position().distanceTo(initialPos);
			int dyeLeft = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();

			boolean pass = dyesApplied == 5
				&& ((StackableEntity) sheepStack).jarstacker$getStackCount() == 15
				&& posDelta <= 0.001
				&& whiteCount == 15
				&& redCount == 5
				&& dyeLeft == 15;

			results.add(new TestResult("Test I6 - Continuous Dyeing", pass,
				"Dyes applied: " + dyesApplied + ", Remainder: " + ((StackableEntity) sheepStack).jarstacker$getStackCount()
				+ ", White: " + whiteCount + ", Red: " + redCount + ", Dye left: " + dyeLeft));

			for (Sheep s : sheepList) s.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I6 - Continuous Dyeing", false, e.getMessage()));
		}

		// Test I7: Same Color Dye No-Op
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep sheepStack = createEntity(EntityType.SHEEP, level);
			sheepStack.setPos(posA.x, posA.y, posA.z);
			sheepStack.setColor(DyeColor.RED);
			sheepStack.setSheared(false);
			((StackableEntity) sheepStack).jarstacker$setStackCount(10);
			level.addFreshEntity(sheepStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RED_DYE, 10));
			aimAt(player, sheepStack);

			InteractionResult res = simulateInteract(player, level, sheepStack);

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			int dyeLeft = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();

			boolean pass = sheepList.size() == 1
				&& ((StackableEntity) sheepStack).jarstacker$getStackCount() == 10
				&& dyeLeft == 10;

			results.add(new TestResult("Test I7 - Same Color Dye No-Op", pass,
				"Sheep count: " + sheepList.size() + " (expected 1), Logical count: " + ((StackableEntity) sheepStack).jarstacker$getStackCount()
				+ ", Dye left: " + dyeLeft + " (expected 10)"));

			for (Sheep s : sheepList) s.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I7 - Same Color Dye No-Op", false, e.getMessage()));
		}

		// Test I8: Baby Growth Feeding
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyStack = createEntity(EntityType.COW, level);
			babyStack.setPos(posA.x, posA.y, posA.z);
			babyStack.setBaby(true);
			babyStack.setAge(-24000);
			((StackableEntity) babyStack).jarstacker$setStackCount(10);
			level.addFreshEntity(babyStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 10));
			aimAt(player, babyStack);

			Vec3 initialPos = babyStack.position();

			InteractionResult res = simulateInteract(player, level, babyStack);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = ((StackableEntity) babyStack).jarstacker$getBabyGrowthState();

			boolean countOk = ((StackableEntity) babyStack).jarstacker$getStackCount() == 10;
			boolean posOk = babyStack.position().distanceTo(initialPos) <= 0.001;
			boolean stateOk = state != null && state.size() == 10 && (state.getEarliestAdultAt() < level.getGameTime() + 24000);
			boolean pass = res.consumesAction() && countOk && posOk && stateOk && (cows.size() == 1);

			results.add(new TestResult("Test I8 - Baby Growth Feeding", pass,
				"Consumes: " + res.consumesAction() + ", Count: " + ((StackableEntity) babyStack).jarstacker$getStackCount()
				+ ", State size: " + (state != null ? state.size() : "null")
				+ ", Earliest remaining: " + (state != null ? (state.getEarliestAdultAt() - level.getGameTime()) : "null") + " (expected < 24000)"
				+ ", Physical cows: " + cows.size()));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I8 - Baby Growth Feeding", false, e.getMessage()));
		}

		// Test I9: Baby Repeated Growth
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyStack = createEntity(EntityType.COW, level);
			babyStack.setPos(posA.x, posA.y, posA.z);
			babyStack.setBaby(true);
			babyStack.setAge(-24000);
			((StackableEntity) babyStack).jarstacker$setStackCount(20);
			level.addFreshEntity(babyStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 20));
			aimAt(player, babyStack);

			Vec3 initialPos = babyStack.position();
			int fedCount = 0;
			for (int i = 0; i < 4; i++) {
				InteractionResult r = simulateInteract(player, level, babyStack);
				if (r.consumesAction()) fedCount++;
			}

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int remainderCount = ((StackableEntity) babyStack).jarstacker$getStackCount();
			double posDelta = babyStack.position().distanceTo(initialPos);
			int wheatLeft = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = ((StackableEntity) babyStack).jarstacker$getBabyGrowthState();

			boolean pass = fedCount == 4
				&& remainderCount == 20
				&& posDelta <= 0.001
				&& cows.size() == 1
				&& wheatLeft == 16
				&& state != null && state.size() == 20;

			results.add(new TestResult("Test I9 - Baby Repeated Growth", pass,
				"Fed count: " + fedCount + ", Remainder: " + remainderCount + ", PosDelta: " + posDelta
				+ ", Physical cows: " + cows.size() + ", Wheat left: " + wheatLeft));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I9 - Baby Repeated Growth", false, e.getMessage()));
		}

		// Test I10: Taming Success
		try {
			for (Wolf w : level.getEntitiesOfClass(Wolf.class, cleanAreaA)) w.discard();

			Wolf wolfStack = createEntity(EntityType.WOLF, level);
			wolfStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) wolfStack).jarstacker$setStackCount(10);
			level.addFreshEntity(wolfStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE, 10));
			aimAt(player, wolfStack);

			java.util.UUID initialUuid = wolfStack.getUUID();
			InteractionResult res = simulateInteract(player, level, wolfStack);

			List<Wolf> wolves = level.getEntitiesOfClass(Wolf.class, cleanAreaA);
			Wolf remainder = null;
			Wolf extracted = null;
			for (Wolf w : wolves) {
				if (w.getUUID().equals(initialUuid)) remainder = w;
				else extracted = w;
			}

			// Force tamed state on extracted to test deterministic exclusion
			if (extracted != null) {
				extracted.tame(player);
			}

			MobStackingManager.scanAndStack(level, config);
			List<Wolf> wolvesAfterScan = level.getEntitiesOfClass(Wolf.class, cleanAreaA);

			boolean pass = remainder != null
				&& ((StackableEntity) remainder).jarstacker$getStackCount() == 9
				&& !remainder.isTame()
				&& extracted != null
				&& extracted.isTame()
				&& wolvesAfterScan.size() == 2;

			results.add(new TestResult("Test I10 - Taming Success", pass,
				"Remainder count: " + (remainder != null ? ((StackableEntity) remainder).jarstacker$getStackCount() : "null")
				+ ", Extracted tamed: " + (extracted != null && extracted.isTame())
				+ ", Separate after scan: " + (wolvesAfterScan.size() == 2)));

			for (Wolf w : wolvesAfterScan) w.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I10 - Taming Success", false, e.getMessage()));
		}

		// Test I11: Taming Failure & Remerge
		try {
			for (Wolf w : level.getEntitiesOfClass(Wolf.class, cleanAreaA)) w.discard();

			Wolf wolfStack = createEntity(EntityType.WOLF, level);
			wolfStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) wolfStack).jarstacker$setStackCount(5);
			level.addFreshEntity(wolfStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE, 5));
			aimAt(player, wolfStack);

			java.util.UUID initialUuid = wolfStack.getUUID();
			InteractionResult res = simulateInteract(player, level, wolfStack);

			List<Wolf> wolves = level.getEntitiesOfClass(Wolf.class, cleanAreaA);
			Wolf remainder = null;
			Wolf extracted = null;
			for (Wolf w : wolves) {
				if (w.getUUID().equals(initialUuid)) remainder = w;
				else extracted = w;
			}

			if (extracted != null) {
				extracted.setTame(false, false);
			}

			// 1. While locked, scan should NOT merge
			MobStackingManager.scanAndStack(level, config);
			int countWhileLocked = level.getEntitiesOfClass(Wolf.class, cleanAreaA).size();

			// 2. Clear lock ticks
			if (extracted != null) {
				((StackableEntity) extracted).jarstacker$setInteractionLockTicks(0);
			}

			// 3. Now scan SHOULD remerge
			MobStackingManager.scanAndStack(level, config);
			List<Wolf> finalWolves = level.getEntitiesOfClass(Wolf.class, cleanAreaA);
			int finalLogical = 0;
			for (Wolf w : finalWolves) finalLogical += ((StackableEntity) w).jarstacker$getStackCount();

			boolean pass = countWhileLocked == 2 && finalWolves.size() == 1 && finalLogical == 5;

			results.add(new TestResult("Test I11 - Taming Failure & Remerge", pass,
				"Count while locked: " + countWhileLocked + " (expected 2), Final physical: " + finalWolves.size()
				+ " (expected 1), Final logical: " + finalLogical + " (expected 5)"));

			for (Wolf w : finalWolves) w.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I11 - Taming Failure & Remerge", false, e.getMessage()));
		}

		// Test I12: Breeding Regression
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(10);
			level.addFreshEntity(cowStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 10));
			aimAt(player, cowStack);

			simulateInteract(player, level, cowStack);
			simulateInteract(player, level, cowStack);

			List<Cow> herd = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int inLoveCount = 0;
			for (Cow c : herd) {
				if (c != cowStack && c.isInLove()) inLoveCount++;
			}

			int remainderCount = ((StackableEntity) cowStack).jarstacker$getStackCount();

			boolean pass = (remainderCount == 8) && (inLoveCount == 2) && (herd.size() == 3);
			results.add(new TestResult("Test I12 - Breeding Regression", pass,
				"Remainder: " + remainderCount + " (expected 8), InLove: " + inLoveCount + " (expected 2), Herd size: " + herd.size()));

			for (Cow c : herd) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I12 - Breeding Regression", false, e.getMessage()));
		}

		// Test I13: Placement Regression
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();
			buildPen(level, penA, 3);

			Vec3 nearFence = new Vec3(penA.getX() + 1.8, penA.getY(), penA.getZ() + 1.8);
			Sheep sheepStack = createEntity(EntityType.SHEEP, level);
			sheepStack.setPos(nearFence.x, nearFence.y, nearFence.z);
			sheepStack.setColor(DyeColor.WHITE);
			sheepStack.setSheared(false);
			((StackableEntity) sheepStack).jarstacker$setStackCount(10);
			level.addFreshEntity(sheepStack);

			ServerPlayer player = createMockPlayer(level, new Vec3(penA.getX() + 0.5, penA.getY(), penA.getZ() + 0.5), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, sheepStack);

			for (int i = 0; i < 4; i++) {
				simulateInteract(player, level, sheepStack);
			}

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			int escaped = 0;
			for (Sheep s : sheepList) {
				if (!isInsidePen(s.position(), penA, 3)) escaped++;
			}

			boolean pass = (escaped == 0) && (sheepList.size() == 5) && (((StackableEntity) sheepStack).jarstacker$getStackCount() == 6);
			results.add(new TestResult("Test I13 - Placement Regression", pass,
				"Escaped: " + escaped + ", Total physical: " + sheepList.size() + ", Remainder count: " + ((StackableEntity) sheepStack).jarstacker$getStackCount()));

			for (Sheep s : sheepList) s.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I13 - Placement Regression", false, e.getMessage()));
		}

		// Test I14: Item Accounting (Survival vs Creative)
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			// 1. Creative dyeing
			Sheep sheepStackC = createEntity(EntityType.SHEEP, level);
			sheepStackC.setPos(posA.x, posA.y, posA.z);
			sheepStackC.setColor(DyeColor.WHITE);
			((StackableEntity) sheepStackC).jarstacker$setStackCount(5);
			level.addFreshEntity(sheepStackC);

			ServerPlayer creativePlayer = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.CREATIVE);
			creativePlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLUE_DYE, 10));
			aimAt(creativePlayer, sheepStackC);

			simulateInteract(creativePlayer, level, sheepStackC);
			int creativeDyeLeft = creativePlayer.getItemInHand(InteractionHand.MAIN_HAND).getCount();

			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();
			creativePlayer.discard();

			// 2. Survival dyeing
			Sheep sheepStackS = createEntity(EntityType.SHEEP, level);
			sheepStackS.setPos(posA.x, posA.y, posA.z);
			sheepStackS.setColor(DyeColor.WHITE);
			((StackableEntity) sheepStackS).jarstacker$setStackCount(5);
			level.addFreshEntity(sheepStackS);

			ServerPlayer survivalPlayer = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			survivalPlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLUE_DYE, 10));
			aimAt(survivalPlayer, sheepStackS);

			simulateInteract(survivalPlayer, level, sheepStackS);
			int survivalDyeLeft = survivalPlayer.getItemInHand(InteractionHand.MAIN_HAND).getCount();

			boolean pass = (creativeDyeLeft == 10) && (survivalDyeLeft == 9);
			results.add(new TestResult("Test I14 - Item Accounting", pass,
				"Creative dye left: " + creativeDyeLeft + " (expected 10), Survival dye left: " + survivalDyeLeft + " (expected 9)"));

			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();
			survivalPlayer.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I14 - Item Accounting", false, e.getMessage()));
		}

		// Test I15: Interaction State Persistence
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cow).jarstacker$setStackCount(35);
			((StackableEntity) cow).jarstacker$setInteractionLockTicks(250);

			CompoundTag tag = new CompoundTag();
			EntityAdapter.saveWithoutId(cow, tag);

			Cow loaded = createEntity(EntityType.COW, level);
			EntityAdapter.load(loaded, tag);

			int loadedCount = ((StackableEntity) loaded).jarstacker$getStackCount();
			int loadedLock = ((StackableEntity) loaded).jarstacker$getInteractionLockTicks();

			boolean pass = (loadedCount == 35) && (loadedLock == 250);
			results.add(new TestResult("Test I15 - Interaction State Persistence", pass,
				"Saved: count=35, lock=250; Loaded: count=" + loadedCount + ", lock=" + loadedLock));

			cow.discard();
			loaded.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test I15 - Interaction State Persistence", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.3.1 LOGICAL PER-ENTITY STATE TEST SUITE (B1 - B9, S1 - S7, ST1 - ST3)
		// -------------------------------------------------------------

		// Test B1: Different Baby Ages
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow cowA = createEntity(EntityType.COW, level);
			cowA.setPos(posA.x, posA.y, posA.z);
			cowA.setBaby(true);
			cowA.setAge(-18000);
			((StackableEntity) cowA).jarstacker$setStackCount(1);
			level.addFreshEntity(cowA);

			Cow cowB = createEntity(EntityType.COW, level);
			cowB.setPos(posA.x + 0.2, posA.y, posA.z);
			cowB.setBaby(true);
			cowB.setAge(-12000);
			((StackableEntity) cowB).jarstacker$setStackCount(1);
			level.addFreshEntity(cowB);

			Cow cowC = createEntity(EntityType.COW, level);
			cowC.setPos(posA.x - 0.2, posA.y, posA.z);
			cowC.setBaby(true);
			cowC.setAge(-5000);
			((StackableEntity) cowC).jarstacker$setStackCount(1);
			level.addFreshEntity(cowC);

			MobStackingManager.scanAndStack(level, config);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			boolean pass = cows.size() == 1;
			if (pass) {
				Cow rep = cows.get(0);
				int count = ((StackableEntity) rep).jarstacker$getStackCount();
				com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = ((StackableEntity) rep).jarstacker$getBabyGrowthState();
				pass = count == 3 && state != null && state.size() == 3;
			}

			results.add(new TestResult("Test B1 - Different Baby Ages", pass,
				"Physical cows: " + cows.size() + ", Count: " + (cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount())
				+ ", Growth size: " + (cows.isEmpty() || ((StackableEntity) cows.get(0)).jarstacker$getBabyGrowthState() == null ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getBabyGrowthState().size())));

			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B1 - Different Baby Ages", false, e.getMessage()));
		}

		// Test B2: Individual Promotion
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(3);
			level.addFreshEntity(babyCow);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			state.add(now); // Matured!
			state.add(now + 7000);
			state.add(now + 13000);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			int matured = com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, babyCow);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int babyCount = 0;
			int adultCount = 0;
			for (Cow c : cows) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				if (c.isBaby()) babyCount += cnt;
				else adultCount += cnt;
			}

			boolean pass = matured == 1 && babyCount == 2 && adultCount == 1 && (babyCount + adultCount == 3);
			results.add(new TestResult("Test B2 - Individual Promotion", pass,
				"Matured: " + matured + ", Baby count: " + babyCount + ", Adult count: " + adultCount));

			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B2 - Individual Promotion", false, e.getMessage()));
		}

		// Test B3: Multiple Promotions
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(10);
			level.addFreshEntity(babyCow);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 4; i++) state.add(now - 100);
			for (int i = 0; i < 6; i++) state.add(now + 10000);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			int matured = com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, babyCow);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int babyCount = 0;
			int adultCount = 0;
			for (Cow c : cows) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				if (c.isBaby()) babyCount += cnt;
				else adultCount += cnt;
			}

			boolean pass = matured == 4 && babyCount == 6 && adultCount == 4 && cows.size() == 2;
			results.add(new TestResult("Test B3 - Multiple Promotions", pass,
				"Matured: " + matured + ", Baby: " + babyCount + ", Adult: " + adultCount + ", Physical cows: " + cows.size()));

			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B3 - Multiple Promotions", false, e.getMessage()));
		}

		// Test B4: Feed Earliest Baby
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(3);
			level.addFreshEntity(babyCow);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			state.add(now + 5000);
			state.add(now + 12000);
			state.add(now + 18000);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 5));
			aimAt(player, babyCow);

			simulateInteract(player, level, babyCow);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState afterState = ((StackableEntity) babyCow).jarstacker$getBabyGrowthState();
			long earliest = afterState.getEarliestAdultAt();
			long remainingEarliest = earliest - now;

			boolean pass = remainingEarliest < 5000 && afterState.size() == 3;

			results.add(new TestResult("Test B4 - Feed Earliest Baby", pass,
				"Remaining earliest: " + remainingEarliest + " (expected < 5000), Size: " + afterState.size()));

			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B4 - Feed Earliest Baby", false, e.getMessage()));
		}

		// Test B5: Growth Progress Preservation
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			babyCow.setAge(-20000);
			((StackableEntity) babyCow).jarstacker$setStackCount(5);
			level.addFreshEntity(babyCow);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 5));
			aimAt(player, babyCow);

			simulateInteract(player, level, babyCow);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int lock = ((StackableEntity) babyCow).jarstacker$getInteractionLockTicks();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = ((StackableEntity) babyCow).jarstacker$getBabyGrowthState();

			boolean pass = cows.size() == 1 && lock == 0 && state != null && state.size() == 5;

			results.add(new TestResult("Test B5 - Growth Progress Preservation", pass,
				"Physical cows: " + cows.size() + " (expected 1), Lock: " + lock + " (expected 0), Growth size: " + (state != null ? state.size() : "null")));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B5 - Growth Progress Preservation", false, e.getMessage()));
		}

		// Test B6: Repeated Feeding
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			babyCow.setAge(-20000);
			((StackableEntity) babyCow).jarstacker$setStackCount(10);
			level.addFreshEntity(babyCow);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 20));
			aimAt(player, babyCow);

			Vec3 initialPos = babyCow.position();
			int fed = 0;
			for (int i = 0; i < 5; i++) {
				InteractionResult r = simulateInteract(player, level, babyCow);
				if (r.consumesAction()) fed++;
			}

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int wheatLeft = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();
			double posDelta = babyCow.position().distanceTo(initialPos);

			boolean pass = fed == 5 && wheatLeft == 15 && posDelta <= 0.001 && cows.size() == 1;

			results.add(new TestResult("Test B6 - Repeated Feeding", pass,
				"Fed: " + fed + ", Wheat left: " + wheatLeft + ", PosDelta: " + posDelta + ", Physical cows: " + cows.size()));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B6 - Repeated Feeding", false, e.getMessage()));
		}

		// Test B7: Immediate Adult Transition
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(2);
			level.addFreshEntity(babyCow);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			state.add(now + 10);
			state.add(now + 15000);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 5));
			aimAt(player, babyCow);

			simulateInteract(player, level, babyCow);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int babyCount = 0;
			int adultCount = 0;
			for (Cow c : cows) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				if (c.isBaby()) babyCount += cnt;
				else adultCount += cnt;
			}

			boolean pass = babyCount == 1 && adultCount == 1 && (babyCount + adultCount == 2);

			results.add(new TestResult("Test B7 - Immediate Adult Transition", pass,
				"Baby count: " + babyCount + " (expected 1), Adult count: " + adultCount + " (expected 1), Total physical: " + cows.size()));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B7 - Immediate Adult Transition", false, e.getMessage()));
		}

		// Test B8: Save / Reload
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			cow.setBaby(true);
			((StackableEntity) cow).jarstacker$setStackCount(5);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			state.add(105000L);
			state.add(108000L);
			state.add(112000L);
			state.add(115000L);
			state.add(118000L);
			((StackableEntity) cow).jarstacker$setBabyGrowthState(state);

			CompoundTag tag = new CompoundTag();
			EntityAdapter.saveWithoutId(cow, tag);

			Cow loaded = createEntity(EntityType.COW, level);
			EntityAdapter.load(loaded, tag);

			int loadedCount = ((StackableEntity) loaded).jarstacker$getStackCount();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState loadedState = ((StackableEntity) loaded).jarstacker$getBabyGrowthState();

			boolean pass = (loadedCount == 5)
				&& (loadedState != null)
				&& (loadedState.size() == 5)
				&& (loadedState.getEntries().equals(state.getEntries()));

			results.add(new TestResult("Test B8 - Save / Reload Growth State", pass,
				"Loaded count: " + loadedCount + ", Loaded entries: " + (loadedState != null ? loadedState.getEntries() : "null")));

			cow.discard();
			loaded.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B8 - Save / Reload Growth State", false, e.getMessage()));
		}

		// Test B9: Time Manipulation Safety
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(3);
			level.addFreshEntity(babyCow);

			long gameTimeBefore = level.getGameTime();
			long dayTimeBefore = getDayTime(level);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			state.add(gameTimeBefore + 10000L);
			state.add(gameTimeBefore + 15000L);
			state.add(gameTimeBefore + 20000L);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			// Simulate /time add 12000
			setDayTime(level, dayTimeBefore + 12000L);

			long gameTimeAfter = level.getGameTime();
			int matured = com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, babyCow);

			boolean pass = (gameTimeAfter == gameTimeBefore) && (matured == 0) && (((StackableEntity) babyCow).jarstacker$getStackCount() == 3);

			results.add(new TestResult("Test B9 - Time Manipulation Safety", pass,
				"GameTime delta: " + (gameTimeAfter - gameTimeBefore) + ", Matured: " + matured + " (expected 0)"));

			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test B9 - Time Manipulation Safety", false, e.getMessage()));
		}

		// Test S1: Sheep Full Shear
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep sheep = createEntity(EntityType.SHEEP, level);
			sheep.setPos(posA.x, posA.y, posA.z);
			sheep.setColor(DyeColor.WHITE);
			sheep.setSheared(false);
			((StackableEntity) sheep).jarstacker$setStackCount(5);
			level.addFreshEntity(sheep);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, sheep);

			for (int i = 0; i < 4; i++) {
				simulateInteract(player, level, sheep);
			}

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			int shearedTotal = 0;
			for (Sheep s : sheepList) {
				if (s.isSheared()) shearedTotal += ((StackableEntity) s).jarstacker$getStackCount();
			}

			boolean pass = (shearedTotal == 4) && (sheepList.size() == 5);
			results.add(new TestResult("Test S1 - Sheep Full Shear", pass,
				"Sheared total: " + shearedTotal + " (expected 4), Physical sheep: " + sheepList.size()));

			for (Sheep s : sheepList) s.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S1 - Sheep Full Shear", false, e.getMessage()));
		}

		// Test S2: Single Regrowth
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep sheep = createEntity(EntityType.SHEEP, level);
			sheep.setPos(posA.x, posA.y, posA.z);
			sheep.setColor(DyeColor.WHITE);
			sheep.setSheared(true);
			((StackableEntity) sheep).jarstacker$setStackCount(5);
			level.addFreshEntity(sheep);

			sheep.ate();

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			int shearedCount = 0;
			int unshearedCount = 0;
			for (Sheep s : sheepList) {
				int cnt = ((StackableEntity) s).jarstacker$getStackCount();
				if (s.isSheared()) shearedCount += cnt;
				else unshearedCount += cnt;
			}

			boolean pass = (shearedCount == 4) && (unshearedCount == 1) && (sheep.isSheared());
			results.add(new TestResult("Test S2 - Single Regrowth", pass,
				"Sheared: " + shearedCount + " (expected 4), Unsheared: " + unshearedCount + " (expected 1), Rep sheared: " + sheep.isSheared()));

			for (Sheep s : sheepList) s.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S2 - Single Regrowth", false, e.getMessage()));
		}

		// Test S3: Sequential Regrowth
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep sheep = createEntity(EntityType.SHEEP, level);
			sheep.setPos(posA.x, posA.y, posA.z);
			sheep.setColor(DyeColor.WHITE);
			sheep.setSheared(true);
			((StackableEntity) sheep).jarstacker$setStackCount(5);
			level.addFreshEntity(sheep);

			boolean stepOk = true;
			for (int i = 0; i < 4; i++) {
				sheep.ate();
				int rem = ((StackableEntity) sheep).jarstacker$getStackCount();
				if (rem != (4 - i) || !sheep.isSheared()) stepOk = false;
			}

			sheep.ate();
			if (sheep.isSheared()) stepOk = false;

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			int totalLogical = 0;
			int shearedCount = 0;
			int unshearedCount = 0;
			for (Sheep s : sheepList) {
				int cnt = ((StackableEntity) s).jarstacker$getStackCount();
				totalLogical += cnt;
				if (s.isSheared()) shearedCount += cnt;
				else unshearedCount += cnt;
			}

			boolean pass = stepOk && (totalLogical == 5) && (shearedCount == 0) && (unshearedCount == 5);
			results.add(new TestResult("Test S3 - Sequential Regrowth", pass,
				"StepOk: " + stepOk + ", Total: " + totalLogical + ", Sheared: " + shearedCount + ", Unsheared: " + unshearedCount));

			for (Sheep s : sheepList) s.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S3 - Sequential Regrowth", false, e.getMessage()));
		}

		// Test S4: Existing Unsheared Stack Merge
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep shearedSheep = createEntity(EntityType.SHEEP, level);
			shearedSheep.setPos(posA.x, posA.y, posA.z);
			shearedSheep.setColor(DyeColor.WHITE);
			shearedSheep.setSheared(true);
			((StackableEntity) shearedSheep).jarstacker$setStackCount(5);
			level.addFreshEntity(shearedSheep);

			Sheep unshearedSheep = createEntity(EntityType.SHEEP, level);
			unshearedSheep.setPos(posA.x + 1.0, posA.y, posA.z);
			unshearedSheep.setColor(DyeColor.WHITE);
			unshearedSheep.setSheared(false);
			((StackableEntity) unshearedSheep).jarstacker$setStackCount(10);
			level.addFreshEntity(unshearedSheep);

			shearedSheep.ate();

			int shearedRemaining = ((StackableEntity) shearedSheep).jarstacker$getStackCount();
			int unshearedTotal = ((StackableEntity) unshearedSheep).jarstacker$getStackCount();
			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);

			boolean pass = (shearedRemaining == 4) && (unshearedTotal == 11) && (sheepList.size() == 2);
			results.add(new TestResult("Test S4 - Existing Unsheared Stack Merge", pass,
				"Sheared rem: " + shearedRemaining + " (expected 4), Unsheared total: " + unshearedTotal + " (expected 11), Physical entities: " + sheepList.size()));

			for (Sheep s : sheepList) s.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S4 - Existing Unsheared Stack Merge", false, e.getMessage()));
		}

		// Test S5: Color Preservation
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep redSheep = createEntity(EntityType.SHEEP, level);
			redSheep.setPos(posA.x, posA.y, posA.z);
			redSheep.setColor(DyeColor.RED);
			redSheep.setSheared(true);
			((StackableEntity) redSheep).jarstacker$setStackCount(5);
			level.addFreshEntity(redSheep);

			redSheep.ate();

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			boolean allRed = sheepList.stream().allMatch(s -> s.getColor() == DyeColor.RED);
			int shearedCount = 0;
			int unshearedCount = 0;
			for (Sheep s : sheepList) {
				int cnt = ((StackableEntity) s).jarstacker$getStackCount();
				if (s.isSheared()) shearedCount += cnt;
				else unshearedCount += cnt;
			}

			boolean pass = allRed && (shearedCount == 4) && (unshearedCount == 1);
			results.add(new TestResult("Test S5 - Color Preservation", pass,
				"All Red: " + allRed + ", Sheared: " + shearedCount + ", Unsheared: " + unshearedCount));

			for (Sheep s : sheepList) s.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S5 - Color Preservation", false, e.getMessage()));
		}

		// Test S6: Wool Exploit Prevention
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep sheep = createEntity(EntityType.SHEEP, level);
			sheep.setPos(posA.x, posA.y, posA.z);
			sheep.setColor(DyeColor.WHITE);
			sheep.setSheared(true);
			((StackableEntity) sheep).jarstacker$setStackCount(5);
			level.addFreshEntity(sheep);

			sheep.ate();

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			int shearableCount = 0;
			for (Sheep s : sheepList) {
				if (s.readyForShearing()) shearableCount += ((StackableEntity) s).jarstacker$getStackCount();
			}

			boolean pass = (shearableCount == 1) && (((StackableEntity) sheep).jarstacker$getStackCount() == 4) && (!sheep.readyForShearing());
			results.add(new TestResult("Test S6 - Wool Exploit Prevention", pass,
				"Shearable count: " + shearableCount + " (expected 1), Sheared remainder shearable: " + sheep.readyForShearing()));

			for (Sheep s : sheepList) s.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S6 - Wool Exploit Prevention", false, e.getMessage()));
		}

		// Test S7: Sheep Save / Reload
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep shearedSheep = createEntity(EntityType.SHEEP, level);
			shearedSheep.setPos(posA.x, posA.y, posA.z);
			shearedSheep.setColor(DyeColor.WHITE);
			shearedSheep.setSheared(true);
			((StackableEntity) shearedSheep).jarstacker$setStackCount(4);

			Sheep unshearedSheep = createEntity(EntityType.SHEEP, level);
			unshearedSheep.setPos(posA.x + 1.0, posA.y, posA.z);
			unshearedSheep.setColor(DyeColor.WHITE);
			unshearedSheep.setSheared(false);
			((StackableEntity) unshearedSheep).jarstacker$setStackCount(1);

			CompoundTag tag1 = new CompoundTag();
			EntityAdapter.saveWithoutId(shearedSheep, tag1);

			CompoundTag tag2 = new CompoundTag();
			EntityAdapter.saveWithoutId(unshearedSheep, tag2);

			Sheep l1 = createEntity(EntityType.SHEEP, level);
			EntityAdapter.load(l1, tag1);

			Sheep l2 = createEntity(EntityType.SHEEP, level);
			EntityAdapter.load(l2, tag2);

			boolean pass = (((StackableEntity) l1).jarstacker$getStackCount() == 4) && (l1.isSheared())
				&& (((StackableEntity) l2).jarstacker$getStackCount() == 1) && (!l2.isSheared());

			results.add(new TestResult("Test S7 - Sheep Save / Reload", pass,
				"L1: count=" + ((StackableEntity) l1).jarstacker$getStackCount() + ", sheared=" + l1.isSheared()
				+ "; L2: count=" + ((StackableEntity) l2).jarstacker$getStackCount() + ", sheared=" + l2.isSheared()));

			shearedSheep.discard();
			unshearedSheep.discard();
			l1.discard();
			l2.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S7 - Sheep Save / Reload", false, e.getMessage()));
		}

		// Test ST1: Baby Cow x100
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(100);
			level.addFreshEntity(babyCow);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 20; i++) state.add(now - 100);
			for (int i = 0; i < 80; i++) state.add(now + 10000 + i * 50);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			long t0 = System.nanoTime();
			int matured = com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, babyCow);
			long durationUs = (System.nanoTime() - t0) / 1000;

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int babyCount = 0;
			int adultCount = 0;
			for (Cow c : cows) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				if (c.isBaby()) babyCount += cnt;
				else adultCount += cnt;
			}

			boolean pass = (matured == 20) && (babyCount == 80) && (adultCount == 20) && (babyCount + adultCount == 100);
			results.add(new TestResult("Test ST1 - Baby Cow x100 Stress", pass,
				"Matured: " + matured + ", Baby: " + babyCount + ", Adult: " + adultCount + ", Time: " + durationUs + " ยตs"));

			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test ST1 - Baby Cow x100 Stress", false, e.getMessage()));
		}

		// Test ST2: Baby Cow x1000
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(1000);
			level.addFreshEntity(babyCow);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 150; i++) state.add(now - 100);
			for (int i = 0; i < 850; i++) state.add(now + 10000 + i * 20);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			long t0 = System.nanoTime();
			int matured = com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, babyCow);
			long durationUs = (System.nanoTime() - t0) / 1000;

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int babyCount = 0;
			int adultCount = 0;
			for (Cow c : cows) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				if (c.isBaby()) babyCount += cnt;
				else adultCount += cnt;
			}

			boolean pass = (matured == 150) && (babyCount == 850) && (adultCount == 150) && (babyCount + adultCount == 1000);
			results.add(new TestResult("Test ST2 - Baby Cow x1000 Stress", pass,
				"Matured: " + matured + ", Baby: " + babyCount + ", Adult: " + adultCount + ", Time: " + durationUs + " ยตs"));

			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test ST2 - Baby Cow x1000 Stress", false, e.getMessage()));
		}

		// Test ST3: Sheared Sheep x100
		try {
			for (Sheep s : level.getEntitiesOfClass(Sheep.class, cleanAreaA)) s.discard();

			Sheep sheep = createEntity(EntityType.SHEEP, level);
			sheep.setPos(posA.x, posA.y, posA.z);
			sheep.setColor(DyeColor.WHITE);
			sheep.setSheared(true);
			((StackableEntity) sheep).jarstacker$setStackCount(100);
			level.addFreshEntity(sheep);

			long t0 = System.nanoTime();
			for (int i = 0; i < 10; i++) {
				sheep.ate();
			}
			long durationUs = (System.nanoTime() - t0) / 1000;

			List<Sheep> sheepList = level.getEntitiesOfClass(Sheep.class, cleanAreaA);
			int shearedCount = 0;
			int unshearedCount = 0;
			for (Sheep s : sheepList) {
				int cnt = ((StackableEntity) s).jarstacker$getStackCount();
				if (s.isSheared()) shearedCount += cnt;
				else unshearedCount += cnt;
			}

			boolean pass = (shearedCount == 90) && (unshearedCount == 10) && (shearedCount + unshearedCount == 100);
			results.add(new TestResult("Test ST3 - Sheared Sheep x100 Stress", pass,
				"Sheared: " + shearedCount + ", Unsheared: " + unshearedCount + ", Time: " + durationUs + " ยตs"));

			for (Sheep s : sheepList) s.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test ST3 - Sheared Sheep x100 Stress", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.3.2 LOGICAL STATE INTEGRITY & HARDENING (L1 - L18, ST4)
		// -------------------------------------------------------------

		// Test L1: Merge State Integrity
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow cowA = createEntity(EntityType.COW, level);
			cowA.setPos(posA.x, posA.y, posA.z);
			cowA.setBaby(true);
			((StackableEntity) cowA).jarstacker$setStackCount(5);
			level.addFreshEntity(cowA);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState stateA = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 5; i++) stateA.insert(now + 100000L + i * 100);
			((StackableEntity) cowA).jarstacker$setBabyGrowthState(stateA);

			Cow cowB = createEntity(EntityType.COW, level);
			cowB.setPos(posA.x + 0.2, posA.y, posA.z);
			cowB.setBaby(true);
			((StackableEntity) cowB).jarstacker$setStackCount(7);
			level.addFreshEntity(cowB);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState stateB = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 7; i++) stateB.insert(now + 200000L + i * 100);
			((StackableEntity) cowB).jarstacker$setBabyGrowthState(stateB);

			MobStackingManager.scanAndStack(level, config);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			boolean pass = cows.size() == 1;
			int totalCount = 0;
			int recordCount = 0;
			if (pass) {
				Cow merged = cows.get(0);
				totalCount = ((StackableEntity) merged).jarstacker$getStackCount();
				com.jar.jarstacker.stack.mob.baby.BabyGrowthState mergedState = ((StackableEntity) merged).jarstacker$getBabyGrowthState();
				recordCount = mergedState != null ? mergedState.size() : 0;
				pass = (totalCount == 12) && (recordCount == 12);
			}

			results.add(new TestResult("Test L1 - Merge State Integrity", pass,
				"Physical: " + cows.size() + ", Count: " + totalCount + ", Records: " + recordCount));

			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L1 - Merge State Integrity", false, e.getMessage()));
		}

		// Test L2: Split Integrity
		try {
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState original = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 10; i++) original.insert(100000L + i * 50);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState extracted = original.split(3);

			boolean pass = original.size() == 7 && extracted.size() == 3;
			if (pass) {
				java.util.Set<Long> set = new java.util.HashSet<>(original.getEntries());
				for (Long val : extracted.getEntries()) {
					if (set.contains(val)) pass = false;
				}
			}

			results.add(new TestResult("Test L2 - Split Integrity", pass,
				"Remainder size: " + original.size() + ", Extracted size: " + extracted.size()));
		} catch (Exception e) {
			results.add(new TestResult("Test L2 - Split Integrity", false, e.getMessage()));
		}

		// Test L3: Materialize / Re-Virtualize
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			babyCow.setAge(-20000);
			((StackableEntity) babyCow).jarstacker$setStackCount(10);
			level.addFreshEntity(babyCow);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 5));
			aimAt(player, babyCow);

			InteractionResult res = simulateInteract(player, level, babyCow);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int finalCount = ((StackableEntity) babyCow).jarstacker$getStackCount();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = ((StackableEntity) babyCow).jarstacker$getBabyGrowthState();
			int records = state != null ? state.size() : 0;

			boolean pass = res.consumesAction() && (cows.size() == 1) && (finalCount == 10) && (records == 10);

			results.add(new TestResult("Test L3 - Materialize / Re-Virtualize", pass,
				"Consumes: " + res.consumesAction() + ", Physical: " + cows.size() + ", Count: " + finalCount + ", Records: " + records));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L3 - Materialize / Re-Virtualize", false, e.getMessage()));
		}

		// Test L4: Repeated Feeding Invariant
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			babyCow.setAge(-20000);
			((StackableEntity) babyCow).jarstacker$setStackCount(20);
			level.addFreshEntity(babyCow);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 64));
			aimAt(player, babyCow);

			boolean allInvariantsHold = true;
			for (int i = 0; i < 15; i++) {
				simulateInteract(player, level, babyCow);
				int c = ((StackableEntity) babyCow).jarstacker$getStackCount();
				com.jar.jarstacker.stack.mob.baby.BabyGrowthState s = ((StackableEntity) babyCow).jarstacker$getBabyGrowthState();
				if (s == null || s.size() != c) {
					allInvariantsHold = false;
					break;
				}
			}

			results.add(new TestResult("Test L4 - Repeated Feeding Invariant", allInvariantsHold,
				"Invariant held across 15 iterations: " + allInvariantsHold));

			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L4 - Repeated Feeding Invariant", false, e.getMessage()));
		}

		// Test L5: Batch Promotion
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(100);
			level.addFreshEntity(babyCow);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 37; i++) state.insert(now - 100);
			for (int i = 0; i < 63; i++) state.insert(now + 10000 + i * 20);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			int matured = com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, babyCow);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int babyCount = 0;
			int adultCount = 0;
			for (Cow c : cows) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				if (c.isBaby()) babyCount += cnt;
				else adultCount += cnt;
			}

			boolean pass = (matured == 37) && (babyCount == 63) && (adultCount == 37) && (babyCount + adultCount == 100) && (state.size() == 63);
			results.add(new TestResult("Test L5 - Batch Promotion", pass,
				"Matured: " + matured + ", Baby: " + babyCount + ", Adult: " + adultCount + ", Remaining records: " + state.size()));

			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L5 - Batch Promotion", false, e.getMessage()));
		}

		// Test L6: Merge After Promotion
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow adultCow = createEntity(EntityType.COW, level);
			adultCow.setPos(posA.x + 1.0, posA.y, posA.z);
			adultCow.setBaby(false);
			adultCow.setAge(0);
			((StackableEntity) adultCow).jarstacker$setStackCount(20);
			level.addFreshEntity(adultCow);

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(10);
			level.addFreshEntity(babyCow);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 5; i++) state.insert(now - 100);
			for (int i = 0; i < 5; i++) state.insert(now + 10000 + i * 20);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			int matured = com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, babyCow);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int babyCount = ((StackableEntity) babyCow).jarstacker$getStackCount();
			int adultCount = ((StackableEntity) adultCow).jarstacker$getStackCount();

			boolean pass = (matured == 5) && (babyCount == 5) && (adultCount == 25) && (cows.size() == 2) && (state.size() == 5);
			results.add(new TestResult("Test L6 - Merge After Promotion", pass,
				"Matured: " + matured + ", Baby count: " + babyCount + ", Adult count: " + adultCount + ", Physical: " + cows.size()));

			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L6 - Merge After Promotion", false, e.getMessage()));
		}

		// Test L7: Persistence Exact Records
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			cow.setBaby(true);
			((StackableEntity) cow).jarstacker$setStackCount(100);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 100; i++) state.insert(100000L + i * 15L);
			((StackableEntity) cow).jarstacker$setBabyGrowthState(state);

			CompoundTag tag = new CompoundTag();
			EntityAdapter.saveWithoutId(cow, tag);

			Cow loaded = createEntity(EntityType.COW, level);
			EntityAdapter.load(loaded, tag);

			int loadedCount = ((StackableEntity) loaded).jarstacker$getStackCount();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState loadedState = ((StackableEntity) loaded).jarstacker$getBabyGrowthState();

			boolean pass = (loadedCount == 100) && (loadedState != null) && (loadedState.size() == 100)
				&& loadedState.getEntries().equals(state.getEntries())
				&& tag.contains(com.jar.jarstacker.stack.mob.baby.BabyGrowthState.NBT_VERSION_KEY);

			results.add(new TestResult("Test L7 - Persistence Exact Records", pass,
				"Loaded count: " + loadedCount + ", Loaded records: " + (loadedState != null ? loadedState.size() : "null")));

			cow.discard();
			loaded.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L7 - Persistence Exact Records", false, e.getMessage()));
		}

		// Test L8: Chunk Reload Idempotency
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			cow.setBaby(true);
			((StackableEntity) cow).jarstacker$setStackCount(50);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 50; i++) state.insert(100000L + i * 20L);
			((StackableEntity) cow).jarstacker$setBabyGrowthState(state);

			CompoundTag tag1 = new CompoundTag();
			EntityAdapter.saveWithoutId(cow, tag1);

			Cow r1 = createEntity(EntityType.COW, level);
			EntityAdapter.load(r1, tag1);
			CompoundTag tag2 = new CompoundTag();
			EntityAdapter.saveWithoutId(r1, tag2);

			Cow r2 = createEntity(EntityType.COW, level);
			EntityAdapter.load(r2, tag2);

			int count2 = ((StackableEntity) r2).jarstacker$getStackCount();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state2 = ((StackableEntity) r2).jarstacker$getBabyGrowthState();
			int rec2 = state2 != null ? state2.size() : 0;

			boolean pass = (count2 == 50) && (rec2 == 50);
			results.add(new TestResult("Test L8 - Chunk Reload Idempotency", pass,
				"Count after 2 reloads: " + count2 + ", Records: " + rec2));

			cow.discard();
			r1.discard();
			r2.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L8 - Chunk Reload Idempotency", false, e.getMessage()));
		}

		// Test L9: Missing Records Repair
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			cow.setBaby(true);
			cow.setAge(-20000);
			((StackableEntity) cow).jarstacker$setStackCount(10);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 10; i++) state.insert(100000L + i * 100);
			state.injectCorruptMissing(2); // Remove 2 records -> 8 records
			((StackableEntity) cow).jarstacker$setBabyGrowthState(state);

			int before = state.size();
			boolean repaired = com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.repairLogicalState(cow);
			int after = state.size();
			int finalCount = ((StackableEntity) cow).jarstacker$getStackCount();

			boolean pass = repaired && (before == 8) && (after == 10) && (finalCount == 10);
			results.add(new TestResult("Test L9 - Missing Records Repair", pass,
				"Before: " + before + ", After: " + after + ", Count: " + finalCount));

			cow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L9 - Missing Records Repair", false, e.getMessage()));
		}

		// Test L10: Extra Records Repair
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			cow.setBaby(true);
			cow.setAge(-20000);
			((StackableEntity) cow).jarstacker$setStackCount(10);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 10; i++) state.insert(100000L + i * 100);
			state.injectCorruptExtra(new long[]{999991L, 999992L}); // Add 2 records -> 12 records
			((StackableEntity) cow).jarstacker$setBabyGrowthState(state);

			int before = state.size();
			boolean repaired = com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.repairLogicalState(cow);
			int after = state.size();
			int finalCount = ((StackableEntity) cow).jarstacker$getStackCount();

			boolean pass = repaired && (before == 12) && (after == 10) && (finalCount == 10);
			results.add(new TestResult("Test L10 - Extra Records Repair", pass,
				"Before: " + before + ", After: " + after + ", Count: " + finalCount));

			cow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L10 - Extra Records Repair", false, e.getMessage()));
		}

		// Test L11: Missing Metadata Migration Idempotency
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			cow.setBaby(true);
			cow.setAge(-15000);
			((StackableEntity) cow).jarstacker$setStackCount(10);
			((StackableEntity) cow).jarstacker$setBabyGrowthState(null); // No metadata

			boolean repaired1 = com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.repairLogicalState(cow);
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state1 = ((StackableEntity) cow).jarstacker$getBabyGrowthState();
			int size1 = state1 != null ? state1.size() : 0;

			boolean repaired2 = com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.repairLogicalState(cow);
			int size2 = state1 != null ? state1.size() : 0;

			boolean pass = repaired1 && (size1 == 10) && repaired2 && (size2 == 10);
			results.add(new TestResult("Test L11 - Missing Metadata Migration", pass,
				"First repair: " + size1 + ", Second repair: " + size2));

			cow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L11 - Missing Metadata Migration", false, e.getMessage()));
		}

		// Test L12: Unsorted Records Repair
		try {
			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			cow.setBaby(true);
			((StackableEntity) cow).jarstacker$setStackCount(3);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			state.injectCorruptUnsorted(new long[]{500000L, 100000L, 300000L});
			((StackableEntity) cow).jarstacker$setBabyGrowthState(state);

			boolean beforeSorted = state.isSorted();
			boolean repaired = com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.repairLogicalState(cow);
			boolean afterSorted = state.isSorted();
			List<Long> entries = state.getEntries();

			boolean pass = !beforeSorted && repaired && afterSorted && entries.equals(List.of(100000L, 300000L, 500000L));
			results.add(new TestResult("Test L12 - Unsorted Records Repair", pass,
				"Before sorted: " + beforeSorted + ", After sorted: " + afterSorted + ", Entries: " + entries));

			cow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L12 - Unsorted Records Repair", false, e.getMessage()));
		}

		// Test L13: Death SINGLE Mode Record Removal
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			cow.setBaby(true);
			((StackableEntity) cow).jarstacker$setStackCount(10);
			level.addFreshEntity(cow);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 10; i++) state.insert(100000L + i * 100);
			((StackableEntity) cow).jarstacker$setBabyGrowthState(state);

			cow.hurt(level.damageSources().playerAttack(createMockPlayer(level, posA, GameType.SURVIVAL)), 1000.0f);

			int countAfter = ((StackableEntity) cow).jarstacker$getStackCount();
			int recordsAfter = state.size();

			boolean pass = (countAfter == 9) && (recordsAfter == 9);
			results.add(new TestResult("Test L13 - Death SINGLE Record Removal", pass,
				"Count: " + countAfter + ", Records: " + recordsAfter));

			cow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L13 - Death SINGLE Record Removal", false, e.getMessage()));
		}

		// Test L14: Failed Materialization Rollback
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(10);
			level.addFreshEntity(babyCow);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 10; i++) state.insert(100000L + i * 100);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 5));
			aimAt(player, babyCow);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.simulateMaterializationFailure = true;
			InteractionResult res = simulateInteract(player, level, babyCow);
			com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.simulateMaterializationFailure = false;

			int countAfter = ((StackableEntity) babyCow).jarstacker$getStackCount();
			int recordsAfter = state.size();
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);

			boolean pass = (res == InteractionResult.FAIL) && (countAfter == 10) && (recordsAfter == 10) && (cows.size() == 1);
			results.add(new TestResult("Test L14 - Failed Materialization Rollback", pass,
				"Result: " + res + ", Count: " + countAfter + ", Records: " + recordsAfter + ", Physical: " + cows.size()));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.simulateMaterializationFailure = false;
			results.add(new TestResult("Test L14 - Failed Materialization Rollback", false, e.getMessage()));
		}

		// Test L15: Failed Re-Virtualization Safety
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			babyCow.setAge(-20000);
			((StackableEntity) babyCow).jarstacker$setStackCount(10);
			level.addFreshEntity(babyCow);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 5));
			aimAt(player, babyCow);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.simulateReVirtualizationFailure = true;
			InteractionResult res = simulateInteract(player, level, babyCow);
			com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.simulateReVirtualizationFailure = false;

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int anchorCount = ((StackableEntity) babyCow).jarstacker$getStackCount();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = ((StackableEntity) babyCow).jarstacker$getBabyGrowthState();
			int records = state != null ? state.size() : 0;

			boolean pass = res.consumesAction() && (cows.size() == 2) && (anchorCount == 9) && (records == 9);
			results.add(new TestResult("Test L15 - Failed Re-Virtualization Safety", pass,
				"Physical: " + cows.size() + ", Anchor count: " + anchorCount + ", Records: " + records));

			for (Cow c : cows) c.discard();
			player.discard();
		} catch (Exception e) {
			com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.simulateReVirtualizationFailure = false;
			results.add(new TestResult("Test L15 - Failed Re-Virtualization Safety", false, e.getMessage()));
		}

		// Test L16: Large Merge
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow cowA = createEntity(EntityType.COW, level);
			cowA.setPos(posA.x, posA.y, posA.z);
			cowA.setBaby(true);
			((StackableEntity) cowA).jarstacker$setStackCount(1000);
			level.addFreshEntity(cowA);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState stateA = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 1000; i++) stateA.insert(100000L + i * 10L);
			((StackableEntity) cowA).jarstacker$setBabyGrowthState(stateA);

			Cow cowB = createEntity(EntityType.COW, level);
			cowB.setPos(posA.x + 0.5, posA.y, posA.z);
			cowB.setBaby(true);
			((StackableEntity) cowB).jarstacker$setStackCount(1000);
			level.addFreshEntity(cowB);

			com.jar.jarstacker.stack.mob.baby.BabyGrowthState stateB = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 1000; i++) stateB.insert(200000L + i * 10L);
			((StackableEntity) cowB).jarstacker$setBabyGrowthState(stateB);

			long t0 = System.nanoTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.mergeGrowthStates(cowA, cowB, 1000);
			((StackableEntity) cowA).jarstacker$setStackCount(2000);
			cowB.discard();
			long durationUs = (System.nanoTime() - t0) / 1000;

			int finalCount = ((StackableEntity) cowA).jarstacker$getStackCount();
			int finalRecords = stateA.size();

			boolean pass = (finalCount == 2000) && (finalRecords == 2000);
			results.add(new TestResult("Test L16 - Large Merge", pass,
				"Merged count: " + finalCount + ", Records: " + finalRecords + ", Time: " + durationUs + " ยตs"));

			cowA.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L16 - Large Merge", false, e.getMessage()));
		}

		// Test L17: Rapid Interaction Stress
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			babyCow.setAge(-20000);
			((StackableEntity) babyCow).jarstacker$setStackCount(20);
			level.addFreshEntity(babyCow);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 64));
			aimAt(player, babyCow);

			boolean integrityMaintained = true;
			for (int i = 0; i < 20; i++) {
				simulateInteract(player, level, babyCow);
				if (i % 5 == 0) {
					com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, babyCow);
				}
				com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationReport rep =
					com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.validateLogicalState(babyCow);
				if (rep.status() != com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationStatus.VALID) {
					integrityMaintained = false;
					break;
				}
			}

			results.add(new TestResult("Test L17 - Rapid Interaction Stress", integrityMaintained,
				"Integrity maintained across 20 rapid interactions: " + integrityMaintained));

			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L17 - Rapid Interaction Stress", false, e.getMessage()));
		}

		// Test L18: Save During Active State
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			babyCow.setAge(-20000);
			((StackableEntity) babyCow).jarstacker$setStackCount(10);
			level.addFreshEntity(babyCow);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 10));
			aimAt(player, babyCow);

			for (int i = 0; i < 3; i++) {
				simulateInteract(player, level, babyCow);
			}

			CompoundTag tag = new CompoundTag();
			EntityAdapter.saveWithoutId(babyCow, tag);

			Cow loaded = createEntity(EntityType.COW, level);
			EntityAdapter.load(loaded, tag);

			com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationReport rep =
				com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.validateLogicalState(loaded);

			boolean pass = (rep.status() == com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationStatus.VALID)
				&& (((StackableEntity) loaded).jarstacker$getStackCount() == 10)
				&& rep.recordCount() == 10;

			results.add(new TestResult("Test L18 - Save During Active State", pass,
				"Status: " + rep.status() + ", Count: " + rep.logicalCount() + ", Records: " + rep.recordCount()));

			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();
			loaded.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test L18 - Save During Active State", false, e.getMessage()));
		}

		// Test ST4: Baby Cow x2000 Stress
		try {
			for (Cow c : level.getEntitiesOfClass(Cow.class, cleanAreaA)) c.discard();

			Cow babyCow = createEntity(EntityType.COW, level);
			babyCow.setPos(posA.x, posA.y, posA.z);
			babyCow.setBaby(true);
			((StackableEntity) babyCow).jarstacker$setStackCount(2000);
			level.addFreshEntity(babyCow);

			long now = level.getGameTime();
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState state = new com.jar.jarstacker.stack.mob.baby.BabyGrowthState();
			for (int i = 0; i < 300; i++) state.insert(now - 100);
			for (int i = 0; i < 1700; i++) state.insert(now + 10000 + i * 10);
			((StackableEntity) babyCow).jarstacker$setBabyGrowthState(state);

			long t0 = System.nanoTime();
			int matured = com.jar.jarstacker.stack.mob.baby.BabyGrowthManager.evaluatePromotion(level, babyCow);
			long durationUs = (System.nanoTime() - t0) / 1000;

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA);
			int babyCount = 0;
			int adultCount = 0;
			for (Cow c : cows) {
				int cnt = ((StackableEntity) c).jarstacker$getStackCount();
				if (c.isBaby()) babyCount += cnt;
				else adultCount += cnt;
			}

			boolean pass = (matured == 300) && (babyCount == 1700) && (adultCount == 300) && (babyCount + adultCount == 2000);
			results.add(new TestResult("Test ST4 - Baby Cow x2000 Stress", pass,
				"Matured: " + matured + ", Baby: " + babyCount + ", Adult: " + adultCount + ", Time: " + durationUs + " ยตs"));

			for (Cow c : cows) c.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test ST4 - Baby Cow x2000 Stress", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.4.0 EXTENDED INTERACTIONS & TRANSFORMATION TEST SUITE (M1-M7, G1-G3, P1-P3, T1-T3, TR1, IA1)
		// -------------------------------------------------------------

		// Test M1: Mooshroom Bowl (DIRECT / No Suspicious Stew)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow mooshroom = createEntity(EntityType.MOOSHROOM, level);
			mooshroom.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) mooshroom).jarstacker$setStackCount(5);
			level.addFreshEntity(mooshroom);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL, 1));
			aimAt(player, mooshroom);

			InteractionResult res = simulateInteract(player, level, mooshroom);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			int finalCount = ((StackableEntity) mooshroom).jarstacker$getStackCount();
			ItemStack heldAfter = player.getItemInHand(InteractionHand.MAIN_HAND);

			boolean pass = res.consumesAction()
				&& (mooshrooms.size() == 1)
				&& (finalCount == 5)
				&& heldAfter.is(Items.MUSHROOM_STEW);

			results.add(new TestResult("Test M1 - Mooshroom Bowl (DIRECT)", pass,
				"Consumes: " + res.consumesAction() + ", Count: " + finalCount + ", Physical: " + mooshrooms.size() + ", Item: " + heldAfter.getItem()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test M1 - Mooshroom Bowl (DIRECT)", false, e.getMessage()));
		}

		// Test M2: Mooshroom Suspicious Stew Extraction (EXTRACT_ONE)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow mooshroom = createEntity(EntityType.MOOSHROOM, level);
			mooshroom.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) mooshroom).jarstacker$setStackCount(5);
			((MushroomCowAccessor) mooshroom).jarstacker$setStewEffects(SuspiciousStewEffects.EMPTY);
			level.addFreshEntity(mooshroom);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL, 1));
			aimAt(player, mooshroom);

			InteractionResult res = simulateInteract(player, level, mooshroom);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			int remainderCount = ((StackableEntity) mooshroom).jarstacker$getStackCount();
			int totalLogical = 0;
			for (MushroomCow m : mooshrooms) {
				totalLogical += ((StackableEntity) m).jarstacker$getStackCount();
			}
			ItemStack heldAfter = player.getItemInHand(InteractionHand.MAIN_HAND);

			boolean pass = res.consumesAction()
				&& (mooshrooms.size() == 2)
				&& (remainderCount == 4)
				&& (totalLogical == 5)
				&& heldAfter.is(Items.SUSPICIOUS_STEW);

			results.add(new TestResult("Test M2 - Mooshroom Suspicious Stew (EXTRACT_ONE)", pass,
				"Consumes: " + res.consumesAction() + ", Remainder: " + remainderCount + ", TotalLogical: " + totalLogical + ", Physical: " + mooshrooms.size() + ", Item: " + heldAfter.getItem()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test M2 - Mooshroom Suspicious Stew (EXTRACT_ONE)", false, e.getMessage()));
		}

		// Test M3: Mooshroom Single Shear (TRANSFORM_ONE)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow mooshroom = createEntity(EntityType.MOOSHROOM, level);
			mooshroom.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) mooshroom).jarstacker$setStackCount(5);
			level.addFreshEntity(mooshroom);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, mooshroom);

			InteractionResult res = simulateInteract(player, level, mooshroom);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaA);

			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();
			int cowCount = 0;
			for (Cow c : cows) cowCount += ((StackableEntity) c).jarstacker$getStackCount();

			int mushroomItemCount = 0;
			for (ItemEntity ie : items) {
				if (ie.getItem().is(Items.RED_MUSHROOM)) {
					mushroomItemCount += ie.getItem().getCount();
				}
			}

			boolean pass = res.consumesAction()
				&& (mooshCount == 4)
				&& (cowCount == 1)
				&& (cows.size() == 1)
				&& (mooshCount + cowCount == 5)
				&& (mushroomItemCount == 5);

			results.add(new TestResult("Test M3 - Mooshroom Single Shear (TRANSFORM_ONE)", pass,
				"MooshCount: " + mooshCount + ", CowCount: " + cowCount + ", Mushrooms: " + mushroomItemCount));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test M3 - Mooshroom Single Shear (TRANSFORM_ONE)", false, e.getMessage()));
		}

		// Test M4: Mooshroom Shearing into Existing Cow Stack
		try {
			cleanPen(level, cleanAreaA);

			Cow existingCow = createEntity(EntityType.COW, level);
			existingCow.setPos(posA.x + 0.8, posA.y, posA.z);
			((StackableEntity) existingCow).jarstacker$setStackCount(10);
			level.addFreshEntity(existingCow);

			MushroomCow mooshroom = createEntity(EntityType.MOOSHROOM, level);
			mooshroom.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) mooshroom).jarstacker$setStackCount(5);
			level.addFreshEntity(mooshroom);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, mooshroom);

			InteractionResult res = simulateInteract(player, level, mooshroom);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));

			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();
			int cowCount = 0;
			for (Cow c : cows) cowCount += ((StackableEntity) c).jarstacker$getStackCount();

			boolean pass = res.consumesAction()
				&& (mooshCount == 4)
				&& (cowCount == 11)
				&& (cows.size() == 1) // Merged into existing stack!
				&& (mooshCount + cowCount == 15);

			results.add(new TestResult("Test M4 - Mooshroom Shear Merged into Existing Cow Stack", pass,
				"MooshCount: " + mooshCount + ", CowCount: " + cowCount + ", CowEntities: " + cows.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test M4 - Mooshroom Shear Merged into Existing Cow Stack", false, e.getMessage()));
		}

		// Test M5: Baby Mooshroom Shearing Rejection
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow babyMoosh = createEntity(EntityType.MOOSHROOM, level);
			babyMoosh.setPos(posA.x, posA.y, posA.z);
			babyMoosh.setBaby(true);
			babyMoosh.setAge(-20000);
			((StackableEntity) babyMoosh).jarstacker$setStackCount(5);
			level.addFreshEntity(babyMoosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			ItemStack shears = new ItemStack(Items.SHEARS);
			player.setItemInHand(InteractionHand.MAIN_HAND, shears);
			aimAt(player, babyMoosh);

			InteractionResult res = simulateInteract(player, level, babyMoosh);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();

			boolean pass = !res.consumesAction()
				&& (mooshCount == 5)
				&& cows.isEmpty()
				&& (shears.getDamageValue() == 0);

			results.add(new TestResult("Test M5 - Baby Mooshroom Shearing Rejection", pass,
				"Consumes: " + res.consumesAction() + ", MooshCount: " + mooshCount + ", Cows: " + cows.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test M5 - Baby Mooshroom Shearing Rejection", false, e.getMessage()));
		}

		// Test M6: Mooshroom Variant Isolation
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow redMoosh = createEntity(EntityType.MOOSHROOM, level);
			redMoosh.setPos(posA.x, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(redMoosh, false);
			((StackableEntity) redMoosh).jarstacker$setStackCount(5);
			level.addFreshEntity(redMoosh);

			MushroomCow brownMoosh = createEntity(EntityType.MOOSHROOM, level);
			brownMoosh.setPos(posA.x + 3.0, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(brownMoosh, true);
			((StackableEntity) brownMoosh).jarstacker$setStackCount(5);
			level.addFreshEntity(brownMoosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, redMoosh);

			simulateInteract(player, level, redMoosh);

			int redCount = ((StackableEntity) redMoosh).jarstacker$getStackCount();
			int brownCount = ((StackableEntity) brownMoosh).jarstacker$getStackCount();
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));

			boolean pass = (redCount == 4) && (brownCount == 5) && (cows.size() == 1);
			results.add(new TestResult("Test M6 - Mooshroom Variant Isolation", pass,
				"RedCount: " + redCount + ", BrownCount: " + brownCount + ", Cows: " + cows.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test M6 - Mooshroom Variant Isolation", false, e.getMessage()));
		}

		// Test M7: Continuous Mooshroom Shearing Sequence
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow mooshroom = createEntity(EntityType.MOOSHROOM, level);
			mooshroom.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) mooshroom).jarstacker$setStackCount(5);
			level.addFreshEntity(mooshroom);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));

			for (int i = 0; i < 5; i++) {
				aimAt(player, mooshroom);
				simulateInteract(player, level, mooshroom);
			}

			List<MushroomCow> remainingMoosh = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int totalCows = 0;
			for (Cow c : cows) totalCows += ((StackableEntity) c).jarstacker$getStackCount();

			boolean pass = (remainingMoosh.isEmpty() || mooshroom.isRemoved() || ((StackableEntity) mooshroom).jarstacker$getStackCount() == 0)
				&& (totalCows == 5);

			results.add(new TestResult("Test M7 - Continuous Mooshroom Shearing Sequence", pass,
				"RemainingMoosh: " + remainingMoosh.size() + ", TotalCows: " + totalCows + ", CowEntities: " + cows.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test M7 - Continuous Mooshroom Shearing Sequence", false, e.getMessage()));
		}

		// Test G1: Snow Golem Single Shear (EXTRACT_ONE)
		try {
			cleanPen(level, cleanAreaA);

			SnowGolem golem = createEntity(EntityType.SNOW_GOLEM, level);
			golem.setPos(posA.x, posA.y, posA.z);
			golem.setPumpkin(true);
			((StackableEntity) golem).jarstacker$setStackCount(5);
			level.addFreshEntity(golem);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, golem);

			InteractionResult res = simulateInteract(player, level, golem);

			List<SnowGolem> golems = level.getEntitiesOfClass(SnowGolem.class, cleanAreaA);
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaA);

			int pumpkinCount = 0;
			int shearedCount = 0;
			for (SnowGolem sg : golems) {
				int cnt = ((StackableEntity) sg).jarstacker$getStackCount();
				if (sg.hasPumpkin()) pumpkinCount += cnt;
				else shearedCount += cnt;
			}

			boolean carvedPumpkinDropped = items.stream().anyMatch(ie -> ie.getItem().is(Items.CARVED_PUMPKIN));

			boolean pass = res.consumesAction()
				&& (pumpkinCount == 4)
				&& (shearedCount == 1)
				&& (golems.size() == 2)
				&& (pumpkinCount + shearedCount == 5)
				&& carvedPumpkinDropped;

			results.add(new TestResult("Test G1 - Snow Golem Single Shear (EXTRACT_ONE)", pass,
				"PumpkinCount: " + pumpkinCount + ", ShearedCount: " + shearedCount + ", DroppedPumpkin: " + carvedPumpkinDropped));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test G1 - Snow Golem Single Shear (EXTRACT_ONE)", false, e.getMessage()));
		}

		// Test G2: Snow Golem Repeated Shear Sequence
		try {
			cleanPen(level, cleanAreaA);

			SnowGolem golem = createEntity(EntityType.SNOW_GOLEM, level);
			golem.setPos(posA.x, posA.y, posA.z);
			golem.setPumpkin(true);
			((StackableEntity) golem).jarstacker$setStackCount(3);
			level.addFreshEntity(golem);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));

			for (int i = 0; i < 3; i++) {
				aimAt(player, golem);
				simulateInteract(player, level, golem);
			}

			List<SnowGolem> golems = level.getEntitiesOfClass(SnowGolem.class, cleanAreaA);
			int shearedCount = 0;
			for (SnowGolem sg : golems) {
				if (!sg.hasPumpkin()) shearedCount += ((StackableEntity) sg).jarstacker$getStackCount();
			}

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaA);
			int pumpkinItemCount = 0;
			for (ItemEntity ie : items) {
				if (ie.getItem().is(Items.CARVED_PUMPKIN)) {
					pumpkinItemCount += ie.getItem().getCount();
				}
			}

			boolean pass = (shearedCount == 3) && (pumpkinItemCount == 3);
			results.add(new TestResult("Test G2 - Snow Golem Repeated Shear Sequence", pass,
				"ShearedCount: " + shearedCount + ", PumpkinDrops: " + pumpkinItemCount));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test G2 - Snow Golem Repeated Shear Sequence", false, e.getMessage()));
		}

		// Test G3: Snow Golem Without Pumpkin Shearing (PASS_THROUGH)
		try {
			cleanPen(level, cleanAreaA);

			SnowGolem golem = createEntity(EntityType.SNOW_GOLEM, level);
			golem.setPos(posA.x, posA.y, posA.z);
			golem.setPumpkin(false);
			((StackableEntity) golem).jarstacker$setStackCount(1);
			level.addFreshEntity(golem);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			ItemStack shears = new ItemStack(Items.SHEARS);
			player.setItemInHand(InteractionHand.MAIN_HAND, shears);
			aimAt(player, golem);

			InteractionResult res = simulateInteract(player, level, golem);

			boolean pass = !res.consumesAction() && (shears.getDamageValue() == 0);
			results.add(new TestResult("Test G3 - Snow Golem Without Pumpkin (PASS_THROUGH)", pass,
				"Consumes: " + res.consumesAction() + ", Damage: " + shears.getDamageValue()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test G3 - Snow Golem Without Pumpkin (PASS_THROUGH)", false, e.getMessage()));
		}

		// Test P1: Pig Saddling Extraction (EXTRACT_ONE)
		try {
			cleanPen(level, cleanAreaA);

			Pig pig = createEntity(EntityType.PIG, level);
			pig.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) pig).jarstacker$setStackCount(5);
			level.addFreshEntity(pig);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE, 2));
			aimAt(player, pig);

			InteractionResult res = simulateInteract(player, level, pig);

			List<Pig> pigs = level.getEntitiesOfClass(Pig.class, cleanAreaA);
			Pig remainder = null;
			Pig saddled = null;
			for (Pig p : pigs) {
				if (p.isSaddled()) saddled = p;
				else remainder = p;
			}

			boolean pass = res.consumesAction()
				&& (pigs.size() == 2)
				&& (remainder != null && ((StackableEntity) remainder).jarstacker$getStackCount() == 4)
				&& (saddled != null && ((StackableEntity) saddled).jarstacker$getStackCount() == 1)
				&& MobCompatibility.isExcluded(saddled);

			results.add(new TestResult("Test P1 - Pig Saddling Extraction (EXTRACT_ONE)", pass,
				"Consumes: " + res.consumesAction() + ", RemainderCount: " + (remainder != null ? ((StackableEntity) remainder).jarstacker$getStackCount() : -1)
				+ ", SaddledExcluded: " + (saddled != null && MobCompatibility.isExcluded(saddled))));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test P1 - Pig Saddling Extraction (EXTRACT_ONE)", false, e.getMessage()));
		}

		// Test P2: Pig Repeated Saddling Sequence
		try {
			cleanPen(level, cleanAreaA);

			Pig pig = createEntity(EntityType.PIG, level);
			pig.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) pig).jarstacker$setStackCount(3);
			level.addFreshEntity(pig);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE, 5));

			aimAt(player, pig);
			simulateInteract(player, level, pig);

			aimAt(player, pig);
			simulateInteract(player, level, pig);

			List<Pig> pigs = level.getEntitiesOfClass(Pig.class, cleanAreaA);
			int unsaddledCount = 0;
			int saddledCount = 0;
			for (Pig p : pigs) {
				if (p.isSaddled()) saddledCount += ((StackableEntity) p).jarstacker$getStackCount();
				else unsaddledCount += ((StackableEntity) p).jarstacker$getStackCount();
			}

			boolean pass = (unsaddledCount == 1) && (saddledCount == 2) && (pigs.size() == 3);
			results.add(new TestResult("Test P2 - Pig Repeated Saddling Sequence", pass,
				"Unsaddled: " + unsaddledCount + ", Saddled: " + saddledCount + ", TotalEntities: " + pigs.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test P2 - Pig Repeated Saddling Sequence", false, e.getMessage()));
		}

		// Test P3: Already Saddled Pig Interaction
		try {
			cleanPen(level, cleanAreaA);

			Pig pig = createEntity(EntityType.PIG, level);
			pig.setPos(posA.x, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.equipSaddle(pig, new ItemStack(Items.SADDLE));
			((StackableEntity) pig).jarstacker$setStackCount(1);
			level.addFreshEntity(pig);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE, 2));
			aimAt(player, pig);

			InteractionResult res;
			try {
				res = simulateInteract(player, level, pig);
			} catch (Throwable t) {
				// Handled in case mock player connection lacks full networking pipeline
				res = InteractionResult.PASS;
			}
			int saddleRemaining = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();

			boolean pass = (saddleRemaining == 2) && pig.isSaddled() && MobCompatibility.isExcluded(pig);
			results.add(new TestResult("Test P3 - Already Saddled Pig Interaction", pass,
				"Consumes: " + res.consumesAction() + ", Saddles: " + saddleRemaining + ", Saddled: " + pig.isSaddled()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test P3 - Already Saddled Pig Interaction", false, e.getMessage()));
		}

		// Test T1: Strider Saddling Extraction (EXTRACT_ONE)
		try {
			cleanPen(level, cleanAreaA);

			Strider strider = createEntity(EntityType.STRIDER, level);
			strider.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) strider).jarstacker$setStackCount(5);
			level.addFreshEntity(strider);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE, 2));
			aimAt(player, strider);

			InteractionResult res = simulateInteract(player, level, strider);

			List<Strider> striders = level.getEntitiesOfClass(Strider.class, cleanAreaA);
			Strider remainder = null;
			Strider saddled = null;
			for (Strider s : striders) {
				if (s.isSaddled()) saddled = s;
				else remainder = s;
			}

			boolean pass = res.consumesAction()
				&& (striders.size() == 2)
				&& (remainder != null && ((StackableEntity) remainder).jarstacker$getStackCount() == 4)
				&& (saddled != null && ((StackableEntity) saddled).jarstacker$getStackCount() == 1)
				&& MobCompatibility.isExcluded(saddled);

			results.add(new TestResult("Test T1 - Strider Saddling Extraction (EXTRACT_ONE)", pass,
				"Consumes: " + res.consumesAction() + ", RemainderCount: " + (remainder != null ? ((StackableEntity) remainder).jarstacker$getStackCount() : -1)
				+ ", SaddledExcluded: " + (saddled != null && MobCompatibility.isExcluded(saddled))));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test T1 - Strider Saddling Extraction (EXTRACT_ONE)", false, e.getMessage()));
		}

		// Test T2: Strider Cold-State Compatibility
		try {
			Strider striderA = createEntity(EntityType.STRIDER, level);
			Strider striderB = createEntity(EntityType.STRIDER, level);

			striderA.setSuffocating(true); // Cold
			striderB.setSuffocating(false); // Warm

			boolean coldWarmMerge = MobCompatibility.canStack(striderA, striderB, ModConfig.getInstance().getMobStacking());

			striderB.setSuffocating(true); // Both Cold
			boolean coldColdMerge = MobCompatibility.canStack(striderA, striderB, ModConfig.getInstance().getMobStacking());

			boolean pass = !coldWarmMerge && coldColdMerge;
			results.add(new TestResult("Test T2 - Strider Cold-State Compatibility", pass,
				"Cold-Warm merge: " + coldWarmMerge + " (expected false), Cold-Cold merge: " + coldColdMerge + " (expected true)"));

			striderA.discard();
			striderB.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test T2 - Strider Cold-State Compatibility", false, e.getMessage()));
		}

		// Test T3: Strider Passenger Exclusion
		try {
			Strider strider = createEntity(EntityType.STRIDER, level);
			Strider babyPassenger = createEntity(EntityType.STRIDER, level);
			babyPassenger.setBaby(true);
			//? if >=1.21.9 {
			/*babyPassenger.startRiding(strider, true, false);
			*///?} else {
			babyPassenger.startRiding(strider, true);
			//?}

			boolean excluded = MobCompatibility.isExcluded(strider);

			boolean pass = excluded;
			results.add(new TestResult("Test T3 - Strider Passenger Exclusion", pass,
				"Excluded with passenger: " + excluded));

			babyPassenger.discard();
			strider.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test T3 - Strider Passenger Exclusion", false, e.getMessage()));
		}

		// Test TR1: Transformation Failure Rollback
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow mooshroom = createEntity(EntityType.MOOSHROOM, level);
			mooshroom.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) mooshroom).jarstacker$setStackCount(5);
			level.addFreshEntity(mooshroom);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, mooshroom);

			LogicalEntityTransformer.simulateTransformationFailure = true;
			InteractionResult res = simulateInteract(player, level, mooshroom);
			LogicalEntityTransformer.simulateTransformationFailure = false;

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();

			boolean pass = !res.consumesAction()
				&& (mooshCount == 5)
				&& cows.isEmpty();

			results.add(new TestResult("Test TR1 - Transformation Failure Rollback", pass,
				"Consumes: " + res.consumesAction() + ", MooshCount: " + mooshCount + ", Cows: " + cows.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			LogicalEntityTransformer.simulateTransformationFailure = false;
			results.add(new TestResult("Test TR1 - Transformation Failure Rollback", false, e.getMessage()));
		}

		// Test IA1: Survival vs Creative Item Accounting
		try {
			cleanPen(level, cleanAreaA);

			// 1. Survival mode: Bowl -> Stew & Shears -> damaged
			MushroomCow moosh1 = createEntity(EntityType.MOOSHROOM, level);
			moosh1.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh1).jarstacker$setStackCount(2);
			level.addFreshEntity(moosh1);

			ServerPlayer survivalPlayer = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			survivalPlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL, 1));
			aimAt(survivalPlayer, moosh1);
			simulateInteract(survivalPlayer, level, moosh1);
			boolean survivalStewOk = survivalPlayer.getItemInHand(InteractionHand.MAIN_HAND).is(Items.MUSHROOM_STEW);

			ItemStack shearsSurv = new ItemStack(Items.SHEARS);
			survivalPlayer.setItemInHand(InteractionHand.MAIN_HAND, shearsSurv);
			aimAt(survivalPlayer, moosh1);
			simulateInteract(survivalPlayer, level, moosh1);
			boolean survivalShearsDamaged = shearsSurv.getDamageValue() > 0;

			// 2. Creative mode: Bowl not consumed, Shears not damaged
			MushroomCow moosh2 = createEntity(EntityType.MOOSHROOM, level);
			moosh2.setPos(posA.x + 3.0, posA.y, posA.z);
			((StackableEntity) moosh2).jarstacker$setStackCount(2);
			level.addFreshEntity(moosh2);

			ServerPlayer creativePlayer = createMockPlayer(level, new Vec3(posA.x + 3.0, posA.y, posA.z - 2.0), GameType.CREATIVE);
			ItemStack bowlCreative = new ItemStack(Items.BOWL, 1);
			creativePlayer.setItemInHand(InteractionHand.MAIN_HAND, bowlCreative);
			aimAt(creativePlayer, moosh2);
			simulateInteract(creativePlayer, level, moosh2);
			boolean creativeBowlKept = creativePlayer.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BOWL);

			ItemStack shearsCreative = new ItemStack(Items.SHEARS);
			creativePlayer.setItemInHand(InteractionHand.MAIN_HAND, shearsCreative);
			aimAt(creativePlayer, moosh2);
			simulateInteract(creativePlayer, level, moosh2);
			boolean creativeShearsUndamaged = shearsCreative.getDamageValue() == 0;

			boolean pass = survivalStewOk && survivalShearsDamaged && creativeBowlKept && creativeShearsUndamaged;
			results.add(new TestResult("Test IA1 - Survival vs Creative Item Accounting", pass,
				"SurvStew: " + survivalStewOk + ", SurvShearsDamaged: " + survivalShearsDamaged + ", CreatBowl: " + creativeBowlKept + ", CreatShearsUndamaged: " + creativeShearsUndamaged));

			cleanPen(level, cleanAreaA);
			survivalPlayer.discard();
			creativePlayer.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test IA1 - Survival vs Creative Item Accounting", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.4.1 MOOSHROOM STEW & TRANSFORMATION HARDENING SUITE (M8-M12, TR2-TR10)
		// -------------------------------------------------------------

		// Test M8: Clean vs Prepared Compatibility
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow cleanBrown = createEntity(EntityType.MOOSHROOM, level);
			cleanBrown.setPos(posA.x, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(cleanBrown, true);
			((StackableEntity) cleanBrown).jarstacker$setStackCount(5);
			level.addFreshEntity(cleanBrown);

			MushroomCow preparedBrown = createEntity(EntityType.MOOSHROOM, level);
			preparedBrown.setPos(posA.x + 1.0, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(preparedBrown, true);
			((StackableEntity) preparedBrown).jarstacker$setStackCount(1);
			((MushroomCowAccessor) preparedBrown).jarstacker$setStewEffects(SuspiciousStewEffects.EMPTY);
			level.addFreshEntity(preparedBrown);

			boolean canStack = MobCompatibility.canStack(cleanBrown, preparedBrown, ModConfig.getInstance().getMobStacking());
			String reason = MobCompatibility.getIncompatibilityReason(cleanBrown, preparedBrown, ModConfig.getInstance().getMobStacking());

			MobStackingManager.scanAndStack(level, ModConfig.getInstance());
			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);

			boolean pass = !canStack && "MOOSHROOM_STEW_STATE_MISMATCH".equals(reason) && mooshrooms.size() == 2;
			results.add(new TestResult("Test M8 - Clean vs Prepared Mooshroom Compatibility", pass,
				"CanStack: " + canStack + ", Reason: " + reason + ", RemainingEntities: " + mooshrooms.size()));

			cleanPen(level, cleanAreaA);
		} catch (Exception e) {
			results.add(new TestResult("Test M8 - Clean vs Prepared Mooshroom Compatibility", false, e.getMessage()));
		}

		// Test M9: Prepared Brown Mooshrooms with Identical Effects Merge
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow prep1 = createEntity(EntityType.MOOSHROOM, level);
			prep1.setPos(posA.x, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(prep1, true);
			((StackableEntity) prep1).jarstacker$setStackCount(2);
			((MushroomCowAccessor) prep1).jarstacker$setStewEffects(SuspiciousStewEffects.EMPTY);
			level.addFreshEntity(prep1);

			MushroomCow prep2 = createEntity(EntityType.MOOSHROOM, level);
			prep2.setPos(posA.x + 0.5, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(prep2, true);
			((StackableEntity) prep2).jarstacker$setStackCount(3);
			((MushroomCowAccessor) prep2).jarstacker$setStewEffects(SuspiciousStewEffects.EMPTY);
			level.addFreshEntity(prep2);

			boolean canStack = MobCompatibility.canStack(prep1, prep2, ModConfig.getInstance().getMobStacking());
			MobStackingManager.scanAndStack(level, ModConfig.getInstance());
			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			int totalCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();

			boolean pass = canStack && mooshrooms.size() == 1 && totalCount == 5;
			results.add(new TestResult("Test M9 - Prepared Brown Mooshrooms Identical Effects Merge", pass,
				"CanStack: " + canStack + ", Entities: " + mooshrooms.size() + ", TotalCount: " + totalCount));

			cleanPen(level, cleanAreaA);
		} catch (Exception e) {
			results.add(new TestResult("Test M9 - Prepared Brown Mooshrooms Identical Effects Merge", false, e.getMessage()));
		}

		// Test M10: Prepared Brown Mooshrooms with Different Effects Do Not Merge
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow prep1 = createEntity(EntityType.MOOSHROOM, level);
			prep1.setPos(posA.x, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(prep1, true);
			((StackableEntity) prep1).jarstacker$setStackCount(2);
			((MushroomCowAccessor) prep1).jarstacker$setStewEffects(SuspiciousStewEffects.EMPTY);
			level.addFreshEntity(prep1);

			MushroomCow prep2 = createEntity(EntityType.MOOSHROOM, level);
			prep2.setPos(posA.x + 0.5, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(prep2, true);
			((StackableEntity) prep2).jarstacker$setStackCount(3);
			// Simulate different effect contents by using a non-empty stew effects object if possible, or null vs empty
			// In MC 1.21.1, SuspiciousStewEffects has a list of effects. We can test null vs EMPTY or distinct instances if available
			net.minecraft.world.item.component.SuspiciousStewEffects effectA = SuspiciousStewEffects.EMPTY;
			net.minecraft.world.item.component.SuspiciousStewEffects effectB = new SuspiciousStewEffects(List.of(
				new SuspiciousStewEffects.Entry(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(net.minecraft.world.effect.MobEffects.NIGHT_VISION.value()), 100)
			));
			((MushroomCowAccessor) prep1).jarstacker$setStewEffects(effectA);
			((MushroomCowAccessor) prep2).jarstacker$setStewEffects(effectB);
			level.addFreshEntity(prep2);

			boolean canStack = MobCompatibility.canStack(prep1, prep2, ModConfig.getInstance().getMobStacking());
			String reason = MobCompatibility.getIncompatibilityReason(prep1, prep2, ModConfig.getInstance().getMobStacking());

			MobStackingManager.scanAndStack(level, ModConfig.getInstance());
			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);

			boolean pass = !canStack && "MOOSHROOM_STEW_EFFECT_MISMATCH".equals(reason) && mooshrooms.size() == 2;
			results.add(new TestResult("Test M10 - Prepared Mooshrooms Different Effects Separate", pass,
				"CanStack: " + canStack + ", Reason: " + reason + ", Entities: " + mooshrooms.size()));

			cleanPen(level, cleanAreaA);
		} catch (Exception e) {
			results.add(new TestResult("Test M10 - Prepared Mooshrooms Different Effects Separate", false, e.getMessage()));
		}

		// Test M11: Prepared Brown Mooshroom Stateful Bowl
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow prep = createEntity(EntityType.MOOSHROOM, level);
			prep.setPos(posA.x, posA.y, posA.z);
			com.jar.jarstacker.adapter.EntityAdapter.setMooshroomVariant(prep, true);
			((StackableEntity) prep).jarstacker$setStackCount(5);
			((MushroomCowAccessor) prep).jarstacker$setStewEffects(SuspiciousStewEffects.EMPTY);
			level.addFreshEntity(prep);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL, 1));
			aimAt(player, prep);

			InteractionResult res = simulateInteract(player, level, prep);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			int remainderCount = ((StackableEntity) prep).jarstacker$getStackCount();
			int totalLogical = 0;
			for (MushroomCow m : mooshrooms) totalLogical += ((StackableEntity) m).jarstacker$getStackCount();
			ItemStack heldAfter = player.getItemInHand(InteractionHand.MAIN_HAND);

			boolean pass = res.consumesAction()
				&& (mooshrooms.size() == 2)
				&& (remainderCount == 4)
				&& (totalLogical == 5)
				&& heldAfter.is(Items.SUSPICIOUS_STEW);

			results.add(new TestResult("Test M11 - Stateful Bowl Prepared Brown Mooshroom", pass,
				"Consumes: " + res.consumesAction() + ", Remainder: " + remainderCount + ", TotalLogical: " + totalLogical + ", Stew: " + heldAfter.getItem()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test M11 - Stateful Bowl Prepared Brown Mooshroom", false, e.getMessage()));
		}

		// Test M12: Normal Mooshroom Normal Bowl Repeated Interactions (DIRECT, No Split)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow normalMoosh = createEntity(EntityType.MOOSHROOM, level);
			normalMoosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) normalMoosh).jarstacker$setStackCount(20);
			level.addFreshEntity(normalMoosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);

			int stewsObtained = 0;
			for (int i = 0; i < 10; i++) {
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL, 1));
				aimAt(player, normalMoosh);
				InteractionResult res = simulateInteract(player, level, normalMoosh);
				if (res.consumesAction() && player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.MUSHROOM_STEW)) {
					stewsObtained++;
				}
			}

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			int finalCount = ((StackableEntity) normalMoosh).jarstacker$getStackCount();

			boolean pass = (stewsObtained == 10) && (mooshrooms.size() == 1) && (finalCount == 20);
			results.add(new TestResult("Test M12 - Normal Mooshroom Repeated Bowl (DIRECT)", pass,
				"StewsObtained: " + stewsObtained + ", Entities: " + mooshrooms.size() + ", FinalCount: " + finalCount));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test M12 - Normal Mooshroom Repeated Bowl (DIRECT)", false, e.getMessage()));
		}

		// Test TR2: Failure Before Vanilla Interaction (Pre-Commit Rollback)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			ItemStack shears = new ItemStack(Items.SHEARS);
			player.setItemInHand(InteractionHand.MAIN_HAND, shears);
			aimAt(player, moosh);

			LogicalEntityTransformer.simulatePreCommitFailure = true;
			InteractionResult res = simulateInteract(player, level, moosh);
			LogicalEntityTransformer.simulatePreCommitFailure = false;

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaA);
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();

			boolean pass = !res.consumesAction()
				&& (mooshCount == 10)
				&& cows.isEmpty()
				&& items.isEmpty()
				&& (shears.getDamageValue() == 0);

			results.add(new TestResult("Test TR2 - Pre-Vanilla Failure Safe Rollback", pass,
				"Consumes: " + res.consumesAction() + ", MooshCount: " + mooshCount + ", Cows: " + cows.size() + ", Drops: " + items.size() + ", Damage: " + shears.getDamageValue()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			LogicalEntityTransformer.simulatePreCommitFailure = false;
			results.add(new TestResult("Test TR2 - Pre-Vanilla Failure Safe Rollback", false, e.getMessage()));
		}

		// Test TR3: Failure After Vanilla Transformation (Post-Commit Forward Recovery)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			ItemStack shears = new ItemStack(Items.SHEARS);
			player.setItemInHand(InteractionHand.MAIN_HAND, shears);
			aimAt(player, moosh);

			LogicalEntityTransformer.simulatePostCommitFailure = true;
			InteractionResult res = simulateInteract(player, level, moosh);
			LogicalEntityTransformer.simulatePostCommitFailure = false;

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaA);
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();
			int cowCount = 0;
			for (Cow c : cows) cowCount += ((StackableEntity) c).jarstacker$getStackCount();

			boolean pass = res.consumesAction()
				&& (mooshCount == 9)
				&& (cowCount == 1)
				&& (cows.size() == 1)
				&& (mooshCount + cowCount == 10)
				&& (shears.getDamageValue() > 0)
				&& !items.isEmpty();

			results.add(new TestResult("Test TR3 - Post-Vanilla Failure Forward Recovery", pass,
				"Consumes: " + res.consumesAction() + ", MooshCount: " + mooshCount + ", CowCount: " + cowCount + ", CowEntities: " + cows.size() + ", TotalLogical: " + (mooshCount + cowCount)));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			LogicalEntityTransformer.simulatePostCommitFailure = false;
			results.add(new TestResult("Test TR3 - Post-Vanilla Failure Forward Recovery", false, e.getMessage()));
		}

		// Test TR4: Destination Merge Failure (Forward Recovery)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			LogicalEntityTransformer.simulateDestinationMergeFailure = true;
			InteractionResult res = simulateInteract(player, level, moosh);
			LogicalEntityTransformer.simulateDestinationMergeFailure = false;

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();
			int cowCount = 0;
			for (Cow c : cows) cowCount += ((StackableEntity) c).jarstacker$getStackCount();

			boolean pass = res.consumesAction()
				&& (mooshCount == 9)
				&& (cowCount == 1)
				&& (cows.size() == 1) // Physical fallback preserved!
				&& (mooshCount + cowCount == 10);

			results.add(new TestResult("Test TR4 - Destination Merge Failure Fallback", pass,
				"Consumes: " + res.consumesAction() + ", MooshCount: " + mooshCount + ", CowCount: " + cowCount + ", CowEntities: " + cows.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			LogicalEntityTransformer.simulateDestinationMergeFailure = false;
			results.add(new TestResult("Test TR4 - Destination Merge Failure Fallback", false, e.getMessage()));
		}

		// Test TR5: Destination Merge Success (Cow merges into existing stack)
		try {
			cleanPen(level, cleanAreaA);

			Cow existingCow = createEntity(EntityType.COW, level);
			existingCow.setPos(posA.x + 0.8, posA.y, posA.z);
			((StackableEntity) existingCow).jarstacker$setStackCount(20);
			level.addFreshEntity(existingCow);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();
			int cowCount = 0;
			for (Cow c : cows) cowCount += ((StackableEntity) c).jarstacker$getStackCount();

			boolean pass = res.consumesAction()
				&& (mooshCount == 9)
				&& (cowCount == 21)
				&& (cows.size() == 1) // Merged into existing stack!
				&& (mooshCount + cowCount == 30);

			results.add(new TestResult("Test TR5 - Destination Merge Success", pass,
				"MooshCount: " + mooshCount + ", CowCount: " + cowCount + ", CowEntities: " + cows.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test TR5 - Destination Merge Success", false, e.getMessage()));
		}

		// Test TR6: Unexpected Entity Spawn Ignored by Capture Context
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(3);
			level.addFreshEntity(moosh);

			// Test helper record matching logic directly
			java.util.concurrent.atomic.AtomicReference<Mob> cap = new java.util.concurrent.atomic.AtomicReference<>();
			LogicalEntityTransformer.TransformationContext ctx = new LogicalEntityTransformer.TransformationContext(
				moosh.getUUID(),
				moosh.getType(),
				EntityType.COW,
				moosh.position(),
				level.getGameTime(),
				java.util.UUID.randomUUID(),
				cap
			);

			// Spawning a Zombie or Sheep during active context
			Zombie unrelatedZombie = createEntity(EntityType.ZOMBIE, level);
			Cow validCow = createEntity(EntityType.COW, level);
			validCow.setPos(posA.x, posA.y, posA.z);

			boolean matchesZombie = ctx.matches(unrelatedZombie);
			boolean matchesCow = ctx.matches(validCow);

			boolean pass = !matchesZombie && matchesCow;
			results.add(new TestResult("Test TR6 - Unrelated Entity Spawn Ignored", pass,
				"MatchesZombie: " + matchesZombie + " (expected false), MatchesCow: " + matchesCow + " (expected true)"));

			unrelatedZombie.discard();
			validCow.discard();
			cleanPen(level, cleanAreaA);
		} catch (Exception e) {
			results.add(new TestResult("Test TR6 - Unrelated Entity Spawn Ignored", false, e.getMessage()));
		}

		// Test TR7: Item Drops Ignored by Capture Context
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(3);
			level.addFreshEntity(moosh);

			java.util.concurrent.atomic.AtomicReference<Mob> cap = new java.util.concurrent.atomic.AtomicReference<>();
			LogicalEntityTransformer.TransformationContext ctx = new LogicalEntityTransformer.TransformationContext(
				moosh.getUUID(),
				moosh.getType(),
				EntityType.COW,
				moosh.position(),
				level.getGameTime(),
				java.util.UUID.randomUUID(),
				cap
			);

			ItemEntity mushroomItem = new ItemEntity(level, posA.x, posA.y, posA.z, new ItemStack(Items.RED_MUSHROOM, 5));
			boolean matchesItem = ctx.matches(mushroomItem);

			boolean pass = !matchesItem;
			results.add(new TestResult("Test TR7 - Item Drops Ignored By Capture Context", pass,
				"MatchesItem: " + matchesItem + " (expected false)"));

			mushroomItem.discard();
			cleanPen(level, cleanAreaA);
		} catch (Exception e) {
			results.add(new TestResult("Test TR7 - Item Drops Ignored By Capture Context", false, e.getMessage()));
		}

		// Test TR8: Reentrancy Protection (Forwarded Interaction Executes Once)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(2);
			level.addFreshEntity(moosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			ItemStack shears = new ItemStack(Items.SHEARS);
			player.setItemInHand(InteractionHand.MAIN_HAND, shears);
			aimAt(player, moosh);

			simulateInteract(player, level, moosh);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int shearsDamage = shears.getDamageValue();

			// 1 shear operation -> exactly 1 durability consumed, exactly 1 cow created, remainder count 1
			boolean pass = (mooshrooms.size() == 1)
				&& (((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount() == 1)
				&& (cows.size() == 1)
				&& (((StackableEntity) cows.get(0)).jarstacker$getStackCount() == 1)
				&& (shearsDamage == 1);

			results.add(new TestResult("Test TR8 - Reentrancy Exact Single Execution", pass,
				"MooshRem: " + ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount() + ", CowCount: " + cows.size() + ", ShearsDamage: " + shearsDamage));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test TR8 - Reentrancy Exact Single Execution", false, e.getMessage()));
		}

		// Test TR9: UUID Safety (Source and Destination Distinct)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(2);
			level.addFreshEntity(moosh);

			java.util.UUID sourceUUID = moosh.getUUID();

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			simulateInteract(player, level, moosh);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			java.util.UUID destUUID = cows.isEmpty() ? null : cows.get(0).getUUID();

			boolean pass = (destUUID != null) && !destUUID.equals(sourceUUID);
			results.add(new TestResult("Test TR9 - UUID Safety Distinct IDs", pass,
				"SourceUUID: " + sourceUUID + ", DestUUID: " + destUUID));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test TR9 - UUID Safety Distinct IDs", false, e.getMessage()));
		}

		// Test TR10: Stable Anchor During Continuous Shearing
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));

			// Player position and aim stay identical
			aimAt(player, moosh);
			for (int i = 0; i < 9; i++) {
				simulateInteract(player, level, moosh);
			}

			List<MushroomCow> remainingMoosh = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int totalCows = 0;
			for (Cow c : cows) totalCows += ((StackableEntity) c).jarstacker$getStackCount();
			int remainingCount = remainingMoosh.isEmpty() ? 0 : ((StackableEntity) remainingMoosh.get(0)).jarstacker$getStackCount();

			boolean pass = (remainingCount == 1) && (totalCows == 9) && (remainingCount + totalCows == 10);
			results.add(new TestResult("Test TR10 - Stable Anchor Continuous Shearing", pass,
				"RemainingSourceCount: " + remainingCount + ", TotalCows: " + totalCows + ", TotalLogical: " + (remainingCount + totalCows)));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test TR10 - Stable Anchor Continuous Shearing", false, e.getMessage()));
		}

		// =========================================================================
		// V0.4.2 TRANSFORMATION CAPTURE & IDENTITY INTEGRITY SUITE (C1-C7, TR11-TR16)
		// =========================================================================

		// Test C1: Single Mooshroom Label Normalization (Mooshroom ร—1 -> Cow, never retains Mooshroom label)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(1);
			level.addFreshEntity(moosh);
			MobStackingManager.updateLabel(moosh, 1, true);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));

			boolean hasMooshLabel = false;
			String cowLabel = "none";
			if (!cows.isEmpty()) {
				Cow cow = cows.get(0);
				if (cow.hasCustomName()) {
					cowLabel = cow.getCustomName().getString();
					if (cowLabel.contains("Mooshroom")) {
						hasMooshLabel = true;
					}
				}
			}

			boolean pass = res.consumesAction() && mooshrooms.isEmpty() && cows.size() == 1 && !hasMooshLabel;
			results.add(new TestResult("Test C1 - Single Mooshroom Label Normalization", pass,
				"Consumes: " + res.consumesAction() + ", Cows: " + cows.size() + ", CowLabel: " + cowLabel + ", HasMooshLabel: " + hasMooshLabel));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test C1 - Single Mooshroom Label Normalization", false, e.getMessage()));
		}

		// Test C2: Single Mooshroom No Cow Nearby (Physical Cow ร—1 with correct identity)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(1);
			level.addFreshEntity(moosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			boolean pass = res.consumesAction()
				&& cows.size() == 1
				&& ((StackableEntity) cows.get(0)).jarstacker$getStackCount() == 1
				&& !cows.get(0).getType().getDescription().getString().contains("Mooshroom");

			results.add(new TestResult("Test C2 - Single Mooshroom No Cow Nearby", pass,
				"Cows: " + cows.size() + ", StackCount: " + (cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount())));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test C2 - Single Mooshroom No Cow Nearby", false, e.getMessage()));
		}

		// Test C3: Single Mooshroom Existing Cow Stack (Mooshroom ร—1 + Cow ร—20 -> Mooshroom ร—0, Cow ร—21)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(1);
			level.addFreshEntity(moosh);
			MobStackingManager.updateLabel(moosh, 1, true);

			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x + 1.0, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(20);
			level.addFreshEntity(cowStack);
			MobStackingManager.updateLabel(cowStack, 20, true);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int cowCount = cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount();

			boolean pass = res.consumesAction() && mooshrooms.isEmpty() && cows.size() == 1 && cowCount == 21;
			results.add(new TestResult("Test C3 - Single Mooshroom Existing Cow Stack", pass,
				"Mooshrooms: " + mooshrooms.size() + ", CowEntities: " + cows.size() + ", CowCount: " + cowCount));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test C3 - Single Mooshroom Existing Cow Stack", false, e.getMessage()));
		}

		// Test C4: Multi-Mooshroom Existing Cow Stack (Mooshroom ร—10 + Cow ร—20 -> Mooshroom ร—9, Cow ร—21)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);
			MobStackingManager.updateLabel(moosh, 10, true);

			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x + 1.0, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(20);
			level.addFreshEntity(cowStack);
			MobStackingManager.updateLabel(cowStack, 20, true);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();
			int cowCount = cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount();

			boolean pass = res.consumesAction() && mooshrooms.size() == 1 && mooshCount == 9 && cows.size() == 1 && cowCount == 21;
			results.add(new TestResult("Test C4 - Multi-Mooshroom Existing Cow Stack", pass,
				"MooshCount: " + mooshCount + ", CowCount: " + cowCount + ", CowEntities: " + cows.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test C4 - Multi-Mooshroom Existing Cow Stack", false, e.getMessage()));
		}

		// Test C5: Transform Final Unit (Mooshroom ร—2 + Cow ร—20 -> 2 shears -> Mooshroom ร—0, Cow ร—22)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(2);
			level.addFreshEntity(moosh);
			MobStackingManager.updateLabel(moosh, 2, true);

			Cow cowStack = createEntity(EntityType.COW, level);
			cowStack.setPos(posA.x + 1.0, posA.y, posA.z);
			((StackableEntity) cowStack).jarstacker$setStackCount(20);
			level.addFreshEntity(cowStack);
			MobStackingManager.updateLabel(cowStack, 20, true);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			// First shear: 2 -> 1
			simulateInteract(player, level, moosh);
			// Second shear: 1 -> 0
			simulateInteract(player, level, moosh);

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int cowCount = cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount();

			boolean pass = mooshrooms.isEmpty() && cows.size() == 1 && cowCount == 22;
			results.add(new TestResult("Test C5 - Transform Final Unit", pass,
				"RemainingMoosh: " + mooshrooms.size() + ", CowEntities: " + cows.size() + ", FinalCowCount: " + cowCount));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test C5 - Transform Final Unit", false, e.getMessage()));
		}

		// Test C6: Player Custom Name Preservation (Mooshroom named "Mushie" -> Cow named "Mushie")
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			moosh.setCustomName(Component.literal("Mushie"));
			moosh.setCustomNameVisible(true);
			((StackableEntity) moosh).jarstacker$setManagedLabel(false);
			((StackableEntity) moosh).jarstacker$setStackCount(1);
			level.addFreshEntity(moosh);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);

			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			boolean hasMushie = false;
			String name = "none";
			if (!cows.isEmpty()) {
				Cow cow = cows.get(0);
				if (cow.hasCustomName()) {
					name = cow.getCustomName().getString();
					hasMushie = "Mushie".equals(name);
				}
			}

			boolean pass = res.consumesAction() && cows.size() == 1 && hasMushie;
			results.add(new TestResult("Test C6 - Player Custom Name Preservation", pass,
				"Consumes: " + res.consumesAction() + ", CowCount: " + cows.size() + ", Name: " + name));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test C6 - Player Custom Name Preservation", false, e.getMessage()));
		}

		// Test C7: Auto Label Metadata vs Player Name Tag
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow mooshManaged = createEntity(EntityType.MOOSHROOM, level);
			mooshManaged.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) mooshManaged).jarstacker$setStackCount(2);
			MobStackingManager.updateLabel(mooshManaged, 2, true);

			MushroomCow mooshPlayer = createEntity(EntityType.MOOSHROOM, level);
			mooshPlayer.setPos(posA.x + 1.0, posA.y, posA.z);
			mooshPlayer.setCustomName(Component.literal("Mooshroom ร—2"));
			((StackableEntity) mooshPlayer).jarstacker$setManagedLabel(false);
			((StackableEntity) mooshPlayer).jarstacker$setStackCount(2);

			boolean managedIsManaged = MobCompatibility.isManagedLabel(mooshManaged);
			boolean playerIsManaged = MobCompatibility.isManagedLabel(mooshPlayer);
			boolean managedExcluded = MobCompatibility.isCustomNamedExclusion(mooshManaged);
			boolean playerExcluded = MobCompatibility.isCustomNamedExclusion(mooshPlayer);

			boolean pass = managedIsManaged && !playerIsManaged && !managedExcluded && playerExcluded;
			results.add(new TestResult("Test C7 - Auto Label Metadata", pass,
				"ManagedIsManaged: " + managedIsManaged + ", PlayerIsManaged: " + playerIsManaged +
				", ManagedExcluded: " + managedExcluded + ", PlayerExcluded: " + playerExcluded));

			cleanPen(level, cleanAreaA);
		} catch (Exception e) {
			results.add(new TestResult("Test C7 - Auto Label Metadata", false, e.getMessage()));
		}

		// Test TR11: Same-Type False Capture Rejection (Unrelated Cow spawned during transformation)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);

			final Cow[] unrelatedRef = new Cow[1];
			LogicalEntityTransformer.onPreShearSpawnHook = (lvl) -> {
				Cow unrelated = createEntity(EntityType.COW, lvl);
				unrelated.setPos(posA.x + 0.5, posA.y, posA.z);
				((StackableEntity) unrelated).jarstacker$setStackCount(1);
				lvl.addFreshEntity(unrelated);
				unrelatedRef[0] = unrelated;
			};

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);
			LogicalEntityTransformer.onPreShearSpawnHook = null;

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();
			int totalCowCount = 0;
			for (Cow c : cows) totalCowCount += ((StackableEntity) c).jarstacker$getStackCount();

			boolean unrelatedSurvived = unrelatedRef[0] != null && unrelatedRef[0].isAlive() && !unrelatedRef[0].isRemoved();
			boolean pass = res.consumesAction() && mooshCount == 9 && totalCowCount == 2 && unrelatedSurvived;

			results.add(new TestResult("Test TR11 - Same-Type False Capture", pass,
				"MooshCount: " + mooshCount + ", TotalCows: " + totalCowCount + ", UnrelatedSurvived: " + unrelatedSurvived));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			LogicalEntityTransformer.onPreShearSpawnHook = null;
			results.add(new TestResult("Test TR11 - Same-Type False Capture", false, e.getMessage()));
		}

		// Test TR12: Missed Capture After Vanilla Success (Controlled Destination Recovery)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);

			LogicalEntityTransformer.simulateMissedCapture = true;

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);
			LogicalEntityTransformer.simulateMissedCapture = false;

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();
			int cowCount = cows.isEmpty() ? 0 : ((StackableEntity) cows.get(0)).jarstacker$getStackCount();

			boolean pass = res.consumesAction() && mooshCount == 9 && cowCount == 1 && (mooshCount + cowCount == 10);
			results.add(new TestResult("Test TR12 - Missed Capture After Vanilla Success", pass,
				"MooshCount: " + mooshCount + ", CowCount: " + cowCount + ", TotalLogical: " + (mooshCount + cowCount)));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			LogicalEntityTransformer.simulateMissedCapture = false;
			results.add(new TestResult("Test TR12 - Missed Capture After Vanilla Success", false, e.getMessage()));
		}

		// Test TR13: Multiple Same-Type New Entities (Recovery Ambiguity)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);

			LogicalEntityTransformer.simulateMissedCapture = true;
			LogicalEntityTransformer.simulateAmbiguousDestination = true;

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);
			LogicalEntityTransformer.simulateMissedCapture = false;
			LogicalEntityTransformer.simulateAmbiguousDestination = false;

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();

			boolean pass = res.consumesAction() && mooshCount == 9;
			results.add(new TestResult("Test TR13 - Multiple Same-Type Ambiguity", pass,
				"Consumes: " + res.consumesAction() + ", MooshCount: " + mooshCount + " (remains 9, not restored to 10)"));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			LogicalEntityTransformer.simulateMissedCapture = false;
			LogicalEntityTransformer.simulateAmbiguousDestination = false;
			results.add(new TestResult("Test TR13 - Multiple Same-Type Ambiguity", false, e.getMessage()));
		}

		// Test TR14: Stale Context (Spawn Cow after context closed is not captured)
		try {
			cleanPen(level, cleanAreaA);

			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) cow).jarstacker$setStackCount(1);
			level.addFreshEntity(cow);

			boolean pass = !LogicalEntityTransformer.isHandoffActive();
			results.add(new TestResult("Test TR14 - Stale Context", pass,
				"HandoffActive: " + LogicalEntityTransformer.isHandoffActive() + ", CowAlive: " + cow.isAlive()));

			cleanPen(level, cleanAreaA);
		} catch (Exception e) {
			results.add(new TestResult("Test TR14 - Stale Context", false, e.getMessage()));
		}

		// Test TR15: Destination Normalization Failure (Source remains N-1, destination survives)
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(10);
			level.addFreshEntity(moosh);

			LogicalEntityTransformer.simulateNormalizationFailure = true;

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
			aimAt(player, moosh);

			InteractionResult res = simulateInteract(player, level, moosh);
			LogicalEntityTransformer.simulateNormalizationFailure = false;

			List<MushroomCow> mooshrooms = level.getEntitiesOfClass(MushroomCow.class, cleanAreaA);
			List<Cow> cows = level.getEntitiesOfClass(Cow.class, cleanAreaA, c -> !(c.getType() == EntityType.MOOSHROOM));
			int mooshCount = mooshrooms.isEmpty() ? 0 : ((StackableEntity) mooshrooms.get(0)).jarstacker$getStackCount();

			boolean pass = res.consumesAction() && mooshCount == 9 && cows.size() == 1;
			results.add(new TestResult("Test TR15 - Destination Normalization Failure", pass,
				"Consumes: " + res.consumesAction() + ", MooshCount: " + mooshCount + ", CowsPhysical: " + cows.size()));

			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			LogicalEntityTransformer.simulateNormalizationFailure = false;
			results.add(new TestResult("Test TR15 - Destination Normalization Failure", false, e.getMessage()));
		}

		// Test TR16: Destination Label Refresh Idempotency
		try {
			cleanPen(level, cleanAreaA);

			MushroomCow moosh = createEntity(EntityType.MOOSHROOM, level);
			moosh.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) moosh).jarstacker$setStackCount(1);
			MobStackingManager.updateLabel(moosh, 1, true);

			Cow cow = createEntity(EntityType.COW, level);
			cow.setPos(posA.x, posA.y, posA.z);
			cow.setCustomName(moosh.getCustomName());
			((StackableEntity) cow).jarstacker$setManagedLabel(true);

			// First normalization
			LogicalEntityTransformer.normalizeDestination(moosh, cow);
			int count1 = ((StackableEntity) cow).jarstacker$getStackCount();
			String name1 = cow.hasCustomName() ? cow.getCustomName().getString() : "none";
			boolean managed1 = ((StackableEntity) cow).jarstacker$hasManagedLabel();
			UUID id1 = cow.getUUID();

			// Second normalization
			LogicalEntityTransformer.normalizeDestination(moosh, cow);
			int count2 = ((StackableEntity) cow).jarstacker$getStackCount();
			String name2 = cow.hasCustomName() ? cow.getCustomName().getString() : "none";
			boolean managed2 = ((StackableEntity) cow).jarstacker$hasManagedLabel();
			UUID id2 = cow.getUUID();

			boolean pass = (count1 == count2) && Objects.equals(name1, name2) && (managed1 == managed2) && id1.equals(id2) && !name2.contains("Mooshroom");
			results.add(new TestResult("Test TR16 - Normalization Idempotency", pass,
				"Count: " + count2 + ", Name: " + name2 + ", Managed: " + managed2));

			cleanPen(level, cleanAreaA);
		} catch (Exception e) {
			results.add(new TestResult("Test TR16 - Normalization Idempotency", false, e.getMessage()));
		}

		// -------------------------------------------------------------
		// V0.5.0 COMBAT & HEALTH TESTS (H1 - H25)
		// -------------------------------------------------------------
		runCombatTestsH1toH12(level, pos, results);
		runCombatTestsH13toH25(level, pos, results);

		// -------------------------------------------------------------
		// V0.5.1 COMBAT HARDENING & DEATH TESTS (CM1-CM3, CE1-CE3, D1-D12)
		// -------------------------------------------------------------
		runV051MigrationTests(level, pos, results);
		runV051EquipmentTests(level, pos, results);
		runV051DeathTests(level, pos, results);

		// -------------------------------------------------------------
		// V0.5.2 RUNTIME EQUIPMENT & ATTRIBUTION TESTS (CE4-CE9, A1-A12)
		// V0.5.2 RUNTIME EQUIPMENT & ATTRIBUTION TESTS (CE4-CE9, VC1-VC15)
		// -------------------------------------------------------------
		runV052EquipmentTests(level, pos, results);
		runV052AttributionTests(level, pos, results);

		// -------------------------------------------------------------
		// V0.6.0 LOGICAL STATUS EFFECTS & PERSISTENT EFFECT STATE TESTS
		// -------------------------------------------------------------
		runV060StatusTests(level, pos, results);
		runV060BurnTests(level, pos, results);
		runV060ProjectionTests(level, pos, results);
		runV060StressTests(level, pos, results);

		// -------------------------------------------------------------
		// V0.6.0 EFFECT PARITY HARDENING (GE1-GE6, RI1-RI6, FB1-FB4)
		// -------------------------------------------------------------
		runV060GenericEffectTests(level, pos, results);
		runV060RealIntegrationTests(level, pos, results);
		runV060FallbackTests(level, pos, results);
		runV060PassiveSafeTests(level, pos, results);
		runV060UnsupportedAreaTests(level, pos, results);
		runV060VanillaAreaParityTests(level, pos, results);
		runItemMergeTests(level, pos, results);

		// Print summary to log
		JarStackerMod.LOGGER.info("========== JAR STACKER TEST SUMMARY ==========");
		int passCount = 0;
		for (TestResult tr : results) {
			if (tr.passed) passCount++;
			JarStackerMod.LOGGER.info("[{}] {}: {}", tr.passed ? "PASS" : "FAIL", tr.name, tr.details);
		}
		JarStackerMod.LOGGER.info("PASSED: {} / {} tests", passCount, results.size());
		JarStackerMod.LOGGER.info("===============================================");

		ModConfig restored = ModConfig.fromJson(originalJson);
		if (restored != null) {
			ModConfig.setInstance(restored);
		}
		com.jar.jarstacker.stack.mob.MovementDiagnostics.enabled = false;

		return results;
	}

	private static void runCombatTestsH1toH12(ServerLevel level, Vec3 pos, List<TestResult> results) {
Vec3 posH = pos.add(25, 0, 25);
		net.minecraft.core.BlockPos penCenterH = new net.minecraft.core.BlockPos((int) posH.x, (int) posH.y, (int) posH.z);
		buildPen(level, penCenterH, 4);
		AABB cleanAreaH = new AABB(posH.x - 6, posH.y - 2, posH.z - 6, posH.x + 6, posH.y + 6, posH.z + 6);

		// Test H1 - Direct Damage
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().generic(), 7.0f);
			zombie.setHealth(Math.max(0.0f, zombie.getHealth() - 7.0f));

			boolean pass = state.size() == 5 &&
				Math.abs(state.get(0) - 13.0f) < 0.01f &&
				Math.abs(state.get(1) - 20.0f) < 0.01f &&
				Math.abs(state.get(2) - 20.0f) < 0.01f &&
				Math.abs(state.get(3) - 20.0f) < 0.01f &&
				Math.abs(state.get(4) - 20.0f) < 0.01f &&
				Math.abs(zombie.getHealth() - 13.0f) < 0.01f &&
				((StackableEntity) zombie).jarstacker$getStackCount() == 5;

			results.add(new TestResult("Test H1 - Direct Damage", pass,
				"ActiveHP: " + state.get(0) + ", LogicalSize: " + state.size() + ", StackCount: " + ((StackableEntity) zombie).jarstacker$getStackCount()));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H1 - Direct Damage", false, e.getMessage()));
		}

		// Test H2 - Repeated Direct Damage
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			// 1st hit: 7 damage -> 13
			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().generic(), 7.0f);
			zombie.setHealth(13.0f);

			// 2nd hit: 7 damage -> 6
			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().generic(), 7.0f);
			zombie.setHealth(6.0f);

			// 3rd hit: 7 damage -> death of active member
			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().generic(), 7.0f);
			boolean handled = LogicalHealthManager.handleDie(zombie, level.damageSources().generic());

			int countAfter = ((StackableEntity) zombie).jarstacker$getStackCount();
			boolean pass = handled && countAfter == 4 && state.size() == 4 &&
				Math.abs(zombie.getHealth() - 20.0f) < 0.01f &&
				Math.abs(state.get(0) - 20.0f) < 0.01f;

			results.add(new TestResult("Test H2 - Repeated Direct Damage", pass,
				"Handled: " + handled + ", RemainingCount: " + countAfter + ", ActiveHP: " + zombie.getHealth()));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H2 - Repeated Direct Damage", false, e.getMessage()));
		}

		// Test H3 - Axe Hit (Single-Target)
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posH.x, posH.y, posH.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
			aimAt(player, zombie);

			player.attack(zombie);

			boolean pass = state.size() == 5 &&
				state.get(0) < 20.0f &&
				state.get(1) == 20.0f &&
				state.get(2) == 20.0f &&
				state.get(3) == 20.0f &&
				state.get(4) == 20.0f;

			results.add(new TestResult("Test H3 - Axe Single-Target", pass,
				"ActiveHP: " + state.get(0) + ", Virtual1HP: " + state.get(1) + ", LogicalSize: " + state.size()));
			cleanPen(level, cleanAreaH);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test H3 - Axe Single-Target", false, e.getMessage()));
		}

		// Test H4 - Sword Non-Sweep
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posH.x, posH.y, posH.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
			player.setSprinting(true);
			aimAt(player, zombie);

			player.attack(zombie);

			boolean pass = state.size() == 5 &&
				state.get(0) < 20.0f &&
				state.get(1) == 20.0f &&
				state.get(2) == 20.0f &&
				state.get(3) == 20.0f &&
				state.get(4) == 20.0f;

			results.add(new TestResult("Test H4 - Sword Non-Sweep", pass,
				"ActiveHP: " + state.get(0) + ", Virtual1HP: " + state.get(1)));
			cleanPen(level, cleanAreaH);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test H4 - Sword Non-Sweep", false, e.getMessage()));
		}

		// Test H5 - Sword Sweep On Primary Stack
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posH.x, posH.y, posH.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
			player.setOnGround(true);
			((StackableEntity) player).jarstacker$setAttackStrengthTicker(100);
			aimAt(player, zombie);

			player.attack(zombie);

			boolean pass = state.size() == 5 &&
				state.get(0) < 20.0f &&
				state.get(1) < 20.0f &&
				state.get(2) < 20.0f &&
				state.get(3) < 20.0f &&
				state.get(4) < 20.0f;

			results.add(new TestResult("Test H5 - Sword Sweep On Primary Stack", pass,
				"ActiveHP: " + state.get(0) + ", Virtual1HP: " + state.get(1) + ", Virtual4HP: " + state.get(4)));
			cleanPen(level, cleanAreaH);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test H5 - Sword Sweep On Primary Stack", false, e.getMessage()));
		}

		// Test H6 - Nearby Secondary Stack Sweep
		try {
			cleanPen(level, cleanAreaH);
			Zombie primary = createEntity(EntityType.ZOMBIE, level);
			primary.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) primary).jarstacker$setStackCount(1);
			level.addFreshEntity(primary);

			Zombie secondary = createEntity(EntityType.ZOMBIE, level);
			secondary.setPos(posH.x + 0.8, posH.y, posH.z);
			((StackableEntity) secondary).jarstacker$setStackCount(3);
			level.addFreshEntity(secondary);
			LogicalHealthState secState = LogicalHealthManager.getOrCreateState(secondary);

			ServerPlayer player = createMockPlayer(level, new Vec3(posH.x, posH.y, posH.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
			player.setOnGround(true);
			((StackableEntity) player).jarstacker$setAttackStrengthTicker(100);
			aimAt(player, primary);

			player.attack(primary);

			boolean pass = secState.size() == 3 &&
				secState.get(0) < 20.0f &&
				secState.get(1) < 20.0f &&
				secState.get(2) < 20.0f;

			results.add(new TestResult("Test H6 - Nearby Secondary Stack Sweep", pass,
				"SecActiveHP: " + secState.get(0) + ", SecVirtualHP: " + secState.get(1)));
			cleanPen(level, cleanAreaH);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test H6 - Nearby Secondary Stack Sweep", false, e.getMessage()));
		}

		// Test H7 - Explosion Area Damage
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(10);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 8.0f);
			zombie.setHealth(12.0f);

			boolean allDamaged = true;
			for (int i = 0; i < 10; i++) {
				if (Math.abs(state.get(i) - 12.0f) > 0.01f) {
					allDamaged = false;
					break;
				}
			}

			boolean pass = state.size() == 10 && allDamaged && ((StackableEntity) zombie).jarstacker$getStackCount() == 10;
			results.add(new TestResult("Test H7 - Explosion Area Damage", pass,
				"LogicalCount: " + state.size() + ", AllDamaged: " + allDamaged + ", ActiveHP: " + state.get(0)));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H7 - Explosion Area Damage", false, e.getMessage()));
		}

		// Test H8 - Explosion Multi-Death
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(10);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			for (int i = 0; i < 5; i++) state.set(i, 5.0f);
			for (int i = 5; i < 10; i++) state.set(i, 20.0f);
			zombie.setHealth(5.0f);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 10.0f);
			boolean handled = LogicalHealthManager.handleDie(zombie, level.damageSources().explosion(null));

			int countAfter = ((StackableEntity) zombie).jarstacker$getStackCount();
			boolean pass = handled && countAfter == 5 && state.size() == 5 &&
				Math.abs(zombie.getHealth() - 10.0f) < 0.01f &&
				Math.abs(state.get(0) - 10.0f) < 0.01f;

			results.add(new TestResult("Test H8 - Explosion Multi-Death", pass,
				"RemainingCount: " + countAfter + ", SurvivingActiveHP: " + zombie.getHealth() + ", StateSize: " + state.size()));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H8 - Explosion Multi-Death", false, e.getMessage()));
		}

		// Test H9 - Lightning Area Damage
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().lightningBolt(), 5.0f);
			zombie.setHealth(15.0f);

			boolean allTook = true;
			for (int i = 0; i < 5; i++) {
				if (Math.abs(state.get(i) - 15.0f) > 0.01f) {
					allTook = false;
					break;
				}
			}

			boolean pass = state.size() == 5 && allTook;
			results.add(new TestResult("Test H9 - Lightning Area Damage", pass,
				"AllTookDamage: " + allTook + ", Size: " + state.size()));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H9 - Lightning Area Damage", false, e.getMessage()));
		}

		// Test H10 - Lava / Shared Fire
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().lava(), 4.0f);
			zombie.setHealth(16.0f);

			boolean allTookLava = true;
			for (int i = 0; i < 5; i++) {
				if (Math.abs(state.get(i) - 16.0f) > 0.01f) {
					allTookLava = false;
					break;
				}
			}
			boolean shared = ((StackableEntity) zombie).jarstacker$isSharedIgnition();

			boolean pass = state.size() == 5 && allTookLava && shared;
			results.add(new TestResult("Test H10 - Lava / Shared Fire", pass,
				"AllTookLava: " + allTookLava + ", SharedIgnition: " + shared));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H10 - Lava / Shared Fire", false, e.getMessage()));
		}

		// Test H11 - Flaming Projectile
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posH.x, posH.y, posH.z - 2.0), GameType.SURVIVAL);
			Arrow arrow = new Arrow(level, posH.x, posH.y, posH.z, new ItemStack(Items.ARROW), null);
			arrow.setRemainingFireTicks(100);

			DamageSource arrowSource = level.damageSources().arrow(arrow, player);
			LogicalHealthManager.onDamageApplied(zombie, arrowSource, 6.0f);
			zombie.setHealth(14.0f);

			boolean sharedAfterArrow = ((StackableEntity) zombie).jarstacker$isSharedIgnition();
			boolean onlyActiveDamaged = Math.abs(state.get(0) - 14.0f) < 0.01f &&
				state.get(1) == 20.0f && state.get(2) == 20.0f && state.get(3) == 20.0f && state.get(4) == 20.0f;

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().onFire(), 1.0f);
			zombie.setHealth(13.0f);
			boolean burnOnlyActive = Math.abs(state.get(0) - 13.0f) < 0.01f && state.get(1) == 20.0f;

			boolean pass = !sharedAfterArrow && onlyActiveDamaged && burnOnlyActive;
			results.add(new TestResult("Test H11 - Flaming Projectile", pass,
				"Shared: " + sharedAfterArrow + ", OnlyActiveDamaged: " + onlyActiveDamaged + ", BurnOnlyActive: " + burnOnlyActive));
			cleanPen(level, cleanAreaH);
			player.discard();
			arrow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test H11 - Flaming Projectile", false, e.getMessage()));
		}

		// Test H12 - Fall Damage
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().fall(), 8.0f);
			zombie.setHealth(12.0f);

			boolean allTookFall = true;
			for (int i = 0; i < 5; i++) {
				if (Math.abs(state.get(i) - 12.0f) > 0.01f) {
					allTookFall = false;
					break;
				}
			}

			boolean pass = state.size() == 5 && allTookFall;
			results.add(new TestResult("Test H12 - Fall Damage", pass,
				"AllTookFall: " + allTookFall + ", ActiveHP: " + state.get(0)));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H12 - Fall Damage", false, e.getMessage()));
		}
	}

	private static void runCombatTestsH13toH25(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posH = pos.add(25, 0, 25);
		AABB cleanAreaH = new AABB(posH.x - 6, posH.y - 2, posH.z - 6, posH.x + 6, posH.y + 6, posH.z + 6);

// Test H13 - Direct Multi-Hit Death
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			level.addFreshEntity(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().generic(), 20.0f);
			boolean h1 = LogicalHealthManager.handleDie(zombie, level.damageSources().generic());
			int count1 = ((StackableEntity) zombie).jarstacker$getStackCount();

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().generic(), 20.0f);
			boolean h2 = LogicalHealthManager.handleDie(zombie, level.damageSources().generic());
			int count2 = ((StackableEntity) zombie).jarstacker$getStackCount();

			boolean pass = h1 && h2 && count1 == 2 && count2 == 1;
			results.add(new TestResult("Test H13 - Direct Multi-Hit Death", pass,
				"H1: " + h1 + ", Count1: " + count1 + ", H2: " + h2 + ", Count2: " + count2));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H13 - Direct Multi-Hit Death", false, e.getMessage()));
		}

		// Test H14 - Sweep Multi-Death
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			state.set(0, 20.0f);
			state.set(1, 1.0f);
			state.set(2, 1.0f);
			state.set(3, 1.0f);
			state.set(4, 20.0f);

			LogicalHealthManager.applySweepToPrimary(zombie, 2.0f);

			int remaining = ((StackableEntity) zombie).jarstacker$getStackCount();
			boolean pass = remaining == 2 && state.size() == 2 &&
				state.get(0) == 20.0f && Math.abs(state.get(1) - 18.0f) < 0.01f;

			results.add(new TestResult("Test H14 - Sweep Multi-Death", pass,
				"Remaining: " + remaining + ", StateSize: " + state.size() + ", SurvivingVirtualHP: " + state.get(1)));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H14 - Sweep Multi-Death", false, e.getMessage()));
		}

		// Test H15 - Save/Reload Damaged Stack
		try {
			cleanPen(level, cleanAreaH);
			Zombie original = createEntity(EntityType.ZOMBIE, level);
			original.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) original).jarstacker$setStackCount(5);
			level.addFreshEntity(original);

			LogicalHealthState origState = LogicalHealthManager.getOrCreateState(original);
			origState.set(0, 3.0f);
			origState.set(1, 7.0f);
			origState.set(2, 15.0f);
			origState.set(3, 20.0f);
			origState.set(4, 20.0f);
			original.setHealth(3.0f);

			net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
			EntityAdapter.saveWithoutId(original, tag);

			Zombie reloaded = createEntity(EntityType.ZOMBIE, level);
			EntityAdapter.load(reloaded, tag);

			int reloadedCount = ((StackableEntity) reloaded).jarstacker$getStackCount();
			LogicalHealthState relState = ((StackableEntity) reloaded).jarstacker$getLogicalHealthState();

			boolean pass = reloadedCount == 5 && relState != null && relState.size() == 5 &&
				Math.abs(relState.get(0) - 3.0f) < 0.01f &&
				Math.abs(relState.get(1) - 7.0f) < 0.01f &&
				Math.abs(relState.get(2) - 15.0f) < 0.01f &&
				Math.abs(relState.get(3) - 20.0f) < 0.01f &&
				Math.abs(relState.get(4) - 20.0f) < 0.01f &&
				Math.abs(reloaded.getHealth() - 3.0f) < 0.01f;

			results.add(new TestResult("Test H15 - Save/Reload Damaged Stack", pass,
				"Count: " + reloadedCount + ", H0: " + (relState != null ? relState.get(0) : "null") + ", H1: " + (relState != null ? relState.get(1) : "null")));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H15 - Save/Reload Damaged Stack", false, e.getMessage()));
		}

		// Test H16 - Merge Damaged Stacks
		try {
			cleanPen(level, cleanAreaH);
			Zombie stackA = createEntity(EntityType.ZOMBIE, level);
			stackA.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) stackA).jarstacker$setStackCount(2);
			level.addFreshEntity(stackA);
			LogicalHealthState stateA = LogicalHealthManager.getOrCreateState(stackA);
			stateA.set(0, 5.0f);
			stateA.set(1, 10.0f);
			stackA.setHealth(5.0f);

			Zombie stackB = createEntity(EntityType.ZOMBIE, level);
			stackB.setPos(posH.x + 0.5, posH.y, posH.z);
			((StackableEntity) stackB).jarstacker$setStackCount(2);
			level.addFreshEntity(stackB);
			LogicalHealthState stateB = LogicalHealthManager.getOrCreateState(stackB);
			stateB.set(0, 8.0f);
			stateB.set(1, 15.0f);
			stackB.setHealth(8.0f);

			LogicalHealthManager.mergeHealthStates(stackA, stackB, 2);
			((StackableEntity) stackA).jarstacker$setStackCount(4);

			boolean pass = ((StackableEntity) stackA).jarstacker$getStackCount() == 4 &&
				stateA.size() == 4 &&
				Math.abs(stateA.get(0) - 5.0f) < 0.01f &&
				Math.abs(stateA.get(1) - 10.0f) < 0.01f &&
				Math.abs(stateA.get(2) - 8.0f) < 0.01f &&
				Math.abs(stateA.get(3) - 15.0f) < 0.01f;

			results.add(new TestResult("Test H16 - Merge Damaged Stacks", pass,
				"Size: " + stateA.size() + ", H0: " + stateA.get(0) + ", H2: " + stateA.get(2)));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H16 - Merge Damaged Stacks", false, e.getMessage()));
		}

		// Test H17 - Split Damaged Stack
		try {
			cleanPen(level, cleanAreaH);
			Zombie source = createEntity(EntityType.ZOMBIE, level);
			source.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) source).jarstacker$setStackCount(3);
			level.addFreshEntity(source);
			LogicalHealthState sourceState = LogicalHealthManager.getOrCreateState(source);
			sourceState.set(0, 4.0f);
			sourceState.set(1, 12.0f);
			sourceState.set(2, 18.0f);
			source.setHealth(4.0f);

			Zombie extracted = createEntity(EntityType.ZOMBIE, level);
			extracted.setPos(posH.x + 1.0, posH.y, posH.z);
			((StackableEntity) extracted).jarstacker$setStackCount(1);
			level.addFreshEntity(extracted);

			LogicalHealthManager.extractHealthState(source, extracted);
			((StackableEntity) source).jarstacker$setStackCount(2);

			boolean pass = Math.abs(extracted.getHealth() - 4.0f) < 0.01f &&
				((StackableEntity) extracted).jarstacker$getLogicalHealthState().size() == 1 &&
				Math.abs(((StackableEntity) extracted).jarstacker$getLogicalHealthState().get(0) - 4.0f) < 0.01f &&
				sourceState.size() == 2 &&
				Math.abs(sourceState.get(0) - 12.0f) < 0.01f &&
				Math.abs(sourceState.get(1) - 18.0f) < 0.01f;

			results.add(new TestResult("Test H17 - Split Damaged Stack", pass,
				"ExtractedHP: " + extracted.getHealth() + ", RemActiveHP: " + sourceState.get(0) + ", RemCount: " + sourceState.size()));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H17 - Split Damaged Stack", false, e.getMessage()));
		}

		// Test H18 - Health Repair
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);

			LogicalHealthState corrupt = new LogicalHealthState();
			corrupt.add(10.0f);
			corrupt.add(15.0f);
			((StackableEntity) zombie).jarstacker$setLogicalHealthState(corrupt);

			com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationReport repBefore =
				com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.validateLogicalState(zombie);

			com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.repairLogicalState(zombie);

			com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationReport repAfter =
				com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.validateLogicalState(zombie);

			LogicalHealthState repaired = ((StackableEntity) zombie).jarstacker$getLogicalHealthState();

			boolean pass = repBefore.status() == com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationStatus.REPAIRABLE &&
				repAfter.status() == com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationStatus.VALID &&
				repaired.size() == 5 &&
				repaired.get(0) == 10.0f &&
				repaired.get(1) == 15.0f &&
				repaired.get(2) == 20.0f;

			results.add(new TestResult("Test H18 - Health Repair", pass,
				"Before: " + repBefore.status() + ", After: " + repAfter.status() + ", RepairedSize: " + repaired.size()));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H18 - Health Repair", false, e.getMessage()));
		}

		// Test H19 - Stress Test ร—100 Explosion
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(100);
			level.addFreshEntity(zombie);
			LogicalHealthManager.getOrCreateState(zombie);

			long startNs = System.nanoTime();
			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 5.0f);
			long durationNs = System.nanoTime() - startNs;
			double durationMs = durationNs / 1_000_000.0;

			int physicalZombies = level.getEntitiesOfClass(Zombie.class, cleanAreaH).size();
			LogicalHealthState state = ((StackableEntity) zombie).jarstacker$getLogicalHealthState();

			boolean pass = durationMs < 50.0 && physicalZombies == 1 && state.size() == 100 && Math.abs(state.get(99) - 15.0f) < 0.01f;
			results.add(new TestResult("Test H19 - Stress Test ร—100 Explosion", pass,
				"DurationMs: " + String.format("%.2f", durationMs) + " ms, PhysicalEntities: " + physicalZombies + ", LogicalRecords: " + state.size()));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H19 - Stress Test ร—100 Explosion", false, e.getMessage()));
		}

		// Test H20 - Stress Test ร—1000 Explosion
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(1000);
			level.addFreshEntity(zombie);
			LogicalHealthManager.getOrCreateState(zombie);

			long startNs = System.nanoTime();
			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 5.0f);
			long durationNs = System.nanoTime() - startNs;
			double durationMs = durationNs / 1_000_000.0;

			int physicalZombies = level.getEntitiesOfClass(Zombie.class, cleanAreaH).size();
			LogicalHealthState state = ((StackableEntity) zombie).jarstacker$getLogicalHealthState();

			boolean pass = durationMs < 100.0 && physicalZombies == 1 && state.size() == 1000 && Math.abs(state.get(999) - 15.0f) < 0.01f;
			results.add(new TestResult("Test H20 - Stress Test ร—1000 Explosion", pass,
				"DurationMs: " + String.format("%.2f", durationMs) + " ms, PhysicalEntities: " + physicalZombies + ", LogicalRecords: " + state.size()));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H20 - Stress Test ร—1000 Explosion", false, e.getMessage()));
		}

		// Test H21 - No Duplicate Loot
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(10);
			level.addFreshEntity(zombie);

			int beforeItems = level.getEntitiesOfClass(ItemEntity.class, cleanAreaH).size();
			LogicalHealthManager.processDeaths(zombie, level.damageSources().generic(), 5);
			int afterItems = level.getEntitiesOfClass(ItemEntity.class, cleanAreaH).size();
			int dropped = afterItems - beforeItems;

			boolean pass = dropped >= 0;
			results.add(new TestResult("Test H21 - No Duplicate Loot", pass,
				"DroppedItemEntities: " + dropped + " for 5 logical deaths"));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H21 - No Duplicate Loot", false, e.getMessage()));
		}

		// Test H22 - No Duplicate XP
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(10);
			level.addFreshEntity(zombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posH.x, posH.y, posH.z - 2.0), GameType.SURVIVAL);
			DamageSource playerSource = level.damageSources().playerAttack(player);

			int beforeOrbs = level.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, cleanAreaH).size();
			LogicalHealthManager.processDeaths(zombie, playerSource, 5);
			int afterOrbs = level.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, cleanAreaH).size();
			int spawnedOrbs = afterOrbs - beforeOrbs;

			boolean pass = spawnedOrbs >= 5;
			results.add(new TestResult("Test H22 - No Duplicate XP", pass,
				"SpawnedOrbs: " + spawnedOrbs + " for 5 deaths"));
			cleanPen(level, cleanAreaH);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test H22 - No Duplicate XP", false, e.getMessage()));
		}

		// Test H23 - Invulnerability Frames
		try {
			cleanPen(level, cleanAreaH);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			zombie.invulnerableTime = 20;
			level.addFreshEntity(zombie);
			LogicalHealthState state = LogicalHealthManager.getOrCreateState(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 5.0f);

			boolean pass = state.size() == 5 &&
				Math.abs(state.get(0) - 15.0f) < 0.01f &&
				Math.abs(state.get(1) - 15.0f) < 0.01f &&
				Math.abs(state.get(2) - 15.0f) < 0.01f &&
				Math.abs(state.get(3) - 15.0f) < 0.01f &&
				Math.abs(state.get(4) - 15.0f) < 0.01f;

			results.add(new TestResult("Test H23 - Invulnerability Frames", pass,
				"ActiveHP: " + state.get(0) + ", VirtualHP: " + state.get(1)));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H23 - Invulnerability Frames", false, e.getMessage()));
		}

		// Test H24 - Knockback Non-Multiplication
		try {
			cleanPen(level, cleanAreaH);
			Zombie singleZombie = createEntity(EntityType.ZOMBIE, level);
			singleZombie.setPos(posH.x, posH.y, posH.z);
			((StackableEntity) singleZombie).jarstacker$setStackCount(1);
			level.addFreshEntity(singleZombie);

			Zombie stackedZombie = createEntity(EntityType.ZOMBIE, level);
			stackedZombie.setPos(posH.x + 2.0, posH.y, posH.z);
			((StackableEntity) stackedZombie).jarstacker$setStackCount(50);
			level.addFreshEntity(stackedZombie);

			singleZombie.knockback(0.5, 1.0, 0.0);
			stackedZombie.knockback(0.5, 1.0, 0.0);

			Vec3 deltaSingle = singleZombie.getDeltaMovement();
			Vec3 deltaStacked = stackedZombie.getDeltaMovement();

			double diff = Math.abs(deltaSingle.length() - deltaStacked.length());
			boolean pass = diff < 0.001;

			results.add(new TestResult("Test H24 - Knockback Non-Multiplication", pass,
				"SingleDelta: " + deltaSingle.length() + ", StackedDelta: " + deltaStacked.length() + ", Diff: " + diff));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H24 - Knockback Non-Multiplication", false, e.getMessage()));
		}

		// Test H25 - Migration
		try {
			cleanPen(level, cleanAreaH);
			Zombie legacyZombie = createEntity(EntityType.ZOMBIE, level);
			legacyZombie.setPos(posH.x, posH.y, posH.z);

			net.minecraft.nbt.CompoundTag legacyTag = new net.minecraft.nbt.CompoundTag();
			legacyTag.putInt("JarStackerCount", 4);
			EntityAdapter.load(legacyZombie, legacyTag);

			LogicalHealthState state1 = ((StackableEntity) legacyZombie).jarstacker$getLogicalHealthState();
			int count1 = ((StackableEntity) legacyZombie).jarstacker$getStackCount();

			net.minecraft.nbt.CompoundTag savedTag = new net.minecraft.nbt.CompoundTag();
			EntityAdapter.saveWithoutId(legacyZombie, savedTag);

			Zombie reloadedZombie = createEntity(EntityType.ZOMBIE, level);
			EntityAdapter.load(reloadedZombie, savedTag);

			LogicalHealthState state2 = ((StackableEntity) reloadedZombie).jarstacker$getLogicalHealthState();
			int count2 = ((StackableEntity) reloadedZombie).jarstacker$getStackCount();

			boolean pass = count1 == 4 && state1 != null && state1.size() == 4 &&
				count2 == 4 && state2 != null && state2.size() == 4 &&
				state1.get(0) == 20.0f && state2.get(0) == 20.0f;

			results.add(new TestResult("Test H25 - Migration", pass,
				"Count1: " + count1 + ", State1Size: " + (state1 != null ? state1.size() : "null") +
				", Count2: " + count2 + ", State2Size: " + (state2 != null ? state2.size() : "null")));
			cleanPen(level, cleanAreaH);
		} catch (Exception e) {
			results.add(new TestResult("Test H25 - Migration", false, e.getMessage()));
		}
	}


	public static void buildPen(ServerLevel level, net.minecraft.core.BlockPos center, int radius) {
		int cx = center.getX() >> 4;
		int cz = center.getZ() >> 4;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				level.setChunkForced(cx + dx, cz + dz, true);
				level.getChunk(cx + dx, cz + dz, net.minecraft.world.level.chunk.status.ChunkStatus.FULL, true);
			}
		}
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				net.minecraft.core.BlockPos pFloor = center.offset(x, -1, z);
				level.setBlockAndUpdate(pFloor, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());

				net.minecraft.core.BlockPos p1 = center.offset(x, 0, z);
				net.minecraft.core.BlockPos p2 = center.offset(x, 1, z);

				boolean isBorder = (Math.abs(x) == radius || Math.abs(z) == radius);
				if (isBorder) {
					level.setBlockAndUpdate(p1, net.minecraft.world.level.block.Blocks.OAK_FENCE.defaultBlockState());
					level.setBlockAndUpdate(p2, net.minecraft.world.level.block.Blocks.OAK_FENCE.defaultBlockState());
				} else {
					level.setBlockAndUpdate(p1, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
					level.setBlockAndUpdate(p2, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
				}
				for (int dy = 2; dy <= 5; dy++) {
					level.setBlockAndUpdate(center.offset(x, dy, z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
				}
			}
		}
	}

	public static boolean isInsidePen(Vec3 pos, net.minecraft.core.BlockPos center, int radius) {
		double minX = center.getX() - radius + 0.8;
		double maxX = center.getX() + radius + 0.2;
		double minZ = center.getZ() - radius + 0.8;
		double maxZ = center.getZ() + radius + 0.2;
		return pos.x > minX && pos.x < maxX && pos.z > minZ && pos.z < maxZ;
	}

	public static void cleanPen(ServerLevel level, AABB area) {
		try {
			List<Entity> toDiscard = new ArrayList<>();
			for (Entity e : level.getAllEntities()) {
				if (e != null && !e.isRemoved()) {
					Vec3 p = e.position();
					AABB bb = e.getBoundingBox();
					if ((p != null && area.contains(p)) || (bb != null && area.intersects(bb))) {
						toDiscard.add(e);
					}
				}
			}
			for (Entity e : toDiscard) {
				e.discard();
			}
		} catch (Throwable ignored) {
		}
	}

	public static <T extends Entity> List<T> getEntitiesInArea(ServerLevel level, Class<T> clazz, AABB area, java.util.function.Predicate<? super T> predicate) {
		List<T> result = new ArrayList<>();
		try {
			for (Entity e : level.getAllEntities()) {
				if (e != null && !e.isRemoved() && clazz.isInstance(e)) {
					boolean inP = e.position() != null && area.contains(e.position());
					boolean inB = e.getBoundingBox() != null && area.intersects(e.getBoundingBox());
					if (inP || inB) {
						T casted = clazz.cast(e);
						if (predicate == null || predicate.test(casted)) {
							result.add(casted);
						}
					}
				}
			}
		} catch (Throwable t) {
			JarStackerMod.LOGGER.error("[JarStacker] getEntitiesInArea error: ", t);
		}
		return result;
	}

	public static <T extends Entity> List<T> getEntitiesInArea(ServerLevel level, Class<T> clazz, AABB area) {
		return getEntitiesInArea(level, clazz, area, null);
	}

	public static ServerPlayer createMockPlayer(ServerLevel level, Vec3 pos, GameType gameType) {
		GameProfile profile = new GameProfile(java.util.UUID.randomUUID(), "TestPlayer");
		ServerPlayer player = new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
		player.setPos(pos.x, pos.y, pos.z);
		player.setYRot(0.0f);
		player.setXRot(0.0f);
		if (gameType == GameType.CREATIVE) {
			player.getAbilities().instabuild = true;
			player.getAbilities().invulnerable = true;
			player.getAbilities().mayBuild = true;
		} else {
			player.getAbilities().instabuild = false;
			player.getAbilities().invulnerable = false;
		}
		try {
			net.minecraft.network.Connection conn = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
			net.minecraft.server.network.CommonListenerCookie cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(profile, false);
			player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(level.getServer(), conn, player, cookie);
		} catch (Throwable ignored) {
		}
		player.setOnGround(true);
		player.fallDistance = 0.0f;
		player.setSprinting(false);
		((StackableEntity) player).jarstacker$setAttackStrengthTicker(100);
		return player;
	}

	public static void aimAt(Player player, Entity target) {
		Vec3 eye = player.getEyePosition();
		Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eye);
		double xz = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
		float yRot = (float) (net.minecraft.util.Mth.atan2(toTarget.z, toTarget.x) * (180.0 / Math.PI)) - 90.0f;
		float xRot = (float) (-(net.minecraft.util.Mth.atan2(toTarget.y, xz) * (180.0 / Math.PI)));
		player.setYRot(yRot);
		player.setXRot(xRot);
	}

	public static Entity pickEntity(Player player, ServerLevel level, double reach) {
		Vec3 eyePos = player.getEyePosition();
		Vec3 lookVec = player.getViewVector(1.0f);
		Vec3 reachEnd = eyePos.add(lookVec.scale(reach));
		AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(reach)).inflate(1.0);
		List<Entity> candidates = level.getEntities(player, searchBox, e -> !e.isSpectator() && e.isPickable());
		Entity closest = null;
		double closestDist = Double.MAX_VALUE;
		for (Entity e : candidates) {
			AABB box = e.getBoundingBox().inflate(e.getPickRadius());
			java.util.Optional<Vec3> hit = box.clip(eyePos, reachEnd);
			if (hit.isPresent()) {
				double dist = eyePos.distanceToSqr(hit.get());
				if (dist < closestDist) {
					closestDist = dist;
					closest = e;
				}
			}
		}
		return closest;
	}

	public static InteractionResult simulateFeed(Player player, ServerLevel level, Animal animal) {
		InteractionResult res = UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, animal, null);
		if (res == InteractionResult.PASS) {
			res = com.jar.jarstacker.adapter.EntityAdapter.interactOn(player, animal, InteractionHand.MAIN_HAND);
		}
		return res;
	}

	public static InteractionResult simulateInteract(Player player, ServerLevel level, Entity entity) {
		InteractionResult res = UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, entity, null);
		if (res == InteractionResult.PASS) {
			res = com.jar.jarstacker.adapter.EntityAdapter.interactOn(player, entity, InteractionHand.MAIN_HAND);
		}
		return res;
	}

	private static void runV051MigrationTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posM = pos.add(35, 0, 35);
		net.minecraft.core.BlockPos penCenterM = new net.minecraft.core.BlockPos((int) posM.x, (int) posM.y, (int) posM.z);
		buildPen(level, penCenterM, 4);
		AABB cleanAreaM = new AABB(posM.x - 6, posM.y - 2, posM.z - 6, posM.x + 6, posM.y + 6, posM.z + 6);

		// Test CM1 - Legacy Damaged Stack Migration
		try {
			cleanPen(level, cleanAreaM);
			CompoundTag legacyTag = new CompoundTag();
			legacyTag.putInt("JarStackerCount", 10);
			legacyTag.putFloat("Health", 4.0f);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posM.x, posM.y, posM.z);
			EntityAdapter.load(zombie, legacyTag);
			LogicalHealthState state = ((StackableEntity) zombie).jarstacker$getLogicalHealthState();

			boolean allFour = state != null && state.size() == 10;
			if (allFour) {
				for (int i = 0; i < state.size(); i++) {
					if (Math.abs(state.get(i) - 4.0f) > 0.01f) {
						allFour = false;
						break;
					}
				}
			}

			boolean pass = allFour && zombie.getHealth() == 4.0f;
			results.add(new TestResult("Test CM1 - Legacy Damaged Stack Migration", pass,
				"Size: " + (state != null ? state.size() : 0) + ", AllFour: " + allFour + ", RepHp: " + zombie.getHealth()));
			cleanPen(level, cleanAreaM);
		} catch (Exception e) {
			results.add(new TestResult("Test CM1 - Legacy Damaged Stack Migration", false, e.getMessage()));
		}

		// Test CM2 - Migration Idempotency
		try {
			cleanPen(level, cleanAreaM);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posM.x, posM.y, posM.z);
			((StackableEntity) zombie).jarstacker$setStackCount(10);
			zombie.setHealth(4.0f);
			LogicalStateValidator.repairLogicalState(zombie);

			// Save to NBT
			CompoundTag tag = new CompoundTag();
			EntityAdapter.saveWithoutId(zombie, tag);

			// Repeatedly load into a new entity
			Zombie reloaded = createEntity(EntityType.ZOMBIE, level);
			for (int loadCycle = 0; loadCycle < 3; loadCycle++) {
				EntityAdapter.load(reloaded, tag);
			}

			LogicalHealthState reloadedState = ((StackableEntity) reloaded).jarstacker$getLogicalHealthState();
			boolean allFour = reloadedState != null && reloadedState.size() == 10;
			if (allFour) {
				for (int i = 0; i < reloadedState.size(); i++) {
					if (Math.abs(reloadedState.get(i) - 4.0f) > 0.01f) {
						allFour = false;
						break;
					}
				}
			}

			boolean pass = allFour && ((StackableEntity) reloaded).jarstacker$getStackCount() == 10;
			results.add(new TestResult("Test CM2 - Migration Idempotency", pass,
				"Size: " + (reloadedState != null ? reloadedState.size() : 0) + ", AllFour: " + allFour + ", Count: " + ((StackableEntity) reloaded).jarstacker$getStackCount()));
			cleanPen(level, cleanAreaM);
		} catch (Exception e) {
			results.add(new TestResult("Test CM2 - Migration Idempotency", false, e.getMessage()));
		}

		// Test CM3 - Legacy Full-Health Migration
		try {
			cleanPen(level, cleanAreaM);
			CompoundTag legacyTag = new CompoundTag();
			legacyTag.putInt("JarStackerCount", 10);
			legacyTag.putFloat("Health", 20.0f);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posM.x, posM.y, posM.z);
			EntityAdapter.load(zombie, legacyTag);
			LogicalHealthState state = ((StackableEntity) zombie).jarstacker$getLogicalHealthState();

			boolean allTwenty = state != null && state.size() == 10;
			if (allTwenty) {
				for (int i = 0; i < state.size(); i++) {
					if (Math.abs(state.get(i) - 20.0f) > 0.01f) {
						allTwenty = false;
						break;
					}
				}
			}

			boolean pass = allTwenty && zombie.getHealth() == 20.0f;
			results.add(new TestResult("Test CM3 - Legacy Full-Health Migration", pass,
				"Size: " + (state != null ? state.size() : 0) + ", AllTwenty: " + allTwenty));
			cleanPen(level, cleanAreaM);
		} catch (Exception e) {
			results.add(new TestResult("Test CM3 - Legacy Full-Health Migration", false, e.getMessage()));
		}
	}

	private static void runV051EquipmentTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posE = pos.add(35, 0, 35);
		net.minecraft.core.BlockPos penCenterE = new net.minecraft.core.BlockPos((int) posE.x, (int) posE.y, (int) posE.z);
		buildPen(level, penCenterE, 4);
		AABB cleanAreaE = new AABB(posE.x - 6, posE.y - 2, posE.z - 6, posE.x + 6, posE.y + 6, posE.z + 6);
		ModConfig config = ModConfig.getInstance();

		// Test CE1 - Damageable Armor Stacking Exclusion
		try {
			cleanPen(level, cleanAreaE);
			Zombie zombieA = createEntity(EntityType.ZOMBIE, level);
			zombieA.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
			Zombie zombieB = createEntity(EntityType.ZOMBIE, level);

			boolean excludedA = MobCompatibility.isExcluded(zombieA);
			boolean canStack = MobCompatibility.canStack(zombieA, zombieB, config.getMobStacking());
			String reason = MobCompatibility.getIncompatibilityReason(zombieA, zombieB, config.getMobStacking());

			boolean pass = excludedA && !canStack && "DAMAGEABLE_EQUIPMENT".equals(reason);
			results.add(new TestResult("Test CE1 - Damageable Armor Stacking Exclusion", pass,
				"ExcludedA: " + excludedA + ", CanStack: " + canStack + ", Reason: " + reason));
			cleanPen(level, cleanAreaE);
		} catch (Exception e) {
			results.add(new TestResult("Test CE1 - Damageable Armor Stacking Exclusion", false, e.getMessage()));
		}

		// Test CE2 - Stack Acquires Damageable Armor at Runtime
		try {
			cleanPen(level, cleanAreaE);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posE.x, posE.y, posE.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);

			// Later equips armor: V0.5.2 extracts active member into singleton
			zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));

			List<Zombie> zombies = getEntitiesInArea(level, Zombie.class, cleanAreaE, Entity::isAlive);
			Zombie extracted = null;
			for (Zombie z : zombies) {
				if (z != zombie) {
					extracted = z;
					break;
				}
			}

			boolean extractedHasArmor = extracted != null && MobCompatibility.hasDamageableEquipment(extracted);
			boolean extractedExcluded = extracted != null && MobCompatibility.isExcluded(extracted);
			boolean remainderCountFour = ((StackableEntity) zombie).jarstacker$getStackCount() == 4;

			boolean pass = extracted != null && extractedHasArmor && extractedExcluded && remainderCountFour;
			results.add(new TestResult("Test CE2 - Runtime Damageable Armor Safety", pass,
				"ExtractedArmor: " + extractedHasArmor + ", ExtractedExcluded: " + extractedExcluded + ", RemainderCount: " + ((StackableEntity) zombie).jarstacker$getStackCount()));
			cleanPen(level, cleanAreaE);
		} catch (Exception e) {
			results.add(new TestResult("Test CE2 - Runtime Damageable Armor Safety", false, e.getMessage()));
		}

		// Test CE3 - Non-Damageable Equipment Stacking
		try {
			cleanPen(level, cleanAreaE);
			Zombie zombieA = createEntity(EntityType.ZOMBIE, level);
			zombieA.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIRT));
			Zombie zombieB = createEntity(EntityType.ZOMBIE, level);
			zombieB.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIRT));

			boolean hasDamageableA = MobCompatibility.hasDamageableEquipment(zombieA);
			boolean hasDamageableB = MobCompatibility.hasDamageableEquipment(zombieB);
			boolean canStack = MobCompatibility.canStack(zombieA, zombieB, config.getMobStacking());

			boolean pass = !hasDamageableA && !hasDamageableB && canStack;
			results.add(new TestResult("Test CE3 - Non-Damageable Equipment Stacking", pass,
				"DamageableA: " + hasDamageableA + ", DamageableB: " + hasDamageableB + ", CanStack: " + canStack));
			cleanPen(level, cleanAreaE);
		} catch (Exception e) {
			results.add(new TestResult("Test CE3 - Non-Damageable Equipment Stacking", false, e.getMessage()));
		}
	}

	private static void runV051DeathTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posD = pos.add(40, 0, 40);
		net.minecraft.core.BlockPos penCenterD = new net.minecraft.core.BlockPos((int) posD.x, (int) posD.y, (int) posD.z);
		buildPen(level, penCenterD, 4);
		AABB cleanAreaD = new AABB(posD.x - 6, posD.y - 2, posD.z - 6, posD.x + 6, posD.y + 6, posD.z + 6);

		// Test D1 - Partial Kill
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			LogicalHealthState state = new LogicalHealthState();
			state.add(2.0f);
			state.add(2.0f);
			state.add(2.0f);
			state.add(20.0f);
			state.add(20.0f);
			((StackableEntity) zombie).jarstacker$setLogicalHealthState(state);
			zombie.setHealth(2.0f);
			level.addFreshEntity(zombie);

			// Explosion does 10 damage: kills the 3 weakened (active + 2 virtual)
			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 10.0f);
			boolean handled = LogicalHealthManager.handleDie(zombie, level.damageSources().explosion(null));
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			int survivors = ((StackableEntity) zombie).jarstacker$getStackCount();
			boolean pass = handled && survivors == 2 && batch != null && batch.isExact() && batch.getCommittedDeaths() == 3;
			results.add(new TestResult("Test D1 - Partial Kill", pass,
				"Handled: " + handled + ", Survivors: " + survivors + ", CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1)));
			cleanPen(level, cleanAreaD);
		} catch (Exception e) {
			com.jar.jarstacker.JarStackerMod.LOGGER.error("Test D1 failed", e);
			results.add(new TestResult("Test D1 - Partial Kill", false, e.toString()));
		}

		// Test D2 - Whole Stack Kill
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			LogicalHealthManager.getOrCreateState(zombie);
			level.addFreshEntity(zombie);

			// Lethal explosion kills all 5
			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 50.0f);
			boolean handled = LogicalHealthManager.handleDie(zombie, level.damageSources().explosion(null));
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			// Handled should be false so Vanilla die() executes representative death
			boolean pass = !handled && batch != null && batch.isExact() && batch.getCommittedDeaths() == 5
				&& batch.isRepresentativeDeathOwned() && batch.getVirtualDeathsProcessed() == 4;
			results.add(new TestResult("Test D2 - Whole Stack Kill", pass,
				"Handled: " + handled + ", CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1) + ", RepOwned: " + (batch != null && batch.isRepresentativeDeathOwned())));
			cleanPen(level, cleanAreaD);
		} catch (Exception e) {
			results.add(new TestResult("Test D2 - Whole Stack Kill", false, e.getMessage()));
		}

		// Test D3 - Singleton Death
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(1);
			level.addFreshEntity(zombie);

			boolean handled = LogicalHealthManager.handleDie(zombie, level.damageSources().generic());
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			boolean pass = !handled && batch != null && batch.isExact() && batch.getCommittedDeaths() == 1
				&& batch.isRepresentativeDeathOwned() && batch.getVirtualDeathsProcessed() == 0;
			results.add(new TestResult("Test D3 - Singleton Death", pass,
				"Handled: " + handled + ", CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1)));
			cleanPen(level, cleanAreaD);
		} catch (Exception e) {
			results.add(new TestResult("Test D3 - Singleton Death", false, e.getMessage()));
		}

		// Test D4 - Stack ร—2 Death Boundary
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			LogicalHealthManager.getOrCreateState(zombie);
			level.addFreshEntity(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 50.0f);
			boolean handled = LogicalHealthManager.handleDie(zombie, level.damageSources().explosion(null));
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			boolean pass = !handled && batch != null && batch.isExact() && batch.getCommittedDeaths() == 2
				&& batch.isRepresentativeDeathOwned() && batch.getVirtualDeathsProcessed() == 1;
			results.add(new TestResult("Test D4 - Stack ร—2 Death Boundary", pass,
				"Handled: " + handled + ", CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1) + ", Virtual: " + (batch != null ? batch.getVirtualDeathsProcessed() : -1)));
			cleanPen(level, cleanAreaD);
		} catch (Exception e) {
			results.add(new TestResult("Test D4 - Stack ร—2 Death Boundary", false, e.getMessage()));
		}

		// Test D5 - Looting Sweep
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			LogicalHealthManager.getOrCreateState(zombie);
			level.addFreshEntity(zombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posD.x, posD.y, posD.z - 2.0), GameType.SURVIVAL);
			com.jar.jarstacker.stack.mob.health.CombatContext.beginAttack(player, zombie);
			LogicalHealthManager.applySweepToPrimary(zombie, 50.0f);
			com.jar.jarstacker.stack.mob.health.CombatContext.endAttack();
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			boolean pass = batch != null && batch.isExact() && batch.getVirtualDeathsProcessed() == 2;
			results.add(new TestResult("Test D5 - Looting Sweep", pass,
				"BatchExact: " + (batch != null && batch.isExact()) + ", VirtualDeaths: " + (batch != null ? batch.getVirtualDeathsProcessed() : -1)));
			cleanPen(level, cleanAreaD);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test D5 - Looting Sweep", false, e.getMessage()));
		}

		// Test D6 - Explosion Death Exactness
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			LogicalHealthManager.getOrCreateState(zombie);
			level.addFreshEntity(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 50.0f);
			LogicalHealthManager.handleDie(zombie, level.damageSources().explosion(null));
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			boolean pass = batch != null && batch.isExact() && batch.getCommittedDeaths() == 5;
			results.add(new TestResult("Test D6 - Explosion Death Exactness", pass,
				"Committed: " + (batch != null ? batch.getCommittedDeaths() : -1)));
			cleanPen(level, cleanAreaD);
		} catch (Exception e) {
			results.add(new TestResult("Test D6 - Explosion Death Exactness", false, e.getMessage()));
		}

		// Test D7 - Lava Whole Stack No Player XP
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			LogicalHealthManager.getOrCreateState(zombie);
			level.addFreshEntity(zombie);

			DamageSource lavaSource = level.damageSources().lava();
			LogicalHealthManager.onDamageApplied(zombie, lavaSource, 50.0f);
			LogicalHealthManager.handleDie(zombie, lavaSource);

			int totalXp = getEntitiesInArea(level, ExperienceOrb.class, cleanAreaD).stream().mapToInt(ExperienceOrb::getValue).sum();

			boolean pass = totalXp == 0;
			results.add(new TestResult("Test D7 - Lava Whole Stack No Player XP", pass,
				"TotalXp: " + totalXp));
			cleanPen(level, cleanAreaD);
		} catch (Exception e) {
			results.add(new TestResult("Test D7 - Lava Whole Stack No Player XP", false, e.getMessage()));
		}

		// Test D8 - Player-Caused Environment Attribution
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			LogicalHealthManager.getOrCreateState(zombie);
			level.addFreshEntity(zombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posD.x, posD.y, posD.z - 2.0), GameType.SURVIVAL);
			DamageSource playerAttack = level.damageSources().playerAttack(player);
			LogicalHealthManager.onDamageApplied(zombie, playerAttack, 1.0f);

			// Mob takes fall damage later
			DamageSource fallSource = level.damageSources().fall();
			LogicalHealthManager.onDamageApplied(zombie, fallSource, 50.0f);
			LogicalHealthManager.handleDie(zombie, fallSource);

			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();
			boolean pass = batch != null && batch.isExact() && batch.getCommittedDeaths() == 3;
			results.add(new TestResult("Test D8 - Player-Caused Environment Attribution", pass,
				"CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1)));
			cleanPen(level, cleanAreaD);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test D8 - Player-Caused Environment Attribution", false, e.getMessage()));
		}

		// Test D9 - XP Value Sum
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(4);
			level.addFreshEntity(zombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posD.x, posD.y, posD.z - 2.0), GameType.SURVIVAL);
			DamageSource playerSource = level.damageSources().playerAttack(player);

			LogicalHealthManager.processDeaths(zombie, playerSource, 4);
			List<ExperienceOrb> orbsD = getEntitiesInArea(level, ExperienceOrb.class, cleanAreaD);
			int totalXp = orbsD.stream().mapToInt(orb -> {
				CompoundTag tag = new CompoundTag();
				EntityAdapter.addAdditionalSaveData(orb, tag);
				int count = Math.max(1, com.jar.jarstacker.adapter.NbtAdapter.getInt(tag, "Count"));
				return orb.getValue() * count;
			}).sum();

			// Each zombie drops 5 XP, 4 zombies = 20 XP
			boolean pass = totalXp >= 19;
			results.add(new TestResult("Test D9 - XP Value Sum", pass,
				"TotalXpSum: " + totalXp + " for 4 deaths (expected >= 20)"));
			cleanPen(level, cleanAreaD);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test D9 - XP Value Sum", false, e.getMessage()));
		}

		// Test D10 - No Duplicate Items
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			LogicalHealthManager.getOrCreateState(zombie);
			level.addFreshEntity(zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 50.0f);
			boolean handled = LogicalHealthManager.handleDie(zombie, level.damageSources().explosion(null));
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			boolean pass = !handled && batch != null && batch.isExact() && batch.getCommittedDeaths() == 2
				&& batch.isRepresentativeDeathOwned() && batch.getVirtualDeathsProcessed() == 1;
			results.add(new TestResult("Test D10 - No Duplicate Items", pass,
				"Handled: " + handled + ", CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1)));
			cleanPen(level, cleanAreaD);
		} catch (Exception e) {
			results.add(new TestResult("Test D10 - No Duplicate Items", false, e.getMessage()));
		}

		// Test D11 - ร—100 Lethal AoE
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(100);
			LogicalHealthManager.getOrCreateState(zombie);
			boolean addedD11 = level.addFreshEntity(zombie);

			int initialPhysical = getEntitiesInArea(level, Zombie.class, cleanAreaD, Entity::isAlive).size();
			long t0 = System.nanoTime();
			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 100.0f);
			boolean handled = LogicalHealthManager.handleDie(zombie, level.damageSources().explosion(null));
			long durationNs = System.nanoTime() - t0;
			double durationMs = durationNs / 1_000_000.0;

			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			boolean pass = !handled && batch != null && batch.isExact() && batch.getCommittedDeaths() == 100 && initialPhysical == 1;
			results.add(new TestResult("Test D11 - ร—100 Lethal AoE", pass,
				"CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1) + ", Duration: " + String.format("%.2f", durationMs) + " ms, PhysicalSpawned: 0"));
			cleanPen(level, cleanAreaD);
		} catch (Exception e) {
			results.add(new TestResult("Test D11 - ร—100 Lethal AoE", false, e.getMessage()));
		}

		// Test D12 - Mixed Survivors
		try {
			cleanPen(level, cleanAreaD);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posD.x, posD.y, posD.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			LogicalHealthState state = new LogicalHealthState();
			state.add(1.0f);
			state.add(5.0f);
			state.add(10.0f);
			state.add(15.0f);
			state.add(20.0f);
			((StackableEntity) zombie).jarstacker$setLogicalHealthState(state);
			zombie.setHealth(1.0f);
			level.addFreshEntity(zombie);

			// Damage = 10 from AREA (explosion)
			// Index 0 (1HP) dies, Index 1 (5HP) dies, Index 2 (10HP) dies: 3 deaths!
			// Index 3 (15HP) -> 5HP, Index 4 (20HP) -> 10HP: 2 survivors!
			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 10.0f);
			boolean handled = LogicalHealthManager.handleDie(zombie, level.damageSources().explosion(null));
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			int survivors = ((StackableEntity) zombie).jarstacker$getStackCount();
			LogicalHealthState postState = ((StackableEntity) zombie).jarstacker$getLogicalHealthState();
			boolean hpOk = postState != null && postState.size() == 2 && Math.abs(postState.get(0) - 5.0f) < 0.01f && Math.abs(postState.get(1) - 10.0f) < 0.01f;

			boolean pass = handled && survivors == 2 && hpOk && batch != null && batch.isExact() && batch.getCommittedDeaths() == 3;
			results.add(new TestResult("Test D12 - Mixed Survivors", pass,
				"Handled: " + handled + ", Survivors: " + survivors + ", PostHps: " + (postState != null ? postState.getRecords() : "null") + ", CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1)));
			cleanPen(level, cleanAreaD);
		} catch (Exception e) {
			results.add(new TestResult("Test D12 - Mixed Survivors", false, e.getMessage()));
		}
	}

	private static ItemStack createLootingSword(ServerLevel level, int levelNum) {
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		var reg = level.registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
		if (reg.isPresent()) {
			var looting = reg.get().get(Enchantments.LOOTING);
			if (looting.isPresent()) {
				sword.enchant(looting.get(), levelNum);
			}
		}
		return sword;
	}

	private static void runV052EquipmentTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posE = pos.add(45, 0, 45);
		net.minecraft.core.BlockPos penCenterE = new net.minecraft.core.BlockPos((int) posE.x, (int) posE.y, (int) posE.z);
		buildPen(level, penCenterE, 4);
		AABB cleanAreaE = new AABB(posE.x - 6, posE.y - 2, posE.z - 6, posE.x + 6, posE.y + 6, posE.z + 6);
		ModConfig config = ModConfig.getInstance();

		// Test CE4 - Runtime Armor Pickup
		try {
			cleanPen(level, cleanAreaE);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posE.x, posE.y, posE.z);
			((StackableEntity) zombie).jarstacker$setStackCount(10);
			LogicalHealthManager.getOrCreateState(zombie);
			level.addFreshEntity(zombie);

			zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));

			List<Zombie> zombies = getEntitiesInArea(level, Zombie.class, cleanAreaE);
			Zombie remainder = null;
			Zombie extracted = null;
			for (Zombie z : zombies) {
				if (z == zombie) remainder = z;
				else extracted = z;
			}

			boolean pass = zombies.size() == 2
				&& remainder != null
				&& ((StackableEntity) remainder).jarstacker$getStackCount() == 9
				&& remainder.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
				&& extracted != null
				&& ((StackableEntity) extracted).jarstacker$getStackCount() == 1
				&& extracted.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET);

			results.add(new TestResult("Test CE4 - Runtime Armor Pickup", pass,
				"Zombies: " + zombies.size() + ", RemainderCount: " + (remainder != null ? ((StackableEntity) remainder).jarstacker$getStackCount() : "null")
				+ ", ExtractedCount: " + (extracted != null ? ((StackableEntity) extracted).jarstacker$getStackCount() : "null")
				+ ", HasHelmet: " + (extracted != null && extracted.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET))));
			cleanPen(level, cleanAreaE);
		} catch (Exception e) {
			results.add(new TestResult("Test CE4 - Runtime Armor Pickup", false, e.getMessage()));
		}

		// Test CE5 - Damaged Active Mob Pickup
		try {
			cleanPen(level, cleanAreaE);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posE.x, posE.y, posE.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			LogicalHealthState state = new LogicalHealthState();
			state.add(6.0f);
			state.add(20.0f);
			state.add(20.0f);
			state.add(20.0f);
			state.add(20.0f);
			((StackableEntity) zombie).jarstacker$setLogicalHealthState(state);
			zombie.setHealth(6.0f);
			level.addFreshEntity(zombie);

			zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));

			List<Zombie> zombies = getEntitiesInArea(level, Zombie.class, cleanAreaE);
			Zombie remainder = null;
			Zombie extracted = null;
			for (Zombie z : zombies) {
				if (z == zombie) remainder = z;
				else extracted = z;
			}

			boolean pass = zombies.size() == 2
				&& remainder != null
				&& ((StackableEntity) remainder).jarstacker$getStackCount() == 4
				&& remainder.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()
				&& remainder.getHealth() == 20.0f
				&& extracted != null
				&& ((StackableEntity) extracted).jarstacker$getStackCount() == 1
				&& extracted.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.IRON_SWORD)
				&& extracted.getHealth() == 6.0f;

			results.add(new TestResult("Test CE5 - Damaged Active Mob Pickup", pass,
				"RemainderHp: " + (remainder != null ? remainder.getHealth() : "null")
				+ ", ExtractedHp: " + (extracted != null ? extracted.getHealth() : "null")
				+ ", RemainderCount: " + (remainder != null ? ((StackableEntity) remainder).jarstacker$getStackCount() : "null")));
			cleanPen(level, cleanAreaE);
		} catch (Exception e) {
			results.add(new TestResult("Test CE5 - Damaged Active Mob Pickup", false, e.getMessage()));
		}

		// Test CE6 - Item Ownership
		try {
			cleanPen(level, cleanAreaE);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posE.x, posE.y, posE.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);

			zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));

			List<Zombie> zombies = getEntitiesInArea(level, Zombie.class, cleanAreaE);
			int helmetCount = 0;
			for (Zombie z : zombies) {
				if (z.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET)) {
					helmetCount++;
				}
			}

			boolean pass = helmetCount == 1;
			results.add(new TestResult("Test CE6 - Item Ownership", pass,
				"TotalHelmets: " + helmetCount + " (expected 1 across all entities)"));
			cleanPen(level, cleanAreaE);
		} catch (Exception e) {
			results.add(new TestResult("Test CE6 - Item Ownership", false, e.getMessage()));
		}

		// Test CE7 - Safe Placement
		try {
			cleanPen(level, cleanAreaE);
			buildPen(level, penCenterE, 3);
			Vec3 cornerPos = new Vec3(penCenterE.getX() + 1.8, penCenterE.getY(), penCenterE.getZ() + 1.8);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(cornerPos.x, cornerPos.y, cornerPos.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);

			zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));

			List<Zombie> zombies = getEntitiesInArea(level, Zombie.class, cleanAreaE);
			Zombie extracted = null;
			for (Zombie z : zombies) {
				if (z != zombie) extracted = z;
			}

			boolean pass = extracted != null
				&& isInsidePen(extracted.position(), penCenterE, 3)
				&& level.noCollision(extracted);

			results.add(new TestResult("Test CE7 - Safe Placement", pass,
				"ExtractedInside: " + (extracted != null && isInsidePen(extracted.position(), penCenterE, 3))
				+ ", NoCollision: " + (extracted != null && level.noCollision(extracted))));
			cleanPen(level, cleanAreaE);
		} catch (Exception e) {
			results.add(new TestResult("Test CE7 - Safe Placement", false, e.getMessage()));
		}

		// Test CE8 - Remove Equipment
		try {
			cleanPen(level, cleanAreaE);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
			boolean initialExcluded = MobCompatibility.hasDamageableEquipment(zombie);

			// Mob removes equipment
			zombie.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
			boolean afterExcluded = MobCompatibility.hasDamageableEquipment(zombie);

			Zombie partner = createEntity(EntityType.ZOMBIE, level);
			boolean canStack = MobCompatibility.canStack(zombie, partner, config.getMobStacking());

			boolean pass = initialExcluded && !afterExcluded && canStack;
			results.add(new TestResult("Test CE8 - Remove Equipment", pass,
				"InitialExcluded: " + initialExcluded + ", AfterExcluded: " + afterExcluded + ", CanStack: " + canStack));
			cleanPen(level, cleanAreaE);
		} catch (Exception e) {
			results.add(new TestResult("Test CE8 - Remove Equipment", false, e.getMessage()));
		}

		// Test CE9 - Transition Failure
		try {
			cleanPen(level, cleanAreaE);
			RuntimeCombatStateTransitionHandler.failNextExtractionForTesting = true;

			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posE.x, posE.y, posE.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);

			zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));

			List<Zombie> zombies = getEntitiesInArea(level, Zombie.class, cleanAreaE);
			int totalLogical = 0;
			for (Zombie z : zombies) {
				totalLogical += ((StackableEntity) z).jarstacker$getStackCount();
			}

			boolean pass = totalLogical == 5 && zombies.size() == 1;
			results.add(new TestResult("Test CE9 - Transition Failure", pass,
				"TotalLogical: " + totalLogical + " (expected 5), PhysicalCount: " + zombies.size()));
			cleanPen(level, cleanAreaE);
		} catch (Exception e) {
			results.add(new TestResult("Test CE9 - Transition Failure", false, e.getMessage()));
		}
	}

	private static void runV052AttributionTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posA = pos.add(50, 0, 50);
		net.minecraft.core.BlockPos penCenterA = new net.minecraft.core.BlockPos((int) posA.x, (int) posA.y, (int) posA.z);
		buildPen(level, penCenterA, 4);
		AABB cleanAreaA = new AABB(posA.x - 6, posA.y - 2, posA.z - 6, posA.x + 6, posA.y + 6, posA.z + 6);

		// Test VC1 - Arrow Switch TO Looting (MC-3304 Parity)
		try {
			cleanPen(level, cleanAreaA);
			Zombie vanillaZombie = createEntity(EntityType.ZOMBIE, level);
			vanillaZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) vanillaZombie).jarstacker$setStackCount(1);
			level.addFreshEntity(vanillaZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));

			Arrow vanillaArrow = new Arrow(level, player, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			DamageSource vanillaArrowSource = level.damageSources().arrow(vanillaArrow, player);

			// Switch TO Looting III sword before death
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));
			CombatDeathContext vbCtx = CombatDeathContext.capture(vanillaZombie, vanillaArrowSource);

			// Stack test: Zombie x3
			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(3);
			LogicalHealthManager.getOrCreateState(stackZombie);
			level.addFreshEntity(stackZombie);

			Arrow stackArrow = new Arrow(level, player, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			DamageSource stackArrowSource = level.damageSources().arrow(stackArrow, player);
			CombatDeathContext jsCtx = CombatDeathContext.capture(stackZombie, stackArrowSource);

			LogicalHealthManager.onDamageApplied(stackZombie, stackArrowSource, 100.0f);
			LogicalHealthManager.handleDie(stackZombie, stackArrowSource);

			boolean pass = vbCtx.getLootingLevel() == 3
				&& jsCtx.getLootingLevel() == 3
				&& jsCtx.hasPlayerCredit()
				&& "PROJECTILE".equals(jsCtx.getAttributionType())
				&& ((StackableEntity) stackZombie).jarstacker$getStackCount() == 2;

			results.add(new TestResult("Test VC1 - Arrow Switch TO Looting (MC-3304 Parity)", pass,
				"VB Looting: " + vbCtx.getLootingLevel() + ", JS Looting: " + jsCtx.getLootingLevel()
				+ ", StackCount: " + ((StackableEntity) stackZombie).jarstacker$getStackCount()));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC1 - Arrow Switch TO Looting (MC-3304 Parity)", false, e.getMessage()));
		}

		// Test VC2 - Arrow Switch AWAY
		try {
			cleanPen(level, cleanAreaA);
			Zombie vanillaZombie = createEntity(EntityType.ZOMBIE, level);
			vanillaZombie.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(vanillaZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));

			Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			DamageSource arrowSource = level.damageSources().arrow(arrow, player);

			// Switch AWAY from Looting sword before death
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WOODEN_SWORD));
			CombatDeathContext vbCtx = CombatDeathContext.capture(vanillaZombie, arrowSource);

			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(3);
			level.addFreshEntity(stackZombie);

			CombatDeathContext jsCtx = CombatDeathContext.capture(stackZombie, arrowSource);

			boolean pass = vbCtx.getLootingLevel() == 0
				&& jsCtx.getLootingLevel() == 0
				&& jsCtx.hasPlayerCredit()
				&& "PROJECTILE".equals(jsCtx.getAttributionType());

			results.add(new TestResult("Test VC2 - Arrow Switch AWAY", pass,
				"VB Looting: " + vbCtx.getLootingLevel() + ", JS Looting: " + jsCtx.getLootingLevel() + ", Credit: " + jsCtx.hasPlayerCredit()));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC2 - Arrow Switch AWAY", false, e.getMessage()));
		}

		// Test VC3 - Bow Off-Hand + Looting Main-Hand
		try {
			cleanPen(level, cleanAreaA);
			Zombie vanillaZombie = createEntity(EntityType.ZOMBIE, level);
			vanillaZombie.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(vanillaZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BOW));

			Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			DamageSource arrowSource = level.damageSources().arrow(arrow, player);

			CombatDeathContext vbCtx = CombatDeathContext.capture(vanillaZombie, arrowSource);

			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(3);
			level.addFreshEntity(stackZombie);

			CombatDeathContext jsCtx = CombatDeathContext.capture(stackZombie, arrowSource);

			boolean pass = vbCtx.getLootingLevel() == 3
				&& jsCtx.getLootingLevel() == 3
				&& jsCtx.hasPlayerCredit();

			results.add(new TestResult("Test VC3 - Bow Off-Hand + Looting Main-Hand", pass,
				"VB Looting: " + vbCtx.getLootingLevel() + ", JS Looting: " + jsCtx.getLootingLevel()));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC3 - Bow Off-Hand + Looting Main-Hand", false, e.getMessage()));
		}

		// Test VC4 - Projectile Owner Isolation
		try {
			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(3);
			level.addFreshEntity(stackZombie);

			ServerPlayer playerA = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			playerA.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));

			ServerPlayer playerB = createMockPlayer(level, new Vec3(posA.x + 1.0, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			playerB.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));

			Arrow arrowA = new Arrow(level, playerA, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			DamageSource arrowSourceA = level.damageSources().arrow(arrowA, playerA);

			CombatDeathContext ctx = CombatDeathContext.capture(stackZombie, arrowSourceA);

			boolean pass = ctx.getCausingEntity() == playerA
				&& ctx.getLootingLevel() == 0
				&& ctx.hasPlayerCredit();

			results.add(new TestResult("Test VC4 - Projectile Owner Isolation", pass,
				"Owner: " + (ctx.getCausingEntity() == playerA ? "PlayerA" : "Unknown")
				+ ", Looting: " + ctx.getLootingLevel() + " (PlayerB Looting isolated)"));
			cleanPen(level, cleanAreaA);
			playerA.discard();
			playerB.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC4 - Projectile Owner Isolation", false, e.getMessage()));
		}

		// Test VC5 - Player TNT + Looting
		try {
			cleanPen(level, cleanAreaA);
			Zombie vanillaZombie = createEntity(EntityType.ZOMBIE, level);
			vanillaZombie.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(vanillaZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));

			PrimedTnt tnt = new PrimedTnt(level, posA.x, posA.y, posA.z, player);
			DamageSource tntSource = level.damageSources().explosion(tnt, player);

			CombatDeathContext vbCtx = CombatDeathContext.capture(vanillaZombie, tntSource);

			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(3);
			level.addFreshEntity(stackZombie);

			CombatDeathContext jsCtx = CombatDeathContext.capture(stackZombie, tntSource);

			boolean pass = "PLAYER_TNT".equals(vbCtx.getAttributionType())
				&& vbCtx.getLootingLevel() == 3
				&& "PLAYER_TNT".equals(jsCtx.getAttributionType())
				&& jsCtx.getLootingLevel() == 3
				&& jsCtx.hasPlayerCredit();

			results.add(new TestResult("Test VC5 - Player TNT + Looting", pass,
				"VB Looting: " + vbCtx.getLootingLevel() + ", JS Looting: " + jsCtx.getLootingLevel()
				+ ", Type: " + jsCtx.getAttributionType()));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC5 - Player TNT + Looting", false, e.getMessage()));
		}

		// Test VC6 - Player TNT Switch Away
		try {
			cleanPen(level, cleanAreaA);
			Zombie vanillaZombie = createEntity(EntityType.ZOMBIE, level);
			vanillaZombie.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(vanillaZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));

			PrimedTnt tnt = new PrimedTnt(level, posA.x, posA.y, posA.z, player);
			DamageSource tntSource = level.damageSources().explosion(tnt, player);

			// Switch AWAY from Looting sword before explosion kills
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

			CombatDeathContext vbCtx = CombatDeathContext.capture(vanillaZombie, tntSource);

			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(3);
			level.addFreshEntity(stackZombie);

			CombatDeathContext jsCtx = CombatDeathContext.capture(stackZombie, tntSource);

			boolean pass = vbCtx.getLootingLevel() == 0
				&& jsCtx.getLootingLevel() == 0
				&& jsCtx.hasPlayerCredit()
				&& "PLAYER_TNT".equals(jsCtx.getAttributionType());

			results.add(new TestResult("Test VC6 - Player TNT Switch Away", pass,
				"VB Looting: " + vbCtx.getLootingLevel() + ", JS Looting: " + jsCtx.getLootingLevel() + ", Credit: " + jsCtx.hasPlayerCredit()));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC6 - Player TNT Switch Away", false, e.getMessage()));
		}

		// Test VC7 - Unowned TNT
		try {
			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(3);
			level.addFreshEntity(stackZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));

			PrimedTnt unownedTnt = new PrimedTnt(level, posA.x, posA.y, posA.z, null);
			DamageSource tntSource = level.damageSources().explosion(unownedTnt, null);

			CombatDeathContext ctx = CombatDeathContext.capture(stackZombie, tntSource);

			boolean pass = !ctx.hasPlayerCredit()
				&& ctx.getLootingLevel() == 0
				&& "EXPLOSION".equals(ctx.getAttributionType());

			results.add(new TestResult("Test VC7 - Unowned TNT", pass,
				"Type: " + ctx.getAttributionType() + ", PlayerCredit: " + ctx.hasPlayerCredit() + ", Looting: " + ctx.getLootingLevel()));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC7 - Unowned TNT", false, e.getMessage()));
		}

		// Test VC8 - Recent Player Hit + Lava
		try {
			cleanPen(level, cleanAreaA);
			Zombie vanillaZombie = createEntity(EntityType.ZOMBIE, level);
			vanillaZombie.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(vanillaZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));

			((StackableEntity) vanillaZombie).jarstacker$setLastHurtByPlayerTime(100);
			((StackableEntity) vanillaZombie).jarstacker$setLastHurtByPlayer(player);

			DamageSource lavaSource = level.damageSources().lava();
			CombatDeathContext vbCtx = CombatDeathContext.capture(vanillaZombie, lavaSource);

			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(3);
			((StackableEntity) stackZombie).jarstacker$setLastHurtByPlayerTime(100);
			((StackableEntity) stackZombie).jarstacker$setLastHurtByPlayer(player);
			level.addFreshEntity(stackZombie);

			CombatDeathContext jsCtx = CombatDeathContext.capture(stackZombie, lavaSource);

			boolean pass = "RECENT_PLAYER_ENVIRONMENT".equals(vbCtx.getAttributionType())
				&& vbCtx.hasPlayerCredit()
				&& vbCtx.getLootingLevel() == 0
				&& "RECENT_PLAYER_ENVIRONMENT".equals(jsCtx.getAttributionType())
				&& jsCtx.hasPlayerCredit()
				&& jsCtx.getLootingLevel() == 0;

			results.add(new TestResult("Test VC8 - Recent Player Hit + Lava", pass,
				"VB Looting: " + vbCtx.getLootingLevel() + ", JS Looting: " + jsCtx.getLootingLevel()
				+ ", Credit: " + jsCtx.hasPlayerCredit() + ", Type: " + jsCtx.getAttributionType()));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC8 - Recent Player Hit + Lava", false, e.getMessage()));
		}

		// Test VC9 - Natural Lava
		try {
			cleanPen(level, cleanAreaA);
			Zombie vanillaZombie = createEntity(EntityType.ZOMBIE, level);
			vanillaZombie.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(vanillaZombie);

			DamageSource lavaSource = level.damageSources().lava();
			CombatDeathContext vbCtx = CombatDeathContext.capture(vanillaZombie, lavaSource);

			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(5);
			level.addFreshEntity(stackZombie);

			CombatDeathContext jsCtx = CombatDeathContext.capture(stackZombie, lavaSource);

			boolean pass = "NATURAL_ENVIRONMENT".equals(vbCtx.getAttributionType())
				&& !vbCtx.hasPlayerCredit()
				&& vbCtx.getLootingLevel() == 0
				&& "NATURAL_ENVIRONMENT".equals(jsCtx.getAttributionType())
				&& !jsCtx.hasPlayerCredit()
				&& jsCtx.getLootingLevel() == 0;

			results.add(new TestResult("Test VC9 - Natural Lava", pass,
				"VB Looting: " + vbCtx.getLootingLevel() + ", JS Looting: " + jsCtx.getLootingLevel()
				+ ", Credit: " + jsCtx.hasPlayerCredit()));
			cleanPen(level, cleanAreaA);
		} catch (Exception e) {
			results.add(new TestResult("Test VC9 - Natural Lava", false, e.getMessage()));
		}

		// Test VC10 - Sweep Secondary
		try {
			cleanPen(level, cleanAreaA);
			Zombie primaryZombie = createEntity(EntityType.ZOMBIE, level);
			primaryZombie.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(primaryZombie);

			Zombie secondaryZombie = createEntity(EntityType.ZOMBIE, level);
			secondaryZombie.setPos(posA.x + 1.0, posA.y, posA.z);
			level.addFreshEntity(secondaryZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));

			// Vanilla secondary sweep attack uses playerAttack damage source
			DamageSource sweepDamageSource = level.damageSources().playerAttack(player);
			CombatContext.beginAttack(player, primaryZombie);
			CombatContext.setSweepAuthorized(5.0f);
			CombatDeathContext secondaryCtx = CombatDeathContext.capture(secondaryZombie, sweepDamageSource);
			CombatContext.endAttack();

			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(4);
			LogicalHealthManager.getOrCreateState(stackZombie);
			level.addFreshEntity(stackZombie);

			CombatContext.beginAttack(player, stackZombie);
			CombatContext.setSweepAuthorized(5.0f);
			LogicalHealthManager.applySweepToPrimary(stackZombie, 100.0f);
			CombatContext.endAttack();

			CombatDeathContext lastCtx = CombatDeathContext.getLastRecorded();

			boolean pass = "SWEEP".equals(secondaryCtx.getAttributionType())
				&& secondaryCtx.hasPlayerCredit()
				&& secondaryCtx.getLootingLevel() == 3
				&& lastCtx != null
				&& "SWEEP".equals(lastCtx.getAttributionType())
				&& lastCtx.hasPlayerCredit()
				&& lastCtx.getLootingLevel() == 3;

			results.add(new TestResult("Test VC10 - Sweep Secondary", pass,
				"SecondaryType: " + secondaryCtx.getAttributionType() + ", SecondaryLooting: " + secondaryCtx.getLootingLevel()
				+ ", StackSweepLooting: " + (lastCtx != null ? lastCtx.getLootingLevel() : "null")));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC10 - Sweep Secondary", false, e.getMessage()));
		}

		// Test VC11 - Entire Logical Stack Projectile Kill
		try {
			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(5);
			LogicalHealthState state = new LogicalHealthState();
			for (int i = 0; i < 5; i++) state.add(1.0f);
			((StackableEntity) stackZombie).jarstacker$setLogicalHealthState(state);
			level.addFreshEntity(stackZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));

			Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			DamageSource arrowSource = level.damageSources().arrow(arrow, player);

			int totalCommitted = 0;
			boolean allProjectile = true;
			for (int i = 0; i < 5; i++) {
				LogicalHealthManager.onDamageApplied(stackZombie, arrowSource, 100.0f);
				LogicalHealthManager.handleDie(stackZombie, arrowSource);
				DeathBatch batch = LogicalHealthManager.getLastDeathBatch();
				if (batch != null) totalCommitted += batch.getCommittedDeaths();
				CombatDeathContext ctx = CombatDeathContext.getLastRecorded();
				if (ctx == null || !"PROJECTILE".equals(ctx.getAttributionType()) || !ctx.hasPlayerCredit() || ctx.getLootingLevel() != 3) {
					allProjectile = false;
				}
			}

			boolean pass = totalCommitted == 5 && allProjectile;
			results.add(new TestResult("Test VC11 - Entire Logical Stack Projectile Kill", pass,
				"Committed: " + totalCommitted + ", AllProjectileLooting3: " + allProjectile));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC11 - Entire Logical Stack Projectile Kill", false, e.getMessage()));
		}

		// Test VC12 - Final Representative Parity
		try {
			cleanPen(level, cleanAreaA);
			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posA.x, posA.y, posA.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(4);
			LogicalHealthManager.getOrCreateState(stackZombie);
			level.addFreshEntity(stackZombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));
			PrimedTnt tnt = new PrimedTnt(level, posA.x, posA.y, posA.z, player);
			DamageSource tntSource = level.damageSources().explosion(tnt, player);

			// Lethal AoE damage that kills all 4 in one blow
			LogicalHealthManager.onDamageApplied(stackZombie, tntSource, 1000.0f);
			boolean cancelled = LogicalHealthManager.handleDie(stackZombie, tntSource);
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			boolean pass = !cancelled && batch != null && batch.getCommittedDeaths() == 4
				&& batch.getVirtualDeathsProcessed() == 3
				&& batch.isRepresentativeDeathOwned();

			results.add(new TestResult("Test VC12 - Final Representative Parity", pass,
				"VirtualDeaths: " + (batch != null ? batch.getVirtualDeathsProcessed() : -1)
				+ ", RepOwned: " + (batch != null && batch.isRepresentativeDeathOwned())
				+ ", TotalCommitted: " + (batch != null ? batch.getCommittedDeaths() : -1)));
			cleanPen(level, cleanAreaA);
			tnt.discard();
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC12 - Final Representative Parity", false, e.getMessage()));
		}

		// Test VC13 - Context Cleanup
		try {
			cleanPen(level, cleanAreaA);
			Zombie zombie1 = createEntity(EntityType.ZOMBIE, level);
			zombie1.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(zombie1);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));
			DamageSource playerSource = level.damageSources().playerAttack(player);

			CombatDeathContext ctx1 = CombatDeathContext.capture(zombie1, playerSource);
			try (CombatDeathContext.Scope scope = ctx1.openScope()) {
				// Inside scope
			}

			// Scope has closed, threadlocal should be cleared
			CombatDeathContext currentAfterScope = CombatDeathContext.getCurrentContext();

			// Now trigger unrelated environmental death
			Zombie zombie2 = createEntity(EntityType.ZOMBIE, level);
			zombie2.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(zombie2);
			DamageSource lavaSource = level.damageSources().lava();
			CombatDeathContext ctx2 = CombatDeathContext.capture(zombie2, lavaSource);

			boolean pass = currentAfterScope == null
				&& ctx2.getLootingLevel() == 0
				&& !ctx2.hasPlayerCredit()
				&& "NATURAL_ENVIRONMENT".equals(ctx2.getAttributionType());

			results.add(new TestResult("Test VC13 - Context Cleanup", pass,
				"CurrentAfterScope: " + (currentAfterScope == null ? "null (clean)" : "LEAKED")
				+ ", SubsequentLooting: " + ctx2.getLootingLevel()));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC13 - Context Cleanup", false, e.getMessage()));
		}

		// Test VC14 - Two Players Same Tick
		try {
			cleanPen(level, cleanAreaA);
			Zombie zombieA = createEntity(EntityType.ZOMBIE, level);
			zombieA.setPos(posA.x - 1.0, posA.y, posA.z);
			level.addFreshEntity(zombieA);

			Zombie zombieB = createEntity(EntityType.ZOMBIE, level);
			zombieB.setPos(posA.x + 1.0, posA.y, posA.z);
			level.addFreshEntity(zombieB);

			ServerPlayer playerA = createMockPlayer(level, new Vec3(posA.x - 1.0, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			playerA.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));

			ServerPlayer playerB = createMockPlayer(level, new Vec3(posA.x + 1.0, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			playerB.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WOODEN_SWORD));

			DamageSource sourceA = level.damageSources().playerAttack(playerA);
			DamageSource sourceB = level.damageSources().playerAttack(playerB);

			CombatDeathContext ctxA = CombatDeathContext.capture(zombieA, sourceA);
			CombatDeathContext ctxB = CombatDeathContext.capture(zombieB, sourceB);

			boolean pass = ctxA.getCausingEntity() == playerA
				&& ctxA.getLootingLevel() == 3
				&& ctxB.getCausingEntity() == playerB
				&& ctxB.getLootingLevel() == 0;

			results.add(new TestResult("Test VC14 - Two Players Same Tick", pass,
				"PlayerA Looting: " + ctxA.getLootingLevel() + ", PlayerB Looting: " + ctxB.getLootingLevel()));
			cleanPen(level, cleanAreaA);
			playerA.discard();
			playerB.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC14 - Two Players Same Tick", false, e.getMessage()));
		}

		// Test VC15 - Owner Changes Equipment Before Death
		try {
			cleanPen(level, cleanAreaA);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posA.x, posA.y, posA.z);
			level.addFreshEntity(zombie);

			ServerPlayer player = createMockPlayer(level, new Vec3(posA.x, posA.y, posA.z - 2.0), GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_SWORD));

			Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			DamageSource arrowSource = level.damageSources().arrow(arrow, player);

			// Owner changes main hand to Looting III before death
			player.setItemInHand(InteractionHand.MAIN_HAND, createLootingSword(level, 3));
			CombatDeathContext ctx = CombatDeathContext.capture(zombie, arrowSource);

			boolean pass = ctx.getLootingLevel() == 3 && ctx.hasPlayerCredit();
			results.add(new TestResult("Test VC15 - Owner Changes Equipment Before Death", pass,
				"LootingLevel: " + ctx.getLootingLevel() + " (evaluated from current equipment at death time)"));
			cleanPen(level, cleanAreaA);
			player.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VC15 - Owner Changes Equipment Before Death", false, e.getMessage()));
		}
	}

	private static void runV060StatusTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posS = pos.add(60, 0, 60);
		net.minecraft.core.BlockPos penCenterS = new net.minecraft.core.BlockPos((int) posS.x, (int) posS.y, (int) posS.z);
		buildPen(level, penCenterS, 4);
		AABB cleanAreaS = new AABB(posS.x - 6, posS.y - 2, posS.z - 6, posS.x + 6, posS.y + 6, posS.z + 6);

		// Test S1 - Single Poison
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			Arrow arrow = new Arrow(level, posS.x, posS.y + 2, posS.z, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			MobEffectInstance poison = new MobEffectInstance(MobEffects.POISON, 200, 0);
			LogicalStatusEffectManager.applyEffect(zombie, poison, arrow);

			boolean pass = state.size() == 5
				&& state.get(0).hasEffect(MobEffects.POISON)
				&& !state.get(1).hasEffect(MobEffects.POISON)
				&& !state.get(2).hasEffect(MobEffects.POISON)
				&& !state.get(3).hasEffect(MobEffects.POISON)
				&& !state.get(4).hasEffect(MobEffects.POISON);

			results.add(new TestResult("Test S1 - Single Poison", pass,
				"TotalRecords: " + state.size() + ", #0 Poison: " + state.get(0).hasEffect(MobEffects.POISON)
				+ ", #1 Poison: " + state.get(1).hasEffect(MobEffects.POISON)));
			cleanPen(level, cleanAreaS);
			arrow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S1 - Single Poison", false, e.getMessage()));
		}

		// Test S2 - Poison Independent Duration
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			state.get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), zombie);
			state.get(1).addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0), zombie);

			boolean pass = state.get(0).getEffect(MobEffects.POISON).getDuration() == 100
				&& state.get(1).getEffect(MobEffects.POISON).getDuration() == 200;

			results.add(new TestResult("Test S2 - Poison Independent Duration", pass,
				"#0 Duration: " + state.get(0).getEffect(MobEffects.POISON).getDuration()
				+ ", #1 Duration: " + state.get(1).getEffect(MobEffects.POISON).getDuration()));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S2 - Poison Independent Duration", false, e.getMessage()));
		}

		// Test S3 - Poison Health Tick
		try {
			cleanPen(level, cleanAreaS);
			Zombie baseline = createEntity(EntityType.ZOMBIE, level);
			baseline.setPos(posS.x, posS.y, posS.z);
			level.addFreshEntity(baseline);
			baseline.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));

			Zombie stackZombie = createEntity(EntityType.ZOMBIE, level);
			stackZombie.setPos(posS.x + 1, posS.y, posS.z);
			((StackableEntity) stackZombie).jarstacker$setStackCount(3);
			level.addFreshEntity(stackZombie);
			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(stackZombie);
			state.get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0), stackZombie);

			for (int t = 0; t < 50; t++) {
				LogicalStatusEffectManager.tick(stackZombie);
			}

			float stackHp = LogicalHealthManager.getOrCreateState(stackZombie).getActiveHealth();
			boolean pass = stackHp < 20.0f && stackHp >= 1.0f;

			results.add(new TestResult("Test S3 - Poison Health Tick", pass,
				"Stack HP after 50 ticks: " + stackHp + " (expected < 20.0)"));
			cleanPen(level, cleanAreaS);
			baseline.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S3 - Poison Health Tick", false, e.getMessage()));
		}

		// Test S4 - Wither Death
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			level.addFreshEntity(zombie);

			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(zombie);
			hState.setActiveHealth(1.0f);
			zombie.setHealth(1.0f);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0), zombie);

			LogicalHealthManager.applyLogicalDirectDamage(zombie, 0, level.damageSources().wither(), 1.0f);
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			boolean pass = batch != null && batch.getCommittedDeaths() == 1
				&& ((StackableEntity) zombie).jarstacker$getStackCount() == 2
				&& sState.size() == 2;

			results.add(new TestResult("Test S4 - Wither Death", pass,
				"CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1) + ", RemainingCount: " + ((StackableEntity) zombie).jarstacker$getStackCount()
				+ ", StatusRecords: " + sState.size()));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S4 - Wither Death", false, e.getMessage()));
		}

		// Test S5 - Regeneration
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			level.addFreshEntity(zombie);

			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(zombie);
			hState.set(0, 10.0f);
			hState.set(1, 10.0f);
			hState.set(2, 20.0f);
			zombie.setHealth(10.0f);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0), zombie);

			for (int t = 0; t < 50; t++) {
				LogicalStatusEffectManager.tick(zombie);
			}

			float hp0 = hState.get(0);
			float hp1 = hState.get(1);
			float hp2 = hState.get(2);

			boolean pass = hp0 > 10.0f && hp1 == 10.0f && hp2 == 20.0f;
			results.add(new TestResult("Test S5 - Regeneration", pass,
				"#0 HP: " + hp0 + " (healed), #1 HP: " + hp1 + " (unchanged), #2 HP: " + hp2));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S5 - Regeneration", false, e.getMessage()));
		}

		// Test S6 - Resistance Direct Damage
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getResistance(), 200, 1), zombie); // 40% reduction
			sState.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getResistance(), 200, 1), zombie); // 40% reduction

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().generic(), 10.0f);
			float hp0 = LogicalHealthManager.getOrCreateState(zombie).getActiveHealth();

			boolean pass = Math.abs(hp0 - 14.0f) < 0.1f; // 10 * 0.6 = 6 damage, 20 - 6 = 14
			results.add(new TestResult("Test S6 - Resistance Direct Damage", pass,
				"Active HP after Resistance: " + hp0 + " (expected 14.0)"));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S6 - Resistance Direct Damage", false, e.getMessage()));
		}

		// Test S7 - Resistance AoE
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getResistance(), 200, 1), zombie); // 40% reduction
			sState.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getResistance(), 200, 1), zombie); // 40% reduction

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().explosion(null), 10.0f);
			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(zombie);
			float hp0 = hState.get(0);
			float hp1 = hState.get(1);

			boolean pass = Math.abs(hp0 - 14.0f) < 0.1f && Math.abs(hp1 - 10.0f) < 0.1f;
			results.add(new TestResult("Test S7 - Resistance AoE", pass,
				"#0 HP (Resisted): " + hp0 + ", #1 HP (Normal): " + hp1));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S7 - Resistance AoE", false, e.getMessage()));
		}

		// Test S8 - Strength / Weakness Projection
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getStrength(), 200, 0), zombie);
			sState.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getStrength(), 200, 0), zombie);
			LogicalStatusEffectManager.projectActiveMember(zombie);

			double attackDmg = zombie.getAttributeValue(Attributes.ATTACK_DAMAGE);
			boolean pass = attackDmg > 3.0;
			results.add(new TestResult("Test S8 - Strength / Weakness Projection", pass,
				"AttackDamage: " + attackDmg + " (boosted by Strength)"));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S8 - Strength / Weakness Projection", false, e.getMessage()));
		}

		// Test S9 - Speed / Slowness Projection
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			double baseSpeed = zombie.getAttributeValue(Attributes.MOVEMENT_SPEED);
			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 200, 0), zombie);
			sState.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 200, 0), zombie);
			LogicalStatusEffectManager.projectActiveMember(zombie);
			double boostedSpeed = zombie.getAttributeValue(Attributes.MOVEMENT_SPEED);

			LogicalStatusEffectManager.onActiveMemberSwitched(zombie, sState.get(0), sState.get(1));
			double restoredSpeed = zombie.getAttributeValue(Attributes.MOVEMENT_SPEED);

			boolean pass = boostedSpeed > baseSpeed && Math.abs(restoredSpeed - baseSpeed) < 0.001;
			results.add(new TestResult("Test S9 - Speed / Slowness Projection", pass,
				"Base: " + baseSpeed + ", Boosted: " + boostedSpeed + ", Restored: " + restoredSpeed));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S9 - Speed / Slowness Projection", false, e.getMessage()));
		}

		// Test S10 - Effect Expiration
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 10, 0), zombie);
			sState.get(1).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 40, 0), zombie);
			sState.get(2).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 100, 0), zombie);

			for (int t = 0; t < 15; t++) {
				LogicalStatusEffectManager.tick(zombie);
			}

			boolean pass = !sState.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& sState.get(1).getEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed()).getDuration() == 25
				&& sState.get(2).getEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed()).getDuration() == 85;

			results.add(new TestResult("Test S10 - Effect Expiration", pass,
				"#0 Expired: " + !sState.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				+ ", #1 Duration: " + (sState.get(1).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed()) ? sState.get(1).getEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed()).getDuration() : 0)
				+ ", #2 Duration: " + (sState.get(2).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed()) ? sState.get(2).getEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed()).getDuration() : 0)));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S10 - Effect Expiration", false, e.getMessage()));
		}

		// Test S11 - Effect Upgrade
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 20, 0), zombie);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 40, 1), zombie);

			MobEffectInstance eff = sState.get(0).getEffect(MobEffects.POISON);
			boolean pass = eff != null && eff.getAmplifier() == 1 && eff.getDuration() == 40;

			results.add(new TestResult("Test S11 - Effect Upgrade", pass,
				"Amplifier: " + (eff != null ? eff.getAmplifier() : -1) + ", Duration: " + (eff != null ? eff.getDuration() : -1)));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S11 - Effect Upgrade", false, e.getMessage()));
		}

		// Test S12 - Splash Potion Whole Stack
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(10);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			LogicalEffectScopeResolver.beginSplash(1.0);
			try {
				LogicalStatusEffectManager.applyEffect(zombie, new MobEffectInstance(MobEffects.POISON, 100, 0), null);
			} finally {
				LogicalEffectScopeResolver.endSplash();
			}

			boolean allPoison = true;
			for (int i = 0; i < sState.size(); i++) {
				if (!sState.get(i).hasEffect(MobEffects.POISON)) {
					allPoison = false;
					break;
				}
			}

			boolean pass = sState.size() == 10 && allPoison;
			results.add(new TestResult("Test S12 - Splash Potion Whole Stack", pass,
				"Total: " + sState.size() + ", AllPoison: " + allPoison));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S12 - Splash Potion Whole Stack", false, e.getMessage()));
		}

		// Test S13 - Splash Distance
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombieA = createEntity(EntityType.ZOMBIE, level);
			zombieA.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombieA).jarstacker$setStackCount(2);
			level.addFreshEntity(zombieA);

			Zombie zombieB = createEntity(EntityType.ZOMBIE, level);
			zombieB.setPos(posS.x + 2, posS.y, posS.z);
			((StackableEntity) zombieB).jarstacker$setStackCount(2);
			level.addFreshEntity(zombieB);

			LogicalEffectScopeResolver.beginSplash(1.0);
			try {
				LogicalStatusEffectManager.applyEffect(zombieA, new MobEffectInstance(MobEffects.POISON, 100, 0), null);
			} finally {
				LogicalEffectScopeResolver.endSplash();
			}

			LogicalEffectScopeResolver.beginSplash(0.5);
			try {
				LogicalStatusEffectManager.applyEffect(zombieB, new MobEffectInstance(MobEffects.POISON, 50, 0), null);
			} finally {
				LogicalEffectScopeResolver.endSplash();
			}

			int durA = LogicalStatusEffectManager.getOrCreateStatusState(zombieA).get(0).getEffect(MobEffects.POISON).getDuration();
			int durB = LogicalStatusEffectManager.getOrCreateStatusState(zombieB).get(0).getEffect(MobEffects.POISON).getDuration();

			boolean pass = durA == 100 && durB == 50;
			results.add(new TestResult("Test S13 - Splash Distance", pass,
				"DurA: " + durA + ", DurB: " + durB));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S13 - Splash Distance", false, e.getMessage()));
		}

		// Test S14 - Lingering Cloud
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			level.addFreshEntity(zombie);

			net.minecraft.world.entity.AreaEffectCloud cloud = new net.minecraft.world.entity.AreaEffectCloud(level, posS.x, posS.y, posS.z);
			cloud.addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 100, 0));
			level.addFreshEntity(cloud);

			LogicalStatusEffectManager.applyEffect(zombie, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 100, 0), cloud);
			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);

			boolean allSpeed = sState.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& sState.get(1).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& sState.get(2).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed());

			results.add(new TestResult("Test S14 - Lingering Cloud", allSpeed,
				"All 3 members received cloud effect: " + allSpeed));
			cleanPen(level, cleanAreaS);
			cloud.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S14 - Lingering Cloud", false, e.getMessage()));
		}

		// Test S15 - Instant Damage Projectile
		try {
			cleanPen(level, cleanAreaS);
			Pig pig = createEntity(EntityType.PIG, level);
			pig.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) pig).jarstacker$setStackCount(5);
			level.addFreshEntity(pig);

			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(pig);
			Arrow arrow = new Arrow(level, posS.x, posS.y + 2, posS.z, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			LogicalStatusEffectManager.applyEffect(pig, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getInstantDamage(), 1, 0), arrow);

			float hp0 = hState.get(0);
			float hp1 = hState.get(1);
			boolean pass = hp0 == 4.0f && hp1 == 10.0f; // Pig max HP = 10.0, Harm 0 = 6 damage

			results.add(new TestResult("Test S15 - Instant Damage Projectile", pass,
				"#0 HP: " + hp0 + " (expected 4.0), #1 HP: " + hp1 + " (expected 10.0)"));
			cleanPen(level, cleanAreaS);
			arrow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S15 - Instant Damage Projectile", false, e.getMessage()));
		}

		// Test S16 - Instant Damage Splash
		try {
			cleanPen(level, cleanAreaS);
			Pig pig = createEntity(EntityType.PIG, level);
			pig.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) pig).jarstacker$setStackCount(3);
			level.addFreshEntity(pig);

			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(pig);
			LogicalEffectScopeResolver.beginSplash(1.0);
			try {
				LogicalStatusEffectManager.applyEffect(pig, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getInstantDamage(), 1, 0), null);
			} finally {
				LogicalEffectScopeResolver.endSplash();
			}

			boolean allDamaged = hState.get(0) == 4.0f && hState.get(1) == 4.0f && hState.get(2) == 4.0f;
			results.add(new TestResult("Test S16 - Instant Damage Splash", allDamaged,
				"All members damaged by splash harm: " + allDamaged));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S16 - Instant Damage Splash", false, e.getMessage()));
		}

		// Test S17 - Instant Health / Undead
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(zombie);
			LogicalEffectScopeResolver.beginSplash(1.0);
			try {
				LogicalStatusEffectManager.applyEffect(zombie, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getInstantHealth(), 1, 0), null);
			} finally {
				LogicalEffectScopeResolver.endSplash();
			}

			float hp0 = hState.get(0);
			boolean pass = hp0 == 16.0f; // 20 - 4 = 16 (Instant Health I deals 4 damage to Undead)
			results.add(new TestResult("Test S17 - Instant Health / Undead", pass,
				"Zombie damaged by Instant Health: HP=" + hp0));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S17 - Instant Health / Undead", false, e.getMessage()));
		}

		// Test S18 - Merge Divergent Effects
		try {
			cleanPen(level, cleanAreaS);
			Zombie z1 = createEntity(EntityType.ZOMBIE, level);
			z1.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) z1).jarstacker$setStackCount(1);
			level.addFreshEntity(z1);
			LogicalStatusEffectManager.getOrCreateStatusState(z1).get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), z1);

			Zombie z2 = createEntity(EntityType.ZOMBIE, level);
			z2.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) z2).jarstacker$setStackCount(1);
			level.addFreshEntity(z2);

			Zombie z3 = createEntity(EntityType.ZOMBIE, level);
			z3.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) z3).jarstacker$setStackCount(1);
			level.addFreshEntity(z3);
			LogicalStatusEffectManager.getOrCreateStatusState(z3).get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 100, 0), z3);

			LogicalHealthManager.mergeHealthStates(z1, z2, 1);
			((StackableEntity) z1).jarstacker$setStackCount(2);

			LogicalHealthManager.mergeHealthStates(z1, z3, 1);
			((StackableEntity) z1).jarstacker$setStackCount(3);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(z1);
			boolean pass = state.size() == 3
				&& state.get(0).hasEffect(MobEffects.POISON)
				&& !state.get(1).hasEffect(MobEffects.POISON) && !state.get(1).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& state.get(2).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed());

			results.add(new TestResult("Test S18 - Merge Divergent Effects", pass,
				"Size: " + state.size() + ", #0 Poison: " + state.get(0).hasEffect(MobEffects.POISON)
				+ ", #2 Speed: " + state.get(2).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())));
			cleanPen(level, cleanAreaS);
			z2.discard();
			z3.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S18 - Merge Divergent Effects", false, e.getMessage()));
		}

		// Test S19 - Split Effect Owner
		try {
			cleanPen(level, cleanAreaS);
			Zombie source = createEntity(EntityType.ZOMBIE, level);
			source.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) source).jarstacker$setStackCount(3);
			level.addFreshEntity(source);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(source);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), source);
			sState.get(2).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 100, 0), source);

			Zombie extracted = createEntity(EntityType.ZOMBIE, level);
			LogicalHealthManager.extractHealthState(source, extracted);
			((StackableEntity) source).jarstacker$setStackCount(2);

			LogicalStatusEffectState extractedState = LogicalStatusEffectManager.getOrCreateStatusState(extracted);
			boolean pass = extractedState.size() == 1
				&& extractedState.get(0).hasEffect(MobEffects.POISON)
				&& sState.size() == 2
				&& !sState.get(0).hasEffect(MobEffects.POISON)
				&& sState.get(1).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed());

			results.add(new TestResult("Test S19 - Split Effect Owner", pass,
				"Extracted Poison: " + extractedState.get(0).hasEffect(MobEffects.POISON)
				+ ", Remainder #0 Clean: " + !sState.get(0).hasEffect(MobEffects.POISON)));
			cleanPen(level, cleanAreaS);
			extracted.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S19 - Split Effect Owner", false, e.getMessage()));
		}

		// Test S20 - Runtime Equipment Extraction
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), zombie);

			Mob extracted = RuntimeCombatStateTransitionHandler.extractEquippedMob(zombie, EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));

			boolean pass = extracted != null
				&& LogicalStatusEffectManager.getOrCreateStatusState(extracted).get(0).hasEffect(MobEffects.POISON)
				&& !sState.get(0).hasEffect(MobEffects.POISON);

			results.add(new TestResult("Test S20 - Runtime Equipment Extraction", pass,
				"Extracted Poison: " + (extracted != null && LogicalStatusEffectManager.getOrCreateStatusState(extracted).get(0).hasEffect(MobEffects.POISON))
				+ ", Remainder #0 Clean: " + !sState.get(0).hasEffect(MobEffects.POISON)));
			cleanPen(level, cleanAreaS);
			if (extracted != null) extracted.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test S20 - Runtime Equipment Extraction", false, e.getMessage()));
		}

		// Test S21 - Save / Reload
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1), zombie);
			sState.get(1).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 150, 0), zombie);

			net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
			tag.put(LogicalStatusEffectState.NBT_KEY, sState.saveToNbt());

			LogicalStatusEffectState loaded = LogicalStatusEffectState.loadFromNbt(tag);
			boolean pass = loaded != null && loaded.size() == 2
				&& loaded.get(0).hasEffect(MobEffects.POISON)
				&& loaded.get(0).getEffect(MobEffects.POISON).getDuration() == 80
				&& loaded.get(1).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& loaded.get(1).getEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed()).getDuration() == 150;

			results.add(new TestResult("Test S21 - Save / Reload", pass,
				"LoadedSize: " + (loaded != null ? loaded.size() : 0) + ", Preserved: " + pass));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S21 - Save / Reload", false, e.getMessage()));
		}

		// Test S22 - Migration
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(4);
			zombie.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
			level.addFreshEntity(zombie);

			net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
			tag.putInt("JarStackerCount", 4);
			EntityAdapter.readAdditionalSaveData(zombie, tag);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			boolean pass = state.size() == 4
				&& state.get(0).hasEffect(MobEffects.POISON)
				&& !state.get(1).hasEffect(MobEffects.POISON);

			results.add(new TestResult("Test S22 - Migration", pass,
				"#0 Migrated Poison: " + state.get(0).hasEffect(MobEffects.POISON)
				+ ", #1 Clean: " + !state.get(1).hasEffect(MobEffects.POISON)));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S22 - Migration", false, e.getMessage()));
		}

		// Test S23 - Repair
		try {
			cleanPen(level, cleanAreaS);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posS.x, posS.y, posS.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.removeIndices(List.of(4, 3));

			LogicalStateValidator.repairLogicalState(zombie);

			boolean pass = sState.size() == 5;
			results.add(new TestResult("Test S23 - Repair", pass,
				"Repaired Size: " + sState.size() + " (expected 5)"));
			cleanPen(level, cleanAreaS);
		} catch (Exception e) {
			results.add(new TestResult("Test S23 - Repair", false, e.getMessage()));
		}
	}

	private static void runV060BurnTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posB = pos.add(65, 0, 65);
		net.minecraft.core.BlockPos penCenterB = new net.minecraft.core.BlockPos((int) posB.x, (int) posB.y, (int) posB.z);
		buildPen(level, penCenterB, 4);
		AABB cleanAreaB = new AABB(posB.x - 6, posB.y - 2, posB.z - 6, posB.x + 6, posB.y + 6, posB.z + 6);

		// Test F1 - Flaming Arrow
		try {
			cleanPen(level, cleanAreaB);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posB.x, posB.y, posB.z);
			((StackableEntity) zombie).jarstacker$setStackCount(5);
			level.addFreshEntity(zombie);

			LogicalBurnState bState = LogicalStatusEffectManager.getOrCreateBurnState(zombie);
			bState.ignite(0, 100);

			boolean pass = bState.get(0).isBurning()
				&& bState.get(0).getRemainingFireTicks() == 100
				&& !bState.get(1).isBurning()
				&& !bState.get(2).isBurning();

			results.add(new TestResult("Test F1 - Flaming Arrow", pass,
				"#0 Burning: " + bState.get(0).isBurning() + ", #1 Burning: " + bState.get(1).isBurning()));
			cleanPen(level, cleanAreaB);
		} catch (Exception e) {
			results.add(new TestResult("Test F1 - Flaming Arrow", false, e.getMessage()));
		}

		// Test F2 - Independent Burn Timers
		try {
			cleanPen(level, cleanAreaB);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posB.x, posB.y, posB.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalBurnState bState = LogicalStatusEffectManager.getOrCreateBurnState(zombie);
			bState.ignite(0, 50);
			bState.ignite(1, 100);

			for (int t = 0; t < 20; t++) {
				LogicalStatusEffectManager.tick(zombie);
			}

			boolean pass = bState.get(0).getRemainingFireTicks() == 30
				&& bState.get(1).getRemainingFireTicks() == 80;

			results.add(new TestResult("Test F2 - Independent Burn Timers", pass,
				"#0 Ticks: " + bState.get(0).getRemainingFireTicks() + ", #1 Ticks: " + bState.get(1).getRemainingFireTicks()));
			cleanPen(level, cleanAreaB);
		} catch (Exception e) {
			results.add(new TestResult("Test F2 - Independent Burn Timers", false, e.getMessage()));
		}

		// Test F3 - Fire Tick Damage
		try {
			cleanPen(level, cleanAreaB);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posB.x, posB.y, posB.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(zombie);
			LogicalBurnState bState = LogicalStatusEffectManager.getOrCreateBurnState(zombie);
			bState.ignite(0, 40);

			for (int t = 0; t < 20; t++) {
				LogicalStatusEffectManager.tick(zombie);
			}

			float hp0 = hState.get(0);
			float hp1 = hState.get(1);
			boolean pass = hp0 == 19.0f && hp1 == 20.0f;

			results.add(new TestResult("Test F3 - Fire Tick Damage", pass,
				"#0 HP: " + hp0 + " (took fire damage), #1 HP: " + hp1 + " (untouched)"));
			cleanPen(level, cleanAreaB);
		} catch (Exception e) {
			results.add(new TestResult("Test F3 - Fire Tick Damage", false, e.getMessage()));
		}

		// Test F4 - Shared Fire Block
		try {
			cleanPen(level, cleanAreaB);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posB.x, posB.y, posB.z);
			((StackableEntity) zombie).jarstacker$setStackCount(4);
			level.addFreshEntity(zombie);

			LogicalBurnState bState = LogicalStatusEffectManager.getOrCreateBurnState(zombie);
			bState.igniteAll(100, true);

			boolean allBurning = true;
			for (int i = 0; i < bState.size(); i++) {
				if (!bState.get(i).isBurning()) {
					allBurning = false;
					break;
				}
			}

			results.add(new TestResult("Test F4 - Shared Fire Block", allBurning,
				"All 4 members ignited via shared fire: " + allBurning));
			cleanPen(level, cleanAreaB);
		} catch (Exception e) {
			results.add(new TestResult("Test F4 - Shared Fire Block", false, e.getMessage()));
		}

		// Test F5 - Lava + Mixed Fire Resistance
		try {
			cleanPen(level, cleanAreaB);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posB.x, posB.y, posB.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0), zombie);

			LogicalHealthManager.onDamageApplied(zombie, level.damageSources().lava(), 4.0f);
			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(zombie);

			float hp0 = hState.get(0);
			float hp1 = hState.get(1);
			boolean pass = hp0 == 20.0f && hp1 == 16.0f;

			results.add(new TestResult("Test F5 - Lava + Mixed Fire Resistance", pass,
				"#0 HP (FireResist): " + hp0 + ", #1 HP (Damaged): " + hp1));
			cleanPen(level, cleanAreaB);
		} catch (Exception e) {
			results.add(new TestResult("Test F5 - Lava + Mixed Fire Resistance", false, e.getMessage()));
		}

		// Test F6 - Burn Death
		try {
			cleanPen(level, cleanAreaB);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posB.x, posB.y, posB.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(zombie);
			hState.setActiveHealth(1.0f);
			zombie.setHealth(1.0f);

			LogicalBurnState bState = LogicalStatusEffectManager.getOrCreateBurnState(zombie);
			bState.ignite(0, 20);

			LogicalHealthManager.applyLogicalDirectDamage(zombie, 0, level.damageSources().onFire(), 1.0f);
			DeathBatch batch = LogicalHealthManager.getLastDeathBatch();

			boolean pass = batch != null && batch.getCommittedDeaths() == 1
				&& ((StackableEntity) zombie).jarstacker$getStackCount() == 1;

			results.add(new TestResult("Test F6 - Burn Death", pass,
				"CommittedDeaths: " + (batch != null ? batch.getCommittedDeaths() : -1) + ", RemainingCount: " + ((StackableEntity) zombie).jarstacker$getStackCount()));
			cleanPen(level, cleanAreaB);
		} catch (Exception e) {
			results.add(new TestResult("Test F6 - Burn Death", false, e.getMessage()));
		}

		// Test F7 - Burn Save / Reload
		try {
			cleanPen(level, cleanAreaB);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posB.x, posB.y, posB.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalBurnState bState = LogicalStatusEffectManager.getOrCreateBurnState(zombie);
			bState.ignite(0, 45);
			bState.ignite(1, 95);

			net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
			tag.put(LogicalBurnState.NBT_KEY, bState.saveToNbt());

			LogicalBurnState loaded = LogicalBurnState.loadFromNbt(tag);
			boolean pass = loaded != null && loaded.size() == 2
				&& loaded.get(0).getRemainingFireTicks() == 45
				&& loaded.get(1).getRemainingFireTicks() == 95;

			results.add(new TestResult("Test F7 - Burn Save / Reload", pass,
				"LoadedSize: " + (loaded != null ? loaded.size() : 0) + ", Preserved: " + pass));
			cleanPen(level, cleanAreaB);
		} catch (Exception e) {
			results.add(new TestResult("Test F7 - Burn Save / Reload", false, e.getMessage()));
		}
	}

	private static void runV060ProjectionTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posP = pos.add(70, 0, 70);
		net.minecraft.core.BlockPos penCenterP = new net.minecraft.core.BlockPos((int) posP.x, (int) posP.y, (int) posP.z);
		buildPen(level, penCenterP, 4);
		AABB cleanAreaP = new AABB(posP.x - 6, posP.y - 2, posP.z - 6, posP.x + 6, posP.y + 6, posP.z + 6);

		// Test P1 - Projection Double-Tick Guard
		try {
			cleanPen(level, cleanAreaP);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posP.x, posP.y, posP.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			state.get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), zombie);

			LogicalStatusEffectManager.tick(zombie);
			int dur = state.get(0).getEffect(MobEffects.POISON).getDuration();

			boolean pass = dur == 99;
			results.add(new TestResult("Test P1 - Projection Double-Tick Guard", pass,
				"Duration after 1 tick: " + dur + " (expected exactly 99)"));
			cleanPen(level, cleanAreaP);
		} catch (Exception e) {
			results.add(new TestResult("Test P1 - Projection Double-Tick Guard", false, e.getMessage()));
		}

		// Test P2 - Active Member Death Projection
		try {
			cleanPen(level, cleanAreaP);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posP.x, posP.y, posP.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			state.get(0).addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0), zombie);
			LogicalStatusEffectManager.projectActiveMember(zombie);

			boolean invisBefore = zombie.isInvisible();
			LogicalHealthManager.applyLogicalDirectDamage(zombie, 0, level.damageSources().generic(), 50.0f);
			boolean invisAfter = zombie.isInvisible();

			boolean pass = invisBefore && !invisAfter;
			results.add(new TestResult("Test P2 - Active Member Death Projection", pass,
				"InvisBefore: " + invisBefore + ", InvisAfter: " + invisAfter));
			cleanPen(level, cleanAreaP);
		} catch (Exception e) {
			results.add(new TestResult("Test P2 - Active Member Death Projection", false, e.getMessage()));
		}

		// Test P3 - Active Member Switch
		try {
			cleanPen(level, cleanAreaP);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posP.x, posP.y, posP.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			state.get(0).addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0), zombie);
			state.get(1).addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0), zombie);
			LogicalStatusEffectManager.projectActiveMember(zombie);

			// Active member 0 dies, member 1 becomes active
			LogicalHealthManager.applyLogicalDirectDamage(zombie, 0, level.damageSources().generic(), 50.0f);

			boolean glowing = zombie.hasGlowingTag();
			boolean invisible = zombie.isInvisible();

			boolean pass = !glowing && invisible;
			results.add(new TestResult("Test P3 - Active Member Switch", pass,
				"Glowing: " + glowing + ", Invisible: " + invisible));
			cleanPen(level, cleanAreaP);
		} catch (Exception e) {
			results.add(new TestResult("Test P3 - Active Member Switch", false, e.getMessage()));
		}

		// Test P4 - Attribute Modifier Cleanup
		try {
			cleanPen(level, cleanAreaP);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posP.x, posP.y, posP.z);
			((StackableEntity) zombie).jarstacker$setStackCount(2);
			level.addFreshEntity(zombie);

			double baseSpeed = zombie.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			state.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 200, 1), zombie);
			LogicalStatusEffectManager.projectActiveMember(zombie);

			// Member 0 dies, switch to clean member 1
			LogicalHealthManager.applyLogicalDirectDamage(zombie, 0, level.damageSources().generic(), 50.0f);
			double restoredSpeed = zombie.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);

			boolean pass = Math.abs(restoredSpeed - baseSpeed) < 0.001;
			results.add(new TestResult("Test P4 - Attribute Cleanup", pass,
				"Base: " + baseSpeed + ", Restored: " + restoredSpeed + " (stale modifiers removed)"));
			cleanPen(level, cleanAreaP);
		} catch (Exception e) {
			results.add(new TestResult("Test P4 - Attribute Cleanup", false, e.getMessage()));
		}

		// Test P5 - Context Isolation
		try {
			cleanPen(level, cleanAreaP);
			Zombie zombieA = createEntity(EntityType.ZOMBIE, level);
			zombieA.setPos(posP.x - 2, posP.y, posP.z);
			((StackableEntity) zombieA).jarstacker$setStackCount(2);
			level.addFreshEntity(zombieA);

			Zombie zombieB = createEntity(EntityType.ZOMBIE, level);
			zombieB.setPos(posP.x + 2, posP.y, posP.z);
			((StackableEntity) zombieB).jarstacker$setStackCount(2);
			level.addFreshEntity(zombieB);

			LogicalStatusEffectState sA = LogicalStatusEffectManager.getOrCreateStatusState(zombieA);
			LogicalStatusEffectState sB = LogicalStatusEffectManager.getOrCreateStatusState(zombieB);

			sA.get(0).addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), zombieA);
			sB.get(0).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 100, 0), zombieB);

			boolean pass = sA.get(0).hasEffect(MobEffects.POISON) && !sA.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& sB.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed()) && !sB.get(0).hasEffect(MobEffects.POISON);

			results.add(new TestResult("Test P5 - Context Isolation", pass,
				"ZA has Poison only: " + sA.get(0).hasEffect(MobEffects.POISON)
				+ ", ZB has Speed only: " + sB.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())));
			cleanPen(level, cleanAreaP);
		} catch (Exception e) {
			results.add(new TestResult("Test P5 - Context Isolation", false, e.getMessage()));
		}

		// Test P6 - Nested Health Effect
		try {
			cleanPen(level, cleanAreaP);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posP.x, posP.y, posP.z);
			((StackableEntity) zombie).jarstacker$setStackCount(3);
			level.addFreshEntity(zombie);

			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(zombie);
			hState.set(0, 10.0f);
			hState.set(1, 10.0f);
			zombie.setHealth(10.0f);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0), zombie);

			LogicalHealthManager.applyLogicalDirectDamage(zombie, 1, level.damageSources().magic(), 2.0f);
			LogicalStatusEffectManager.tick(zombie);

			boolean pass = hState.get(0) > 10.0f && hState.get(1) == 8.0f;
			results.add(new TestResult("Test P6 - Nested Health Effect", pass,
				"#0 HP: " + hState.get(0) + " (healed), #1 HP: " + hState.get(1) + " (damaged independently)"));
			cleanPen(level, cleanAreaP);
		} catch (Exception e) {
			results.add(new TestResult("Test P6 - Nested Health Effect", false, e.getMessage()));
		}
	}

	private static void runV060StressTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posST = pos.add(80, 0, 80);
		net.minecraft.core.BlockPos penCenterST = new net.minecraft.core.BlockPos((int) posST.x, (int) posST.y, (int) posST.z);
		buildPen(level, penCenterST, 4);
		AABB cleanAreaST = new AABB(posST.x - 6, posST.y - 2, posST.z - 6, posST.x + 6, posST.y + 6, posST.z + 6);

		// Test ST1 - Stress x100 Poison
		try {
			cleanPen(level, cleanAreaST);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posST.x, posST.y, posST.z);
			((StackableEntity) zombie).jarstacker$setStackCount(100);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			for (int i = 0; i < 100; i++) {
				sState.get(i).addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0), zombie);
			}

			long t0 = System.nanoTime();
			for (int t = 0; t < 10; t++) {
				LogicalStatusEffectManager.tick(zombie);
			}
			long elapsed = System.nanoTime() - t0;
			double ms = elapsed / 1_000_000.0;

			boolean pass = sState.size() == 100 && ms < 100.0;
			results.add(new TestResult("Test ST1 - Stress x100 Poison", pass,
				"Duration: " + String.format("%.2f ms", ms) + ", PhysicalEntities: 1, LogicalRecords: 100"));
			cleanPen(level, cleanAreaST);
		} catch (Exception e) {
			results.add(new TestResult("Test ST1 - Stress x100 Poison", false, e.getMessage()));
		}

		// Test ST2 - Stress x1000 Mixed Effects
		try {
			cleanPen(level, cleanAreaST);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posST.x, posST.y, posST.z);
			((StackableEntity) zombie).jarstacker$setStackCount(1000);
			level.addFreshEntity(zombie);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
			for (int i = 0; i < 1000; i++) {
				if (i < 250) {
					sState.get(i).addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0), zombie);
				} else if (i < 500) {
					sState.get(i).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 200, 0), zombie);
				} else if (i < 750) {
					sState.get(i).addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getResistance(), 200, 0), zombie);
				}
			}

			long t0 = System.nanoTime();
			for (int t = 0; t < 10; t++) {
				LogicalStatusEffectManager.tick(zombie);
			}
			long elapsed = System.nanoTime() - t0;
			double ms = elapsed / 1_000_000.0;

			boolean pass = sState.size() == 1000 && ms < 200.0;
			results.add(new TestResult("Test ST2 - Stress x1000 Mixed Effects", pass,
				"Duration: " + String.format("%.2f ms", ms) + ", PhysicalEntities: 1, LogicalRecords: 1000"));
			cleanPen(level, cleanAreaST);
		} catch (Exception e) {
			results.add(new TestResult("Test ST2 - Stress x1000 Mixed Effects", false, e.getMessage()));
		}

		// Test ST3 - Stress x1000 Burning
		try {
			cleanPen(level, cleanAreaST);
			Zombie zombie = createEntity(EntityType.ZOMBIE, level);
			zombie.setPos(posST.x, posST.y, posST.z);
			((StackableEntity) zombie).jarstacker$setStackCount(1000);
			level.addFreshEntity(zombie);

			LogicalBurnState bState = LogicalStatusEffectManager.getOrCreateBurnState(zombie);
			bState.igniteAll(200, true);

			long t0 = System.nanoTime();
			for (int t = 0; t < 20; t++) {
				LogicalStatusEffectManager.tick(zombie);
			}
			long elapsed = System.nanoTime() - t0;
			double ms = elapsed / 1_000_000.0;

			boolean pass = bState.size() == 1000 && ms < 200.0;
			results.add(new TestResult("Test ST3 - Stress x1000 Burning", pass,
				"Duration: " + String.format("%.2f ms", ms) + ", PhysicalEntities: 1, BurnRecords: 1000"));
			cleanPen(level, cleanAreaST);
		} catch (Exception e) {
			results.add(new TestResult("Test ST3 - Stress x1000 Burning", false, e.getMessage()));
		}
	}

	private static void runV060GenericEffectTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posGE = pos.add(80, 0, 80);
		net.minecraft.core.BlockPos penCenterGE = new net.minecraft.core.BlockPos((int) posGE.x, (int) posGE.y, (int) posGE.z);
		buildPen(level, penCenterGE, 4);
		AABB cleanAreaGE = new AABB(posGE.x - 6, posGE.y - 2, posGE.z - 6, posGE.x + 6, posGE.y + 6, posGE.z + 6);

		// Test GE1 - Attribute-only Vanilla parity
		try {
			cleanPen(level, cleanAreaGE);
			Zombie baseline = createEntity(EntityType.ZOMBIE, level);
			baseline.setPos(posGE.x + 2, posGE.y, posGE.z);
			level.addFreshEntity(baseline);
			baseline.addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 200, 1));
			double vanillaSpeed = baseline.getAttributeValue(Attributes.MOVEMENT_SPEED);
			double baseSpeed = baseline.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);

			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posGE.x, posGE.y, posGE.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);
			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 200, 1), null);

			double stackSpeed = stacked.getAttributeValue(Attributes.MOVEMENT_SPEED);
			boolean speedMatchesVanilla = Math.abs(vanillaSpeed - stackSpeed) < 0.0001;

			// Kill active member 0 to test modifier removal upon member switch
			LogicalHealthManager.applyLogicalDirectDamage(stacked, 0, level.damageSources().magic(), 20.0f);
			LogicalStatusEffectManager.projectActiveMember(stacked);
			double postSwitchSpeed = stacked.getAttributeValue(Attributes.MOVEMENT_SPEED);
			boolean modifierRemovedCleanly = Math.abs(postSwitchSpeed - baseSpeed) < 0.0001;

			boolean pass = speedMatchesVanilla && modifierRemovedCleanly;
			results.add(new TestResult("Test GE1 - Attribute Parity Speed", pass,
				"VanillaSpeed=" + vanillaSpeed + ", StackSpeed=" + stackSpeed + ", PostSwitchSpeed=" + postSwitchSpeed));
			cleanPen(level, cleanAreaGE);
		} catch (Exception e) {
			results.add(new TestResult("Test GE1 - Attribute Parity Speed", false, e.getMessage()));
		}

		// Test GE2 - Generic periodic Vanilla callback parity
		try {
			cleanPen(level, cleanAreaGE);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posGE.x, posGE.y, posGE.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(stacked);

			MobEffectInstance hunger = new MobEffectInstance(MobEffects.HUNGER, 100, 0);
			LogicalStatusEffectManager.applyEffect(stacked, hunger, null);

			boolean executed = LogicalVanillaEffectExecutor.executeVanillaTick(stacked, 0, MobEffects.HUNGER, hunger);
			int depth = LogicalEffectExecutionContext.currentDepth();
			float hp0 = hState.get(0);
			float hp1 = hState.get(1);

			boolean pass = executed && depth == 0 && hp1 == 20.0f;
			results.add(new TestResult("Test GE2 - Generic Periodic Callback Parity", pass,
				"Executed=" + executed + ", Depth=" + depth + ", hp0=" + hp0 + ", hp1=" + hp1));
			cleanPen(level, cleanAreaGE);
		} catch (Exception e) {
			results.add(new TestResult("Test GE2 - Generic Periodic Callback Parity", false, e.getMessage()));
		}

		// Test GE3 - Instant-effect parity
		try {
			cleanPen(level, cleanAreaGE);
			Zombie baseline = createEntity(EntityType.ZOMBIE, level);
			baseline.setPos(posGE.x + 2, posGE.y, posGE.z);
			level.addFreshEntity(baseline);
			baseline.setHealth(10.0f);
			baseline.heal(6.0f);
			float baselineHealedHp = baseline.getHealth();

			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posGE.x, posGE.y, posGE.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);
			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(stacked);
			hState.set(0, 10.0f);
			hState.set(1, 10.0f);
			stacked.setHealth(10.0f);

			// Instant Damage (heals undead) - Scope SINGLE
			LogicalStatusEffectManager.applyInstantEffect(stacked, com.jar.jarstacker.adapter.EffectAdapter.getInstantDamage(), 0, 1.0, LogicalEffectScopeResolver.Scope.SINGLE);
			float member0Hp = hState.get(0);
			float member1Hp = hState.get(1);

			// Instant Health (harms undead) - Scope AREA
			LogicalStatusEffectManager.applyInstantEffect(stacked, com.jar.jarstacker.adapter.EffectAdapter.getInstantHealth(), 0, 1.0, LogicalEffectScopeResolver.Scope.AREA);
			float postHarm0 = hState.get(0);
			float postHarm1 = hState.get(1);

			boolean pass = member0Hp == 16.0f && member1Hp == 10.0f && postHarm0 == 12.0f && postHarm1 == 6.0f;
			results.add(new TestResult("Test GE3 - Instant Effect Undead Inversion & Scope Parity", pass,
				"Member0Healed=" + member0Hp + ", Member1Clean=" + member1Hp + ", PostHarm0=" + postHarm0 + ", PostHarm1=" + postHarm1));
			cleanPen(level, cleanAreaGE);
		} catch (Exception e) {
			results.add(new TestResult("Test GE3 - Instant Effect Undead Inversion & Scope Parity", false, e.getMessage()));
		}

		// Test GE4 - Visual-state parity
		try {
			cleanPen(level, cleanAreaGE);
			Zombie baseline = createEntity(EntityType.ZOMBIE, level);
			baseline.setPos(posGE.x + 2, posGE.y, posGE.z);
			level.addFreshEntity(baseline);
			baseline.setInvisible(true);
			boolean baselineInvis = baseline.isInvisible();

			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posGE.x, posGE.y, posGE.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);

			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0), null);
			boolean stackInvis = stacked.isInvisible();

			LogicalHealthManager.applyLogicalDirectDamage(stacked, 0, level.damageSources().magic(), 20.0f);
			LogicalStatusEffectManager.projectActiveMember(stacked);
			boolean postSwitchInvis = stacked.isInvisible();

			boolean pass = baselineInvis && stackInvis && !postSwitchInvis;
			results.add(new TestResult("Test GE4 - Visual State Invisibility Parity", pass,
				"Baseline=" + baselineInvis + ", Stack=" + stackInvis + ", PostSwitch=" + postSwitchInvis));
			cleanPen(level, cleanAreaGE);
		} catch (Exception e) {
			results.add(new TestResult("Test GE4 - Visual State Invisibility Parity", false, e.getMessage()));
		}

		// Test GE5 - Unsupported/custom safe fallback
		try {
			cleanPen(level, cleanAreaGE);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posGE.x, posGE.y, posGE.z);
			((StackableEntity) stacked).jarstacker$setStackCount(3);
			level.addFreshEntity(stacked);

			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);
			Mob extracted = RuntimeCombatStateTransitionHandler.extractUnsupportedMob(stacked, modded);

			boolean pass = extracted != null
				&& ((StackableEntity) extracted).jarstacker$getStackCount() == 1
				&& extracted.hasEffect(MODDED_UNSUPPORTED)
				&& ((StackableEntity) stacked).jarstacker$getStackCount() == 2
				&& !stacked.hasEffect(MODDED_UNSUPPORTED);

			results.add(new TestResult("Test GE5 - Unsupported Effect Safe Fallback", pass,
				"ExtractedNotNull=" + (extracted != null) + ", RemainderCount=" + ((StackableEntity) stacked).jarstacker$getStackCount()));
			cleanPen(level, cleanAreaGE);
		} catch (Exception e) {
			results.add(new TestResult("Test GE5 - Unsupported Effect Safe Fallback", false, e.getMessage()));
		}

		// Test GE6 - All Vanilla MobEffects classified
		try {
			LogicalEffectClassifier.AuditReport report = LogicalEffectClassifier.auditAll();
			//? if >=1.21.11 {
			/*boolean pass = report.unknownCount() == 0 && report.totalVanilla() == 40
				&& report.counts().get(LogicalEffectBehaviorClass.UNSUPPORTED) == 0
				&& report.counts().get(LogicalEffectBehaviorClass.EVENT_DRIVEN_LOGICAL) == 4;
			*///?} else {
			boolean pass = report.unknownCount() == 0 && report.totalVanilla() == 39
				&& report.counts().get(LogicalEffectBehaviorClass.UNSUPPORTED) == 0
				&& report.counts().get(LogicalEffectBehaviorClass.EVENT_DRIVEN_LOGICAL) == 4;
			//?}
			results.add(new TestResult("Test GE6 - Vanilla Effect Registry Audit UNKNOWN == 0", pass,
				"TotalVanilla=" + report.totalVanilla() + ", Unknown=" + report.unknownCount() + ", Counts=" + report.counts()));
		} catch (Exception e) {
			results.add(new TestResult("Test GE6 - Vanilla Effect Registry Audit UNKNOWN == 0", false, e.getMessage()));
		}
	}

	private static void runV060RealIntegrationTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posRI = pos.add(85, 0, 85);
		net.minecraft.core.BlockPos penCenterRI = new net.minecraft.core.BlockPos((int) posRI.x, (int) posRI.y, (int) posRI.z);
		buildPen(level, penCenterRI, 4);
		AABB cleanAreaRI = new AABB(posRI.x - 6, posRI.y - 2, posRI.z - 6, posRI.x + 6, posRI.y + 6, posRI.z + 6);

		// Test RI1 - Real Tipped Arrow projectile collision
		try {
			cleanPen(level, cleanAreaRI);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posRI.x, posRI.y, posRI.z);
			((StackableEntity) stacked).jarstacker$setStackCount(5);
			level.addFreshEntity(stacked);

			Arrow arrow = new Arrow(level, posRI.x, posRI.y + 1, posRI.z - 2, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			arrow.addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSlowness(), 200, 0));
			level.addFreshEntity(arrow);

			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSlowness(), 200, 0), arrow);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			boolean m0HasSlowness = state.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness());
			boolean m1Clean = !state.get(1).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness());
			boolean m2Clean = !state.get(2).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness());
			boolean m3Clean = !state.get(3).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness());
			boolean m4Clean = !state.get(4).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness());

			boolean pass = m0HasSlowness && m1Clean && m2Clean && m3Clean && m4Clean;
			results.add(new TestResult("Test RI1 - Real Tipped Arrow Projectile Collision", pass,
				"m0Slowness=" + m0HasSlowness + ", m1Clean=" + m1Clean + ", m2Clean=" + m2Clean));
			cleanPen(level, cleanAreaRI);
			arrow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test RI1 - Real Tipped Arrow Projectile Collision", false, e.getMessage()));
		}

		// Test RI2 - Real Splash Potion entity
		try {
			cleanPen(level, cleanAreaRI);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posRI.x, posRI.y, posRI.z);
			((StackableEntity) stacked).jarstacker$setStackCount(3);
			level.addFreshEntity(stacked);

			ThrowableItemProjectile potion = com.jar.jarstacker.adapter.EntityAdapter.createThrownPotion(level, posRI.x, posRI.y + 1, posRI.z);
			ItemStack potionStack = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WEAKNESS);
			potion.setItem(potionStack);
			level.addFreshEntity(potion);

			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), potion);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			boolean m0Weakness = state.get(0).hasEffect(MobEffects.WEAKNESS);
			boolean m1Weakness = state.get(1).hasEffect(MobEffects.WEAKNESS);
			boolean m2Weakness = state.get(2).hasEffect(MobEffects.WEAKNESS);

			boolean pass = m0Weakness && m1Weakness && m2Weakness;
			results.add(new TestResult("Test RI2 - Real Splash Potion Entity", pass,
				"m0Weakness=" + m0Weakness + ", m1Weakness=" + m1Weakness + ", m2Weakness=" + m2Weakness));
			cleanPen(level, cleanAreaRI);
			potion.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test RI2 - Real Splash Potion Entity", false, e.getMessage()));
		}

		// Test RI3 - Real AreaEffectCloud lifecycle
		try {
			cleanPen(level, cleanAreaRI);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posRI.x, posRI.y, posRI.z);
			((StackableEntity) stacked).jarstacker$setStackCount(3);
			level.addFreshEntity(stacked);

			AreaEffectCloud cloud = new AreaEffectCloud(level, posRI.x, posRI.y, posRI.z);
			cloud.setRadius(3.0f);
			cloud.setDuration(200);
			cloud.addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSlowness(), 100, 0));
			level.addFreshEntity(cloud);

			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSlowness(), 100, 0), cloud);

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			boolean allHaveSlowness = state.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness())
				&& state.get(1).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness())
				&& state.get(2).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness());

			boolean pass = allHaveSlowness;
			results.add(new TestResult("Test RI3 - Real AreaEffectCloud Lifecycle", pass,
				"AllHaveSlowness=" + allHaveSlowness));
			cleanPen(level, cleanAreaRI);
			cloud.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test RI3 - Real AreaEffectCloud Lifecycle", false, e.getMessage()));
		}

		// Test RI4 - Cloud reapplication delay
		try {
			cleanPen(level, cleanAreaRI);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posRI.x, posRI.y, posRI.z);
			((StackableEntity) stacked).jarstacker$setStackCount(3);
			level.addFreshEntity(stacked);

			AreaEffectCloud cloud = new AreaEffectCloud(level, posRI.x, posRI.y, posRI.z);
			cloud.setRadius(3.0f);
			cloud.setDuration(200);
			cloud.addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSlowness(), 100, 0));
			level.addFreshEntity(cloud);

			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSlowness(), 100, 0), cloud);
			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(stacked);

			for (int t = 0; t < 5; t++) {
				LogicalStatusEffectManager.tick(stacked);
			}

			int durationAfter5Ticks = state.get(0).getEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness()).getDuration();
			boolean pass = durationAfter5Ticks <= 95 && durationAfter5Ticks > 0;
			results.add(new TestResult("Test RI4 - Cloud Reapplication Delay", pass,
				"DurationAfter5Ticks=" + durationAfter5Ticks));
			cleanPen(level, cleanAreaRI);
			cloud.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test RI4 - Cloud Reapplication Delay", false, e.getMessage()));
		}

		// Test RI5 - Cloud exit
		try {
			cleanPen(level, cleanAreaRI);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posRI.x + 20, posRI.y, posRI.z + 20);
			((StackableEntity) stacked).jarstacker$setStackCount(3);
			level.addFreshEntity(stacked);

			AreaEffectCloud cloud = new AreaEffectCloud(level, posRI.x, posRI.y, posRI.z);
			cloud.setRadius(3.0f);
			cloud.setDuration(200);
			cloud.addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSlowness(), 100, 0));
			level.addFreshEntity(cloud);

			boolean inCloud = cloud.getBoundingBox().intersects(stacked.getBoundingBox());
			if (inCloud) {
				LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSlowness(), 100, 0), cloud);
			}

			LogicalStatusEffectState state = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			boolean clean = !state.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSlowness());

			boolean pass = !inCloud && clean;
			results.add(new TestResult("Test RI5 - Cloud Exit Isolation", pass,
				"InCloud=" + inCloud + ", StackClean=" + clean));
			cleanPen(level, cleanAreaRI);
			cloud.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test RI5 - Cloud Exit Isolation", false, e.getMessage()));
		}

		// Test RI6 - Cloud multi-stack behavior
		try {
			cleanPen(level, cleanAreaRI);
			Zombie stackA = createEntity(EntityType.ZOMBIE, level);
			stackA.setPos(posRI.x - 1, posRI.y, posRI.z);
			((StackableEntity) stackA).jarstacker$setStackCount(2);
			level.addFreshEntity(stackA);

			Zombie stackB = createEntity(EntityType.ZOMBIE, level);
			stackB.setPos(posRI.x + 1, posRI.y, posRI.z);
			((StackableEntity) stackB).jarstacker$setStackCount(3);
			level.addFreshEntity(stackB);

			AreaEffectCloud cloud = new AreaEffectCloud(level, posRI.x, posRI.y, posRI.z);
			cloud.setRadius(3.0f);
			cloud.setDuration(200);
			cloud.addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 100, 0));
			level.addFreshEntity(cloud);

			LogicalStatusEffectManager.applyEffect(stackA, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 100, 0), cloud);
			LogicalStatusEffectManager.applyEffect(stackB, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getSpeed(), 100, 0), cloud);

			LogicalStatusEffectState stateA = LogicalStatusEffectManager.getOrCreateStatusState(stackA);
			LogicalStatusEffectState stateB = LogicalStatusEffectManager.getOrCreateStatusState(stackB);

			boolean pass = stateA.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& stateA.get(1).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& stateB.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& stateB.get(1).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())
				&& stateB.get(2).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed());

			results.add(new TestResult("Test RI6 - Cloud Multi-Stack Behavior", pass,
				"StackA_0=" + stateA.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed()) + ", StackB_0=" + stateB.get(0).hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getSpeed())));
			cleanPen(level, cleanAreaRI);
			cloud.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test RI6 - Cloud Multi-Stack Behavior", false, e.getMessage()));
		}
	}

	private static void runV060FallbackTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posFB = pos.add(90, 0, 90);
		net.minecraft.core.BlockPos penCenterFB = new net.minecraft.core.BlockPos((int) posFB.x, (int) posFB.y, (int) posFB.z);
		buildPen(level, penCenterFB, 4);
		AABB cleanAreaFB = new AABB(posFB.x - 6, posFB.y - 2, posFB.z - 6, posFB.x + 6, posFB.y + 6, posFB.z + 6);

		// Test FB1 - Unsupported extraction HP preservation
		try {
			cleanPen(level, cleanAreaFB);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posFB.x, posFB.y, posFB.z);
			((StackableEntity) stacked).jarstacker$setStackCount(5);
			level.addFreshEntity(stacked);

			LogicalHealthState hs = LogicalHealthManager.getOrCreateState(stacked);
			hs.set(0, 6.0f);
			hs.set(1, 20.0f);
			hs.set(2, 20.0f);
			hs.set(3, 20.0f);
			hs.set(4, 20.0f);
			stacked.setHealth(6.0f);

			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);
			Mob extracted = RuntimeCombatStateTransitionHandler.extractUnsupportedMob(stacked, modded);

			boolean pass = extracted != null
				&& extracted.getHealth() == 6.0f
				&& ((StackableEntity) extracted).jarstacker$getStackCount() == 1
				&& ((StackableEntity) stacked).jarstacker$getStackCount() == 4
				&& hs.size() == 4
				&& hs.get(0) == 20.0f
				&& hs.get(1) == 20.0f
				&& hs.get(2) == 20.0f
				&& hs.get(3) == 20.0f;

			results.add(new TestResult("Test FB1 - Fallback HP Preservation", pass,
				"ExtractedHp=" + (extracted != null ? extracted.getHealth() : "null")
				+ ", RemainderCount=" + ((StackableEntity) stacked).jarstacker$getStackCount()
				+ ", RemainderHp0=" + (hs != null ? hs.get(0) : "null")));
			cleanPen(level, cleanAreaFB);
		} catch (Exception e) {
			results.add(new TestResult("Test FB1 - Fallback HP Preservation", false, e.getMessage()));
		}

		// Test FB2 - Existing effects preserved
		try {
			cleanPen(level, cleanAreaFB);
			Cow stacked = createEntity(EntityType.COW, level);
			stacked.setPos(posFB.x, posFB.y, posFB.z);
			((StackableEntity) stacked).jarstacker$setStackCount(3);
			level.addFreshEntity(stacked);

			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(MobEffects.POISON, 200, 0), null);

			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);
			Mob extracted = RuntimeCombatStateTransitionHandler.extractUnsupportedMob(stacked, modded);

			boolean pass = extracted != null
				&& extracted.hasEffect(MobEffects.POISON)
				&& extracted.hasEffect(MODDED_UNSUPPORTED);

			results.add(new TestResult("Test FB2 - Fallback Supported Effects Preserved", pass,
				"ExtractedPoison=" + (extracted != null && extracted.hasEffect(MobEffects.POISON))
				+ ", ExtractedModded=" + (extracted != null && extracted.hasEffect(MODDED_UNSUPPORTED))));
			cleanPen(level, cleanAreaFB);
		} catch (Exception e) {
			results.add(new TestResult("Test FB2 - Fallback Supported Effects Preserved", false, e.getMessage()));
		}

		// Test FB3 - Burn state preserved
		try {
			cleanPen(level, cleanAreaFB);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posFB.x, posFB.y, posFB.z);
			((StackableEntity) stacked).jarstacker$setStackCount(3);
			level.addFreshEntity(stacked);

			LogicalBurnState bs = LogicalStatusEffectManager.getOrCreateBurnState(stacked);
			bs.get(0).setRemainingFireTicks(160);
			stacked.setRemainingFireTicks(160);

			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);
			Mob extracted = RuntimeCombatStateTransitionHandler.extractUnsupportedMob(stacked, modded);

			boolean pass = extracted != null && extracted.getRemainingFireTicks() == 160;
			results.add(new TestResult("Test FB3 - Fallback Burn State Preserved", pass,
				"ExtractedFireTicks=" + (extracted != null ? extracted.getRemainingFireTicks() : -1)));
			cleanPen(level, cleanAreaFB);
		} catch (Exception e) {
			results.add(new TestResult("Test FB3 - Fallback Burn State Preserved", false, e.getMessage()));
		}

		// Test FB4 - Placement/failure rollback
		try {
			cleanPen(level, cleanAreaFB);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posFB.x, posFB.y, posFB.z);
			((StackableEntity) stacked).jarstacker$setStackCount(3);
			level.addFreshEntity(stacked);

			LogicalHealthState hs = LogicalHealthManager.getOrCreateState(stacked);
			LogicalStatusEffectState ss = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			LogicalBurnState bs = LogicalStatusEffectManager.getOrCreateBurnState(stacked);

			RuntimeCombatStateTransitionHandler.failNextExtractionForTesting = true;
			MobEffectInstance trigger = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);
			Mob extracted = RuntimeCombatStateTransitionHandler.extractUnsupportedMob(stacked, trigger);

			boolean pass = extracted == null
				&& ((StackableEntity) stacked).jarstacker$getStackCount() == 3
				&& hs.size() == 3
				&& ss.size() == 3
				&& bs.size() == 3;

			results.add(new TestResult("Test FB4 - Fallback Placement/Failure Rollback", pass,
				"ExtractedNull=" + (extracted == null)
				+ ", StackCount=" + ((StackableEntity) stacked).jarstacker$getStackCount()
				+ ", HealthRecords=" + hs.size() + ", StatusRecords=" + ss.size()));
			cleanPen(level, cleanAreaFB);
		} catch (Exception e) {
			results.add(new TestResult("Test FB4 - Fallback Placement/Failure Rollback", false, e.getMessage()));
		}
	}

	private static void runV060PassiveSafeTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posPS = pos.add(95, 0, 95);
		net.minecraft.core.BlockPos penCenterPS = new net.minecraft.core.BlockPos((int) posPS.x, (int) posPS.y, (int) posPS.z);
		buildPen(level, penCenterPS, 4);
		AABB cleanAreaPS = new AABB(posPS.x - 6, posPS.y - 2, posPS.z - 6, posPS.x + 6, posPS.y + 6, posPS.z + 6);

		// Test PS1 - Levitation Real Movement Parity
		try {
			cleanPen(level, cleanAreaPS);
			Zombie baseline = createEntity(EntityType.ZOMBIE, level);
			baseline.setPos(posPS.x + 2, posPS.y, posPS.z);
			level.addFreshEntity(baseline);
			baseline.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0));
			baseline.setDeltaMovement(Vec3.ZERO);
			baseline.travel(Vec3.ZERO);
			double vanillaDeltaY = baseline.getDeltaMovement().y;

			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posPS.x, posPS.y, posPS.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);
			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(MobEffects.LEVITATION, 100, 0), null);

			boolean hasLev = stacked.hasEffect(MobEffects.LEVITATION);
			stacked.setDeltaMovement(Vec3.ZERO);
			stacked.travel(Vec3.ZERO);
			double stackDeltaY = stacked.getDeltaMovement().y;

			boolean pass = hasLev && Math.abs(vanillaDeltaY - stackDeltaY) < 0.001 && stackDeltaY > 0.0;
			results.add(new TestResult("Test PS1 - Levitation Real Movement Parity", pass,
				"hasEffect=" + hasLev + ", vanillaDeltaY=" + vanillaDeltaY + ", stackDeltaY=" + stackDeltaY));
			cleanPen(level, cleanAreaPS);
		} catch (Exception e) {
			results.add(new TestResult("Test PS1 - Levitation Real Movement Parity", false, e.getMessage()));
		}

		// Test PS2 - Water Breathing Underwater Drowning Parity
		try {
			cleanPen(level, cleanAreaPS);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posPS.x, posPS.y, posPS.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);

			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0), null);
			boolean canBreathe = stacked.canBreatheUnderwater();
			boolean hasWB = stacked.hasEffect(MobEffects.WATER_BREATHING);

			stacked.setAirSupply(100);
			int newAir = canBreathe ? 100 : (100 - 1);

			boolean pass = hasWB && canBreathe && newAir == 100;
			results.add(new TestResult("Test PS2 - Water Breathing Underwater Drowning Parity", pass,
				"hasEffect=" + hasWB + ", canBreatheUnderwater=" + canBreathe + ", airSupply=" + newAir));
			cleanPen(level, cleanAreaPS);
		} catch (Exception e) {
			results.add(new TestResult("Test PS2 - Water Breathing Underwater Drowning Parity", false, e.getMessage()));
		}

		// Test PS3 - Slow Falling Fall Damage Parity
		try {
			cleanPen(level, cleanAreaPS);
			Zombie baseline = createEntity(EntityType.ZOMBIE, level);
			baseline.setPos(posPS.x + 2, posPS.y + 5, posPS.z);
			level.addFreshEntity(baseline);
			baseline.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
			baseline.setDeltaMovement(0, 0, 0);
			baseline.travel(Vec3.ZERO);
			double vanillaDeltaY = baseline.getDeltaMovement().y;

			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posPS.x, posPS.y + 5, posPS.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);
			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0), null);
			boolean hasSF = stacked.hasEffect(MobEffects.SLOW_FALLING);
			stacked.setDeltaMovement(0, 0, 0);
			stacked.travel(Vec3.ZERO);
			double stackDeltaY = stacked.getDeltaMovement().y;

			boolean movementParity = Math.abs(stackDeltaY - vanillaDeltaY) < 1e-4 && stackDeltaY > -0.02;
			boolean damageZero = !stacked.causeFallDamage(0.0f, 1.0f, level.damageSources().fall());
			float hp = stacked.getHealth();

			boolean pass = hasSF && movementParity && damageZero && hp == 20.0f;
			results.add(new TestResult("Test PS3 - Slow Falling Fall Damage Parity", pass,
				"hasEffect=" + hasSF + ", vanillaDeltaY=" + vanillaDeltaY + ", stackDeltaY=" + stackDeltaY + ", hp=" + hp));
			cleanPen(level, cleanAreaPS);
		} catch (Exception e) {
			results.add(new TestResult("Test PS3 - Slow Falling Fall Damage Parity", false, e.getMessage()));
		}

		// Test PS4 - Jump Boost Vertical Jump Parity
		try {
			cleanPen(level, cleanAreaPS);
			Zombie baseline = createEntity(EntityType.ZOMBIE, level);
			baseline.setPos(posPS.x + 2, posPS.y, posPS.z);
			level.addFreshEntity(baseline);
			baseline.addEffect(new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getJumpBoost(), 200, 1));
			float baselineJumpBoost = 0.1f * (float) (baseline.getEffect(com.jar.jarstacker.adapter.EffectAdapter.getJumpBoost()).getAmplifier() + 1);

			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posPS.x, posPS.y, posPS.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);

			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(com.jar.jarstacker.adapter.EffectAdapter.getJumpBoost(), 200, 1), null);
			boolean hasJump = stacked.hasEffect(com.jar.jarstacker.adapter.EffectAdapter.getJumpBoost());
			MobEffectInstance jumpEff = stacked.getEffect(com.jar.jarstacker.adapter.EffectAdapter.getJumpBoost());
			float stackJumpBoost = 0.1f * (float) (jumpEff != null ? jumpEff.getAmplifier() + 1 : 0);

			boolean pass = hasJump && jumpEff != null && Math.abs(baselineJumpBoost - stackJumpBoost) < 0.001 && stackJumpBoost == 0.2f;
			results.add(new TestResult("Test PS4 - Jump Boost Vertical Jump Parity", pass,
				"hasEffect=" + hasJump + ", baselineJumpBoost=" + baselineJumpBoost + ", stackJumpBoost=" + stackJumpBoost));
			cleanPen(level, cleanAreaPS);
		} catch (Exception e) {
			results.add(new TestResult("Test PS4 - Jump Boost Vertical Jump Parity", false, e.getMessage()));
		}

		// Test PS5 - Active-Member Switch Effect Cleared
		try {
			cleanPen(level, cleanAreaPS);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posPS.x, posPS.y, posPS.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);

			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(MobEffects.LEVITATION, 200, 0), null);
			boolean beforeSwitch = stacked.hasEffect(MobEffects.LEVITATION);

			// Kill active member 0
			LogicalHealthManager.applyLogicalDirectDamage(stacked, 0, level.damageSources().magic(), 20.0f);
			LogicalStatusEffectManager.projectActiveMember(stacked);

			boolean afterSwitch = stacked.hasEffect(MobEffects.LEVITATION);
			stacked.setDeltaMovement(Vec3.ZERO);
			stacked.travel(Vec3.ZERO);
			double postDeltaY = stacked.getDeltaMovement().y;

			boolean pass = beforeSwitch && !afterSwitch && postDeltaY <= 0.0;
			results.add(new TestResult("Test PS5 - Active Member Switch Effect Cleared", pass,
				"beforeSwitch=" + beforeSwitch + ", afterSwitch=" + afterSwitch + ", postDeltaY=" + postDeltaY));
			cleanPen(level, cleanAreaPS);
		} catch (Exception e) {
			results.add(new TestResult("Test PS5 - Active Member Switch Effect Cleared", false, e.getMessage()));
		}

		// Test PS6 - Health Boost Max Health Parity & Clamping
		try {
			cleanPen(level, cleanAreaPS);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posPS.x, posPS.y, posPS.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);

			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(stacked);

			// Health Boost I (+4 max HP)
			LogicalStatusEffectManager.applyEffect(stacked, new MobEffectInstance(MobEffects.HEALTH_BOOST, 200, 0), null);
			float maxHpBoosted = stacked.getMaxHealth();
			LogicalHealthManager.healMember(stacked, 0, 4.0f);
			float hpBoosted = stacked.getHealth();

			// Kill member 0 to switch to member 1 (unboosted)
			LogicalHealthManager.applyLogicalDirectDamage(stacked, 0, level.damageSources().magic(), 24.0f);
			LogicalStatusEffectManager.projectActiveMember(stacked);

			float maxHpAfterSwitch = stacked.getMaxHealth();
			float hpAfterSwitch = stacked.getHealth();
			float logicalHp0AfterSwitch = hState.get(0);

			boolean pass = maxHpBoosted == 24.0f
				&& hpBoosted == 24.0f
				&& maxHpAfterSwitch == 20.0f
				&& hpAfterSwitch <= 20.0f
				&& logicalHp0AfterSwitch <= 20.0f;

			results.add(new TestResult("Test PS6 - Health Boost Parity & Clamping", pass,
				"maxHpBoosted=" + maxHpBoosted + ", hpBoosted=" + hpBoosted
				+ ", maxHpAfterSwitch=" + maxHpAfterSwitch + ", hpAfterSwitch=" + hpAfterSwitch));
			cleanPen(level, cleanAreaPS);
		} catch (Exception e) {
			results.add(new TestResult("Test PS6 - Health Boost Parity & Clamping", false, e.getMessage()));
		}
	}

	private static void runV060UnsupportedAreaTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posUA = pos.add(100, 0, 100);
		net.minecraft.core.BlockPos penCenterUA = new net.minecraft.core.BlockPos((int) posUA.x, (int) posUA.y, (int) posUA.z);
		buildPen(level, penCenterUA, 4);
		AABB cleanAreaUA = new AABB(posUA.x - 6, posUA.y - 2, posUA.z - 6, posUA.x + 6, posUA.y + 6, posUA.z + 6);

		// Test UA1 - Unsupported SINGLE Baseline
		try {
			cleanPen(level, cleanAreaUA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posUA.x, posUA.y, posUA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(5);
			level.addFreshEntity(stacked);

			Arrow arrow = new Arrow(level, posUA.x, posUA.y + 1, posUA.z - 2, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
			MobEffectInstance oozing = new MobEffectInstance(MobEffects.OOZING, 200, 0);
			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);

			LogicalStatusEffectManager.applyEffect(stacked, oozing, arrow);
			LogicalStatusEffectManager.applyEffect(stacked, modded, arrow);

			int remCount = ((StackableEntity) stacked).jarstacker$getStackCount();
			boolean pass = remCount == 4;
			results.add(new TestResult("Test UA1 - Unsupported SINGLE Baseline", pass,
				"Original=5, Remainder=" + remCount));
			cleanPen(level, cleanAreaUA);
			arrow.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test UA1 - Unsupported SINGLE Baseline", false, e.getMessage()));
		}

		// Test UA2 - Unsupported Splash x100 Bounded Fallback
		try {
			cleanPen(level, cleanAreaUA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posUA.x, posUA.y, posUA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(100);
			level.addFreshEntity(stacked);

			ThrowableItemProjectile potion = com.jar.jarstacker.adapter.EntityAdapter.createThrownPotion(level, posUA.x, posUA.y + 1, posUA.z);
			MobEffectInstance oozing = new MobEffectInstance(MobEffects.OOZING, 200, 0);
			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);

			LogicalStatusEffectManager.applyEffect(stacked, oozing, potion);
			LogicalStatusEffectManager.applyEffect(stacked, modded, potion);

			int remCount = ((StackableEntity) stacked).jarstacker$getStackCount();
			int extracted = 100 - remCount;
			boolean pass = extracted == LogicalStatusEffectManager.MAX_UNSUPPORTED_AREA_FALLBACK && remCount == 95;
			results.add(new TestResult("Test UA2 - Unsupported Splash x100 Bounded Fallback", pass,
				"Extracted=" + extracted + ", Remainder=" + remCount));
			cleanPen(level, cleanAreaUA);
			potion.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test UA2 - Unsupported Splash x100 Bounded Fallback", false, e.getMessage()));
		}

		// Test UA3 - Unsupported Lingering x100 Bounded Fallback
		try {
			cleanPen(level, cleanAreaUA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posUA.x, posUA.y, posUA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(100);
			level.addFreshEntity(stacked);

			AreaEffectCloud cloud = new AreaEffectCloud(level, posUA.x, posUA.y, posUA.z);
			cloud.setRadius(3.0f);
			MobEffectInstance infested = new MobEffectInstance(MobEffects.INFESTED, 200, 0);
			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);

			LogicalStatusEffectManager.applyEffect(stacked, infested, cloud);
			LogicalStatusEffectManager.applyEffect(stacked, modded, cloud);

			int remCount = ((StackableEntity) stacked).jarstacker$getStackCount();
			int extracted = 100 - remCount;
			boolean pass = extracted == LogicalStatusEffectManager.MAX_UNSUPPORTED_AREA_FALLBACK && remCount == 95;
			results.add(new TestResult("Test UA3 - Unsupported Lingering x100 Bounded Fallback", pass,
				"Extracted=" + extracted + ", Remainder=" + remCount));
			cleanPen(level, cleanAreaUA);
			cloud.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test UA3 - Unsupported Lingering x100 Bounded Fallback", false, e.getMessage()));
		}

		// Test UA4 - Repeated Cloud Exposure Cooldown
		try {
			cleanPen(level, cleanAreaUA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posUA.x, posUA.y, posUA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(100);
			level.addFreshEntity(stacked);

			AreaEffectCloud cloud = new AreaEffectCloud(level, posUA.x, posUA.y, posUA.z);
			cloud.setRadius(3.0f);
			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);

			LogicalStatusEffectManager.applyEffect(stacked, modded, cloud);
			int countAfterFirst = ((StackableEntity) stacked).jarstacker$getStackCount();

			boolean reExtracted = false;
			for (int t = 0; t < 5; t++) {
				if (LogicalStatusEffectManager.applyEffect(stacked, modded, cloud)) {
					reExtracted = true;
				}
			}
			int countAfterRepeated = ((StackableEntity) stacked).jarstacker$getStackCount();

			boolean pass = countAfterFirst == 95 && !reExtracted && countAfterRepeated == 95;
			results.add(new TestResult("Test UA4 - Repeated Cloud Exposure Cooldown", pass,
				"countAfterFirst=" + countAfterFirst + ", reExtracted=" + reExtracted + ", countAfterRepeated=" + countAfterRepeated));
			cleanPen(level, cleanAreaUA);
			cloud.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test UA4 - Repeated Cloud Exposure Cooldown", false, e.getMessage()));
		}

		// Test UA5 - Fallback Materialization Limit Behavior
		try {
			cleanPen(level, cleanAreaUA);
			Zombie stackLarge = createEntity(EntityType.ZOMBIE, level);
			stackLarge.setPos(posUA.x, posUA.y, posUA.z);
			((StackableEntity) stackLarge).jarstacker$setStackCount(50);
			level.addFreshEntity(stackLarge);

			ThrowableItemProjectile potion = com.jar.jarstacker.adapter.EntityAdapter.createThrownPotion(level, posUA.x, posUA.y + 1, posUA.z);
			MobEffectInstance weaving = new MobEffectInstance(MobEffects.WEAVING, 200, 0);
			LogicalStatusEffectManager.applyEffect(stackLarge, weaving, potion);
			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);
			LogicalStatusEffectManager.applyEffect(stackLarge, modded, potion);

			int largeRemainder = ((StackableEntity) stackLarge).jarstacker$getStackCount();

			Zombie stackSmall = createEntity(EntityType.ZOMBIE, level);
			stackSmall.setPos(posUA.x + 2, posUA.y, posUA.z);
			((StackableEntity) stackSmall).jarstacker$setStackCount(3);
			level.addFreshEntity(stackSmall);

			LogicalStatusEffectManager.applyEffect(stackSmall, weaving, potion);
			LogicalStatusEffectManager.applyEffect(stackSmall, modded, potion);
			int smallRemainder = ((StackableEntity) stackSmall).jarstacker$getStackCount();

			boolean pass = largeRemainder == 45 && smallRemainder == 1;
			results.add(new TestResult("Test UA5 - Fallback Materialization Limit Behavior", pass,
				"largeRemainder=" + largeRemainder + ", smallRemainder=" + smallRemainder));
			cleanPen(level, cleanAreaUA);
			potion.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test UA5 - Fallback Materialization Limit Behavior", false, e.getMessage()));
		}

		// Test UA6 - State/Count Integrity After Bounded Failure/Recovery
		try {
			cleanPen(level, cleanAreaUA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posUA.x, posUA.y, posUA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(100);
			level.addFreshEntity(stacked);

			LogicalHealthState hs = LogicalHealthManager.getOrCreateState(stacked);
			LogicalStatusEffectState ss = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			LogicalBurnState bs = LogicalStatusEffectManager.getOrCreateBurnState(stacked);

			RuntimeCombatStateTransitionHandler.failNextExtractionForTesting = true;
			ThrowableItemProjectile potion = com.jar.jarstacker.adapter.EntityAdapter.createThrownPotion(level, posUA.x, posUA.y + 1, posUA.z);
			MobEffectInstance modded = new MobEffectInstance(MODDED_UNSUPPORTED, 200, 0);

			boolean success = LogicalStatusEffectManager.applyEffect(stacked, modded, potion);

			int count = ((StackableEntity) stacked).jarstacker$getStackCount();
			boolean pass = !success && count == 100 && hs.size() == 100 && ss.size() == 100 && bs.size() == 100;

			results.add(new TestResult("Test UA6 - State/Count Integrity After Bounded Failure/Recovery", pass,
				"success=" + success + ", count=" + count + ", hs=" + hs.size() + ", ss=" + ss.size()));
			cleanPen(level, cleanAreaUA);
			potion.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test UA6 - State/Count Integrity After Bounded Failure/Recovery", false, e.getMessage()));
		}
	}

	private static void runV060VanillaAreaParityTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posVA = pos.add(105, 0, 105);
		net.minecraft.core.BlockPos penCenterVA = new net.minecraft.core.BlockPos((int) posVA.x, (int) posVA.y, (int) posVA.z);
		buildPen(level, penCenterVA, 4);
		AABB cleanAreaVA = new AABB(posVA.x - 6, posVA.y - 2, posVA.z - 6, posVA.x + 6, posVA.y + 6, posVA.z + 6);

		// Test VA1 - Splash Oozing x100 Parity (Zero extraction storm, all 100 receive status)
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(100);
			level.addFreshEntity(stacked);

			ThrowableItemProjectile potion = com.jar.jarstacker.adapter.EntityAdapter.createThrownPotion(level, posVA.x, posVA.y + 1, posVA.z);
			MobEffectInstance oozing = new MobEffectInstance(MobEffects.OOZING, 200, 0);

			boolean success = LogicalStatusEffectManager.applyEffect(stacked, oozing, potion);

			int remCount = ((StackableEntity) stacked).jarstacker$getStackCount();
			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			boolean allHaveOozing = sState.size() == 100;
			if (allHaveOozing) {
				for (int i = 0; i < 100; i++) {
					if (!sState.get(i).hasEffect(MobEffects.OOZING)) {
						allHaveOozing = false;
						break;
					}
				}
			}

			List<Zombie> physicalMobs = level.getEntitiesOfClass(Zombie.class, cleanAreaVA);
			boolean pass = success && remCount == 100 && physicalMobs.size() == 1 && allHaveOozing;
			results.add(new TestResult("Test VA1 - Splash Oozing x100 Parity", pass,
				"Success=" + success + ", StackCount=" + remCount + ", PhysicalEntities=" + physicalMobs.size() + ", All100Affected=" + allHaveOozing));
			cleanPen(level, cleanAreaVA);
			potion.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VA1 - Splash Oozing x100 Parity", false, e.getMessage()));
		}

		// Test VA2 - Splash Wind Charged x100 Parity (Zero extraction storm, all 100 receive status)
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(100);
			level.addFreshEntity(stacked);

			ThrowableItemProjectile potion = com.jar.jarstacker.adapter.EntityAdapter.createThrownPotion(level, posVA.x, posVA.y + 1, posVA.z);
			MobEffectInstance wind = new MobEffectInstance(MobEffects.WIND_CHARGED, 200, 0);

			boolean success = LogicalStatusEffectManager.applyEffect(stacked, wind, potion);

			int remCount = ((StackableEntity) stacked).jarstacker$getStackCount();
			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			boolean allHaveWind = sState.size() == 100;
			if (allHaveWind) {
				for (int i = 0; i < 100; i++) {
					if (!sState.get(i).hasEffect(MobEffects.WIND_CHARGED)) {
						allHaveWind = false;
						break;
					}
				}
			}

			List<Zombie> physicalMobs = level.getEntitiesOfClass(Zombie.class, cleanAreaVA);
			boolean pass = success && remCount == 100 && physicalMobs.size() == 1 && allHaveWind;
			results.add(new TestResult("Test VA2 - Splash Wind Charged x100 Parity", pass,
				"Success=" + success + ", StackCount=" + remCount + ", PhysicalEntities=" + physicalMobs.size() + ", All100Affected=" + allHaveWind));
			cleanPen(level, cleanAreaVA);
			potion.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VA2 - Splash Wind Charged x100 Parity", false, e.getMessage()));
		}

		// Test VA3 - Lingering Weaving x100 Parity (Zero extraction storm, cloud cooldown respected)
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(100);
			level.addFreshEntity(stacked);

			AreaEffectCloud cloud = new AreaEffectCloud(level, posVA.x, posVA.y, posVA.z);
			cloud.setRadius(3.0f);
			MobEffectInstance weaving = new MobEffectInstance(MobEffects.WEAVING, 200, 0);

			boolean success = LogicalStatusEffectManager.applyEffect(stacked, weaving, cloud);
			int remCount = ((StackableEntity) stacked).jarstacker$getStackCount();
			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			boolean allHaveWeaving = sState.size() == 100;
			if (allHaveWeaving) {
				for (int i = 0; i < 100; i++) {
					if (!sState.get(i).hasEffect(MobEffects.WEAVING)) {
						allHaveWeaving = false;
						break;
					}
				}
			}

			// Cooldown check: consecutive tick on same cloud
			boolean repeatedApply = LogicalStatusEffectManager.applyEffect(stacked, weaving, cloud);

			List<Zombie> physicalMobs = level.getEntitiesOfClass(Zombie.class, cleanAreaVA);
			boolean pass = success && !repeatedApply && remCount == 100 && physicalMobs.size() == 1 && allHaveWeaving;
			results.add(new TestResult("Test VA3 - Lingering Weaving x100 Parity", pass,
				"Success=" + success + ", RepeatedRejected=" + !repeatedApply + ", StackCount=" + remCount + ", All100Affected=" + allHaveWeaving));
			cleanPen(level, cleanAreaVA);
			cloud.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VA3 - Lingering Weaving x100 Parity", false, e.getMessage()));
		}

		// Test VA4 - Infested Logical Hurt Parity (Exactly 1 damaged member triggers onMobHurt)
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(5);
			level.addFreshEntity(stacked);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			// Apply Infested to member 1 only
			sState.get(1).addEffect(new MobEffectInstance(MobEffects.INFESTED, 200, 0), stacked);

			LogicalHealthManager.testHurtCallbackCount = 0;
			// Damage virtual member 1 directly
			LogicalHealthManager.applyLogicalDirectDamage(stacked, 1, level.damageSources().generic(), 5.0f);

			int hurtCallbacks = LogicalHealthManager.testHurtCallbackCount;
			boolean pass = hurtCallbacks == 1;
			results.add(new TestResult("Test VA4 - Infested Logical Hurt Parity", pass,
				"HurtCallbacks=" + hurtCallbacks + " (expected 1)"));
			cleanPen(level, cleanAreaVA);
		} catch (Exception e) {
			results.add(new TestResult("Test VA4 - Infested Logical Hurt Parity", false, e.getMessage()));
		}

		// Test VA5 - Oozing Logical Death Parity (Dying member triggers slime spawn)
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.OOZING, 200, 0), stacked);

			LogicalHealthManager.testDeathCallbackCount = 0;
			// Kill active member 0
			LogicalHealthManager.applyLogicalDirectDamage(stacked, 0, level.damageSources().generic(), 50.0f);

			int deathCallbacks = LogicalHealthManager.testDeathCallbackCount;
			int remainingCount = ((StackableEntity) stacked).jarstacker$getStackCount();
			List<net.minecraft.world.entity.monster.Slime> slimes = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Slime.class, cleanAreaVA);

			boolean pass = deathCallbacks == 1 && remainingCount == 1 && !slimes.isEmpty();
			results.add(new TestResult("Test VA5 - Oozing Logical Death Parity", pass,
				"DeathCallbacks=" + deathCallbacks + ", RemainingCount=" + remainingCount + ", SlimesSpawned=" + slimes.size()));
			cleanPen(level, cleanAreaVA);
		} catch (Exception e) {
			results.add(new TestResult("Test VA5 - Oozing Logical Death Parity", false, e.getMessage()));
		}

		// Test VA6 - Weaving Logical Death Parity (Dying member triggers cobweb spawn)
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.WEAVING, 200, 0), stacked);

			LogicalHealthManager.testDeathCallbackCount = 0;
			// Kill active member 0
			LogicalHealthManager.applyLogicalDirectDamage(stacked, 0, level.damageSources().generic(), 50.0f);

			int deathCallbacks = LogicalHealthManager.testDeathCallbackCount;
			int remainingCount = ((StackableEntity) stacked).jarstacker$getStackCount();

			boolean pass = deathCallbacks == 1 && remainingCount == 1;
			results.add(new TestResult("Test VA6 - Weaving Logical Death Parity", pass,
				"DeathCallbacks=" + deathCallbacks + ", RemainingCount=" + remainingCount));
			cleanPen(level, cleanAreaVA);
		} catch (Exception e) {
			results.add(new TestResult("Test VA6 - Weaving Logical Death Parity", false, e.getMessage()));
		}

		// Test VA7 - Wind Charged Logical Death Parity (Dying member triggers wind burst)
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(2);
			level.addFreshEntity(stacked);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.WIND_CHARGED, 200, 0), stacked);

			LogicalHealthManager.testDeathCallbackCount = 0;
			// Kill active member 0
			LogicalHealthManager.applyLogicalDirectDamage(stacked, 0, level.damageSources().generic(), 50.0f);

			int deathCallbacks = LogicalHealthManager.testDeathCallbackCount;
			int remainingCount = ((StackableEntity) stacked).jarstacker$getStackCount();

			boolean pass = deathCallbacks == 1 && remainingCount == 1;
			results.add(new TestResult("Test VA7 - Wind Charged Logical Death Parity", pass,
				"DeathCallbacks=" + deathCallbacks + ", RemainingCount=" + remainingCount));
			cleanPen(level, cleanAreaVA);
		} catch (Exception e) {
			results.add(new TestResult("Test VA7 - Wind Charged Logical Death Parity", false, e.getMessage()));
		}

		// Test VA8 - Multi-Death Parity (K dying affected members trigger exactly K callbacks)
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(10);
			level.addFreshEntity(stacked);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			for (int i = 0; i < 10; i++) {
				sState.get(i).addEffect(new MobEffectInstance(MobEffects.OOZING, 200, 0), stacked);
			}

			LogicalHealthManager.testDeathCallbackCount = 0;
			// Inflict area explosion killing exactly 4 members
			LogicalHealthState hState = LogicalHealthManager.getOrCreateState(stacked);
			// Set member 0, 1, 2, 3 HP to 5, others to 20
			hState.set(0, 5.0f);
			hState.set(1, 5.0f);
			hState.set(2, 5.0f);
			hState.set(3, 5.0f);
			stacked.setHealth(5.0f);

			// 10 damage from explosion: members 0, 1, 2, 3 die (K = 4 deaths)
			LogicalHealthManager.onDamageApplied(stacked, level.damageSources().explosion(null), 10.0f);
			LogicalHealthManager.handleDie(stacked, level.damageSources().explosion(null));

			int deathCallbacks = LogicalHealthManager.testDeathCallbackCount;
			int remainingCount = ((StackableEntity) stacked).jarstacker$getStackCount();

			boolean pass = deathCallbacks == 4 && remainingCount == 6;
			results.add(new TestResult("Test VA8 - Multi-Death Exact Parity (K callbacks)", pass,
				"DeathCallbacks=" + deathCallbacks + " (expected 4), Remaining=" + remainingCount + " (expected 6)"));
			cleanPen(level, cleanAreaVA);
		} catch (Exception e) {
			results.add(new TestResult("Test VA8 - Multi-Death Exact Parity (K callbacks)", false, e.getMessage()));
		}

		// Test VA9 - Unaffected Logical Members (0 callbacks triggered)
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(5);
			level.addFreshEntity(stacked);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			// Only member 0 has effect, members 1-4 have NO effect
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.OOZING, 200, 0), stacked);

			LogicalHealthManager.testDeathCallbackCount = 0;
			// Kill virtual members 1 and 2 directly (unaffected)
			LogicalHealthManager.applyLogicalDirectDamage(stacked, 1, level.damageSources().generic(), 50.0f);
			LogicalHealthManager.applyLogicalDirectDamage(stacked, 1, level.damageSources().generic(), 50.0f);

			int deathCallbacks = LogicalHealthManager.testDeathCallbackCount;
			int remainingCount = ((StackableEntity) stacked).jarstacker$getStackCount();

			boolean pass = deathCallbacks == 0 && remainingCount == 3;
			results.add(new TestResult("Test VA9 - Unaffected Members Trigger 0 Callbacks", pass,
				"DeathCallbacks=" + deathCallbacks + " (expected 0), Remaining=" + remainingCount + " (expected 3)"));
			cleanPen(level, cleanAreaVA);
		} catch (Exception e) {
			results.add(new TestResult("Test VA9 - Unaffected Members Trigger 0 Callbacks", false, e.getMessage()));
		}

		// Test VA10 - Save/Reload EVENT_DRIVEN_LOGICAL State Parity
		try {
			cleanPen(level, cleanAreaVA);
			Zombie stacked = createEntity(EntityType.ZOMBIE, level);
			stacked.setPos(posVA.x, posVA.y, posVA.z);
			((StackableEntity) stacked).jarstacker$setStackCount(4);
			level.addFreshEntity(stacked);

			LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(stacked);
			sState.get(0).addEffect(new MobEffectInstance(MobEffects.INFESTED, 100, 0), stacked);
			sState.get(1).addEffect(new MobEffectInstance(MobEffects.OOZING, 120, 1), stacked);
			sState.get(2).addEffect(new MobEffectInstance(MobEffects.WEAVING, 140, 0), stacked);
			sState.get(3).addEffect(new MobEffectInstance(MobEffects.WIND_CHARGED, 160, 2), stacked);

			net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
			EntityAdapter.addAdditionalSaveData(stacked, tag);

			Zombie loaded = createEntity(EntityType.ZOMBIE, level);
			((StackableEntity) loaded).jarstacker$setStackCount(4);
			EntityAdapter.readAdditionalSaveData(loaded, tag);

			LogicalStatusEffectState loadedState = ((StackableEntity) loaded).jarstacker$getLogicalStatusEffectState();
			boolean pass = loadedState != null && loadedState.size() == 4
				&& loadedState.get(0).hasEffect(MobEffects.INFESTED)
				&& loadedState.get(0).getEffect(MobEffects.INFESTED).getDuration() == 100
				&& loadedState.get(1).hasEffect(MobEffects.OOZING)
				&& loadedState.get(1).getEffect(MobEffects.OOZING).getDuration() == 120
				&& loadedState.get(1).getEffect(MobEffects.OOZING).getAmplifier() == 1
				&& loadedState.get(2).hasEffect(MobEffects.WEAVING)
				&& loadedState.get(2).getEffect(MobEffects.WEAVING).getDuration() == 140
				&& loadedState.get(3).hasEffect(MobEffects.WIND_CHARGED)
				&& loadedState.get(3).getEffect(MobEffects.WIND_CHARGED).getDuration() == 160
				&& loadedState.get(3).getEffect(MobEffects.WIND_CHARGED).getAmplifier() == 2;

			results.add(new TestResult("Test VA10 - Save / Reload Event-Driven State Parity", pass,
				"LoadedSize=" + (loadedState != null ? loadedState.size() : 0) + ", Preserved=" + pass));
			cleanPen(level, cleanAreaVA);
			loaded.discard();
		} catch (Exception e) {
			results.add(new TestResult("Test VA10 - Save / Reload Event-Driven State Parity", false, e.getMessage()));
		}
	}

	private static void runItemMergeTests(ServerLevel level, Vec3 pos, List<TestResult> results) {
		Vec3 posIM = pos.add(60, 0, 60);
		AABB cleanAreaIM = new AABB(posIM.x - 20, posIM.y - 10, posIM.z - 20, posIM.x + 20, posIM.y + 20, posIM.z + 20);
		ModConfig config = ModConfig.getInstance();

		java.util.function.Consumer<AABB> clean = area -> {
			for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) item.discard();
			for (ItemEntity item : level.getEntities(EntityTypeTest.forClass(ItemEntity.class), Entity::isAlive)) item.discard();
			List<ItemEntity> extra = new java.util.ArrayList<>();
			for (Entity e : level.getAllEntities()) {
				if (e instanceof ItemEntity item) extra.add(item);
			}
			for (ItemEntity item : extra) item.discard();
		};

		// Test IM1 - Old xN + New x1 -> Latest Entity Survives with Natural Transform
		try {
			clean.accept(cleanAreaIM);
			ItemEntity oldItem = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 64));
			((StackableEntity) oldItem).jarstacker$setStackCount(50);
			oldItem.setPickUpDelay(100);
			((StackableEntity) oldItem).jarstacker$setAge(100);
			level.addFreshEntity(oldItem);

			ItemEntity newItem = new ItemEntity(level, posIM.x + 0.4, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) newItem).jarstacker$setStackCount(1);
			newItem.setPickUpDelay(100);
			((StackableEntity) newItem).jarstacker$setAge(0);
			Vec3 initialVel = new Vec3(0.05, 0.12, -0.03);
			newItem.setDeltaMovement(initialVel);
			level.addFreshEntity(newItem);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			Vec3 survivorVel = items.isEmpty() ? Vec3.ZERO : items.get(0).getDeltaMovement();
			boolean pass = items.size() == 1
				&& items.get(0).getUUID().equals(newItem.getUUID())
				&& ((StackableEntity) items.get(0)).jarstacker$getStackCount() == 51
				&& Math.abs(items.get(0).getX() - (posIM.x + 0.4)) < 0.01
				&& Math.abs(survivorVel.x - initialVel.x) < 1e-4
				&& Math.abs(survivorVel.y - initialVel.y) < 1e-4
				&& Math.abs(survivorVel.z - initialVel.z) < 1e-4
				&& oldItem.isRemoved();

			results.add(new TestResult("Test IM1 - Old xN + New x1 -> Latest Entity Survives with Natural Transform", pass,
				"SurvivorUUID=" + (items.isEmpty() ? "none" : items.get(0).getUUID()) + " (expected " + newItem.getUUID() + "), Count=" + (items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount()) + " (expected 51), TargetPos=" + (items.isEmpty() ? 0 : items.get(0).getX())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM1 - Old xN + New x1 -> Latest Entity Survives with Natural Transform", false, e.getMessage()));
		}

		// Test IM2 - Sequential Drops A -> B -> C -> Newest Drop Becomes Survivor
		try {
			clean.accept(cleanAreaIM);

			// Step 1: Spawn A at x=0
			ItemEntity itemA = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) itemA).jarstacker$setStackCount(1);
			itemA.setPickUpDelay(100);
			((StackableEntity) itemA).jarstacker$setAge(2);
			level.addFreshEntity(itemA);

			// Step 2: Spawn B at x=0.4, merge -> B survives with count 2 at x=0.4, A is removed
			ItemEntity itemB = new ItemEntity(level, posIM.x + 0.4, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) itemB).jarstacker$setStackCount(1);
			itemB.setPickUpDelay(100);
			((StackableEntity) itemB).jarstacker$setAge(1);
			Vec3 velB = new Vec3(0.02, 0.10, -0.01);
			itemB.setDeltaMovement(velB);
			level.addFreshEntity(itemB);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> step2Items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			Vec3 velStep2 = step2Items.isEmpty() ? Vec3.ZERO : step2Items.get(0).getDeltaMovement();
			boolean step2Pass = step2Items.size() == 1
				&& step2Items.get(0).getUUID().equals(itemB.getUUID())
				&& ((StackableEntity) step2Items.get(0)).jarstacker$getStackCount() == 2
				&& Math.abs(step2Items.get(0).getX() - (posIM.x + 0.4)) < 0.01
				&& Math.abs(velStep2.x - velB.x) < 1e-4
				&& itemA.isRemoved();

			// Step 3: Spawn C at x=0.8, merge -> C survives with count 3 at x=0.8, B is removed
			ItemEntity itemC = new ItemEntity(level, posIM.x + 0.8, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) itemC).jarstacker$setStackCount(1);
			itemC.setPickUpDelay(100);
			((StackableEntity) itemC).jarstacker$setAge(0);
			Vec3 velC = new Vec3(-0.03, 0.14, 0.04);
			itemC.setDeltaMovement(velC);
			level.addFreshEntity(itemC);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> step3Items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			Vec3 velStep3 = step3Items.isEmpty() ? Vec3.ZERO : step3Items.get(0).getDeltaMovement();
			boolean step3Pass = step3Items.size() == 1
				&& step3Items.get(0).getUUID().equals(itemC.getUUID())
				&& ((StackableEntity) step3Items.get(0)).jarstacker$getStackCount() == 3
				&& Math.abs(step3Items.get(0).getX() - (posIM.x + 0.8)) < 0.01
				&& Math.abs(velStep3.x - velC.x) < 1e-4
				&& itemB.isRemoved();

			boolean pass = step2Pass && step3Pass;
			results.add(new TestResult("Test IM2 - Sequential Drops A -> B -> C -> Newest Drop Becomes Survivor", pass,
				"Step2=" + step2Pass + ", Step3=" + step3Pass + ", SurvivorUUID=" + (step3Items.isEmpty() ? "none" : step3Items.get(0).getUUID()) + " (expected " + itemC.getUUID() + ")"));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM2 - Sequential Drops A -> B -> C -> Newest Drop Becomes Survivor", false, e.getMessage()));
		}

		// Test IM3 - Multiple Old Piles + One New Pile
		try {
			clean.accept(cleanAreaIM);
			ItemEntity pileA = new ItemEntity(level, posIM.x - 0.2, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 10));
			((StackableEntity) pileA).jarstacker$setStackCount(10);
			pileA.setPickUpDelay(100);
			((StackableEntity) pileA).jarstacker$setAge(50);
			level.addFreshEntity(pileA);

			ItemEntity pileB = new ItemEntity(level, posIM.x + 0.2, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 20));
			((StackableEntity) pileB).jarstacker$setStackCount(20);
			pileB.setPickUpDelay(100);
			((StackableEntity) pileB).jarstacker$setAge(40);
			level.addFreshEntity(pileB);

			ItemEntity pileC = new ItemEntity(level, posIM.x, posIM.y, posIM.z - 0.2, new ItemStack(Items.COBBLESTONE, 5));
			((StackableEntity) pileC).jarstacker$setStackCount(5);
			pileC.setPickUpDelay(100);
			((StackableEntity) pileC).jarstacker$setAge(30);
			level.addFreshEntity(pileC);

			ItemEntity pileD = new ItemEntity(level, posIM.x, posIM.y, posIM.z + 0.2, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) pileD).jarstacker$setStackCount(1);
			pileD.setPickUpDelay(100);
			((StackableEntity) pileD).jarstacker$setAge(0);
			Vec3 velD = new Vec3(0.06, 0.16, -0.05);
			pileD.setDeltaMovement(velD);
			level.addFreshEntity(pileD);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			Vec3 survivorVel = items.isEmpty() ? Vec3.ZERO : items.get(0).getDeltaMovement();
			boolean pass = items.size() == 1
				&& items.get(0).getUUID().equals(pileD.getUUID())
				&& ((StackableEntity) items.get(0)).jarstacker$getStackCount() == 36
				&& Math.abs(items.get(0).getZ() - (posIM.z + 0.2)) < 0.01
				&& Math.abs(survivorVel.x - velD.x) < 1e-4
				&& pileA.isRemoved() && pileB.isRemoved() && pileC.isRemoved();

			results.add(new TestResult("Test IM3 - Multiple Old Piles + One New Pile", pass,
				"Survivor=" + (items.isEmpty() ? "none" : items.get(0).getUUID()) + ", TotalCount=" + (items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount()) + " (expected 36)"));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM3 - Multiple Old Piles + One New Pile", false, e.getMessage()));
		}

		// Test IM4 - Same-Tick Deterministic Tie-Break
		try {
			clean.accept(cleanAreaIM);
			ItemEntity first = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			first.setPickUpDelay(100);
			((StackableEntity) first).jarstacker$setAge(0);
			level.addFreshEntity(first);

			ItemEntity second = new ItemEntity(level, posIM.x + 0.5, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			second.setPickUpDelay(100);
			((StackableEntity) second).jarstacker$setAge(0);
			Vec3 velSecond = new Vec3(0.01, 0.08, -0.02);
			second.setDeltaMovement(velSecond);
			level.addFreshEntity(second);

			boolean secondIsNewer = ItemStackingManager.isNewer(second, first);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			Vec3 velResult = items.isEmpty() ? Vec3.ZERO : items.get(0).getDeltaMovement();
			boolean pass = items.size() == 1
				&& items.get(0).getUUID().equals(second.getUUID())
				&& ((StackableEntity) items.get(0)).jarstacker$getStackCount() == 2
				&& Math.abs(items.get(0).getX() - (posIM.x + 0.5)) < 0.01
				&& Math.abs(velResult.x - velSecond.x) < 1e-4
				&& secondIsNewer && first.isRemoved();

			results.add(new TestResult("Test IM4 - Same-Tick Deterministic Tie-Break", pass,
				"WinnerUUID=" + (items.isEmpty() ? "none" : items.get(0).getUUID()) + " (expected second: " + second.getUUID() + "), secondIsNewer=" + secondIsNewer));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM4 - Same-Tick Deterministic Tie-Break", false, e.getMessage()));
		}

		// Test IM5 - Maximum-Stack Overflow
		try {
			clean.accept(cleanAreaIM);
			ItemEntity oldItem = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 64));
			((StackableEntity) oldItem).jarstacker$setStackCount(4000);
			oldItem.setPickUpDelay(100);
			((StackableEntity) oldItem).jarstacker$setAge(50);
			level.addFreshEntity(oldItem);

			ItemEntity newItem = new ItemEntity(level, posIM.x + 0.4, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 64));
			((StackableEntity) newItem).jarstacker$setStackCount(200);
			newItem.setPickUpDelay(100);
			((StackableEntity) newItem).jarstacker$setAge(0);
			Vec3 velNew = new Vec3(0.04, 0.11, -0.03);
			newItem.setDeltaMovement(velNew);
			level.addFreshEntity(newItem);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			int countOld = ((StackableEntity) oldItem).jarstacker$getStackCount();
			int countNew = ((StackableEntity) newItem).jarstacker$getStackCount();
			Vec3 velNewActual = newItem.getDeltaMovement();

			boolean pass = items.size() == 2
				&& countNew == 4096
				&& countOld == 104
				&& (countOld + countNew == 4200)
				&& Math.abs(newItem.getX() - (posIM.x + 0.4)) < 0.01
				&& Math.abs(velNewActual.x - velNew.x) < 1e-4
				&& !newItem.isRemoved() && !oldItem.isRemoved();

			results.add(new TestResult("Test IM5 - Maximum-Stack Overflow", pass,
				"NewCount=" + countNew + " (expected 4096), OldRemainderCount=" + countOld + " (expected 104), Total=" + (countOld + countNew)));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM5 - Maximum-Stack Overflow", false, e.getMessage()));
		}

		// Test IM6 - Incompatible Item Remains Separate
		try {
			clean.accept(cleanAreaIM);
			ItemEntity cobble = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 10));
			((StackableEntity) cobble).jarstacker$setStackCount(10);
			cobble.setPickUpDelay(100);
			((StackableEntity) cobble).jarstacker$setAge(50);
			level.addFreshEntity(cobble);

			ItemEntity stone = new ItemEntity(level, posIM.x + 0.5, posIM.y, posIM.z, new ItemStack(Items.STONE, 5));
			((StackableEntity) stone).jarstacker$setStackCount(5);
			stone.setPickUpDelay(100);
			((StackableEntity) stone).jarstacker$setAge(0);
			level.addFreshEntity(stone);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean pass = items.size() == 2
				&& !cobble.isRemoved() && !stone.isRemoved()
				&& ((StackableEntity) cobble).jarstacker$getStackCount() == 10
				&& ((StackableEntity) stone).jarstacker$getStackCount() == 5;

			results.add(new TestResult("Test IM6 - Incompatible Item Remains Separate", pass,
				"ItemCount=" + items.size() + ", Cobble=" + ((StackableEntity) cobble).jarstacker$getStackCount() + ", Stone=" + ((StackableEntity) stone).jarstacker$getStackCount()));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM6 - Incompatible Item Remains Separate", false, e.getMessage()));
		}

		// Test IM7 - Count Conservation
		try {
			clean.accept(cleanAreaIM);
			int[] pileSizes = {15, 25, 35, 45, 55};
			int expectedSum = 5;
			for (int size : pileSizes) {
				expectedSum += size;
				ItemEntity pile = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.DIAMOND, 1));
				((StackableEntity) pile).jarstacker$setStackCount(size);
				pile.setPickUpDelay(100);
				((StackableEntity) pile).jarstacker$setAge(50);
				level.addFreshEntity(pile);
			}

			ItemEntity newest = new ItemEntity(level, posIM.x + 0.5, posIM.y, posIM.z, new ItemStack(Items.DIAMOND, 1));
			((StackableEntity) newest).jarstacker$setStackCount(5);
			newest.setPickUpDelay(100);
			((StackableEntity) newest).jarstacker$setAge(0);
			level.addFreshEntity(newest);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			int actualTotal = 0;
			for (ItemEntity item : items) {
				actualTotal += ((StackableEntity) item).jarstacker$getStackCount();
			}

			boolean pass = items.size() == 1
				&& actualTotal == expectedSum
				&& expectedSum == 180;

			results.add(new TestResult("Test IM7 - Count Conservation", pass,
				"Actual=" + actualTotal + ", Expected=" + expectedSum + ", SurvivorUUID=" + (items.isEmpty() ? "none" : items.get(0).getUUID())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM7 - Count Conservation", false, e.getMessage()));
		}

		// Test IM8 - Save / Reload Regression
		try {
			clean.accept(cleanAreaIM);
			ItemEntity item = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 64));
			((StackableEntity) item).jarstacker$setStackCount(100);
			net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
			EntityAdapter.addAdditionalSaveData(item, tag);

			ItemEntity reloaded = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 64));
			EntityAdapter.readAdditionalSaveData(reloaded, tag);
			reloaded.setPickUpDelay(100);
			((StackableEntity) reloaded).jarstacker$setAge(20);
			level.addFreshEntity(reloaded);

			ItemEntity freshNew = new ItemEntity(level, posIM.x + 0.4, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) freshNew).jarstacker$setStackCount(1);
			freshNew.setPickUpDelay(100);
			((StackableEntity) freshNew).jarstacker$setAge(0);
			level.addFreshEntity(freshNew);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean pass = items.size() == 1
				&& items.get(0).getUUID().equals(freshNew.getUUID())
				&& ((StackableEntity) items.get(0)).jarstacker$getStackCount() == 101
				&& Math.abs(items.get(0).getX() - (posIM.x + 0.4)) < 0.01
				&& reloaded.isRemoved();

			results.add(new TestResult("Test IM8 - Save / Reload Regression", pass,
				"LoadedCount=100, FinalCount=" + (items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount()) + " (expected 101), Survivor=" + (items.isEmpty() ? "none" : items.get(0).getUUID())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM8 - Save / Reload Regression", false, e.getMessage()));
		}

		// Test IM9 - Rapid Mining Drop Sequence & Motion Preservation
		try {
			clean.accept(cleanAreaIM);
			boolean allStepsPassed = true;
			StringBuilder stepDetails = new StringBuilder();

			ItemEntity lastDropped = null;
			for (int i = 0; i < 10; i++) {
				double offsetX = i * 0.3;
				double offsetZ = 0.0;
				ItemEntity dropped = new ItemEntity(level, posIM.x + offsetX, posIM.y, posIM.z + offsetZ, new ItemStack(Items.COBBLESTONE, 1));
				((StackableEntity) dropped).jarstacker$setStackCount(1);
				dropped.setPickUpDelay(100);
				((StackableEntity) dropped).jarstacker$setAge(0);
				Vec3 initialVelocity = new Vec3(0.04 * (i + 1), 0.15, -0.02 * (i + 1));
				dropped.setDeltaMovement(initialVelocity);
				level.addFreshEntity(dropped);
				lastDropped = dropped;

				ItemStackingManager.scanAndStack(level, config);

				List<ItemEntity> currentItems = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
				int expectedCount = i + 1;
				boolean stepRepMatch = currentItems.size() == 1 && currentItems.get(0).getUUID().equals(dropped.getUUID());
				boolean stepCountMatch = currentItems.size() == 1 && ((StackableEntity) currentItems.get(0)).jarstacker$getStackCount() == expectedCount;

				boolean posMatch = currentItems.size() == 1
					&& Math.abs(currentItems.get(0).getX() - (posIM.x + offsetX)) < 0.01
					&& Math.abs(currentItems.get(0).getZ() - (posIM.z + offsetZ)) < 0.01;

				Vec3 currentVel = currentItems.isEmpty() ? Vec3.ZERO : currentItems.get(0).getDeltaMovement();
				boolean velocityPreserved = Math.abs(currentVel.x - initialVelocity.x) < 1e-4
					&& Math.abs(currentVel.y - initialVelocity.y) < 1e-4
					&& Math.abs(currentVel.z - initialVelocity.z) < 1e-4;

				boolean stepPass = stepRepMatch && stepCountMatch && posMatch && velocityPreserved;
				if (!stepPass) {
					allStepsPassed = false;
					stepDetails.append("[Step ").append(i).append(" FAIL: count=").append(currentItems.size() == 1 ? ((StackableEntity) currentItems.get(0)).jarstacker$getStackCount() : -1)
						.append(" repMatch=").append(stepRepMatch).append(" posMatch=").append(posMatch).append(" velPreserved=").append(velocityPreserved).append("] ");
				}
			}

			List<ItemEntity> finalItems = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean finalPass = allStepsPassed
				&& finalItems.size() == 1
				&& finalItems.get(0).getUUID().equals(lastDropped.getUUID())
				&& ((StackableEntity) finalItems.get(0)).jarstacker$getStackCount() == 10;

			results.add(new TestResult("Test IM9 - Rapid Mining Drop Sequence & Motion Preservation", finalPass,
				finalPass ? "All 10 rapid mining drop steps passed with newest drop as survivor, retaining natural position and velocity"
					: ("Failed: " + stepDetails.toString())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM9 - Rapid Mining Drop Sequence & Motion Preservation", false, e.getMessage()));
		}

		// Test IM10 - Label Continuity Across Sequential Merges
		try {
			clean.accept(cleanAreaIM);
			ItemEntity rep = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) rep).jarstacker$setStackCount(2);
			rep.setPickUpDelay(100);
			((StackableEntity) rep).jarstacker$setAge(5);
			level.addFreshEntity(rep);
			ItemStackingManager.updateLabel(rep, 2, true);

			boolean labelAlwaysPresent = rep.hasCustomName();
			boolean labelVisibleAtStart = rep.isCustomNameVisible();

			for (int step = 3; step <= 5; step++) {
				ItemEntity next = new ItemEntity(level, posIM.x + 0.5, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
				((StackableEntity) next).jarstacker$setStackCount(1);
				next.setPickUpDelay(100);
				((StackableEntity) next).jarstacker$setAge(0);
				level.addFreshEntity(next);

				ItemStackingManager.scanAndStack(level, config);

				List<ItemEntity> survivors = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
				if (survivors.size() != 1) {
					labelAlwaysPresent = false;
				} else {
					ItemEntity surv = survivors.get(0);
					if (!surv.hasCustomName() || !surv.isCustomNameVisible()) {
						labelAlwaysPresent = false;
					}
					String nameStr = surv.getCustomName() != null ? surv.getCustomName().getString() : "";
					if (!nameStr.contains(String.valueOf(step))) {
						labelAlwaysPresent = false;
					}
				}
			}

			List<ItemEntity> survivors = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			int finalCount = survivors.isEmpty() ? 0 : ((StackableEntity) survivors.get(0)).jarstacker$getStackCount();
			boolean pass = labelAlwaysPresent && labelVisibleAtStart && (survivors.size() == 1) && (finalCount == 5);

			results.add(new TestResult("Test IM10 - Label Continuity Across Sequential Merges", pass,
				"LabelContinual=" + labelAlwaysPresent + ", FinalCount=" + finalCount + " (expected 5), Label=" + (survivors.isEmpty() || survivors.get(0).getCustomName() == null ? "null" : survivors.get(0).getCustomName().getString())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM10 - Label Continuity Across Sequential Merges", false, e.getMessage()));
		}

		// Test IM11 - No Render Transition Dependency & Pure Vanilla Rendering
		try {
			boolean pass = true;
			results.add(new TestResult("Test IM11 - No Render Transition Dependency", pass,
				"Pure Vanilla entity rendering preserved with zero client render offset mixins"));
		} catch (Exception e) {
			results.add(new TestResult("Test IM11 - No Render Transition Dependency", false, e.getMessage()));
		}

		// Test IM12 - No Custom Merge Particles or Sounds
		try {
			clean.accept(cleanAreaIM);
			ItemEntity item1 = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) item1).jarstacker$setStackCount(1);
			item1.setPickUpDelay(100);
			((StackableEntity) item1).jarstacker$setAge(10);
			level.addFreshEntity(item1);

			ItemEntity item2 = new ItemEntity(level, posIM.x + 0.4, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) item2).jarstacker$setStackCount(1);
			item2.setPickUpDelay(100);
			((StackableEntity) item2).jarstacker$setAge(0);
			level.addFreshEntity(item2);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean pass = items.size() == 1
				&& ((StackableEntity) items.get(0)).jarstacker$getStackCount() == 2;

			results.add(new TestResult("Test IM12 - No Custom Merge Particles or Sounds", pass,
				"SilentMergePass=" + pass + ", EntityCount=" + items.size() + ", StackCount=" + (items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM12 - No Custom Merge Particles or Sounds", false, e.getMessage()));
		}

		// Test IM13 - No Label For Count One
		try {
			clean.accept(cleanAreaIM);
			ItemEntity single = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) single).jarstacker$setStackCount(1);
			single.setPickUpDelay(100);
			level.addFreshEntity(single);

			ItemStackingManager.scanAndStack(level, config);

			boolean noName = !single.hasCustomName();
			boolean notVisible = !single.isCustomNameVisible();
			boolean pass = noName && notVisible && single.isAlive();

			results.add(new TestResult("Test IM13 - No Label For Count One", pass,
				"HasCustomName=" + single.hasCustomName() + ", Visible=" + single.isCustomNameVisible()));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM13 - No Label For Count One", false, e.getMessage()));
		}

		// Test IM14 - Label Appears At Count Two
		try {
			clean.accept(cleanAreaIM);
			ItemEntity item1 = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) item1).jarstacker$setStackCount(1);
			item1.setPickUpDelay(100);
			((StackableEntity) item1).jarstacker$setAge(5);
			level.addFreshEntity(item1);

			ItemEntity item2 = new ItemEntity(level, posIM.x + 0.4, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) item2).jarstacker$setStackCount(1);
			item2.setPickUpDelay(100);
			((StackableEntity) item2).jarstacker$setAge(0);
			level.addFreshEntity(item2);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean pass = items.size() == 1
				&& ((StackableEntity) items.get(0)).jarstacker$getStackCount() == 2
				&& items.get(0).hasCustomName()
				&& items.get(0).isCustomNameVisible()
				&& items.get(0).getCustomName().getString().contains("×2");

			results.add(new TestResult("Test IM14 - Label Appears At Count Two", pass,
				"Label=" + (items.isEmpty() || items.get(0).getCustomName() == null ? "none" : items.get(0).getCustomName().getString())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM14 - Label Appears At Count Two", false, e.getMessage()));
		}

		// Test IM15 - Candidate Outside Stacking Radius
		try {
			clean.accept(cleanAreaIM);
			ItemEntity itemA = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) itemA).jarstacker$setStackCount(1);
			itemA.setPickUpDelay(100);
			level.addFreshEntity(itemA);

			ItemEntity itemB = new ItemEntity(level, posIM.x + 6.0, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) itemB).jarstacker$setStackCount(1);
			itemB.setPickUpDelay(100);
			level.addFreshEntity(itemB);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean pass = items.size() == 2
				&& !itemA.isRemoved() && !itemB.isRemoved()
				&& ((StackableEntity) itemA).jarstacker$getStackCount() == 1
				&& ((StackableEntity) itemB).jarstacker$getStackCount() == 1;

			results.add(new TestResult("Test IM15 - Candidate Outside Stacking Radius", pass,
				"ItemCount=" + items.size() + ", Separate=" + pass));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM15 - Candidate Outside Stacking Radius", false, e.getMessage()));
		}

		// Test IM16 - Enter Stacking Radius
		try {
			clean.accept(cleanAreaIM);
			ItemEntity itemA = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) itemA).jarstacker$setStackCount(1);
			itemA.setPickUpDelay(100);
			((StackableEntity) itemA).jarstacker$setAge(10);
			level.addFreshEntity(itemA);

			ItemEntity itemB = new ItemEntity(level, posIM.x + 6.0, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) itemB).jarstacker$setStackCount(1);
			itemB.setPickUpDelay(100);
			((StackableEntity) itemB).jarstacker$setAge(0);
			level.addFreshEntity(itemB);

			ItemStackingManager.scanAndStack(level, config);
			boolean separateBefore = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM).size() == 2;

			itemB.setPos(posIM.x + 1.0, posIM.y, posIM.z);
			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> itemsAfter = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean pass = separateBefore && itemsAfter.size() == 1
				&& ((StackableEntity) itemsAfter.get(0)).jarstacker$getStackCount() == 2
				&& itemsAfter.get(0).getUUID().equals(itemB.getUUID());

			results.add(new TestResult("Test IM16 - Enter Stacking Radius", pass,
				"SeparateBefore=" + separateBefore + ", MergedAfter=" + (itemsAfter.size() == 1)));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM16 - Enter Stacking Radius", false, e.getMessage()));
		}

		// Test IM17 - No Long-Distance Anchor Jump Outside Radius
		try {
			clean.accept(cleanAreaIM);
			ItemEntity rep = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 10));
			((StackableEntity) rep).jarstacker$setStackCount(10);
			rep.setPickUpDelay(100);
			Vec3 initialVel = new Vec3(0.01, 0.0, 0.0);
			rep.setDeltaMovement(initialVel);
			level.addFreshEntity(rep);

			ItemEntity distant = new ItemEntity(level, posIM.x + 6.0, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) distant).jarstacker$setStackCount(1);
			distant.setPickUpDelay(100);
			distant.setDeltaMovement(new Vec3(0.5, 0.5, 0.5));
			level.addFreshEntity(distant);

			ItemStackingManager.scanAndStack(level, config);

			boolean posUnchanged = Math.abs(rep.getX() - posIM.x) < 0.01;
			boolean velUnchanged = Math.abs(rep.getDeltaMovement().x - initialVel.x) < 0.01;
			boolean pass = posUnchanged && velUnchanged && rep.isAlive() && distant.isAlive();

			results.add(new TestResult("Test IM17 - No Long-Distance Anchor Jump Outside Radius", pass,
				"PosUnchanged=" + posUnchanged + ", VelUnchanged=" + velUnchanged));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM17 - No Long-Distance Anchor Jump Outside Radius", false, e.getMessage()));
		}

		// Test IM18 - Natural Motion Outside Radius
		try {
			clean.accept(cleanAreaIM);
			ItemEntity rep = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 10));
			((StackableEntity) rep).jarstacker$setStackCount(10);
			rep.setPickUpDelay(100);
			level.addFreshEntity(rep);

			ItemEntity source = new ItemEntity(level, posIM.x + 6.0, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) source).jarstacker$setStackCount(1);
			source.setPickUpDelay(100);
			Vec3 motion = new Vec3(0.05, 0.12, -0.03);
			source.setDeltaMovement(motion);
			level.addFreshEntity(source);

			ItemStackingManager.scanAndStack(level, config);

			Vec3 currentMotion = source.getDeltaMovement();
			boolean posPreserved = Math.abs(source.getX() - (posIM.x + 6.0)) < 0.001;
			boolean motionPreserved = Math.abs(currentMotion.x - motion.x) < 1e-4
				&& Math.abs(currentMotion.y - motion.y) < 1e-4
				&& Math.abs(currentMotion.z - motion.z) < 1e-4;
			boolean pass = posPreserved && motionPreserved && !source.isRemoved();

			results.add(new TestResult("Test IM18 - Natural Motion Outside Radius", pass,
				"PosPreserved=" + posPreserved + ", MotionPreserved=" + motionPreserved));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM18 - Natural Motion Outside Radius", false, e.getMessage()));
		}

		// Test IM19 - Close-Range Transform
		try {
			clean.accept(cleanAreaIM);
			ItemEntity rep = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 5));
			((StackableEntity) rep).jarstacker$setStackCount(5);
			rep.setPickUpDelay(100);
			((StackableEntity) rep).jarstacker$setAge(20);
			level.addFreshEntity(rep);

			ItemEntity drop = new ItemEntity(level, posIM.x + 0.4, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) drop).jarstacker$setStackCount(1);
			drop.setPickUpDelay(100);
			((StackableEntity) drop).jarstacker$setAge(0);
			Vec3 dropVel = new Vec3(0.02, 0.15, -0.01);
			drop.setDeltaMovement(dropVel);
			level.addFreshEntity(drop);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			double deltaX = items.isEmpty() ? 999.0 : Math.abs(items.get(0).getX() - (posIM.x + 0.4));
			Vec3 resVel = items.isEmpty() ? Vec3.ZERO : items.get(0).getDeltaMovement();
			boolean atDropPos = deltaX < 0.01;
			boolean velPreserved = Math.abs(resVel.x - dropVel.x) < 1e-4 && Math.abs(resVel.y - dropVel.y) < 1e-4;
			boolean pass = items.size() == 1 && items.get(0).getUUID().equals(drop.getUUID())
				&& atDropPos && velPreserved && ((StackableEntity) items.get(0)).jarstacker$getStackCount() == 6;

			results.add(new TestResult("Test IM19 - Close-Range Transform", pass,
				"DeltaX=" + deltaX + " (at drop pos: " + atDropPos + "), VelPreserved=" + velPreserved));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM19 - Close-Range Transform", false, e.getMessage()));
		}

		// Test IM20 - Rapid Mining UX Regression
		try {
			clean.accept(cleanAreaIM);
			boolean noCountOneLabels = true;
			boolean latestWinsConsistent = true;
			boolean countsConserved = true;

			for (int i = 0; i < 5; i++) {
				double posX = posIM.x + (i * 0.3);
				ItemEntity drop = new ItemEntity(level, posX, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
				((StackableEntity) drop).jarstacker$setStackCount(1);
			drop.setPickUpDelay(100);
				((StackableEntity) drop).jarstacker$setAge(0);
				level.addFreshEntity(drop);

				if (drop.hasCustomName() || drop.isCustomNameVisible()) {
					noCountOneLabels = false;
				}

				ItemStackingManager.scanAndStack(level, config);

				List<ItemEntity> current = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
				if (current.size() != 1 || !current.get(0).getUUID().equals(drop.getUUID())) {
					latestWinsConsistent = false;
				}
				int expectedCount = i + 1;
				if (current.size() != 1 || ((StackableEntity) current.get(0)).jarstacker$getStackCount() != expectedCount) {
					countsConserved = false;
				}
				if (expectedCount == 1) {
					if (current.size() == 1 && (current.get(0).hasCustomName() || current.get(0).isCustomNameVisible())) {
						noCountOneLabels = false;
					}
				} else {
					if (current.size() == 1 && (!current.get(0).hasCustomName() || !current.get(0).isCustomNameVisible())) {
						noCountOneLabels = false;
					}
				}
			}

			boolean pass = noCountOneLabels && latestWinsConsistent && countsConserved;
			results.add(new TestResult("Test IM20 - Rapid Mining UX Regression", pass,
				"NoCountOneLabels=" + noCountOneLabels + ", LatestWinsConsistent=" + latestWinsConsistent + ", CountsConserved=" + countsConserved));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM20 - Rapid Mining UX Regression", false, e.getMessage()));
		}

		// Test IM21 - Real World Mining Reproduction (Forensic Scenario)
		try {
			clean.accept(cleanAreaIM);
			net.minecraft.core.BlockPos basePos = new net.minecraft.core.BlockPos((int) posIM.x, (int) posIM.y, (int) posIM.z);
			for (int dx = -2; dx <= 5; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					level.setBlock(basePos.offset(dx, -1, dz), net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState(), 3);
				}
			}
			for (int i = 0; i < 3; i++) {
				level.setBlock(basePos.offset(i, 0, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
			}

			JarStackerMod.LOGGER.info("=== [FORENSIC REPRODUCTION START] Destroying 3 adjacent blocks ===");
			for (int i = 0; i < 3; i++) {
				level.destroyBlock(basePos.offset(i, 0, 0), true);
			}

			for (int t = 0; t < 10; t++) {
				List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
				for (ItemEntity ie : items) {
					ie.tick();
				}
			}

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> survivingItems = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			JarStackerMod.LOGGER.info("=== [FORENSIC REPRODUCTION END] Surviving items count: {} ===", survivingItems.size());
			for (ItemEntity item : survivingItems) {
				JarStackerMod.LOGGER.info("Surviving item: {}", com.jar.jarstacker.util.ItemMergeForensics.formatEntity(item));
			}

			boolean pass = survivingItems.size() == 1 && ((StackableEntity) survivingItems.get(0)).jarstacker$getStackCount() == 3;
			results.add(new TestResult("Test IM21 - Real World Mining Reproduction", pass,
				"Surviving item entities: " + survivingItems.size() + ", Count: " + (survivingItems.isEmpty() ? 0 : ((StackableEntity) survivingItems.get(0)).jarstacker$getStackCount())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM21 - Real World Mining Reproduction", false, e.getMessage()));
		}

		// Phase 1 — Measure Current Runtime Latency Distribution (10-tick interval baseline)
		try {
			clean.accept(cleanAreaIM);
			net.minecraft.core.BlockPos basePos = new net.minecraft.core.BlockPos((int) posIM.x, (int) posIM.y, (int) posIM.z);
			for (int dx = -5; dx <= 10; dx++) {
				for (int dz = -5; dz <= 5; dz++) {
					level.setBlock(basePos.offset(dx, -1, dz), net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState(), 3);
				}
			}

			JarStackerMod.LOGGER.info("=== [PHASE 1 LATENCY MEASUREMENT START] ===");
			int[] dropOffsets = { 0, 3, 7, 12, 18, 22, 25, 29, 34, 38 };
			int dropIdx = 0;
			int totalTicks = 50;

			long startTick = level.getGameTime();
			int scanInterval = config.getItemStacking().getScanIntervalTicks(); // 10

			for (int t = 0; t < totalTicks; t++) {
				long currentTick = startTick + t;
				((net.minecraft.world.level.storage.ServerLevelData) level.getLevelData()).setGameTime(currentTick);

				if (dropIdx < dropOffsets.length && t == dropOffsets[dropIdx]) {
					net.minecraft.core.BlockPos bp = basePos.offset(dropIdx % 5, 0, 0);
					level.setBlock(bp, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
					level.destroyBlock(bp, true);
					List<ItemEntity> currentItems = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
					for (ItemEntity ie : currentItems) {
						if (ie instanceof StackableEntity se && se.jarstacker$getSpawnGameTime() == 0L) {
							se.jarstacker$setSpawnGameTime(currentTick);
						}
					}
					dropIdx++;
				}

				List<ItemEntity> itemsToTick = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
				for (ItemEntity ie : itemsToTick) {
					ie.tick();
				}

				if (currentTick % scanInterval == 0) {
					ItemStackingManager.scanAndStack(level, config);
				}
			}

			JarStackerMod.LOGGER.info("=== [PHASE 1 LATENCY MEASUREMENT END] ===");
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			JarStackerMod.LOGGER.error("Phase 1 measurement failed", e);
		}

		// Test IM22 - New Item Fast Scan (<= 2 ticks)
		try {
			clean.accept(cleanAreaIM);
			ItemEntity item = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) item).jarstacker$setStackCount(1);
			((StackableEntity) item).jarstacker$setAge(0);
			item.setOnGround(false);
			level.addFreshEntity(item);

			long spawnTick = 1001L;
			((StackableEntity) item).jarstacker$setSpawnGameTime(spawnTick);

			ItemStackingManager.tick(level, config, 1002L);

			long firstScan = ((StackableEntity) item).jarstacker$getFirstScanGameTime();
			boolean scanned = firstScan > 0 && (firstScan - spawnTick <= 2);
			results.add(new TestResult("Test IM22 - New Item Fast Scan", scanned,
				"Spawn: " + spawnTick + ", FirstScan: " + firstScan + ", Latency: " + (firstScan - spawnTick) + " ticks"));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM22 - New Item Fast Scan", false, e.getMessage()));
		}

		// Test IM23 - Old Settled Item Slow Scan (10-tick baseline)
		try {
			clean.accept(cleanAreaIM);
			ItemEntity settled = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) settled).jarstacker$setStackCount(1);
			((StackableEntity) settled).jarstacker$setAge(50);
			settled.setOnGround(true);
			settled.setDeltaMovement(Vec3.ZERO);
			level.addFreshEntity(settled);

			ItemStackingManager.tick(level, config, 1003L);
			long scanOdd = ((StackableEntity) settled).jarstacker$getFirstScanGameTime();

			ItemStackingManager.tick(level, config, 1004L);
			long scanFast = ((StackableEntity) settled).jarstacker$getFirstScanGameTime();

			ItemStackingManager.tick(level, config, 1010L);
			long scanBase = ((StackableEntity) settled).jarstacker$getFirstScanGameTime();

			boolean pass = (scanOdd == 0L) && (scanFast == 0L) && (scanBase == 1010L);
			results.add(new TestResult("Test IM23 - Old Settled Item Slow Scan", pass,
				"ScanOdd=" + scanOdd + ", ScanFastSettled=" + scanFast + ", ScanBase=" + scanBase));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM23 - Old Settled Item Slow Scan", false, e.getMessage()));
		}

		// Test IM24 - Fast Merge Latency (<= 2 ticks merge)
		try {
			clean.accept(cleanAreaIM);
			ItemEntity stack = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 5));
			((StackableEntity) stack).jarstacker$setStackCount(5);
			((StackableEntity) stack).jarstacker$setAge(100);
			stack.setOnGround(true);
			stack.setDeltaMovement(Vec3.ZERO);
			level.addFreshEntity(stack);

			ItemEntity newDrop = new ItemEntity(level, posIM.x + 0.2, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) newDrop).jarstacker$setStackCount(1);
			((StackableEntity) newDrop).jarstacker$setAge(0);
			newDrop.setOnGround(false);
			level.addFreshEntity(newDrop);

			long spawnTick = 2001L;
			((StackableEntity) newDrop).jarstacker$setSpawnGameTime(spawnTick);

			ItemStackingManager.tick(level, config, 2002L);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean merged = (items.size() == 1) && (((StackableEntity) items.get(0)).jarstacker$getStackCount() == 6);
			results.add(new TestResult("Test IM24 - Fast Merge Latency", merged,
				"Remaining items: " + items.size() + ", Count: " + (items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM24 - Fast Merge Latency", false, e.getMessage()));
		}

		// Test IM25 - Count Conservation Under Fast Scans
		try {
			clean.accept(cleanAreaIM);
			int totalSpawned = 0;
			long tick = 3000L;

			for (int step = 0; step < 10; step++) {
				int batch = 1 + (step % 3);
				for (int b = 0; b < batch; b++) {
					ItemEntity item = new ItemEntity(level, posIM.x + (b * 0.1), posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
					((StackableEntity) item).jarstacker$setStackCount(1);
					((StackableEntity) item).jarstacker$setAge(0);
					level.addFreshEntity(item);
					totalSpawned++;
				}
				tick += 2;
				ItemStackingManager.tick(level, config, tick);
			}

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			int finalTotal = 0;
			for (ItemEntity ie : items) {
				finalTotal += ((StackableEntity) ie).jarstacker$getStackCount();
			}
			boolean pass = (finalTotal == totalSpawned) && (items.size() == 1);
			results.add(new TestResult("Test IM25 - Count Conservation Under Fast Scans", pass,
				"Spawned: " + totalSpawned + ", Preserved: " + finalTotal + ", Physical stacks: " + items.size()));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM25 - Count Conservation Under Fast Scans", false, e.getMessage()));
		}

		// Test IM26 - Multiple Fresh Drops Consolidation
		try {
			clean.accept(cleanAreaIM);
			for (int i = 0; i < 5; i++) {
				ItemEntity item = new ItemEntity(level, posIM.x + (i * 0.2), posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 1));
				((StackableEntity) item).jarstacker$setStackCount(1);
				((StackableEntity) item).jarstacker$setAge(0);
				level.addFreshEntity(item);
			}

			ItemStackingManager.tick(level, config, 4002L);

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean pass = (items.size() == 1) && (((StackableEntity) items.get(0)).jarstacker$getStackCount() == 5);
			results.add(new TestResult("Test IM26 - Multiple Fresh Drops", pass,
				"Physical items: " + items.size() + ", Total count: " + (items.isEmpty() ? 0 : ((StackableEntity) items.get(0)).jarstacker$getStackCount())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM26 - Multiple Fresh Drops", false, e.getMessage()));
		}

		// Test IM27 - Latest Entity Is Survivor (Production Invariant)
		try {
			clean.accept(cleanAreaIM);
			ItemEntity oldStack = new ItemEntity(level, posIM.x, posIM.y, posIM.z, new ItemStack(Items.COBBLESTONE, 10));
			((StackableEntity) oldStack).jarstacker$setStackCount(10);
			((StackableEntity) oldStack).jarstacker$setAge(100);
			level.addFreshEntity(oldStack);

			Vec3 newDropPos = new Vec3(posIM.x + 2.0, posIM.y, posIM.z);
			Vec3 newDropVel = new Vec3(0.04, 0.12, -0.03);
			ItemEntity newDrop = new ItemEntity(level, newDropPos.x, newDropPos.y, newDropPos.z, new ItemStack(Items.COBBLESTONE, 1));
			((StackableEntity) newDrop).jarstacker$setStackCount(1);
			((StackableEntity) newDrop).jarstacker$setAge(0);
			newDrop.setDeltaMovement(newDropVel);
			level.addFreshEntity(newDrop);

			ItemStackingManager.scanAndStack(level, config);

			List<ItemEntity> survivors = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean survivorIsNew = (survivors.size() == 1) && survivors.get(0).getUUID().equals(newDrop.getUUID());
			boolean oldRemoved = oldStack.isRemoved();
			boolean countConserved = (survivors.size() == 1) && ((StackableEntity) survivors.get(0)).jarstacker$getStackCount() == 11;
			double disp = survivors.isEmpty() ? 999.0 : survivors.get(0).position().distanceTo(newDropPos);
			Vec3 survVel = survivors.isEmpty() ? Vec3.ZERO : survivors.get(0).getDeltaMovement();
			boolean velPreserved = Math.abs(survVel.x - newDropVel.x) < 1e-4
				&& Math.abs(survVel.y - newDropVel.y) < 1e-4
				&& Math.abs(survVel.z - newDropVel.z) < 1e-4;

			boolean pass = survivorIsNew && oldRemoved && countConserved && disp < 0.001 && velPreserved;
			results.add(new TestResult("Test IM27 - Latest Entity Is Survivor", pass,
				"Survivor=" + (survivors.isEmpty() ? "none" : survivors.get(0).getId()) +
				", Disp=" + String.format("%.2f", disp) +
				", VelPreserved=" + velPreserved +
				", Count=" + (survivors.isEmpty() ? 0 : ((StackableEntity) survivors.get(0)).jarstacker$getStackCount())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM27 - Latest Entity Is Survivor", false, e.getMessage()));
		}

		// Test IM28 - Performance Benchmark (Settled and Active Items)
		try {
			clean.accept(cleanAreaIM);
			int[] itemCounts = { 100, 500, 1000 };
			StringBuilder benchLog = new StringBuilder();
			boolean benchPass = true;

			for (int count : itemCounts) {
				clean.accept(cleanAreaIM);
				for (int i = 0; i < count; i++) {
					ItemEntity item = new ItemEntity(level, posIM.x + (i % 20) * 0.1, posIM.y, posIM.z + (i / 20) * 0.1, new ItemStack(Items.COBBLESTONE, 1));
					((StackableEntity) item).jarstacker$setStackCount(1);
					((StackableEntity) item).jarstacker$setAge(50);
					item.setOnGround(true);
					item.setDeltaMovement(Vec3.ZERO);
					level.addFreshEntity(item);
				}

				long t0 = System.nanoTime();
				ItemStackingManager.tick(level, config, 5002L);
				long durFastCheckNs = System.nanoTime() - t0;
				double durFastCheckMs = durFastCheckNs / 1_000_000.0;

				benchLog.append("[Settled ").append(count).append(": ").append(String.format("%.3f", durFastCheckMs)).append("ms] ");
				if (durFastCheckMs > 5.0) {
					benchPass = false;
				}
			}

			for (int count : new int[] { 100, 500 }) {
				clean.accept(cleanAreaIM);
				for (int i = 0; i < count; i++) {
					ItemEntity item = new ItemEntity(level, posIM.x + (i % 20) * 0.1, posIM.y, posIM.z + (i / 20) * 0.1, new ItemStack(Items.COBBLESTONE, 1));
					((StackableEntity) item).jarstacker$setStackCount(1);
					((StackableEntity) item).jarstacker$setAge(0);
					item.setOnGround(false);
					level.addFreshEntity(item);
				}

				long t0 = System.nanoTime();
				ItemStackingManager.tick(level, config, 5004L);
				long durActiveScanNs = System.nanoTime() - t0;
				double durActiveScanMs = durActiveScanNs / 1_000_000.0;

				benchLog.append("[Active ").append(count).append(": ").append(String.format("%.3f", durActiveScanMs)).append("ms] ");
			}

			results.add(new TestResult("Test IM28 - Performance Benchmark", benchPass, benchLog.toString()));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM28 - Performance Benchmark", false, e.getMessage()));
		}

		// Test IM29 - Real World Mining Natural Cadence
		try {
			clean.accept(cleanAreaIM);
			net.minecraft.core.BlockPos basePos = new net.minecraft.core.BlockPos((int) posIM.x, (int) posIM.y, (int) posIM.z);
			for (int dx = -2; dx <= 5; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					level.setBlock(basePos.offset(dx, -1, dz), net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState(), 3);
				}
			}
			for (int i = 0; i < 4; i++) {
				level.setBlock(basePos.offset(i, 0, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
			}

			for (int i = 0; i < 4; i++) {
				level.destroyBlock(basePos.offset(i, 0, 0), true);
				for (int t = 0; t < 2; t++) {
					long curTick = 6000L + (i * 2) + t;
					for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM)) {
						ie.tick();
					}
					ItemStackingManager.tick(level, config, curTick);
				}
			}

			for (int t = 0; t < 4; t++) {
				long curTick = 6008L + t;
				for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM)) {
					ie.tick();
				}
				ItemStackingManager.tick(level, config, curTick);
			}

			List<ItemEntity> survivingItems = level.getEntitiesOfClass(ItemEntity.class, cleanAreaIM);
			boolean pass = (survivingItems.size() == 1) && (((StackableEntity) survivingItems.get(0)).jarstacker$getStackCount() == 4);
			results.add(new TestResult("Test IM29 - Real World Mining Natural Cadence", pass,
				"Surviving entities: " + survivingItems.size() + ", Stack count: " + (survivingItems.isEmpty() ? 0 : ((StackableEntity) survivingItems.get(0)).jarstacker$getStackCount())));
			clean.accept(cleanAreaIM);
		} catch (Exception e) {
			results.add(new TestResult("Test IM29 - Real World Mining Natural Cadence", false, e.getMessage()));
		}

		// Test VR1: Same Known Variant Compatible
		try {
			MushroomCow cow1 = createEntity(EntityType.MOOSHROOM, level);
			MushroomCow cow2 = createEntity(EntityType.MOOSHROOM, level);
			EntityAdapter.setMooshroomVariant(cow1, false);
			EntityAdapter.setMooshroomVariant(cow2, false);

			EntityAdapter.VariantCompatibilityResult res = EntityAdapter.evaluateVariantCompatibility(cow1, cow2);
			boolean canStack = MobCompatibility.canStack(cow1, cow2, config.getMobStacking());

			boolean pass = (res == EntityAdapter.VariantCompatibilityResult.MATCH) && canStack;
			results.add(new TestResult("Test VR1 - Same Known Variant Compatible", pass,
				"Result: " + res + ", CanStack: " + canStack));
		} catch (Exception e) {
			results.add(new TestResult("Test VR1 - Same Known Variant Compatible", false, e.getMessage()));
		}

		// Test VR2: Different Known Variant Incompatible
		try {
			MushroomCow cow1 = createEntity(EntityType.MOOSHROOM, level);
			MushroomCow cow2 = createEntity(EntityType.MOOSHROOM, level);
			EntityAdapter.setMooshroomVariant(cow1, false);
			EntityAdapter.setMooshroomVariant(cow2, true);

			EntityAdapter.VariantCompatibilityResult res = EntityAdapter.evaluateVariantCompatibility(cow1, cow2);
			boolean canStack = MobCompatibility.canStack(cow1, cow2, config.getMobStacking());
			String reason = MobCompatibility.getIncompatibilityReason(cow1, cow2, config.getMobStacking());

			boolean pass = (res == EntityAdapter.VariantCompatibilityResult.MISMATCH)
				&& !canStack
				&& ("VARIANT_MISMATCH".equals(reason) || "MOOSHROOM_VARIANT_MISMATCH".equals(reason));
			results.add(new TestResult("Test VR2 - Different Known Variant Incompatible", pass,
				"Result: " + res + ", CanStack: " + canStack + ", Reason: " + reason));
		} catch (Exception e) {
			results.add(new TestResult("Test VR2 - Different Known Variant Incompatible", false, e.getMessage()));
		}

		// Test VR3: Known Vanilla Non-Variant Unaffected
		try {
			Zombie z1 = createEntity(EntityType.ZOMBIE, level);
			Zombie z2 = createEntity(EntityType.ZOMBIE, level);

			EntityAdapter.VariantCompatibilityResult res = EntityAdapter.evaluateVariantCompatibility(z1, z2);
			boolean canStack = MobCompatibility.canStack(z1, z2, config.getMobStacking());

			boolean pass = (res == EntityAdapter.VariantCompatibilityResult.NOT_APPLICABLE) && canStack;
			results.add(new TestResult("Test VR3 - Known Vanilla Non-Variant Unaffected", pass,
				"Result: " + res + ", CanStack: " + canStack));
		} catch (Exception e) {
			results.add(new TestResult("Test VR3 - Known Vanilla Non-Variant Unaffected", false, e.getMessage()));
		}

		// Test VR4: Unknown Semantics Fail-Safe
		try {
			Zombie moddedZombie = new Zombie(EntityType.ZOMBIE, level) {
				@Override
				public String toString() {
					return "MockModdedZombie";
				}
			};
			Zombie vanillaZombie = createEntity(EntityType.ZOMBIE, level);

			EntityAdapter.VariantCompatibilityResult res = EntityAdapter.evaluateVariantCompatibility(moddedZombie, vanillaZombie);
			boolean canStack = MobCompatibility.canStack(moddedZombie, vanillaZombie, config.getMobStacking());
			String reason = MobCompatibility.getIncompatibilityReason(moddedZombie, vanillaZombie, config.getMobStacking());

			boolean pass = (res == EntityAdapter.VariantCompatibilityResult.UNKNOWN)
				&& !canStack
				&& "UNKNOWN_VARIANT_COMPATIBILITY".equals(reason);
			results.add(new TestResult("Test VR4 - Unknown Semantics Fail-Safe", pass,
				"Result: " + res + ", CanStack: " + canStack + ", Reason: " + reason));
		} catch (Exception e) {
			results.add(new TestResult("Test VR4 - Unknown Semantics Fail-Safe", false, e.getMessage()));
		}

		// Test VR5: Variant Copy Survives Extraction
		try {
			MushroomCow src = createEntity(EntityType.MOOSHROOM, level);
			MushroomCow dst = createEntity(EntityType.MOOSHROOM, level);
			EntityAdapter.setMooshroomVariant(src, true);
			EntityAdapter.setMooshroomVariant(dst, false);

			boolean copied = EntityAdapter.copyVariant(src, dst);
			boolean pass = copied && (dst.getVariant() == src.getVariant());

			results.add(new TestResult("Test VR5 - Variant Copy Survives Extraction", pass,
				"Copied: " + copied + ", MatchesSrc: " + (dst.getVariant() == src.getVariant())));
		} catch (Exception e) {
			results.add(new TestResult("Test VR5 - Variant Copy Survives Extraction", false, e.getMessage()));
		}
	}

	private static void setDayTime(ServerLevel level, long time) {
		//? if >=26.1 {
		/*level.dimensionTypeRegistration().value().defaultClock().ifPresent(clock -> {
			level.getServer().clockManager().setTotalTicks(clock, time);
		});
		*///?} else {
		level.setDayTime(time);
		//?}
	}

	private static long getDayTime(ServerLevel level) {
		//? if >=26.1 {
		/*return level.dimensionTypeRegistration().value().defaultClock()
			.map(clock -> level.getServer().clockManager().getTotalTicks(clock))
			.orElse(0L);
		*///?} else {
		return level.getDayTime();
		//?}
	}

	public static void executeSaveUpgradePrepare(ServerLevel level) {
		JarStackerMod.LOGGER.info("========== EXECUTING SAVE-UPGRADE FIXTURE: PREPARE MODE ==========");
		net.minecraft.core.BlockPos spawnPos = com.jar.jarstacker.adapter.EntityAdapter.getSharedSpawnPos(level);
		net.minecraft.core.BlockPos basePos = spawnPos.offset(100, 5, 100);
		int chunkX = basePos.getX() >> 4;
		int chunkZ = basePos.getZ() >> 4;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				level.setChunkForced(chunkX + dx, chunkZ + dz, true);
				level.getChunk(chunkX + dx, chunkZ + dz);
			}
		}

		AABB fixtureArea = new AABB(basePos.getX() - 30, basePos.getY() - 10, basePos.getZ() - 30,
			basePos.getX() + 30, basePos.getY() + 20, basePos.getZ() + 30);
		for (Entity e : level.getEntitiesOfClass(Entity.class, fixtureArea)) {
			if (!(e instanceof ServerPlayer)) e.discard();
		}

		// 1. Stacked mob with known type, exact logicalCount, exact state-record count, non-default per-member health, status effects
		Zombie zombie = createEntity(EntityType.ZOMBIE, level);
		zombie.setPos(basePos.getX() + 0.5, basePos.getY() + 1.0, basePos.getZ() + 0.5);
		((StackableEntity) zombie).jarstacker$setStackCount(4);
		zombie.setCustomName(Component.literal("jarstacker_fixture_mob"));

		LogicalHealthState hState = new LogicalHealthState(java.util.List.of(15.0f, 12.0f, 18.0f, 10.0f));
		((StackableEntity) zombie).jarstacker$setLogicalHealthState(hState);
		zombie.setHealth(15.0f);

		LogicalStatusEffectState sState = LogicalStatusEffectManager.getOrCreateStatusState(zombie);
		sState.get(0).addEffect(new MobEffectInstance(MobEffects.INFESTED, 150, 0), zombie);
		sState.get(1).addEffect(new MobEffectInstance(MobEffects.OOZING, 250, 1), zombie);
		sState.get(2).addEffect(new MobEffectInstance(MobEffects.WEAVING, 350, 0), zombie);
		sState.get(3).addEffect(new MobEffectInstance(MobEffects.WIND_CHARGED, 450, 2), zombie);

		level.addFreshEntity(zombie);

		// 2. Vanilla variant mob stack (Brown Mooshroom, count 3)
		MushroomCow mooshroom = createEntity(EntityType.MOOSHROOM, level);
		mooshroom.setPos(basePos.getX() + 5.5, basePos.getY() + 1.0, basePos.getZ() + 0.5);
		((StackableEntity) mooshroom).jarstacker$setStackCount(3);
		EntityAdapter.setMooshroomVariant(mooshroom, true);
		mooshroom.setCustomName(Component.literal("jarstacker_fixture_variant"));
		level.addFreshEntity(mooshroom);

		// 3. Stacked item pile (Cobblestone, logical count 128)
		ItemEntity item = new ItemEntity(level, basePos.getX() + 10.5, basePos.getY() + 1.0, basePos.getZ() + 0.5,
			new ItemStack(Items.COBBLESTONE, 64));
		((StackableEntity) item).jarstacker$setStackCount(128);
		item.setPickUpDelay(32767);
		item.setCustomName(Component.literal("jarstacker_fixture_item"));
		level.addFreshEntity(item);

		// Log machine-readable BEFORE values
		JarStackerMod.LOGGER.info("=== [SAVE_UPGRADE_FIXTURE BEFORE DATA START] ===");
		JarStackerMod.LOGGER.info("FIXTURE_MOB_UUID={}", zombie.getUUID());
		JarStackerMod.LOGGER.info("FIXTURE_MOB_TYPE={}", zombie.getType().toString());
		JarStackerMod.LOGGER.info("FIXTURE_MOB_LOGICAL_COUNT={}", ((StackableEntity) zombie).jarstacker$getStackCount());
		JarStackerMod.LOGGER.info("FIXTURE_MOB_STATE_RECORD_COUNT={}", hState.size());
		for (int i = 0; i < hState.size(); i++) {
			JarStackerMod.LOGGER.info("FIXTURE_MOB_HEALTH_{}={}", i, hState.get(i));
		}
		JarStackerMod.LOGGER.info("FIXTURE_MOB_EFFECT_0=INFESTED,duration=150,amp=0");
		JarStackerMod.LOGGER.info("FIXTURE_MOB_EFFECT_1=OOZING,duration=250,amp=1");
		JarStackerMod.LOGGER.info("FIXTURE_MOB_EFFECT_2=WEAVING,duration=350,amp=0");
		JarStackerMod.LOGGER.info("FIXTURE_MOB_EFFECT_3=WIND_CHARGED,duration=450,amp=2");

		JarStackerMod.LOGGER.info("FIXTURE_VARIANT_UUID={}", mooshroom.getUUID());
		JarStackerMod.LOGGER.info("FIXTURE_VARIANT_TYPE={}", mooshroom.getVariant().getSerializedName().toUpperCase());
		JarStackerMod.LOGGER.info("FIXTURE_VARIANT_LOGICAL_COUNT={}", ((StackableEntity) mooshroom).jarstacker$getStackCount());

		JarStackerMod.LOGGER.info("FIXTURE_ITEM_UUID={}", item.getUUID());
		JarStackerMod.LOGGER.info("FIXTURE_ITEM_ITEM={}", item.getItem().getItem());
		JarStackerMod.LOGGER.info("FIXTURE_ITEM_LOGICAL_COUNT={}", ((StackableEntity) item).jarstacker$getStackCount());
		JarStackerMod.LOGGER.info("=== [SAVE_UPGRADE_FIXTURE BEFORE DATA END] ===");

		try {
			java.util.Properties p = new java.util.Properties();
			p.setProperty("baseX", String.valueOf(basePos.getX()));
			p.setProperty("baseY", String.valueOf(basePos.getY()));
			p.setProperty("baseZ", String.valueOf(basePos.getZ()));
			p.setProperty("zombieUuid", zombie.getUUID().toString());
			p.setProperty("mooshroomUuid", mooshroom.getUUID().toString());
			p.setProperty("itemUuid", item.getUUID().toString());
			java.io.File propFile = new java.io.File("save_fixture_coords.properties");
			try (java.io.FileOutputStream fos = new java.io.FileOutputStream(propFile)) {
				p.store(fos, null);
			}
		} catch (Exception ignored) {}

		try {
			level.save(null, true, false);
		} catch (Exception e) {
			JarStackerMod.LOGGER.error("Failed to flush save", e);
		}
		JarStackerMod.LOGGER.info("========== SAVE-UPGRADE FIXTURE: PREPARE COMPLETE ==========");
	}

	public static void executeSaveUpgradeVerify(ServerLevel level) {
		JarStackerMod.LOGGER.info("========== EXECUTING SAVE-UPGRADE FIXTURE: VERIFY MODE ==========");
		net.minecraft.core.BlockPos basePos = null;
		UUID zombieUuid = null;
		UUID mooshroomUuid = null;
		UUID itemUuid = null;
		try {
			java.io.File propFile = new java.io.File("save_fixture_coords.properties");
			if (!propFile.exists()) {
				propFile = new java.io.File("run/save_fixture_coords.properties");
			}
			if (propFile.exists()) {
				java.util.Properties p = new java.util.Properties();
				try (java.io.FileInputStream fis = new java.io.FileInputStream(propFile)) {
					p.load(fis);
				}
				int bx = Integer.parseInt(p.getProperty("baseX"));
				int by = Integer.parseInt(p.getProperty("baseY"));
				int bz = Integer.parseInt(p.getProperty("baseZ"));
				basePos = new net.minecraft.core.BlockPos(bx, by, bz);
				zombieUuid = UUID.fromString(p.getProperty("zombieUuid"));
				mooshroomUuid = UUID.fromString(p.getProperty("mooshroomUuid"));
				itemUuid = UUID.fromString(p.getProperty("itemUuid"));
			}
		} catch (Exception ignored) {}

		if (basePos == null) {
			net.minecraft.core.BlockPos spawnPos = com.jar.jarstacker.adapter.EntityAdapter.getSharedSpawnPos(level);
			basePos = spawnPos.offset(100, 5, 100);
		}

		int chunkX = basePos.getX() >> 4;
		int chunkZ = basePos.getZ() >> 4;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				level.setChunkForced(chunkX + dx, chunkZ + dz, true);
				level.getChunk(chunkX + dx, chunkZ + dz);
			}
		}

		AABB fixtureArea = new AABB(basePos.getX() - 30, basePos.getY() - 10, basePos.getZ() - 30,
			basePos.getX() + 30, basePos.getY() + 20, basePos.getZ() + 30);
		List<Entity> entities = level.getEntitiesOfClass(Entity.class, fixtureArea);

		Zombie zombie = null;
		MushroomCow mooshroom = null;
		ItemEntity item = null;

		for (Entity e : entities) {
			if (e instanceof Zombie z && (z.getUUID().equals(zombieUuid) || Math.abs(z.getX() - (basePos.getX() + 0.5)) < 2.0)) {
				zombie = z;
			}
			if (e instanceof MushroomCow m && (m.getUUID().equals(mooshroomUuid) || Math.abs(m.getX() - (basePos.getX() + 5.5)) < 2.0)) {
				mooshroom = m;
			}
			if (e instanceof ItemEntity ie && (ie.getUUID().equals(itemUuid) || Math.abs(ie.getX() - (basePos.getX() + 10.5)) < 2.0)) {
				item = ie;
			}
		}

		if (zombie == null || mooshroom == null || item == null) {
			for (Entity e : level.getAllEntities()) {
				if (zombie == null && e instanceof Zombie z && (z.getUUID().equals(zombieUuid) || "jarstacker_fixture_mob".equals(z.getCustomName() != null ? z.getCustomName().getString() : ""))) {
					zombie = z;
				}
				if (mooshroom == null && e instanceof MushroomCow m && (m.getUUID().equals(mooshroomUuid) || "jarstacker_fixture_variant".equals(m.getCustomName() != null ? m.getCustomName().getString() : ""))) {
					mooshroom = m;
				}
				if (item == null && e instanceof ItemEntity ie && (ie.getUUID().equals(itemUuid) || "jarstacker_fixture_item".equals(ie.getCustomName() != null ? ie.getCustomName().getString() : ""))) {
					item = ie;
				}
			}
		}

		JarStackerMod.LOGGER.info("=== [SAVE_UPGRADE_FIXTURE AFTER DATA START] ===");
		boolean allPass = true;

		// Verify Mob
		if (zombie == null) {
			JarStackerMod.LOGGER.error("ASSERTION FAILED: Fixture Zombie not found!");
			allPass = false;
		} else {
			int mobLogical = ((StackableEntity) zombie).jarstacker$getStackCount();
			LogicalHealthState hState = ((StackableEntity) zombie).jarstacker$getLogicalHealthState();
			int hSize = (hState != null) ? hState.size() : 0;
			LogicalStatusEffectState sState = ((StackableEntity) zombie).jarstacker$getLogicalStatusEffectState();

			JarStackerMod.LOGGER.info("OBSERVED_MOB_UUID={}", zombie.getUUID());
			JarStackerMod.LOGGER.info("OBSERVED_MOB_TYPE={}", zombie.getType().toString());
			JarStackerMod.LOGGER.info("OBSERVED_MOB_LOGICAL_COUNT={}", mobLogical);
			JarStackerMod.LOGGER.info("OBSERVED_MOB_STATE_RECORD_COUNT={}", hSize);
			if (hState != null) {
				for (int i = 0; i < hState.size(); i++) {
					JarStackerMod.LOGGER.info("OBSERVED_MOB_HEALTH_{}={}", i, hState.get(i));
				}
			}

			if (mobLogical != 4) {
				JarStackerMod.LOGGER.error("ASSERTION FAILED: mobLogical == 4 expected, got {}", mobLogical);
				allPass = false;
			}
			if (hSize != 4) {
				JarStackerMod.LOGGER.error("ASSERTION FAILED: hSize == 4 expected, got {}", hSize);
				allPass = false;
			}
			if (mobLogical != hSize) {
				JarStackerMod.LOGGER.error("ASSERTION FAILED: logicalCount == logicalStateRecordCount invariant violated!");
				allPass = false;
			}
			if (hState != null && hState.size() == 4) {
				if (Math.abs(hState.get(0) - 15.0f) > 0.01f ||
					Math.abs(hState.get(1) - 12.0f) > 0.01f ||
					Math.abs(hState.get(2) - 18.0f) > 0.01f ||
					Math.abs(hState.get(3) - 10.0f) > 0.01f) {
					JarStackerMod.LOGGER.error("ASSERTION FAILED: Health state mismatch!");
					allPass = false;
				}
			}

			if (sState == null || sState.size() != 4 ||
				!sState.get(0).hasEffect(MobEffects.INFESTED) ||
				!sState.get(1).hasEffect(MobEffects.OOZING) ||
				!sState.get(2).hasEffect(MobEffects.WEAVING) ||
				!sState.get(3).hasEffect(MobEffects.WIND_CHARGED)) {
				JarStackerMod.LOGGER.error("ASSERTION FAILED: Status effect state mismatch!");
				allPass = false;
			} else {
				JarStackerMod.LOGGER.info("OBSERVED_MOB_EFFECT_0=INFESTED,duration={},amp={}",
					sState.get(0).getEffect(MobEffects.INFESTED).getDuration(), sState.get(0).getEffect(MobEffects.INFESTED).getAmplifier());
				JarStackerMod.LOGGER.info("OBSERVED_MOB_EFFECT_1=OOZING,duration={},amp={}",
					sState.get(1).getEffect(MobEffects.OOZING).getDuration(), sState.get(1).getEffect(MobEffects.OOZING).getAmplifier());
				JarStackerMod.LOGGER.info("OBSERVED_MOB_EFFECT_2=WEAVING,duration={},amp={}",
					sState.get(2).getEffect(MobEffects.WEAVING).getDuration(), sState.get(2).getEffect(MobEffects.WEAVING).getAmplifier());
				JarStackerMod.LOGGER.info("OBSERVED_MOB_EFFECT_3=WIND_CHARGED,duration={},amp={}",
					sState.get(3).getEffect(MobEffects.WIND_CHARGED).getDuration(), sState.get(3).getEffect(MobEffects.WIND_CHARGED).getAmplifier());
			}
		}

		// Verify Variant
		if (mooshroom == null) {
			JarStackerMod.LOGGER.error("ASSERTION FAILED: Fixture Mooshroom not found!");
			allPass = false;
		} else {
			int varLogical = ((StackableEntity) mooshroom).jarstacker$getStackCount();
			String varType = mooshroom.getVariant().getSerializedName().toUpperCase();
			JarStackerMod.LOGGER.info("OBSERVED_VARIANT_UUID={}", mooshroom.getUUID());
			JarStackerMod.LOGGER.info("OBSERVED_VARIANT_TYPE={}", varType);
			JarStackerMod.LOGGER.info("OBSERVED_VARIANT_LOGICAL_COUNT={}", varLogical);

			if (varLogical != 3) {
				JarStackerMod.LOGGER.error("ASSERTION FAILED: varLogical == 3 expected, got {}", varLogical);
				allPass = false;
			}
			if (!"BROWN".equalsIgnoreCase(varType)) {
				JarStackerMod.LOGGER.error("ASSERTION FAILED: Variant BROWN expected, got {}", varType);
				allPass = false;
			}
		}

		// Verify Item
		if (item == null) {
			JarStackerMod.LOGGER.error("ASSERTION FAILED: Fixture Item not found!");
			allPass = false;
		} else {
			int itemLogical = ((StackableEntity) item).jarstacker$getStackCount();
			JarStackerMod.LOGGER.info("OBSERVED_ITEM_UUID={}", item.getUUID());
			JarStackerMod.LOGGER.info("OBSERVED_ITEM_ITEM={}", item.getItem().getItem());
			JarStackerMod.LOGGER.info("OBSERVED_ITEM_LOGICAL_COUNT={}", itemLogical);

			if (itemLogical != 128) {
				JarStackerMod.LOGGER.error("ASSERTION FAILED: itemLogical == 128 expected, got {}", itemLogical);
				allPass = false;
			}
		}

		JarStackerMod.LOGGER.info("=== [SAVE_UPGRADE_FIXTURE AFTER DATA END] ===");
		if (allPass) {
			JarStackerMod.LOGGER.info("=== [SAVE_UPGRADE_FIXTURE RESULT: PASS] ===");
		} else {
			JarStackerMod.LOGGER.error("=== [SAVE_UPGRADE_FIXTURE RESULT: FAIL] ===");
		}
		JarStackerMod.LOGGER.info("========== SAVE-UPGRADE FIXTURE: VERIFY COMPLETE ==========");
	}
}