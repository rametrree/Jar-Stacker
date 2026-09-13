package com.jar.jarstacker.stack.mob.status;

import com.jar.jarstacker.stack.mob.health.LogicalHealthManager;
import com.jar.jarstacker.stack.mob.health.LogicalHealthState;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Executes generic Vanilla MobEffect callbacks for logical stack members
 * using a strictly scoped temporary projection transaction.
 */
public class LogicalVanillaEffectExecutor {

	public static boolean executeVanillaTick(LivingEntity representative, int memberIndex, Holder<MobEffect> holder, MobEffectInstance instance) {
		if (representative == null || holder == null || instance == null) {
			return false;
		}

		LogicalHealthState healthState = LogicalHealthManager.getOrCreateState(representative);
		LogicalStatusEffectState statusState = LogicalStatusEffectManager.getOrCreateStatusState(representative);
		if (healthState == null || statusState == null || memberIndex >= healthState.size() || memberIndex >= statusState.size()) {
			return false;
		}

		LogicalStatusRecord record = statusState.get(memberIndex);
		if (record == null) {
			return false;
		}

		// 1. Capture representative physical state
		float repHp = representative.getHealth();
		float repAbsorption = representative.getAbsorptionAmount();

		// 2. Project target member state onto representative
		float memberHp = healthState.get(memberIndex);
		float memberAbsorption = record.getAbsorptionAmount();
		representative.setHealth(memberHp);
		representative.setAbsorptionAmount(memberAbsorption);

		boolean keepEffect = true;
		try (LogicalEffectExecutionContext.Scope scope = LogicalEffectExecutionContext.open(representative.getUUID(), memberIndex, holder)) {
			// 3. Invoke actual Vanilla applyEffectTick
			keepEffect = com.jar.jarstacker.adapter.EffectAdapter.applyEffectTick(holder, representative, instance.getAmplifier());

			// 4. Capture resulting state into target logical member
			float postHp = representative.getHealth();
			float postAbsorption = representative.getAbsorptionAmount();

			if (postHp <= 0.0f) {
				// Member died during effect execution: route to LogicalHealthManager
				LogicalHealthManager.applyLogicalDirectDamage(representative, memberIndex, representative.damageSources().magic(), memberHp);
			} else {
				healthState.set(memberIndex, postHp);
				record.setAbsorptionAmount(postAbsorption);
			}
		} catch (Throwable t) {
			keepEffect = false;
		} finally {
			// 5. Restore representative physical state
			if (memberIndex == 0) {
				representative.setHealth(healthState.getActiveHealth());
				representative.setAbsorptionAmount(record.getAbsorptionAmount());
			} else {
				representative.setHealth(repHp);
				representative.setAbsorptionAmount(repAbsorption);
			}
		}

		return keepEffect;
	}
}

