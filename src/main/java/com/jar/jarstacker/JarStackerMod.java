package com.jar.jarstacker;

import com.jar.jarstacker.command.JarStackerCommands;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.item.ItemStackingManager;
import com.jar.jarstacker.stack.mob.interaction.MobInteractionHandler;
import com.jar.jarstacker.stack.mob.MobStackingManager;
import com.jar.jarstacker.test.JarStackerTestRunner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarStackerMod implements ModInitializer {
	public static final String MOD_ID = "jarstacker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Jar Stacker initialized");
		ModConfig.load();

		com.jar.jarstacker.network.JarStackerPackets.registerPayloads();
		com.jar.jarstacker.network.ServerConfigNetworkHandler.registerServerReceivers();

		if (ModConfig.getInstance().getItemStacking().isEnabled()) {
			LOGGER.info("Item stacking enabled");
		}
		if (ModConfig.getInstance().getMobStacking().isEnabled()) {
			LOGGER.info("Mob stacking enabled");
		}

		MobInteractionHandler.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			JarStackerCommands.register(dispatcher);
		});

		//? if >=26.1 {
		/*ServerTickEvents.END_LEVEL_TICK.register(level -> {
		*///?} else {
		ServerTickEvents.END_WORLD_TICK.register(level -> {
		//?}
			ModConfig config = ModConfig.getInstance();
			long gameTime = level.getGameTime();

			if (config.getItemStacking().isEnabled()) {
				ItemStackingManager.tick(level, config, gameTime);
			}

			if (config.getMobStacking().isEnabled()) {
				int mobInterval = config.getMobStacking().getScanIntervalTicks();
				if (gameTime % mobInterval == 0) {
					MobStackingManager.scanAndStack(level, config);
				}
			}
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (Boolean.getBoolean("jarstacker.test")) {
				ServerLevel overworld = server.overworld();
				if (overworld != null) {
					Vec3 pos = new Vec3(0, 100, 0);
					JarStackerTestRunner.runAllTests(overworld, pos);
				}
				server.halt(false);
			}
		});
	}
}