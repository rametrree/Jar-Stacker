package com.jar.jarstacker.network;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?} else {
import net.minecraft.resources.ResourceLocation;
//?}

public class JarStackerPackets {

	private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String path) {
		//? if >=1.21.11 {
		/*return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("jarstacker", path));
		*///?} else {
		return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("jarstacker", path));
		//?}
	}

	// 1. ConfigRequestPayload (C2S)
	public record ConfigRequestPayload() implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ConfigRequestPayload> TYPE = createType("config_request");

		public static final StreamCodec<ByteBuf, ConfigRequestPayload> CODEC =
			StreamCodec.unit(new ConfigRequestPayload());

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	// 2. ConfigDataPayload (S2C)
	public record ConfigDataPayload(int configVersion, long revision, String configJson) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ConfigDataPayload> TYPE = createType("config_data");

		public static final StreamCodec<ByteBuf, ConfigDataPayload> CODEC = StreamCodec.of(
			(buf, payload) -> {
				ByteBufCodecs.VAR_INT.encode(buf, payload.configVersion());
				ByteBufCodecs.VAR_LONG.encode(buf, payload.revision());
				ByteBufCodecs.stringUtf8(1048576).encode(buf, payload.configJson());
			},
			buf -> new ConfigDataPayload(
				ByteBufCodecs.VAR_INT.decode(buf),
				ByteBufCodecs.VAR_LONG.decode(buf),
				ByteBufCodecs.stringUtf8(1048576).decode(buf)
			)
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	// 3. ConfigUpdatePayload (C2S)
	public record ConfigUpdatePayload(long baseRevision, String configJson) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ConfigUpdatePayload> TYPE = createType("config_update");

		public static final StreamCodec<ByteBuf, ConfigUpdatePayload> CODEC = StreamCodec.of(
			(buf, payload) -> {
				ByteBufCodecs.VAR_LONG.encode(buf, payload.baseRevision());
				ByteBufCodecs.stringUtf8(1048576).encode(buf, payload.configJson());
			},
			buf -> new ConfigUpdatePayload(
				ByteBufCodecs.VAR_LONG.decode(buf),
				ByteBufCodecs.stringUtf8(1048576).decode(buf)
			)
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	// 4. ConfigResultPayload (S2C)
	public record ConfigResultPayload(boolean success, long newRevision, String message) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ConfigResultPayload> TYPE = createType("config_result");

		public static final StreamCodec<ByteBuf, ConfigResultPayload> CODEC = StreamCodec.of(
			(buf, payload) -> {
				ByteBufCodecs.BOOL.encode(buf, payload.success());
				ByteBufCodecs.VAR_LONG.encode(buf, payload.newRevision());
				ByteBufCodecs.stringUtf8(32767).encode(buf, payload.message());
			},
			buf -> new ConfigResultPayload(
				ByteBufCodecs.BOOL.decode(buf),
				ByteBufCodecs.VAR_LONG.decode(buf),
				ByteBufCodecs.stringUtf8(32767).decode(buf)
			)
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public static void registerPayloads() {
		// C2S (Play)
		PayloadTypeRegistry.playC2S().register(ConfigRequestPayload.TYPE, ConfigRequestPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ConfigUpdatePayload.TYPE, ConfigUpdatePayload.CODEC);

		// S2C (Play)
		PayloadTypeRegistry.playS2C().register(ConfigDataPayload.TYPE, ConfigDataPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ConfigResultPayload.TYPE, ConfigResultPayload.CODEC);
	}
}

