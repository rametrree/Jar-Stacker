package com.jar.jarstacker.command;

import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.network.JarStackerPackets;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.item.ItemCompatibility;
import com.jar.jarstacker.stack.item.ItemStackingManager;
import com.jar.jarstacker.stack.mob.MobCompatibility;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import com.jar.jarstacker.test.JarStackerTestRunner;
import com.jar.jarstacker.util.StackMetrics;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
//? if >=1.21.11 {
/*import net.minecraft.world.entity.monster.zombie.Zombie;
*///?} else {
import net.minecraft.world.entity.monster.Zombie;
//?}
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class JarStackerCommands {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal("jarstacker")
				.then(Commands.literal("config")
					.executes(ctx -> openConfigGui(ctx.getSource()))
				)
				.then(Commands.literal("status")
					.executes(ctx -> showStatus(ctx.getSource()))
				)
				.then(Commands.literal("inspect")
					.executes(ctx -> inspectEntity(ctx.getSource()))
				)
				.then(Commands.literal("reload")
					.requires(JarStackerCommands::hasAdminPermission)
					.executes(ctx -> reloadConfig(ctx.getSource()))
				)
				.then(Commands.literal("items")
					.then(Commands.literal("on")
						.requires(JarStackerCommands::hasAdminPermission)
						.executes(ctx -> setItemState(ctx.getSource(), true))
					)
					.then(Commands.literal("off")
						.requires(JarStackerCommands::hasAdminPermission)
						.executes(ctx -> setItemState(ctx.getSource(), false))
					)
				)
				.then(Commands.literal("mobs")
					.then(Commands.literal("on")
						.requires(JarStackerCommands::hasAdminPermission)
						.executes(ctx -> setMobState(ctx.getSource(), true))
					)
					.then(Commands.literal("off")
						.requires(JarStackerCommands::hasAdminPermission)
						.executes(ctx -> setMobState(ctx.getSource(), false))
					)
				)
				.then(Commands.literal("debug")
					.requires(JarStackerCommands::hasAdminPermission)
					.executes(ctx -> toggleDebug(ctx.getSource()))
					.then(Commands.literal("state")
						.executes(ctx -> debugState(ctx.getSource()))
					)
					.then(Commands.literal("combat")
						.executes(ctx -> debugCombat(ctx.getSource()))
					)
				)
				.then(Commands.literal("test")
					.requires(JarStackerCommands::hasAdminPermission)
					.then(Commands.literal("runAll")
						.executes(ctx -> runAllTests(ctx.getSource()))
					)
					.then(Commands.literal("spawnItems")
						.then(Commands.argument("count", IntegerArgumentType.integer(1, 2000))
							.executes(ctx -> spawnTestItems(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "count")))
						)
					)
					.then(Commands.literal("spawnZombies")
						.then(Commands.argument("count", IntegerArgumentType.integer(1, 1000))
							.executes(ctx -> spawnTestZombies(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "count")))
						)
					)
					.then(Commands.literal("runStack")
						.executes(ctx -> runManualStack(ctx.getSource()))
					)
				)
		);
	}

	private static boolean hasAdminPermission(CommandSourceStack src) {
		//? if >=1.21.11 {
		/*return src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
		*///? } else {
		return src.hasPermission(2);
		//? }
	}

	private static int openConfigGui(CommandSourceStack source) {
		if (source.getEntity() instanceof ServerPlayer player) {
			ModConfig config = ModConfig.getInstance();
			String json = config.toJson();
			ServerPlayNetworking.send(player, new JarStackerPackets.ConfigDataPayload(config.getConfigVersion(), ModConfig.getConfigRevision(), json));
			return 1;
		} else {
			source.sendFailure(Component.literal("The configuration GUI can only be opened by a player."));
			return 0;
		}
	}

	private static int inspectEntity(CommandSourceStack source) {
		ServerPlayer player;
		try {
			player = source.getPlayerOrException();
		} catch (Exception e) {
			source.sendFailure(Component.literal("Only players can inspect entities in line of sight."));
			return 0;
		}

		Vec3 eyePos = player.getEyePosition(1.0f);
		Vec3 viewVec = player.getViewVector(1.0f);
		Vec3 endPos = eyePos.add(viewVec.scale(6.0));
		AABB aabb = player.getBoundingBox().expandTowards(viewVec.scale(6.0)).inflate(1.0);

		EntityHitResult hit = com.jar.jarstacker.adapter.EntityAdapter.getEntityHitResult(player, eyePos, endPos, aabb, e -> !e.isSpectator() && e.isPickable());
		if (hit == null || hit.getEntity() == null) {
			source.sendFailure(Component.literal("No entity in line of sight (range: 6 blocks)."));
			return 0;
		}

		Entity target = hit.getEntity();
		ModConfig config = ModConfig.getInstance();

		if (target instanceof ItemEntity itemEntity) {
			int logical = ((StackableEntity) itemEntity).jarstacker$getStackCount();
			if (logical <= 0) logical = itemEntity.getItem().getCount();
			int physical = itemEntity.getItem().getCount();
			boolean isStacked = logical > physical;
			String itemId = BuiltInRegistries.ITEM.getKey(itemEntity.getItem().getItem()).toString();

			ItemCompatibility.ItemInspection insp = ItemCompatibility.inspectItem(itemEntity, config.getItemStacking());

			StringBuilder sb = new StringBuilder();
			sb.append("\n=== Jar Stacker Inspection ===\n");
			sb.append("Entity: minecraft:item\n");
			sb.append("Item: ").append(itemId).append("\n");
			sb.append(String.format("Logical count: %,d\n", logical));
			sb.append("Physical count: ").append(physical).append("\n");
			sb.append("Stacked: ").append(isStacked ? "Yes" : "No").append("\n");
			sb.append("UUID: ").append(itemEntity.getUUID()).append("\n");
			sb.append("Stackable: ").append(insp.stackable ? "Yes" : "No").append("\n");
			if (!insp.stackable) {
				sb.append("Reason: ").append(insp.reason).append("\n");
			}

			source.sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.AQUA), false);
			return 1;
		} else if (target instanceof Mob mob) {
			int count = ((StackableEntity) mob).jarstacker$getStackCount();
			if (count <= 0) count = 1;
			boolean isStacked = count > 1;
			String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();

			MobCompatibility.MobInspection insp = MobCompatibility.inspectMob(mob, config.getMobStacking());

			StringBuilder sb = new StringBuilder();
			sb.append("\n=== Jar Stacker Inspection ===\n");
			sb.append("Entity: ").append(mobId).append("\n");
			sb.append(String.format("Logical count: %,d\n", count));
			if (mob instanceof net.minecraft.world.entity.animal.Animal animal) {
				sb.append("Lifecycle: ").append(MobCompatibility.getStackState(animal).name()).append("\n");
				sb.append("In Love: ").append(animal.isInLove() ? "Yes" : "No").append("\n");
				sb.append("Age: ").append(animal.getAge()).append("\n");
				boolean locked = ((StackableEntity) animal).jarstacker$isBreedingLocked();
				int lockTicks = ((StackableEntity) animal).jarstacker$getBreedingLockTicks();
				sb.append("Breeding Lock: ").append(locked ? "YES" : "NO").append("\n");
				if (locked) {
					sb.append("Lock Remaining: ").append(lockTicks).append(" ticks\n");
				}
				if (animal.isBaby()) {
					com.jar.jarstacker.stack.mob.baby.BabyGrowthState growthState = ((StackableEntity) animal).jarstacker$getBabyGrowthState();
					com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationReport valReport =
						com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.validateLogicalState(animal);
					sb.append("Logical State Records: ").append(valReport.recordCount()).append("\n");
					sb.append("Logical State Integrity: ").append(valReport.status().name()).append("\n");
					if (growthState != null && !growthState.isEmpty()) {
						sb.append("Logical Growth Entries: ").append(growthState.size()).append("\n");
						long now = animal.level().getGameTime();
						long earliest = growthState.getEarliestAdultAt();
						long latest = growthState.getLatestAdultAt();
						long nextTicks = Math.max(0, earliest - now);
						long youngestTicks = Math.max(0, latest - now);
						sb.append("Next Adult In: ").append(nextTicks).append(" ticks\n");
						sb.append("Oldest Remaining: ").append(nextTicks).append("\n");
						sb.append("Youngest Remaining: ").append(youngestTicks).append("\n");
					}
				}
			}
			int interactionLock = ((StackableEntity) mob).jarstacker$getInteractionLockTicks();
			if (interactionLock > 0) {
				sb.append("Interaction Lock: ").append(interactionLock).append(" ticks\n");
			}
			if (com.jar.jarstacker.adapter.EntityAdapter.isSheep(mob)) {
				sb.append("Color: ").append(com.jar.jarstacker.adapter.EntityAdapter.getSheepColorName(mob)).append("\n");
				sb.append("Sheared: ").append(com.jar.jarstacker.adapter.EntityAdapter.isSheepSheared(mob) ? "YES" : "NO").append("\n");
			}
			//? if >=1.21.11 {
			/*if (mob instanceof net.minecraft.world.entity.animal.cow.MushroomCow mc) {
				sb.append("Variant: ").append(mc.getVariant().name()).append("\n");
				net.minecraft.world.item.component.SuspiciousStewEffects stew =
					((com.jar.jarstacker.mixin.MushroomCowAccessor) mc).jarstacker$getStewEffects();
				sb.append("Stew State: ").append(stew != null ? "PRESENT" : "NONE").append("\n");
				if (stew != null) {
					sb.append("Stew Effect Signature: ").append(stew.effects()).append("\n");
				}
			}
			if (mob instanceof net.minecraft.world.entity.animal.golem.SnowGolem snowGolem) {
				sb.append("Pumpkin: ").append(snowGolem.hasPumpkin() ? "YES" : "NO").append("\n");
			}
			*///?} else {
			if (mob instanceof net.minecraft.world.entity.animal.MushroomCow mc) {
				sb.append("Variant: ").append(mc.getVariant().name()).append("\n");
				net.minecraft.world.item.component.SuspiciousStewEffects stew =
					((com.jar.jarstacker.mixin.MushroomCowAccessor) mc).jarstacker$getStewEffects();
				sb.append("Stew State: ").append(stew != null ? "PRESENT" : "NONE").append("\n");
				if (stew != null) {
					sb.append("Stew Effect Signature: ").append(stew.effects()).append("\n");
				}
			}
			if (mob instanceof net.minecraft.world.entity.animal.SnowGolem snowGolem) {
				sb.append("Pumpkin: ").append(snowGolem.hasPumpkin() ? "YES" : "NO").append("\n");
			}
			//?}
			sb.append("Saddled: ").append(com.jar.jarstacker.adapter.EntityAdapter.isSaddled(mob) ? "YES" : "NO").append("\n");
			if (mob instanceof net.minecraft.world.entity.TamableAnimal tamable) {
				sb.append("Tamed: ").append(tamable.isTame() ? "YES" : "NO").append("\n");
				java.util.UUID ownerUuid = com.jar.jarstacker.adapter.EntityAdapter.getOwnerUUID(tamable);
				sb.append("Owner: ").append(ownerUuid != null ? ownerUuid.toString() : "none").append("\n");
			}
			sb.append("Stacked: ").append(isStacked ? "Yes" : "No").append("\n");
			com.jar.jarstacker.stack.mob.health.LogicalHealthState healthState = ((StackableEntity) mob).jarstacker$getLogicalHealthState();
			com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationReport hReport =
				com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.validateLogicalState(mob);
			sb.append("Health Records: ").append(healthState != null ? healthState.size() : 0).append("\n");
			sb.append("Health Integrity: ").append(hReport.status().name()).append("\n");
			sb.append(String.format("Active HP: %.1f / %.1f\n", mob.getHealth(), mob.getMaxHealth()));
			int damagedCount = 0;
			if (healthState != null) {
				float maxHp = mob.getMaxHealth();
				for (int i = 0; i < healthState.size(); i++) {
					if (healthState.get(i) < maxHp - 0.001f) {
						damagedCount++;
					}
				}
			}
			sb.append("Damaged Logical Mobs: ").append(damagedCount).append("\n");
			sb.append(String.format("Health: %.1f / %.1f\n", mob.getHealth(), mob.getMaxHealth()));
			com.jar.jarstacker.stack.mob.status.LogicalStatusEffectState statusState = ((StackableEntity) mob).jarstacker$getLogicalStatusEffectState();
			sb.append("Status Records: ").append(statusState != null ? statusState.size() : 0).append("\n");

			com.jar.jarstacker.stack.mob.status.LogicalBurnState burnState = ((StackableEntity) mob).jarstacker$getLogicalBurnState();
			sb.append("Burn Records: ").append(burnState != null ? burnState.size() : 0).append("\n");

			if (statusState != null && !statusState.isEmpty()) {
				com.jar.jarstacker.stack.mob.status.LogicalStatusRecord record0 = statusState.get(0);
				sb.append("Active Effects: ").append(record0.getActiveEffects().size()).append("\n");
			}
			if (burnState != null && !burnState.isEmpty()) {
				sb.append("Active Burn Ticks: ").append(burnState.get(0).getRemainingFireTicks()).append("\n");
			}

			sb.append("Status Virtualization:\n");
			int knownEffects = 0;
			int unsupportedEffects = 0;
			if (statusState != null) {
				for (int i = 0; i < statusState.size(); i++) {
					com.jar.jarstacker.stack.mob.status.LogicalStatusRecord rec = statusState.get(i);
					if (rec != null) {
						for (net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> eff : rec.getActiveEffects().keySet()) {
							if (com.jar.jarstacker.stack.mob.status.LogicalEffectClassifier.isSupported(eff)) {
								knownEffects++;
							} else {
								unsupportedEffects++;
							}
						}
					}
				}
			}
			sb.append("Known effects: ").append(knownEffects).append("\n");
			sb.append("Unsupported effects: ").append(unsupportedEffects).append("\n");
			sb.append("Effect Integrity: ").append(unsupportedEffects == 0 ? "VALID" : "UNSUPPORTED_DETECTED").append("\n");
			if (unsupportedEffects > 0) {
				sb.append("Fallback Reason: UNSUPPORTED_MOB_EFFECT\n");
			}

			sb.append("UUID: ").append(mob.getUUID()).append("\n");
			sb.append("Stackable: ").append(insp.stackable ? "Yes" : "No").append("\n");
			if (!insp.stackable) {
				sb.append("Reason: ").append(insp.reason).append("\n");
			}

			source.sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.YELLOW), false);
			return 1;
		} else {
			String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
			source.sendSuccess(() -> Component.literal("\n=== Jar Stacker Inspection ===\n" +
				"Entity: " + entityId + "\n" +
				"UUID: " + target.getUUID() + "\n" +
				"Stackable: No (Not an ItemEntity or Living Mob)").withStyle(ChatFormatting.GRAY), false);
			return 1;
		}
	}

	private static int debugState(CommandSourceStack source) {
		ServerPlayer player;
		try {
			player = source.getPlayerOrException();
		} catch (Exception e) {
			source.sendFailure(Component.literal("Only players can debug state of targeted entity."));
			return 0;
		}

		Vec3 eyePos = player.getEyePosition(1.0f);
		Vec3 viewVec = player.getViewVector(1.0f);
		Vec3 endPos = eyePos.add(viewVec.scale(6.0));
		AABB aabb = player.getBoundingBox().expandTowards(viewVec.scale(6.0)).inflate(1.0);
		EntityHitResult hit = com.jar.jarstacker.adapter.EntityAdapter.getEntityHitResult(player, eyePos, endPos, aabb, entity -> !entity.isSpectator());

		if (hit == null || !(hit.getEntity() instanceof LivingEntity living)) {
			source.sendFailure(Component.literal("No living entity in line of sight (within 6 blocks)."));
			return 0;
		}

		com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.ValidationReport report =
			com.jar.jarstacker.stack.mob.logical.LogicalStateValidator.validateLogicalState(living);

		StringBuilder sb = new StringBuilder();
		sb.append("\n=== Jar Stacker Logical State Debug ===\n");
		sb.append("UUID: ").append(living.getUUID()).append("\n");
		sb.append("Type: ").append(BuiltInRegistries.ENTITY_TYPE.getKey(living.getType())).append("\n");
		sb.append("Count: ").append(report.logicalCount()).append("\n");
		sb.append("Metadata Records: ").append(report.recordCount()).append("\n");
		sb.append("Validation Status: ").append(report.status()).append("\n");
		sb.append("Status Reason: ").append(report.reason()).append("\n");
		sb.append("Schema Version: ").append(com.jar.jarstacker.stack.mob.baby.BabyGrowthState.CURRENT_VERSION).append("\n");

		if (living instanceof Animal animal && animal.isBaby()) {
			com.jar.jarstacker.stack.mob.baby.BabyGrowthState growthState = ((StackableEntity) animal).jarstacker$getBabyGrowthState();
			if (growthState != null) {
				sb.append("Sorted: ").append(growthState.isSorted() ? "YES" : "NO").append("\n");
				long now = animal.level().getGameTime();
				sb.append("Next Transition: ").append(Math.max(0, growthState.getEarliestAdultAt() - now)).append(" ticks\n");
				sb.append("Latest Transition: ").append(Math.max(0, growthState.getLatestAdultAt() - now)).append(" ticks\n");
			}
		}

		source.sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.GOLD), false);
		return 1;
	}

	private static int debugCombat(CommandSourceStack source) {
		com.jar.jarstacker.stack.mob.death.CombatDeathContext ctx = com.jar.jarstacker.stack.mob.death.CombatDeathContext.getLastRecorded();
		com.jar.jarstacker.stack.mob.health.DeathBatch batch = com.jar.jarstacker.stack.mob.health.LogicalHealthManager.getLastDeathBatch();

		StringBuilder sb = new StringBuilder();
		sb.append("\n=== Jar Stacker Combat Debug ===\n");
		if (ctx != null) {
			sb.append("Attribution Type: ").append(ctx.getAttributionType()).append("\n");
			sb.append("Damage Source: ").append(ctx.getDamageSource() != null ? ctx.getDamageSource().type().msgId() : "none").append("\n");
			sb.append("Direct Entity: ").append(ctx.getDirectEntity() != null ? BuiltInRegistries.ENTITY_TYPE.getKey(ctx.getDirectEntity().getType()) : "none").append("\n");
			sb.append("Causing Entity: ").append(ctx.getCausingEntity() != null ? BuiltInRegistries.ENTITY_TYPE.getKey(ctx.getCausingEntity().getType()) : "none").append("\n");
			sb.append("Player Credit: ").append(ctx.hasPlayerCredit() ? "Yes" : "No").append("\n");
			sb.append("Recent Player Hit: ").append(ctx.isRecentPlayerHit() ? "Yes" : "No").append("\n");
			sb.append("Looting Level: ").append(ctx.getLootingLevel()).append("\n");
		} else {
			sb.append("Last Combat Context: None recorded\n");
		}

		if (batch != null) {
			sb.append("Last DeathBatch: Requested=").append(batch.getRequestedDeaths())
				.append(", Virtual=").append(batch.getVirtualDeathsProcessed())
				.append(", RepOwned=").append(batch.isRepresentativeDeathOwned())
				.append(", Committed=").append(batch.getCommittedDeaths())
				.append(", Exact=").append(batch.isExact()).append("\n");
		} else {
			sb.append("Last DeathBatch: None recorded\n");
		}

		source.sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.AQUA), false);
		return 1;
	}

	private static int runAllTests(CommandSourceStack source) {
		ServerLevel level = source.getLevel();
		Vec3 pos = source.getPosition();
		source.sendSuccess(() -> Component.literal("Running Jar Stacker test suite..."), true);
		List<JarStackerTestRunner.TestResult> results = JarStackerTestRunner.runAllTests(level, pos);
		int passed = 0;
		for (JarStackerTestRunner.TestResult tr : results) {
			if (tr.passed) passed++;
			source.sendSuccess(() -> Component.literal("[" + (tr.passed ? "PASS" : "FAIL") + "] " + tr.name + ": " + tr.details), false);
		}
		int total = results.size();
		int p = passed;
		source.sendSuccess(() -> Component.literal("Finished: " + p + "/" + total + " tests passed."), true);
		return passed;
	}

	private static int showStatus(CommandSourceStack source) {
		ModConfig config = ModConfig.getInstance();
		ModConfig.ItemStackingConfig itemCfg = config.getItemStacking();
		ModConfig.MobStackingConfig mobCfg = config.getMobStacking();

		long physicalStackedItems = 0;
		long logicalItems = 0;
		long physicalStackedMobs = 0;
		long logicalMobs = 0;

		for (ServerLevel level : source.getServer().getAllLevels()) {
			List<? extends ItemEntity> items = level.getEntities(EntityTypeTest.forClass(ItemEntity.class), Entity::isAlive);
			for (ItemEntity item : items) {
				int count = ((StackableEntity) item).jarstacker$getStackCount();
				if (count > item.getItem().getCount()) {
					physicalStackedItems++;
					logicalItems += count;
				}
			}

			List<? extends Mob> mobs = level.getEntities(EntityTypeTest.forClass(Mob.class), Entity::isAlive);
			for (Mob mob : mobs) {
				int count = ((StackableEntity) mob).jarstacker$getStackCount();
				if (count > 1) {
					physicalStackedMobs++;
					logicalMobs += count;
				}
			}
		}

		double lastItemMs = StackMetrics.lastItemScanDurationNs / 1_000_000.0;
		double lastMobMs = StackMetrics.lastMobScanDurationNs / 1_000_000.0;
		double avgItemMs = StackMetrics.getAverageItemScanMs();
		double avgMobMs = StackMetrics.getAverageMobScanMs();
		double maxItemMs = StackMetrics.maxItemScanDurationNs / 1_000_000.0;
		double maxMobMs = StackMetrics.maxMobScanDurationNs / 1_000_000.0;

		String msg = String.format(
			"\n=== Jar Stacker 0.5.1 ===\n" +
			"\n=== Jar Stacker 0.5.2 ===\n" +
			"Revision: %d | Config Version: %d\n\n" +
			"Items\n" +
			"  Enabled: %s\n" +
			"  Radius: %.1f\n" +
			"  Scan interval: %d ticks\n" +
			"  Maximum stack: %,d\n" +
			"  Filter: %s (Blacklist: %d, Whitelist: %d, Rules: %d)\n\n" +
			"Mobs\n" +
			"  Enabled: %s\n" +
			"  Radius: %.1f\n" +
			"  Scan interval: %d ticks\n" +
			"  Maximum stack: %,d\n" +
			"  Death mode: %s\n" +
			"  Filter: %s (Blacklist: %d, Whitelist: %d, Rules: %d)\n\n" +
			"Runtime\n" +
			"  Physical stacked ItemEntities: %,d\n" +
			"  Logical items represented: %,d\n" +
			"  Physical stacked mobs: %,d\n" +
			"  Logical mobs represented: %,d\n\n" +
			"Performance\n" +
			"  Last item scan: %.2f ms\n" +
			"  Last mob scan: %.2f ms\n" +
			"  Average item scan: %.2f ms\n" +
			"  Average mob scan: %.2f ms\n" +
			"  Max item scan: %.2f ms\n" +
			"  Max mob scan: %.2f ms\n" +
			"  Items merged total: %,d\n" +
			"  Mobs merged total: %,d\n",
			ModConfig.getConfigRevision(),
			config.getConfigVersion(),
			itemCfg.isEnabled() ? "Yes" : "No",
			itemCfg.getRadius(),
			itemCfg.getScanIntervalTicks(),
			itemCfg.getMaxStackSize(),
			itemCfg.getFilterMode(),
			itemCfg.getBlacklist().size(),
			itemCfg.getWhitelist().size(),
			itemCfg.getRules().size(),
			mobCfg.isEnabled() ? "Yes" : "No",
			mobCfg.getRadius(),
			mobCfg.getScanIntervalTicks(),
			mobCfg.getMaxStackSize(),
			mobCfg.getDeathMode(),
			mobCfg.getFilterMode(),
			mobCfg.getBlacklist().size(),
			mobCfg.getWhitelist().size(),
			mobCfg.getRules().size(),
			physicalStackedItems,
			logicalItems,
			physicalStackedMobs,
			logicalMobs,
			lastItemMs,
			lastMobMs,
			avgItemMs,
			avgMobMs,
			maxItemMs,
			maxMobMs,
			StackMetrics.TOTAL_ITEMS_MERGED.get(),
			StackMetrics.TOTAL_MOBS_MERGED.get()
		);

		source.sendSuccess(() -> Component.literal(msg), false);
		return 1;
	}

	private static int reloadConfig(CommandSourceStack source) {
		ModConfig.load();
		source.sendSuccess(() -> Component.literal("Jar Stacker configuration reloaded (revision " + ModConfig.getConfigRevision() + ")!"), true);
		return 1;
	}

	private static int setItemState(CommandSourceStack source, boolean enabled) {
		ModConfig.getInstance().getItemStacking().setEnabled(enabled);
		ModConfig.save();
		ModConfig.incrementRevision();
		source.sendSuccess(() -> Component.literal("Item stacking " + (enabled ? "enabled" : "disabled") + "."), true);
		return 1;
	}

	private static int setMobState(CommandSourceStack source, boolean enabled) {
		ModConfig.getInstance().getMobStacking().setEnabled(enabled);
		ModConfig.save();
		ModConfig.incrementRevision();
		source.sendSuccess(() -> Component.literal("Mob stacking " + (enabled ? "enabled" : "disabled") + "."), true);
		return 1;
	}

	private static int toggleDebug(CommandSourceStack source) {
		boolean current = ModConfig.getInstance().getPerformance().isDebugLogging();
		ModConfig.getInstance().getPerformance().setDebugLogging(!current);
		ModConfig.save();
		source.sendSuccess(() -> Component.literal("Debug logging " + (!current ? "enabled" : "disabled") + "."), true);
		return 1;
	}

	private static int spawnTestItems(CommandSourceStack source, int count) {
		ServerLevel level = source.getLevel();
		Vec3 pos = source.getPosition();
		for (int i = 0; i < count; i++) {
			ItemEntity item = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, 1));
			item.setPickUpDelay(40);
			level.addFreshEntity(item);
		}
		source.sendSuccess(() -> Component.literal("Spawned " + count + " Diamonds at " + pos), true);
		return count;
	}

	private static int spawnTestZombies(CommandSourceStack source, int count) {
		ServerLevel level = source.getLevel();
		Vec3 pos = source.getPosition();
		for (int i = 0; i < count; i++) {
			//? if >=26.2 {
			/*Zombie z = com.jar.jarstacker.adapter.EntityAdapter.create(net.minecraft.world.entity.EntityTypes.ZOMBIE, level);
			*///?} else {
			Zombie z = com.jar.jarstacker.adapter.EntityAdapter.create(EntityType.ZOMBIE, level);
			//?}
			if (z != null) {
				z.setPos(pos.x, pos.y, pos.z);
				level.addFreshEntity(z);
			}
		}
		source.sendSuccess(() -> Component.literal("Spawned " + count + " Zombies at " + pos), true);
		return count;
	}

	private static int runManualStack(CommandSourceStack source) {
		ServerLevel level = source.getLevel();
		ModConfig config = ModConfig.getInstance();
		ItemStackingManager.scanAndStack(level, config);
		MobStackingManager.scanAndStack(level, config);
		source.sendSuccess(() -> Component.literal("Executed manual scan and stack on current level."), true);
		return 1;
	}
}