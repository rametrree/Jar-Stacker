package com.jar.jarstacker.stack.mob.status;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the complete status effect state and absorption hearts
 * for a single logical member in a mob stack (Jar Stacker V0.6.0).
 */
public class LogicalStatusRecord {

	private final Map<Holder<MobEffect>, MobEffectInstance> activeEffects = new HashMap<>();
	private float absorptionAmount = 0.0f;

	public LogicalStatusRecord() {
	}

	public Map<Holder<MobEffect>, MobEffectInstance> getActiveEffects() {
		return activeEffects;
	}

	public Collection<MobEffectInstance> getEffectInstances() {
		return activeEffects.values();
	}

	public boolean hasEffect(Holder<MobEffect> effect) {
		return activeEffects.containsKey(effect);
	}

	public MobEffectInstance getEffect(Holder<MobEffect> effect) {
		return activeEffects.get(effect);
	}

	public boolean canBeAffected(LivingEntity representative, MobEffectInstance effectInstance) {
		if (representative == null) {
			return true;
		}
		if (representative.getType().is(EntityTypeTags.IMMUNE_TO_INFESTED)) {
			return !effectInstance.is(MobEffects.INFESTED);
		} else if (representative.getType().is(EntityTypeTags.IMMUNE_TO_OOZING)) {
			return !effectInstance.is(MobEffects.OOZING);
		} else if (representative.getType().is(EntityTypeTags.IGNORES_POISON_AND_REGEN)) {
			return !effectInstance.is(MobEffects.REGENERATION) && !effectInstance.is(MobEffects.POISON);
		}
		return true;
	}

	public boolean addEffect(MobEffectInstance instance, LivingEntity representative) {
		if (instance == null) {
			return false;
		}

		MobEffectInstance existing = activeEffects.get(instance.getEffect());
		if (existing == null) {
			activeEffects.put(instance.getEffect(), instance);
			if (representative != null) {
				instance.onEffectAdded(representative);
				instance.onEffectStarted(representative);
			}
			return true;
		} else if (existing.update(instance)) {
			if (representative != null) {
				instance.onEffectStarted(representative);
			}
			return true;
		}
		return false;
	}

	public boolean removeEffect(Holder<MobEffect> effect) {
		return activeEffects.remove(effect) != null;
	}

	public void clearEffects() {
		activeEffects.clear();
	}

	public float getAbsorptionAmount() {
		return absorptionAmount;
	}

	public void setAbsorptionAmount(float absorptionAmount) {
		this.absorptionAmount = Math.max(0.0f, absorptionAmount);
	}

	public LogicalStatusRecord copy() {
		LogicalStatusRecord copy = new LogicalStatusRecord();
		copy.absorptionAmount = this.absorptionAmount;
		for (Map.Entry<Holder<MobEffect>, MobEffectInstance> entry : this.activeEffects.entrySet()) {
			copy.activeEffects.put(entry.getKey(), new MobEffectInstance(entry.getValue()));
		}
		return copy;
	}

	public CompoundTag saveNbt() {
		CompoundTag tag = new CompoundTag();
		tag.putFloat("Absorption", this.absorptionAmount);

		ListTag effectsList = new ListTag();
		for (MobEffectInstance instance : this.activeEffects.values()) {
			Tag effectTag = instance.save();
			if (effectTag != null) {
				effectsList.add(effectTag);
			}
		}
		tag.put("Effects", effectsList);
		return tag;
	}

	public static LogicalStatusRecord loadNbt(CompoundTag tag) {
		LogicalStatusRecord record = new LogicalStatusRecord();
		if (tag == null) {
			return record;
		}

		if (tag.contains("Absorption", Tag.TAG_FLOAT)) {
			record.absorptionAmount = tag.getFloat("Absorption");
		}

		if (tag.contains("Effects", Tag.TAG_LIST)) {
			ListTag effectsList = tag.getList("Effects", Tag.TAG_COMPOUND);
			for (int i = 0; i < effectsList.size(); i++) {
				CompoundTag effectCompound = effectsList.getCompound(i);
				MobEffectInstance instance = MobEffectInstance.load(effectCompound);
				if (instance != null) {
					record.activeEffects.put(instance.getEffect(), instance);
				}
			}
		}
		return record;
	}
}

