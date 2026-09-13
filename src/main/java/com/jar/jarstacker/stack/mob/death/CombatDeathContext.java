package com.jar.jarstacker.stack.mob.death;

import com.jar.jarstacker.JarStackerMod;
import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Immutable attribution context captured at the moment of a damage event.
 * Separates player kill credit from held-weapon Looting and guarantees
 * zero Looting leakage on indirect, projectile, explosion, or environmental deaths.
 */
public class CombatDeathContext {

	private static final ThreadLocal<CombatDeathContext> CURRENT = new ThreadLocal<>();
	private static volatile CombatDeathContext lastRecorded = null;

	private final DamageSource damageSource;
	private final Entity directEntity;
	private final Entity causingEntity;
	private final boolean playerCredit;
	private final boolean recentPlayerHit;
	private final int overrideLootingLevel;
	private final boolean lootingOverride;
	private final String attributionType;
	private int evaluatedLooting = -1;
	private boolean lootingEvaluated = false;

	public CombatDeathContext(DamageSource damageSource, Entity directEntity, Entity causingEntity,
							  boolean playerCredit, boolean recentPlayerHit, int overrideLootingLevel,
							  boolean lootingOverride, String attributionType) {
		this.damageSource = damageSource;
		this.directEntity = directEntity;
		this.causingEntity = causingEntity;
		this.playerCredit = playerCredit;
		this.recentPlayerHit = recentPlayerHit;
		this.overrideLootingLevel = overrideLootingLevel;
		this.lootingOverride = lootingOverride;
		this.attributionType = attributionType;
	}

	public static CombatDeathContext getCurrentContext() {
		return CURRENT.get();
	}

	public static CombatDeathContext getLastRecorded() {
		return lastRecorded;
	}

	public static void record(CombatDeathContext context) {
		lastRecorded = context;
	}

	public DamageSource getDamageSource() {
		return damageSource;
	}

	public Entity getDirectEntity() {
		return directEntity;
	}

	public Entity getCausingEntity() {
		return causingEntity;
	}

	public boolean hasPlayerCredit() {
		return playerCredit;
	}

	public boolean isRecentPlayerHit() {
		return recentPlayerHit;
	}

	public void recordEvaluatedLooting(int level) {
		this.evaluatedLooting = level;
		this.lootingEvaluated = true;
	}

	public int getEvaluatedLooting() {
		return this.evaluatedLooting;
	}

	public boolean isLootingEvaluated() {
		return this.lootingEvaluated;
	}

	public int getLootingLevel() {
		if (this.lootingOverride) {
			return this.overrideLootingLevel;
		}
		if (this.lootingEvaluated) {
			return this.evaluatedLooting;
		}
		if ("NATURAL_ENVIRONMENT".equals(this.attributionType) || "RECENT_PLAYER_ENVIRONMENT".equals(this.attributionType)) {
			return 0;
		}
		Entity attacker = this.damageSource != null ? this.damageSource.getEntity() : null;
		if (attacker == null && this.causingEntity != null) {
			attacker = this.causingEntity;
		}
		if (attacker instanceof LivingEntity living) {
			return resolveLootingFromEntity(living);
		}
		return 0;
	}

	public boolean hasLootingOverride() {
		return lootingOverride;
	}

	public String getAttributionType() {
		return attributionType;
	}

	public Scope openScope() {
		CombatDeathContext previous = CURRENT.get();
		CURRENT.set(this);
		record(this);
		return new Scope(previous);
	}

	public static class Scope implements AutoCloseable {
		private final CombatDeathContext previous;
		private boolean closed = false;

		public Scope(CombatDeathContext previous) {
			this.previous = previous;
		}

		@Override
		public void close() {
			if (!closed) {
				closed = true;
				if (previous != null) {
					CURRENT.set(previous);
				} else {
					CURRENT.remove();
				}
			}
		}
	}

