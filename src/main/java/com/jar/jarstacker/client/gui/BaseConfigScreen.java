package com.jar.jarstacker.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
//?}

public abstract class BaseConfigScreen extends Screen {

	protected BaseConfigScreen(Component title) {
		super(title);
	}

	//? if >=26.1 {
	/*@Override
	public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		renderGui(new GuiDrawer(guiGraphics), mouseX, mouseY, partialTick);
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}
	*///?} else {
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		renderGui(new GuiDrawer(guiGraphics), mouseX, mouseY, partialTick);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}
	//?}

	protected void renderGui(GuiDrawer drawer, int mouseX, int mouseY, float partialTick) {
		drawer.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
	}

	public static void setScreen(net.minecraft.client.Minecraft mc, Screen screen) {
		if (mc == null) return;
		//? if >=26.2 {
		/*mc.gui.setScreen(screen);
		*///?} else {
		mc.setScreen(screen);
		//?}
	}

	public static Screen getScreen(net.minecraft.client.Minecraft mc) {
		if (mc == null) return null;
		//? if >=26.2 {
		/*return mc.gui.screen();
		*///?} else {
		return mc.screen;
		//?}
	}

	public static boolean isSingleplayer(net.minecraft.client.Minecraft mc) {
		if (mc == null) return false;
		//? if >=26.2 {
		/*return mc.hasSingleplayerServer();
		*///?} else {
		return mc.isSingleplayer();
		//?}
	}

	public static class GuiDrawer {
		//? if >=26.1 {
		/*private final net.minecraft.client.gui.GuiGraphicsExtractor g;

		public GuiDrawer(net.minecraft.client.gui.GuiGraphicsExtractor g) {
			this.g = g;
		}

		public void drawString(Font font, String text, int x, int y, int color) {
			g.text(font, text, x, y, color, false);
		}

		public void drawCenteredString(Font font, Component text, int x, int y, int color) {
			g.centeredText(font, text, x, y, color);
		}

		public void drawCenteredString(Font font, String text, int x, int y, int color) {
			g.centeredText(font, text, x, y, color);
		}
		*///?} else {
		private final GuiGraphics g;

		public GuiDrawer(GuiGraphics g) {
			this.g = g;
		}

		public void drawString(Font font, String text, int x, int y, int color) {
			g.drawString(font, text, x, y, color);
		}

		public void drawCenteredString(Font font, Component text, int x, int y, int color) {
			g.drawCenteredString(font, text, x, y, color);
		}

		public void drawCenteredString(Font font, String text, int x, int y, int color) {
			g.drawCenteredString(font, text, x, y, color);
		}
		//?}
	}
}
