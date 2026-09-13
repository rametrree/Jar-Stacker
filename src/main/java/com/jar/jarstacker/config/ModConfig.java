package com.jar.jarstacker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ModConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("jarstacker-config");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("jarstacker.json");
	private static final Path BACKUP_PATH = FabricLoader.getInstance().getConfigDir().resolve("jarstacker.json.bak");

	private static ModConfig INSTANCE = new ModConfig();
	private static long configRevision = 1L;

	private int configVersion = 2;

	private ItemStackingConfig itemStacking = new ItemStackingConfig();
	private MobStackingConfig mobStacking = new MobStackingConfig();
	private DisplayConfig display = new DisplayConfig();
	private PerformanceConfig performance = new PerformanceConfig();

	public static ModConfig getInstance() {
		return INSTANCE;
	}

	public static void setInstance(ModConfig config) {
		config.validate();
		INSTANCE = config;
		incrementRevision();
	}

	public static long getConfigRevision() {
		return configRevision;
	}

	public static void incrementRevision() {
		configRevision++;
	}

	public int getConfigVersion() {
		return configVersion;
	}

	public void setConfigVersion(int configVersion) {
		this.configVersion = configVersion;
	}

	public ItemStackingConfig getItemStacking() {
		return itemStacking;
	}

	public MobStackingConfig getMobStacking() {
		return mobStacking;
	}

	public DisplayConfig getDisplay() {
		return display;
	}

	public PerformanceConfig getPerformance() {
		return performance;
	}

	public String toJson() {
		return GSON.toJson(this);
	}

	public static ModConfig fromJson(String json) {
		ModConfig config = GSON.fromJson(json, ModConfig.class);
		if (config != null) {
			config.validate();
		}
		return config;
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try {
				String content = Files.readString(CONFIG_PATH);
				JsonObject json = JsonParser.parseString(content).getAsJsonObject();
				boolean isV1 = !json.has("configVersion") || json.get("configVersion").getAsInt() < 2;

				if (isV1) {
					LOGGER.info("Detected V0.1 config. Backing up to {} and migrating to V2 schema.", BACKUP_PATH);
					try {
						Files.copy(CONFIG_PATH, BACKUP_PATH, StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception e) {
						LOGGER.error("Failed to create config backup at {}", BACKUP_PATH, e);
					}
				}

				ModConfig loaded = GSON.fromJson(content, ModConfig.class);
				if (loaded != null) {
					loaded.setConfigVersion(2);
					loaded.validate();
					INSTANCE = loaded;
					if (isV1) {
						save(); // Save migrated schema to disk
					}
					LOGGER.info("Config loaded successfully from {}", CONFIG_PATH);
					return;
				}
			} catch (Exception e) {
				LOGGER.error("Failed to parse config file {}, creating backup and using defaults", CONFIG_PATH, e);
				try {
					Path corruptBackup = FabricLoader.getInstance().getConfigDir().resolve("jarstacker.json.corrupt");
					Files.copy(CONFIG_PATH, corruptBackup, StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception ex) {
					// Ignore secondary backup error
				}
			}
		}
		// If file missing or error, save current default
		INSTANCE = new ModConfig();
		INSTANCE.validate();
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(INSTANCE, writer);
			}
			LOGGER.info("Config saved to {}", CONFIG_PATH);
		} catch (Exception e) {
			LOGGER.error("Failed to save config file {}", CONFIG_PATH, e);
		}
	}

	public void validate() {
		if (itemStacking == null) itemStacking = new ItemStackingConfig();
		itemStacking.validate();

		if (mobStacking == null) mobStacking = new MobStackingConfig();
		mobStacking.validate();

		if (display == null) display = new DisplayConfig();
		display.validate();

		if (performance == null) performance = new PerformanceConfig();
	}

	public static class ItemStackingConfig {
		private boolean enabled = true;
		private double radius = 4.0;
		private int scanIntervalTicks = 10;
		private int maxStackSize = 4096;
		private boolean showLabel = true;
		private boolean stackUnstackableItems = false;
		private String filterMode = "BLACKLIST";
		private List<String> blacklist = new ArrayList<>();
		private List<String> whitelist = new ArrayList<>();
		private Map<String, ItemRuleConfig> rules = new LinkedHashMap<>();

		// Runtime cache
		private transient Set<String> cachedBlacklist = new HashSet<>();
		private transient Set<String> cachedWhitelist = new HashSet<>();

		public boolean isEnabled() { return enabled; }
		public void setEnabled(boolean enabled) { this.enabled = enabled; }

		public double getRadius() { return radius; }
		public void setRadius(double radius) { this.radius = radius; }

		public int getScanIntervalTicks() { return scanIntervalTicks; }
		public void setScanIntervalTicks(int scanIntervalTicks) { this.scanIntervalTicks = scanIntervalTicks; }

		public int getMaxStackSize() { return maxStackSize; }
		public void setMaxStackSize(int maxStackSize) { this.maxStackSize = maxStackSize; }

		public boolean isShowLabel() { return showLabel; }
		public void setShowLabel(boolean showLabel) { this.showLabel = showLabel; }

		public boolean isStackUnstackableItems() { return stackUnstackableItems; }
		public void setStackUnstackableItems(boolean stackUnstackableItems) { this.stackUnstackableItems = stackUnstackableItems; }

		public String getFilterMode() { return filterMode; }
		public void setFilterMode(String filterMode) { this.filterMode = filterMode; }

		public List<String> getBlacklist() { return blacklist; }
		public void setBlacklist(List<String> blacklist) { this.blacklist = blacklist; }

		public List<String> getWhitelist() { return whitelist; }
		public void setWhitelist(List<String> whitelist) { this.whitelist = whitelist; }

		public Map<String, ItemRuleConfig> getRules() { return rules; }
		public void setRules(Map<String, ItemRuleConfig> rules) { this.rules = rules; }

		public Set<String> getCachedBlacklist() { return cachedBlacklist; }
		public Set<String> getCachedWhitelist() { return cachedWhitelist; }

		public void validate() {
			if (radius < 1.0) radius = 1.0;
			if (radius > 32.0) radius = 32.0;
			if (scanIntervalTicks < 1) scanIntervalTicks = 1;
			if (scanIntervalTicks > 1200) scanIntervalTicks = 1200;
			if (maxStackSize < 1) maxStackSize = 4096;

			if (!"WHITELIST".equalsIgnoreCase(filterMode)) {
				filterMode = "BLACKLIST";
			} else {
				filterMode = "WHITELIST";
			}

			if (blacklist == null) blacklist = new ArrayList<>();
			if (whitelist == null) whitelist = new ArrayList<>();
			if (rules == null) rules = new LinkedHashMap<>();

			cachedBlacklist = new HashSet<>();
			for (String s : blacklist) {
				if (s != null && !s.isBlank()) cachedBlacklist.add(s.trim().toLowerCase(Locale.ROOT));
			}

			cachedWhitelist = new HashSet<>();
			for (String s : whitelist) {
				if (s != null && !s.isBlank()) cachedWhitelist.add(s.trim().toLowerCase(Locale.ROOT));
			}

			for (Map.Entry<String, ItemRuleConfig> entry : rules.entrySet()) {
				if (entry.getValue() != null) entry.getValue().validate();
			}
		}
	}

	public static class ItemRuleConfig {
		private Boolean enabled;
		private Integer maxStackSize;

		public Boolean getEnabled() { return enabled; }
		public void setEnabled(Boolean enabled) { this.enabled = enabled; }

		public Integer getMaxStackSize() { return maxStackSize; }
		public void setMaxStackSize(Integer maxStackSize) { this.maxStackSize = maxStackSize; }

		public void validate() {
			if (maxStackSize != null && maxStackSize < 1) {
				maxStackSize = 1;
			}
		}
	}

	public static class MobStackingConfig {
		private boolean enabled = true;
		private double radius = 6.0;
		private int scanIntervalTicks = 20;
		private int maxStackSize = 256;
		private boolean showLabel = true;
		private String deathMode = "SINGLE";
		private String filterMode = "BLACKLIST";
		private List<String> blacklist = new ArrayList<>();
		private List<String> whitelist = new ArrayList<>();
		private Map<String, MobRuleConfig> rules = new LinkedHashMap<>();

		// Runtime cache
		private transient Set<String> cachedBlacklist = new HashSet<>();
		private transient Set<String> cachedWhitelist = new HashSet<>();

		public boolean isEnabled() { return enabled; }
		public void setEnabled(boolean enabled) { this.enabled = enabled; }

		public double getRadius() { return radius; }
		public void setRadius(double radius) { this.radius = radius; }

		public int getScanIntervalTicks() { return scanIntervalTicks; }
		public void setScanIntervalTicks(int scanIntervalTicks) { this.scanIntervalTicks = scanIntervalTicks; }

		public int getMaxStackSize() { return maxStackSize; }
		public void setMaxStackSize(int maxStackSize) { this.maxStackSize = maxStackSize; }

		public boolean isShowLabel() { return showLabel; }
		public void setShowLabel(boolean showLabel) { this.showLabel = showLabel; }

		public String getDeathMode() { return deathMode; }
		public void setDeathMode(String deathMode) { this.deathMode = deathMode; }

		public String getFilterMode() { return filterMode; }
		public void setFilterMode(String filterMode) { this.filterMode = filterMode; }

		public List<String> getBlacklist() { return blacklist; }
		public void setBlacklist(List<String> blacklist) { this.blacklist = blacklist; }

		public List<String> getWhitelist() { return whitelist; }
		public void setWhitelist(List<String> whitelist) { this.whitelist = whitelist; }

		public Map<String, MobRuleConfig> getRules() { return rules; }
		public void setRules(Map<String, MobRuleConfig> rules) { this.rules = rules; }

		public Set<String> getCachedBlacklist() { return cachedBlacklist; }
		public Set<String> getCachedWhitelist() { return cachedWhitelist; }

		public void validate() {
			if (radius < 1.0) radius = 1.0;
			if (radius > 32.0) radius = 32.0;
			if (scanIntervalTicks < 1) scanIntervalTicks = 1;
			if (scanIntervalTicks > 1200) scanIntervalTicks = 1200;
			if (maxStackSize < 2) maxStackSize = 2;
			if (!"SINGLE".equalsIgnoreCase(deathMode)) deathMode = "SINGLE";

			if (!"WHITELIST".equalsIgnoreCase(filterMode)) {
				filterMode = "BLACKLIST";
			} else {
				filterMode = "WHITELIST";
			}

			if (blacklist == null) blacklist = new ArrayList<>();
			if (whitelist == null) whitelist = new ArrayList<>();
			if (rules == null) rules = new LinkedHashMap<>();

			cachedBlacklist = new HashSet<>();
			for (String s : blacklist) {
				if (s != null && !s.isBlank()) cachedBlacklist.add(s.trim().toLowerCase(Locale.ROOT));
			}

			cachedWhitelist = new HashSet<>();
			for (String s : whitelist) {
				if (s != null && !s.isBlank()) cachedWhitelist.add(s.trim().toLowerCase(Locale.ROOT));
			}

			for (Map.Entry<String, MobRuleConfig> entry : rules.entrySet()) {
				if (entry.getValue() != null) entry.getValue().validate();
			}
		}
	}

	public static class MobRuleConfig {
		private Boolean enabled;
		private Double radius;
		private Integer maxStackSize;

		public Boolean getEnabled() { return enabled; }
		public void setEnabled(Boolean enabled) { this.enabled = enabled; }

		public Double getRadius() { return radius; }
		public void setRadius(Double radius) { this.radius = radius; }

		public Integer getMaxStackSize() { return maxStackSize; }
		public void setMaxStackSize(Integer maxStackSize) { this.maxStackSize = maxStackSize; }

		public void validate() {
			if (radius != null && radius < 1.0) radius = 1.0;
			if (radius != null && radius > 32.0) radius = 32.0;
			if (maxStackSize != null && maxStackSize < 2) maxStackSize = 2;
		}
	}

	public static class DisplayConfig {
		private boolean showItemLabels = true;
		private boolean showMobLabels = true;
		private int labelUpdateIntervalTicks = 20;
		private boolean showCountOnlyWhenStacked = true;

		public boolean isShowItemLabels() { return showItemLabels; }
		public void setShowItemLabels(boolean showItemLabels) { this.showItemLabels = showItemLabels; }

		public boolean isShowMobLabels() { return showMobLabels; }
		public void setShowMobLabels(boolean showMobLabels) { this.showMobLabels = showMobLabels; }

		public int getLabelUpdateIntervalTicks() { return labelUpdateIntervalTicks; }
		public void setLabelUpdateIntervalTicks(int labelUpdateIntervalTicks) { this.labelUpdateIntervalTicks = labelUpdateIntervalTicks; }

		public boolean isShowCountOnlyWhenStacked() { return showCountOnlyWhenStacked; }
		public void setShowCountOnlyWhenStacked(boolean showCountOnlyWhenStacked) { this.showCountOnlyWhenStacked = showCountOnlyWhenStacked; }

		public void validate() {
			if (labelUpdateIntervalTicks < 1) labelUpdateIntervalTicks = 20;
		}
	}

	public static class PerformanceConfig {
		private boolean debugLogging = false;
		private boolean collectMetrics = true;

		public boolean isDebugLogging() { return debugLogging; }
		public void setDebugLogging(boolean debugLogging) { this.debugLogging = debugLogging; }

		public boolean isCollectMetrics() { return collectMetrics; }
		public void setCollectMetrics(boolean collectMetrics) { this.collectMetrics = collectMetrics; }
	}
}