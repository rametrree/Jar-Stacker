package com.jar.jarstacker.client.integration;

import com.jar.jarstacker.client.gui.JarStackerConfigScreenFactory;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class JarStackerModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return JarStackerConfigScreenFactory::createConfigScreen;
	}
}

