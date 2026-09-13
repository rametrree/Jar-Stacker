package com.jar.jarstacker.stack.item;

import com.jar.jarstacker.config.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class ItemCompatibility {

	public static class ItemInspection {
		public final boolean stackable;
		public final String reason;

		public ItemInspection(boolean stackable, String reason) {
			this.stackable = stackable;
			this.reason = reason;
		}
	}

	public static ItemInspection inspectItem(ItemEntity entity, ModConfig.ItemStackingConfig config) {
		if (!entity.isAlive() || entity.isRemoved()) {
			return new ItemInspection(false, "Entity is dead or removed");
		}
		ItemStack stack = entity.getItem();
		if (stack.isEmpty()) {
			return new ItemInspection(false, "Item is empty");
		}
		if (!config.isStackUnstackableItems() && stack.getMaxStackSize() <= 1) {
			return new ItemInspection(false, "Item is normally unstackable");
		}
		if (stack.isDamageableItem() && stack.isDamaged()) {
			return new ItemInspection(false, "Item is damaged");
		}

		ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
		String itemId = key.toString();

		if ("BLACKLIST".equalsIgnoreCase(config.getFilterMode())) {
			if (config.getCachedBlacklist().contains(itemId)) {
				return new ItemInspection(false, "Item is in blacklist (" + itemId + ")");
			}
		} else if ("WHITELIST".equalsIgnoreCase(config.getFilterMode())) {
			if (!config.getCachedWhitelist().contains(itemId)) {
				return new ItemInspection(false, "Item is not in whitelist (" + itemId + ")");
			}
		}

		ModConfig.ItemRuleConfig rule = config.getRules().get(itemId);
		if (rule != null && rule.getEnabled() != null && !rule.getEnabled()) {
			return new ItemInspection(false, "Item stacking disabled by per-item rule (" + itemId + ")");
		}

		return new ItemInspection(true, "Stackable");
	}

	public static int getMaxStackSize(ItemStack stack, ModConfig.ItemStackingConfig config) {
		ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
		String itemId = key.toString();
		ModConfig.ItemRuleConfig rule = config.getRules().get(itemId);
		if (rule != null && rule.getMaxStackSize() != null && rule.getMaxStackSize() > 0) {
			return rule.getMaxStackSize();
		}
		return config.getMaxStackSize();
	}

	public static boolean canStack(ItemEntity a, ItemEntity b, ModConfig.ItemStackingConfig config) {
		if (!a.isAlive() || !b.isAlive() || a.isRemoved() || b.isRemoved()) {
			return false;
		}
		if (a == b) {
			return false;
		}

		ItemStack stackA = a.getItem();
		ItemStack stackB = b.getItem();
		if (stackA.isEmpty() || stackB.isEmpty()) {
			return false;
		}

		if (!config.isStackUnstackableItems() && stackA.getMaxStackSize() <= 1) {
			return false;
		}

		if (stackA.isDamageableItem() && (stackA.isDamaged() || stackB.isDamaged())) {
			return false;
		}

		if (!ItemStack.isSameItemSameComponents(stackA, stackB)) {
			return false;
		}

		ResourceLocation key = BuiltInRegistries.ITEM.getKey(stackA.getItem());
		String itemId = key.toString();

		if ("BLACKLIST".equalsIgnoreCase(config.getFilterMode())) {
			if (config.getCachedBlacklist().contains(itemId)) {
				return false;
			}
		} else if ("WHITELIST".equalsIgnoreCase(config.getFilterMode())) {
			if (!config.getCachedWhitelist().contains(itemId)) {
				return false;
			}
		}

		ModConfig.ItemRuleConfig rule = config.getRules().get(itemId);
		if (rule != null && rule.getEnabled() != null && !rule.getEnabled()) {
			return false;
		}

		return true;
	}
}