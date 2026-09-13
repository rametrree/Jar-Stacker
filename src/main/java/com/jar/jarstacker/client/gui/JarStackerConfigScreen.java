package com.jar.jarstacker.client.gui;

import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.network.JarStackerPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.*;

public class JarStackerConfigScreen extends Screen {
	private final Screen parent;
	private final ModConfig model;
	private final long baseRevision;

	public JarStackerConfigScreen(Screen parent, ModConfig sourceConfig, long baseRevision) {
		super(Component.literal("Jar Stacker Configuration"));
		this.parent = parent;
		// Clone source config to work on a temporary model
		this.model = ModConfig.fromJson(sourceConfig.toJson());
		this.baseRevision = baseRevision;
	}

	public ModConfig getModel() {
		return model;
	}

	public long getBaseRevision() {
		return baseRevision;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int startY = this.height / 4 - 10;
		int buttonWidth = 200;
		int buttonHeight = 20;
		int spacing = 24;

		// 1. Item Stacking Submenu
		this.addRenderableWidget(Button.builder(Component.literal("Item Stacking Options"),
			b -> this.minecraft.setScreen(new ItemConfigScreen(this, model))
		).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

		// 2. Mob Stacking Submenu
		this.addRenderableWidget(Button.builder(Component.literal("Mob Stacking Options"),
			b -> this.minecraft.setScreen(new MobConfigScreen(this, model))
		).bounds(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

		// 3. Filters & Rules Submenu
		this.addRenderableWidget(Button.builder(Component.literal("Filters & Rules"),
			b -> this.minecraft.setScreen(new FiltersRulesMenuScreen(this, model))
		).bounds(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());

		// 4. Display Submenu
		this.addRenderableWidget(Button.builder(Component.literal("Display Settings"),
			b -> this.minecraft.setScreen(new DisplayConfigScreen(this, model))
		).bounds(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight).build());

		// 5. Performance Submenu
		this.addRenderableWidget(Button.builder(Component.literal("Performance & Metrics"),
			b -> this.minecraft.setScreen(new PerformanceConfigScreen(this, model))
		).bounds(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight).build());

		// 6. Reset to Defaults
		this.addRenderableWidget(Button.builder(Component.literal("Reset to Defaults"), b -> {
			ModConfig def = new ModConfig();
			def.validate();
			this.minecraft.setScreen(new JarStackerConfigScreen(parent, def, baseRevision));
		}).bounds(centerX - buttonWidth / 2, startY + spacing * 5 + 4, buttonWidth, buttonHeight).build());

		// 7. Save and Cancel
		int bottomY = this.height - 32;
		this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
			model.validate();
			if (this.minecraft.isSingleplayer()) {
				// Apply directly on integrated server
				ModConfig.setInstance(model);
				ModConfig.save();
				if (this.minecraft.player != null) {
					this.minecraft.player.displayClientMessage(Component.literal("Jar Stacker configuration saved!"), false);
				}
			} else {
				// Send update payload to multiplayer server
				ClientPlayNetworking.send(new JarStackerPackets.ConfigUpdatePayload(baseRevision, model.toJson()));
			}
			this.minecraft.setScreen(parent);
		}).bounds(centerX - 105, bottomY, 100, buttonHeight).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> {
			// Cancel discards changes without sending any update
			this.minecraft.setScreen(parent);
		}).bounds(centerX + 5, bottomY, 100, buttonHeight).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
		String revText = "Revision: " + baseRevision;
		guiGraphics.drawString(this.font, revText, 10, this.height - 15, 0x888888);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}

	// =========================================================================
	// SUB-SCREENS
	// =========================================================================

	public static class ItemConfigScreen extends Screen {
		private final Screen parent;
		private final ModConfig model;
		private EditBox radiusBox;
		private EditBox intervalBox;
		private EditBox maxStackBox;

		public ItemConfigScreen(Screen parent, ModConfig model) {
			super(Component.literal("Item Stacking Settings"));
			this.parent = parent;
			this.model = model;
		}

		@Override
		protected void init() {
			int cx = this.width / 2;
			int y = 45;
			int w = 220;
			int h = 20;

			// Enabled toggle
			this.addRenderableWidget(Button.builder(Component.literal("Item Stacking: " + (model.getItemStacking().isEnabled() ? "ON" : "OFF")), b -> {
				boolean next = !model.getItemStacking().isEnabled();
				model.getItemStacking().setEnabled(next);
				b.setMessage(Component.literal("Item Stacking: " + (next ? "ON" : "OFF")));
			}).bounds(cx - w / 2, y, w, h).build());
			y += 24;

			// Radius Box
			radiusBox = new EditBox(this.font, cx - w / 2, y + 10, w, h, Component.literal("Radius"));
			radiusBox.setValue(String.valueOf(model.getItemStacking().getRadius()));
			this.addRenderableWidget(radiusBox);
			y += 34;

			// Interval Box
			intervalBox = new EditBox(this.font, cx - w / 2, y + 10, w, h, Component.literal("Interval"));
			intervalBox.setValue(String.valueOf(model.getItemStacking().getScanIntervalTicks()));
			this.addRenderableWidget(intervalBox);
			y += 34;

			// Max Stack Box
			maxStackBox = new EditBox(this.font, cx - w / 2, y + 10, w, h, Component.literal("Max Stack"));
			maxStackBox.setValue(String.valueOf(model.getItemStacking().getMaxStackSize()));
			this.addRenderableWidget(maxStackBox);
			y += 34;

			// Filter Mode Toggle
			this.addRenderableWidget(Button.builder(Component.literal("Filter Mode: " + model.getItemStacking().getFilterMode()), b -> {
				String mode = "BLACKLIST".equalsIgnoreCase(model.getItemStacking().getFilterMode()) ? "WHITELIST" : "BLACKLIST";
				model.getItemStacking().setFilterMode(mode);
				b.setMessage(Component.literal("Filter Mode: " + mode));
			}).bounds(cx - w / 2, y, w, h).build());
			y += 24;

			// Done button
			this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
				try {
					model.getItemStacking().setRadius(Double.parseDouble(radiusBox.getValue()));
				} catch (Exception ignored) {}
				try {
					model.getItemStacking().setScanIntervalTicks(Integer.parseInt(intervalBox.getValue()));
				} catch (Exception ignored) {}
				try {
					model.getItemStacking().setMaxStackSize(Integer.parseInt(maxStackBox.getValue()));
				} catch (Exception ignored) {}
				model.validate();
				this.minecraft.setScreen(parent);
			}).bounds(cx - 100, this.height - 30, 200, 20).build());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
			int cx = this.width / 2 - 110;
			guiGraphics.drawString(this.font, "Merge Radius (blocks):", cx, 69, 0xAAAAAA);
			guiGraphics.drawString(this.font, "Scan Interval (ticks):", cx, 103, 0xAAAAAA);
			guiGraphics.drawString(this.font, "Max Stack Size:", cx, 137, 0xAAAAAA);
			super.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	public static class MobConfigScreen extends Screen {
		private final Screen parent;
		private final ModConfig model;
		private EditBox radiusBox;
		private EditBox intervalBox;
		private EditBox maxStackBox;

		public MobConfigScreen(Screen parent, ModConfig model) {
			super(Component.literal("Mob Stacking Settings"));
			this.parent = parent;
			this.model = model;
		}

		@Override
		protected void init() {
			int cx = this.width / 2;
			int y = 45;
			int w = 220;
			int h = 20;

			// Enabled toggle
			this.addRenderableWidget(Button.builder(Component.literal("Mob Stacking: " + (model.getMobStacking().isEnabled() ? "ON" : "OFF")), b -> {
				boolean next = !model.getMobStacking().isEnabled();
				model.getMobStacking().setEnabled(next);
				b.setMessage(Component.literal("Mob Stacking: " + (next ? "ON" : "OFF")));
			}).bounds(cx - w / 2, y, w, h).build());
			y += 24;

			// Radius Box
			radiusBox = new EditBox(this.font, cx - w / 2, y + 10, w, h, Component.literal("Radius"));
			radiusBox.setValue(String.valueOf(model.getMobStacking().getRadius()));
			this.addRenderableWidget(radiusBox);
			y += 34;

			// Interval Box
			intervalBox = new EditBox(this.font, cx - w / 2, y + 10, w, h, Component.literal("Interval"));
			intervalBox.setValue(String.valueOf(model.getMobStacking().getScanIntervalTicks()));
			this.addRenderableWidget(intervalBox);
			y += 34;

			// Max Stack Box
			maxStackBox = new EditBox(this.font, cx - w / 2, y + 10, w, h, Component.literal("Max Stack"));
			maxStackBox.setValue(String.valueOf(model.getMobStacking().getMaxStackSize()));
			this.addRenderableWidget(maxStackBox);
			y += 34;

			// Death Mode (Single only in V0.2)
			Button deathBtn = Button.builder(Component.literal("Death Mode: SINGLE"), b -> {}).bounds(cx - w / 2, y, w, h).build();
			deathBtn.active = false;
			this.addRenderableWidget(deathBtn);
			y += 24;

			// Filter Mode Toggle
			this.addRenderableWidget(Button.builder(Component.literal("Filter Mode: " + model.getMobStacking().getFilterMode()), b -> {
				String mode = "BLACKLIST".equalsIgnoreCase(model.getMobStacking().getFilterMode()) ? "WHITELIST" : "BLACKLIST";
				model.getMobStacking().setFilterMode(mode);
				b.setMessage(Component.literal("Filter Mode: " + mode));
			}).bounds(cx - w / 2, y, w, h).build());

			// Done button
			this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
				try {
					model.getMobStacking().setRadius(Double.parseDouble(radiusBox.getValue()));
				} catch (Exception ignored) {}
				try {
					model.getMobStacking().setScanIntervalTicks(Integer.parseInt(intervalBox.getValue()));
				} catch (Exception ignored) {}
				try {
					model.getMobStacking().setMaxStackSize(Integer.parseInt(maxStackBox.getValue()));
				} catch (Exception ignored) {}
				model.validate();
				this.minecraft.setScreen(parent);
			}).bounds(cx - 100, this.height - 30, 200, 20).build());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
			int cx = this.width / 2 - 110;
			guiGraphics.drawString(this.font, "Merge Radius (blocks):", cx, 69, 0xAAAAAA);
			guiGraphics.drawString(this.font, "Scan Interval (ticks):", cx, 103, 0xAAAAAA);
			guiGraphics.drawString(this.font, "Max Stack Size:", cx, 137, 0xAAAAAA);
			super.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	public static class FiltersRulesMenuScreen extends Screen {
		private final Screen parent;
		private final ModConfig model;

		public FiltersRulesMenuScreen(Screen parent, ModConfig model) {
			super(Component.literal("Filters & Rules Menu"));
			this.parent = parent;
			this.model = model;
		}

		@Override
		protected void init() {
			int cx = this.width / 2;
			int y = 50;
			int w = 220;
			int h = 20;

			// Item Blacklist
			this.addRenderableWidget(Button.builder(Component.literal("Edit Item Blacklist (" + model.getItemStacking().getBlacklist().size() + ")"),
				b -> this.minecraft.setScreen(new ListEditorScreen(this, "Item Blacklist", model.getItemStacking().getBlacklist(), true))
			).bounds(cx - w / 2, y, w, h).build());
			y += 26;

			// Item Whitelist
			this.addRenderableWidget(Button.builder(Component.literal("Edit Item Whitelist (" + model.getItemStacking().getWhitelist().size() + ")"),
				b -> this.minecraft.setScreen(new ListEditorScreen(this, "Item Whitelist", model.getItemStacking().getWhitelist(), true))
			).bounds(cx - w / 2, y, w, h).build());
			y += 26;

			// Mob Blacklist
			this.addRenderableWidget(Button.builder(Component.literal("Edit Mob Blacklist (" + model.getMobStacking().getBlacklist().size() + ")"),
				b -> this.minecraft.setScreen(new ListEditorScreen(this, "Mob Blacklist", model.getMobStacking().getBlacklist(), false))
			).bounds(cx - w / 2, y, w, h).build());
			y += 26;

			// Mob Whitelist
			this.addRenderableWidget(Button.builder(Component.literal("Edit Mob Whitelist (" + model.getMobStacking().getWhitelist().size() + ")"),
				b -> this.minecraft.setScreen(new ListEditorScreen(this, "Mob Whitelist", model.getMobStacking().getWhitelist(), false))
			).bounds(cx - w / 2, y, w, h).build());
			y += 26;

			// Item Rules
			this.addRenderableWidget(Button.builder(Component.literal("Item Rules (" + model.getItemStacking().getRules().size() + ")"),
				b -> this.minecraft.setScreen(new ItemRulesScreen(this, model))
			).bounds(cx - w / 2, y, w, h).build());
			y += 26;

			// Mob Rules
			this.addRenderableWidget(Button.builder(Component.literal("Mob Rules (" + model.getMobStacking().getRules().size() + ")"),
				b -> this.minecraft.setScreen(new MobRulesScreen(this, model))
			).bounds(cx - w / 2, y, w, h).build());

			// Back
			this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> this.minecraft.setScreen(parent))
				.bounds(cx - 100, this.height - 30, 200, 20).build());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
			super.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	public static class ListEditorScreen extends Screen {
		private final Screen parent;
		private final String titleName;
		private final List<String> list;
		private final boolean isItem;
		private EditBox searchBox;
		private int selectedIndex = -1;

		public ListEditorScreen(Screen parent, String titleName, List<String> list, boolean isItem) {
			super(Component.literal(titleName));
			this.parent = parent;
			this.titleName = titleName;
			this.list = list;
			this.isItem = isItem;
		}

		@Override
		protected void init() {
			int cx = this.width / 2;

			searchBox = new EditBox(this.font, cx - 110, 45, 160, 20, Component.literal("Registry ID"));
			this.addRenderableWidget(searchBox);

			this.addRenderableWidget(Button.builder(Component.literal("Add"), b -> {
				String val = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
				if (!val.isBlank() && !list.contains(val)) {
					list.add(val);
					searchBox.setValue("");
				}
			}).bounds(cx + 55, 45, 55, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Remove Selected"), b -> {
				if (selectedIndex >= 0 && selectedIndex < list.size()) {
					list.remove(selectedIndex);
					selectedIndex = -1;
				}
			}).bounds(cx - 100, this.height - 55, 200, 20).build());

			this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.minecraft.setScreen(parent))
				.bounds(cx - 100, this.height - 30, 200, 20).build());
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			int cx = this.width / 2;
			int startY = 75;
			int rowH = 12;
			for (int i = 0; i < Math.min(10, list.size()); i++) {
				int itemY = startY + i * rowH;
				if (mouseY >= itemY && mouseY < itemY + rowH && mouseX >= cx - 110 && mouseX <= cx + 110) {
					selectedIndex = i;
					return true;
				}
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
			guiGraphics.drawString(this.font, "Type registry ID (e.g. " + (isItem ? "minecraft:diamond" : "minecraft:zombie") + "):", this.width / 2 - 110, 32, 0xAAAAAA);

			int cx = this.width / 2;
			int startY = 75;
			int rowH = 12;
			for (int i = 0; i < Math.min(10, list.size()); i++) {
				String item = list.get(i);
				int color = (i == selectedIndex) ? 0xFFFF55 : 0xFFFFFF;
				guiGraphics.drawString(this.font, "• " + item, cx - 105, startY + i * rowH, color);
			}
			if (list.isEmpty()) {
				guiGraphics.drawCenteredString(this.font, "(List is currently empty)", cx, startY + 20, 0x777777);
			} else if (list.size() > 10) {
				guiGraphics.drawString(this.font, "... and " + (list.size() - 10) + " more", cx - 105, startY + 10 * rowH, 0x888888);
			}

			super.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	public static class ItemRulesScreen extends Screen {
		private final Screen parent;
		private final ModConfig model;
		private EditBox idBox;
		private EditBox capBox;

		public ItemRulesScreen(Screen parent, ModConfig model) {
			super(Component.literal("Item Custom Stack Rules"));
			this.parent = parent;
			this.model = model;
		}

		@Override
		protected void init() {
			int cx = this.width / 2;
			idBox = new EditBox(this.font, cx - 110, 45, 140, 20, Component.literal("Item ID"));
			this.addRenderableWidget(idBox);

			capBox = new EditBox(this.font, cx + 35, 45, 75, 20, Component.literal("Max Stack"));
			capBox.setValue("128");
			this.addRenderableWidget(capBox);

			this.addRenderableWidget(Button.builder(Component.literal("Add / Update Rule"), b -> {
				String id = idBox.getValue().trim().toLowerCase(Locale.ROOT);
				if (!id.isBlank()) {
					int cap = 4096;
					try {
						cap = Integer.parseInt(capBox.getValue().trim());
					} catch (Exception ignored) {}
					ModConfig.ItemRuleConfig rule = new ModConfig.ItemRuleConfig();
					rule.setEnabled(true);
					rule.setMaxStackSize(cap);
					model.getItemStacking().getRules().put(id, rule);
					idBox.setValue("");
				}
			}).bounds(cx - 110, 70, 220, 20).build());

			this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.minecraft.setScreen(parent))
				.bounds(cx - 100, this.height - 30, 200, 20).build());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
			guiGraphics.drawString(this.font, "Item ID (e.g. minecraft:cobblestone):", this.width / 2 - 110, 32, 0xAAAAAA);
			guiGraphics.drawString(this.font, "Max Stack:", this.width / 2 + 35, 32, 0xAAAAAA);

			int cx = this.width / 2;
			int startY = 100;
			int count = 0;
			for (Map.Entry<String, ModConfig.ItemRuleConfig> entry : model.getItemStacking().getRules().entrySet()) {
				if (count++ >= 8) break;
				guiGraphics.drawString(this.font, "• " + entry.getKey() + " -> max: " + entry.getValue().getMaxStackSize(), cx - 105, startY + (count - 1) * 12, 0xFFFFFF);
			}
			if (model.getItemStacking().getRules().isEmpty()) {
				guiGraphics.drawCenteredString(this.font, "(No per-item rules defined)", cx, startY + 15, 0x777777);
			}

			super.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	public static class MobRulesScreen extends Screen {
		private final Screen parent;
		private final ModConfig model;
		private EditBox idBox;
		private EditBox capBox;

		public MobRulesScreen(Screen parent, ModConfig model) {
			super(Component.literal("Mob Custom Stack Rules"));
			this.parent = parent;
			this.model = model;
		}

		@Override
		protected void init() {
			int cx = this.width / 2;
			idBox = new EditBox(this.font, cx - 110, 45, 140, 20, Component.literal("Mob ID"));
			this.addRenderableWidget(idBox);

			capBox = new EditBox(this.font, cx + 35, 45, 75, 20, Component.literal("Max Stack"));
			capBox.setValue("20");
			this.addRenderableWidget(capBox);

			this.addRenderableWidget(Button.builder(Component.literal("Add / Update Rule"), b -> {
				String id = idBox.getValue().trim().toLowerCase(Locale.ROOT);
				if (!id.isBlank()) {
					int cap = 256;
					try {
						cap = Integer.parseInt(capBox.getValue().trim());
					} catch (Exception ignored) {}
					ModConfig.MobRuleConfig rule = new ModConfig.MobRuleConfig();
					rule.setEnabled(true);
					rule.setMaxStackSize(cap);
					model.getMobStacking().getRules().put(id, rule);
					idBox.setValue("");
				}
			}).bounds(cx - 110, 70, 220, 20).build());

			this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.minecraft.setScreen(parent))
				.bounds(cx - 100, this.height - 30, 200, 20).build());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
			guiGraphics.drawString(this.font, "Mob ID (e.g. minecraft:zombie):", this.width / 2 - 110, 32, 0xAAAAAA);
			guiGraphics.drawString(this.font, "Max Stack:", this.width / 2 + 35, 32, 0xAAAAAA);

			int cx = this.width / 2;
			int startY = 100;
			int count = 0;
			for (Map.Entry<String, ModConfig.MobRuleConfig> entry : model.getMobStacking().getRules().entrySet()) {
				if (count++ >= 8) break;
				guiGraphics.drawString(this.font, "• " + entry.getKey() + " -> max: " + entry.getValue().getMaxStackSize(), cx - 105, startY + (count - 1) * 12, 0xFFFFFF);
			}
			if (model.getMobStacking().getRules().isEmpty()) {
				guiGraphics.drawCenteredString(this.font, "(No per-mob rules defined)", cx, startY + 15, 0x777777);
			}

			super.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	public static class DisplayConfigScreen extends Screen {
		private final Screen parent;
		private final ModConfig model;

		public DisplayConfigScreen(Screen parent, ModConfig model) {
			super(Component.literal("Display Settings"));
			this.parent = parent;
			this.model = model;
		}

		@Override
		protected void init() {
			int cx = this.width / 2;
			int y = 50;
			int w = 220;
			int h = 20;

			// Show Item Labels
			this.addRenderableWidget(Button.builder(Component.literal("Item Labels: " + (model.getDisplay().isShowItemLabels() ? "ON" : "OFF")), b -> {
				boolean next = !model.getDisplay().isShowItemLabels();
				model.getDisplay().setShowItemLabels(next);
				b.setMessage(Component.literal("Item Labels: " + (next ? "ON" : "OFF")));
			}).bounds(cx - w / 2, y, w, h).build());
			y += 26;

			// Show Mob Labels
			this.addRenderableWidget(Button.builder(Component.literal("Mob Labels: " + (model.getDisplay().isShowMobLabels() ? "ON" : "OFF")), b -> {
				boolean next = !model.getDisplay().isShowMobLabels();
				model.getDisplay().setShowMobLabels(next);
				b.setMessage(Component.literal("Mob Labels: " + (next ? "ON" : "OFF")));
			}).bounds(cx - w / 2, y, w, h).build());
			y += 26;

			// Show Count Only When Stacked (>1)
			this.addRenderableWidget(Button.builder(Component.literal("Hide Label When Count is 1: " + (model.getDisplay().isShowCountOnlyWhenStacked() ? "YES" : "NO")), b -> {
				boolean next = !model.getDisplay().isShowCountOnlyWhenStacked();
				model.getDisplay().setShowCountOnlyWhenStacked(next);
				b.setMessage(Component.literal("Hide Label When Count is 1: " + (next ? "YES" : "NO")));
			}).bounds(cx - w / 2, y, w, h).build());

			// Done button
			this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.minecraft.setScreen(parent))
				.bounds(cx - 100, this.height - 30, 200, 20).build());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
			super.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	public static class PerformanceConfigScreen extends Screen {
		private final Screen parent;
		private final ModConfig model;

		public PerformanceConfigScreen(Screen parent, ModConfig model) {
			super(Component.literal("Performance & Metrics"));
			this.parent = parent;
			this.model = model;
		}

		@Override
		protected void init() {
			int cx = this.width / 2;
			int y = 50;
			int w = 220;
			int h = 20;

			// Debug Logging
			this.addRenderableWidget(Button.builder(Component.literal("Debug Logging: " + (model.getPerformance().isDebugLogging() ? "ON" : "OFF")), b -> {
				boolean next = !model.getPerformance().isDebugLogging();
				model.getPerformance().setDebugLogging(next);
				b.setMessage(Component.literal("Debug Logging: " + (next ? "ON" : "OFF")));
			}).bounds(cx - w / 2, y, w, h).build());
			y += 26;

			// Collect Metrics
			this.addRenderableWidget(Button.builder(Component.literal("Collect Metrics: " + (model.getPerformance().isCollectMetrics() ? "ON" : "OFF")), b -> {
				boolean next = !model.getPerformance().isCollectMetrics();
				model.getPerformance().setCollectMetrics(next);
				b.setMessage(Component.literal("Collect Metrics: " + (next ? "ON" : "OFF")));
			}).bounds(cx - w / 2, y, w, h).build());

			// Done button
			this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.minecraft.setScreen(parent))
				.bounds(cx - 100, this.height - 30, 200, 20).build());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
			super.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}
}

