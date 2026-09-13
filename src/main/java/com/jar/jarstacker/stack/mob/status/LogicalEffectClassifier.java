package com.jar.jarstacker.stack.mob.status;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Authoritative classifier for all Vanilla Minecraft 1.21.1 MobEffects and custom effects.
 * Guarantees UNKNOWN == 0 across the entire Vanilla MobEffect registry.
 */
public class LogicalEffectClassifier {

	private static final Map<ResourceLocation, LogicalEffectBehaviorClass> OVERRIDES = new HashMap<>();

	static {
		// ATTRIBUTE_ONLY: Modifiers applied to entity attributes
		register("speed", LogicalEffectBehaviorClass.ATTRIBUTE_ONLY);
		register("slowness", LogicalEffectBehaviorClass.ATTRIBUTE_ONLY);
		register("haste", LogicalEffectBehaviorClass.ATTRIBUTE_ONLY);
		register("mining_fatigue", LogicalEffectBehaviorClass.ATTRIBUTE_ONLY);
		register("strength", LogicalEffectBehaviorClass.ATTRIBUTE_ONLY);
		register("weakness", LogicalEffectBehaviorClass.ATTRIBUTE_ONLY);
		register("health_boost", LogicalEffectBehaviorClass.ATTRIBUTE_ONLY);
		register("luck", LogicalEffectBehaviorClass.ATTRIBUTE_ONLY);
		register("unluck", LogicalEffectBehaviorClass.ATTRIBUTE_ONLY);

		// INSTANT: Application-time execution (no tick loop)
		register("instant_health", LogicalEffectBehaviorClass.INSTANT);
		register("instant_damage", LogicalEffectBehaviorClass.INSTANT);

		// SPECIAL_LOGICAL: Mapped to LogicalHealthManager / DeathBatch / per-member damage
		register("poison", LogicalEffectBehaviorClass.SPECIAL_LOGICAL);
		register("wither", LogicalEffectBehaviorClass.SPECIAL_LOGICAL);
		register("regeneration", LogicalEffectBehaviorClass.SPECIAL_LOGICAL);
		register("resistance", LogicalEffectBehaviorClass.SPECIAL_LOGICAL);
		register("fire_resistance", LogicalEffectBehaviorClass.SPECIAL_LOGICAL);
		register("absorption", LogicalEffectBehaviorClass.SPECIAL_LOGICAL);

		// VISUAL_STATE: Visual/rendering flags projected to representative
		register("invisibility", LogicalEffectBehaviorClass.VISUAL_STATE);
		register("glowing", LogicalEffectBehaviorClass.VISUAL_STATE);
		register("blindness", LogicalEffectBehaviorClass.VISUAL_STATE);
		register("night_vision", LogicalEffectBehaviorClass.VISUAL_STATE);
		register("darkness", LogicalEffectBehaviorClass.VISUAL_STATE);
		register("nausea", LogicalEffectBehaviorClass.VISUAL_STATE);

		// PASSIVE_SAFE: Safe logical state without special recurring callbacks
		register("water_breathing", LogicalEffectBehaviorClass.PASSIVE_SAFE);
		register("jump_boost", LogicalEffectBehaviorClass.PASSIVE_SAFE);
		register("slow_falling", LogicalEffectBehaviorClass.PASSIVE_SAFE);
		register("levitation", LogicalEffectBehaviorClass.PASSIVE_SAFE);
		register("conduit_power", LogicalEffectBehaviorClass.PASSIVE_SAFE);
		register("dolphins_grace", LogicalEffectBehaviorClass.PASSIVE_SAFE);
		register("hero_of_the_village", LogicalEffectBehaviorClass.PASSIVE_SAFE);
		register("bad_omen", LogicalEffectBehaviorClass.PASSIVE_SAFE);
		register("trial_omen", LogicalEffectBehaviorClass.PASSIVE_SAFE);
		register("raid_omen", LogicalEffectBehaviorClass.PASSIVE_SAFE);

		// PERIODIC_VANILLA: Generic recurring tick callbacks
		register("hunger", LogicalEffectBehaviorClass.PERIODIC_VANILLA);
		register("saturation", LogicalEffectBehaviorClass.PERIODIC_VANILLA);

		// UNSUPPORTED: Spawns world entities on damage/death or cannot be virtualized safely
		register("infested", LogicalEffectBehaviorClass.UNSUPPORTED);
		register("oozing", LogicalEffectBehaviorClass.UNSUPPORTED);
		register("weaving", LogicalEffectBehaviorClass.UNSUPPORTED);
		register("wind_charged", LogicalEffectBehaviorClass.UNSUPPORTED);
		// EVENT_DRIVEN_LOGICAL: Spawns world entities on damage/death; executed on logical event
		register("infested", LogicalEffectBehaviorClass.EVENT_DRIVEN_LOGICAL);
		register("oozing", LogicalEffectBehaviorClass.EVENT_DRIVEN_LOGICAL);
		register("weaving", LogicalEffectBehaviorClass.EVENT_DRIVEN_LOGICAL);
		register("wind_charged", LogicalEffectBehaviorClass.EVENT_DRIVEN_LOGICAL);
	}

	private static void register(String path, LogicalEffectBehaviorClass behaviorClass) {
		OVERRIDES.put(ResourceLocation.fromNamespaceAndPath("minecraft", path), behaviorClass);
	}

	public static LogicalEffectBehaviorClass classify(Holder<MobEffect> holder) {
		if (holder == null) {
			return LogicalEffectBehaviorClass.UNSUPPORTED;
		}
		ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(holder.value());
		if (id == null) {
			return LogicalEffectBehaviorClass.UNSUPPORTED;
		}
		return classify(id);
	}

	public static LogicalEffectBehaviorClass classify(ResourceLocation id) {
		if (id == null) {
			return LogicalEffectBehaviorClass.UNSUPPORTED;
		}
		LogicalEffectBehaviorClass overridden = OVERRIDES.get(id);
		if (overridden != null) {
			return overridden;
		}
		// Any effect outside "minecraft" namespace defaults to UNSUPPORTED
		if (!"minecraft".equals(id.getNamespace())) {
			return LogicalEffectBehaviorClass.UNSUPPORTED;
		}
		// If an unclassified Vanilla effect is encountered, default to UNSUPPORTED
		return LogicalEffectBehaviorClass.UNSUPPORTED;
	}

	public static boolean isSupported(Holder<MobEffect> holder) {
		return classify(holder) != LogicalEffectBehaviorClass.UNSUPPORTED;
	}

	public record AuditReport(
		int totalVanilla,
		Map<LogicalEffectBehaviorClass, Integer> counts,
		int unknownCount
	) {}

	public static AuditReport auditAll() {
		int total = 0;
		int unknown = 0;
		Map<LogicalEffectBehaviorClass, Integer> counts = new EnumMap<>(LogicalEffectBehaviorClass.class);
		for (LogicalEffectBehaviorClass c : LogicalEffectBehaviorClass.values()) {
			counts.put(c, 0);
		}

		for (Holder.Reference<MobEffect> ref : BuiltInRegistries.MOB_EFFECT.holders().toList()) {
			ResourceLocation key = ref.key().location();
			if ("minecraft".equals(key.getNamespace())) {
				total++;
				LogicalEffectBehaviorClass behavior = OVERRIDES.get(key);
				if (behavior == null) {
					unknown++;
				} else {
					counts.put(behavior, counts.get(behavior) + 1);
				}
			}
		}

		return new AuditReport(total, counts, unknown);
	}
}