	public static CombatDeathContext capture(LivingEntity victim, DamageSource damageSource) {
		if (damageSource == null) {
			return new CombatDeathContext(null, null, null, false, false, 0, false, "UNKNOWN");
		}

		Entity direct = damageSource.getDirectEntity();
		Entity causing = damageSource.getEntity();
		boolean recentPlayerHit = false;
		Player lastPlayer = null;

		if (victim instanceof StackableEntity stackable) {
			recentPlayerHit = stackable.jarstacker$getLastHurtByPlayerTime() > 0;
			lastPlayer = stackable.jarstacker$getLastHurtByPlayer();
		}

		// 1. Sword sweep (must check before PLAYER_ATTACK because sweeps have player attack damage source)
		if (com.jar.jarstacker.stack.mob.health.CombatContext.isSweepActive()) {
			Player player = com.jar.jarstacker.stack.mob.health.CombatContext.getAttackingPlayer();
			return new CombatDeathContext(damageSource, direct, player != null ? player : causing, true, recentPlayerHit, 0, false, "SWEEP");
		}

		// 2. Direct melee attack by player
		if (damageSource.is(DamageTypes.PLAYER_ATTACK) || (direct == causing && causing instanceof Player)) {
			return new CombatDeathContext(damageSource, direct, causing, true, recentPlayerHit, 0, false, "DIRECT_MELEE");
		}

		// 3. Projectiles (Arrow, Trident, etc.)
		if (direct instanceof Projectile) {
			boolean playerCaused = causing instanceof Player;
			return new CombatDeathContext(damageSource, direct, causing, playerCaused, recentPlayerHit, 0, false, "PROJECTILE");
		}

		// 4. Explosions (TNT, Creeper)
		if (damageSource.is(DamageTypes.EXPLOSION) || damageSource.is(DamageTypes.PLAYER_EXPLOSION)) {
			if (direct instanceof PrimedTnt primedTnt) {
				LivingEntity owner = primedTnt.getOwner();
				Entity effectiveCauser = owner != null ? owner : causing;
				boolean playerCaused = effectiveCauser instanceof Player;
				return new CombatDeathContext(damageSource, direct, effectiveCauser, playerCaused, recentPlayerHit, 0, false, playerCaused ? "PLAYER_TNT" : "EXPLOSION");
			}
			if (direct instanceof Creeper || causing instanceof Creeper) {
				return new CombatDeathContext(damageSource, direct, causing, false, recentPlayerHit, 0, false, "CREEPER");
			}
			boolean playerCaused = causing instanceof Player;
			return new CombatDeathContext(damageSource, direct, causing, playerCaused, recentPlayerHit, 0, false, "EXPLOSION");
		}

		// 5. Environmental hazards (Lava, Fire, Fall, Drown, etc.)
		if (damageSource.is(DamageTypes.LAVA) || damageSource.is(DamageTypes.IN_FIRE)
			|| damageSource.is(DamageTypes.ON_FIRE) || damageSource.is(DamageTypes.FALL)
			|| damageSource.is(DamageTypes.DROWN) || damageSource.is(DamageTypes.FREEZE)
			|| damageSource.is(DamageTypes.IN_WALL) || damageSource.is(DamageTypes.CACTUS)
			|| damageSource.is(DamageTypes.CAMPFIRE) || damageSource.is(DamageTypes.HOT_FLOOR)) {
			if (recentPlayerHit && lastPlayer != null) {
				// Recent player hit gives Vanilla player XP eligibility, but NO held Looting
				return new CombatDeathContext(damageSource, direct, lastPlayer, true, true, 0, false, "RECENT_PLAYER_ENVIRONMENT");
			}
			return new CombatDeathContext(damageSource, direct, null, false, false, 0, false, "NATURAL_ENVIRONMENT");
		}

		// 6. Generic / Custom damage sources
		boolean playerCredit = causing instanceof Player || (recentPlayerHit && lastPlayer != null);
		return new CombatDeathContext(damageSource, direct, causing != null ? causing : (recentPlayerHit ? lastPlayer : null),
			playerCredit, recentPlayerHit, 0, false, "CUSTOM");
	}

	public static int resolveLootingFromEntity(LivingEntity entity) {
		if (entity == null || entity.level() == null) {
			return 0;
		}
		try {
			var registry = entity.level().registryAccess().lookup(Registries.ENCHANTMENT);
			if (registry.isPresent()) {
				var lootingHolder = registry.get().get(Enchantments.LOOTING);
				if (lootingHolder.isPresent()) {
					return EnchantmentHelper.getEnchantmentLevel(lootingHolder.get(), entity);
				}
			}
		} catch (Exception e) {
			JarStackerMod.LOGGER.debug("[JarStackerCombat] Failed to lookup LOOTING enchantment: {}", e.getMessage());
		}
		return 0;
	}

	public static void logAttribution(CombatDeathContext context, int logicalDeaths) {
		if (context != null && ModConfig.getInstance().getPerformance().isDebugLogging()) {
			JarStackerMod.LOGGER.info("[JarStackerCombat] LOGICAL_DEATH_ATTRIBUTION type={} source={} direct={} causing={} playerCredit={} lootingLevel={} logicalDeaths={}",
				context.getAttributionType(),
				context.getDamageSource() != null ? context.getDamageSource().type().msgId() : "none",
				context.getDirectEntity() != null ? BuiltInRegistries.ENTITY_TYPE.getKey(context.getDirectEntity().getType()) : "none",
				context.getCausingEntity() != null ? BuiltInRegistries.ENTITY_TYPE.getKey(context.getCausingEntity().getType()) : "none",
				context.hasPlayerCredit(),
				context.getLootingLevel(),
				logicalDeaths);
		}
	}
}

