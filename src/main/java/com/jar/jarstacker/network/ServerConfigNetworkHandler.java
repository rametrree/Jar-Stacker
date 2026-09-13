package com.jar.jarstacker.network;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ServerConfigNetworkHandler {

	public static void registerServerReceivers() {
		// Receiver for ConfigRequestPayload
		ServerPlayNetworking.registerGlobalReceiver(JarStackerPackets.ConfigRequestPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ModConfig config = ModConfig.getInstance();
				long revision = ModConfig.getConfigRevision();
				int version = config.getConfigVersion();
				String json = config.toJson();
				ServerPlayNetworking.send(context.player(), new JarStackerPackets.ConfigDataPayload(version, revision, json));
			});
		});

		// Receiver for ConfigUpdatePayload
		ServerPlayNetworking.registerGlobalReceiver(JarStackerPackets.ConfigUpdatePayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();

				// 1. Permission check (level 2 operator)
				if (!player.hasPermissions(2)) {
					ServerPlayNetworking.send(player, new JarStackerPackets.ConfigResultPayload(
						false,
						ModConfig.getConfigRevision(),
						"You do not have permission to modify server configuration."
					));
					return;
				}

				// 2. Revision check
				long currentRevision = ModConfig.getConfigRevision();
				if (payload.baseRevision() != currentRevision) {
					ServerPlayNetworking.send(player, new JarStackerPackets.ConfigResultPayload(
						false,
						currentRevision,
						"Configuration changed since this screen was opened. Reload the current server configuration and try again."
					));
					return;
				}

				// 3. Validation
				try {
					ModConfig parsed = ModConfig.fromJson(payload.configJson());
					if (parsed == null) {
						ServerPlayNetworking.send(player, new JarStackerPackets.ConfigResultPayload(
							false,
							currentRevision,
							"Invalid configuration payload."
						));
						return;
					}

					// 4. Apply, rebuild caches, save, and increment revision
					ModConfig.setInstance(parsed);
					ModConfig.save();
					long newRevision = ModConfig.getConfigRevision();

					JarStackerMod.LOGGER.info("Configuration updated and saved by {} (new revision: {})", player.getScoreboardName(), newRevision);

					// 5. Acknowledge success with new revision
					ServerPlayNetworking.send(player, new JarStackerPackets.ConfigResultPayload(
						true,
						newRevision,
						"Configuration saved successfully!"
					));
				} catch (Exception e) {
					ServerPlayNetworking.send(player, new JarStackerPackets.ConfigResultPayload(
						false,
						currentRevision,
						"Error applying configuration: " + e.getMessage()
					));
				}
			});
		});
	}
}

