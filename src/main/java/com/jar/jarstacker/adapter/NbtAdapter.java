package com.jar.jarstacker.adapter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public final class NbtAdapter {
	private NbtAdapter() {}

	public static int getInt(CompoundTag tag, String key) {
		//? if >=1.21.5 {
		/*return tag.getIntOr(key, 0);
		*///?} else {
		return tag.getInt(key);
		//?}
	}

	public static boolean getBoolean(CompoundTag tag, String key) {
		//? if >=1.21.5 {
		/*return tag.getBooleanOr(key, false);
		*///?} else {
		return tag.getBoolean(key);
		//?}
	}

	public static float getFloat(CompoundTag tag, String key) {
		//? if >=1.21.5 {
		/*return tag.getFloatOr(key, 0.0f);
		*///?} else {
		return tag.getFloat(key);
		//?}
	}

	public static CompoundTag getCompound(CompoundTag tag, String key) {
		//? if >=1.21.5 {
		/*return tag.getCompoundOrEmpty(key);
		*///?} else {
		return tag.getCompound(key);
		//?}
	}

	public static long[] getLongArray(CompoundTag tag, String key) {
		//? if >=1.21.5 {
		/*return tag.getLongArray(key).orElse(new long[0]);
		*///?} else {
		return tag.getLongArray(key);
		//?}
	}

	public static CompoundTag getCompound(ListTag list, int index) {
		//? if >=1.21.5 {
		/*return list.getCompoundOrEmpty(index);
		*///?} else {
		return list.getCompound(index);
		//?}
	}

	public static float getFloat(ListTag list, int index) {
		//? if >=1.21.5 {
		/*return list.getFloatOr(index, 0.0f);
		*///?} else {
		return list.getFloat(index);
		//?}
	}

	public static ListTag getList(CompoundTag tag, String key, int type) {
		//? if >=1.21.5 {
		/*return tag.getListOrEmpty(key);
		*///?} else {
		return tag.getList(key, type);
		//?}
	}

	public static boolean contains(CompoundTag tag, String key, int type) {
		if (tag == null) return false;
		//? if >=1.21.5 {
		/*return tag.contains(key);
		*///?} else {
		return tag.contains(key, type);
		//?}
	}
}
