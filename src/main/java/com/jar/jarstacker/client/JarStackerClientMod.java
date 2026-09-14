package com.jar.jarstacker.client;

import com.jar.jarstacker.client.gui.JarStackerConfigScreen;
import com.jar.jarstacker.client.gui.JarStackerConfigScreenFactory;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.network.JarStackerPackets;
import net.fabricmc.api.ClientModInitializer;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
*///?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
//?}
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class JarStackerClientMod implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// 1. Client receiver for ConfigDataPayload (S2C)
		ClientPlayNetworking.registerGlobalReceiver(JarStackerPackets.ConfigDataPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				ModConfig serverConfig = ModConfig.fromJson(payload.configJson());
				if (serverConfig != null) {
					Minecraft.getInstance().setScreen(JarStackerConfigScreenFactory.createScreen(
						Minecraft.getInstance().screen,
						serverConfig,
						payload.revision()
					));
				}
			});
		});

		// 2. Client receiver for ConfigResultPayload (S2C)
		ClientPlayNetworking.registerGlobalReceiver(JarStackerPackets.ConfigResultPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				if (Minecraft.getInstance().player != null) {
					Component msg = Component.literal("[Jar Stacker] " + payload.message())
						.withStyle(payload.success() ? ChatFormatting.GREEN : ChatFormatting.RED);
					//? if >=26.1 {
					/*Minecraft.getInstance().player.sendSystemMessage(msg);
					*///?} else {
					Minecraft.getInstance().player.displayClientMessage(msg, false);
					//?}
				}
			});
		});

		// 3. Register client command: /jarstacker config
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			//? if >=26.1 {
			/*dispatcher.register(ClientCommands.literal("jarstacker")
				.then(ClientCommands.literal("config")
			*///?} else {
			dispatcher.register(ClientCommandManager.literal("jarstacker")
				.then(ClientCommandManager.literal("config")
			//?}
					.executes(context -> {
						Minecraft mc = Minecraft.getInstance();
						if (mc.isSingleplayer()) {
							mc.execute(() -> mc.setScreen(JarStackerConfigScreenFactory.createScreen(
								mc.screen,
								ModConfig.getInstance(),
								ModConfig.getConfigRevision()
							)));
						} else {
							// Multiplayer: request authoritative configuration from server
							ClientPlayNetworking.send(new JarStackerPackets.ConfigRequestPayload());
						}
						return 1;
					})
				)
			);
		});
	}
}

