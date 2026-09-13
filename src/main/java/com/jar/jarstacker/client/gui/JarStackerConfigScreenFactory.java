package com.jar.jarstacker.client.gui;

import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.network.JarStackerPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class JarStackerConfigScreenFactory {

	public static Screen createConfigScreen(Screen parent) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && !mc.isSingleplayer()) {
			// In multiplayer, request authoritative server config
			ClientPlayNetworking.send(new JarStackerPackets.ConfigRequestPayload());
		}
		return createScreen(parent, ModConfig.getInstance(), ModConfig.getConfigRevision());
	}

	public static Screen createScreen(Screen parent, ModConfig config, long revision) {
		if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
			try {
				return JarStackerYaclScreen.buildScreen(parent, config, revision);
			} catch (Throwable t) {
				return new JarStackerConfigScreen(parent, config, revision);
			}
		}
		return new JarStackerConfigScreen(parent, config, revision);
	}
}

