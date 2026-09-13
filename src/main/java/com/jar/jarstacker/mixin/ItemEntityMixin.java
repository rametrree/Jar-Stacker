package com.jar.jarstacker.mixin;

import com.jar.jarstacker.config.ModConfig;
import com.jar.jarstacker.stack.StackableEntity;
import com.jar.jarstacker.stack.item.ItemStackingManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Mixin to support logical stack counts on ItemEntity, persistence, and safe player pickup.
 * Reason: Vanilla ItemEntity only natively tracks an ItemStack with max size 64.
 * To support logical counts up to 4096 and prevent item duplication or loss during pickup,
 * we intercept persistence serialization and playerTouch.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements StackableEntity {

	@Shadow private int pickupDelay;
	@Shadow private UUID target;
	@Shadow private int age;
	@Shadow public abstract ItemStack getItem();

	@Unique
	private static final java.util.concurrent.atomic.AtomicLong JARSTACKER$SPAWN_COUNTER = new java.util.concurrent.atomic.AtomicLong();

	@Unique
	private final long jarstacker$spawnSequence = JARSTACKER$SPAWN_COUNTER.incrementAndGet();

	@Unique
	private int jarstacker$stackCount = 0;

	@Unique
	private long jarstacker$spawnGameTime = 0L;

	@Unique
	private long jarstacker$firstScanGameTime = 0L;

	public ItemEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.jarstacker$spawnGameTime = level != null ? level.getGameTime() : 0L;
	}

	@Override
	public long jarstacker$getSpawnGameTime() {
		return this.jarstacker$spawnGameTime;
	}

	@Override
	public void jarstacker$setSpawnGameTime(long time) {
		this.jarstacker$spawnGameTime = time;
	}

	@Override
	public long jarstacker$getFirstScanGameTime() {
		return this.jarstacker$firstScanGameTime;
	}

	@Override
	public void jarstacker$setFirstScanGameTime(long time) {
		this.jarstacker$firstScanGameTime = time;
	}

	@Override
	public int jarstacker$getStackCount() {
		return this.jarstacker$stackCount;
	}

	@Override
	public void jarstacker$setStackCount(int count) {
		this.jarstacker$stackCount = count;
	}

	@Override
	public long jarstacker$getSpawnSequence() {
		return this.jarstacker$spawnSequence;
	}

	@Override
	public int jarstacker$getAge() {
		return this.age;
	}

	@Override
	public void jarstacker$setAge(int age) {
		this.age = age;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void jarstacker$saveData(CompoundTag tag, CallbackInfo ci) {
		if (this.jarstacker$stackCount > 0) {
			tag.putInt("JarStackerCount", this.jarstacker$stackCount);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void jarstacker$loadData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains("JarStackerCount")) {
			this.jarstacker$stackCount = tag.getInt("JarStackerCount");
			ItemEntity self = (ItemEntity) (Object) this;
			ItemStackingManager.updateLabel(self, this.jarstacker$stackCount, ModConfig.getInstance().getItemStacking().isShowLabel());
		}
	}

	@Inject(method = "mergeWithNeighbours", at = @At("HEAD"), cancellable = true)
	private void jarstacker$cancelVanillaMerge(CallbackInfo ci) {
		if (ModConfig.getInstance().getItemStacking().isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void jarstacker$onPlayerTouch(Player player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		if (self.level().isClientSide) {
			return;
		}

		int logical = this.jarstacker$stackCount > 0 ? this.jarstacker$stackCount : self.getItem().getCount();
		ItemStack physical = self.getItem();
		if (logical <= 1 && physical.getCount() <= 1) {
			return; // Handled normally by vanilla for single items
		}

		if (this.pickupDelay != 0) {
			return;
		}

		if (this.target != null && !this.target.equals(player.getUUID())) {
			return;
		}

		int totalInserted = 0;
		int remaining = logical;

		while (remaining > 0) {
			int batchSize = Math.min(physical.getMaxStackSize(), remaining);
			ItemStack batch = physical.copyWithCount(batchSize);
			boolean added = player.getInventory().add(batch);
			int accepted = batchSize - batch.getCount();
			if (accepted <= 0) {
				break;
			}
			totalInserted += accepted;
			remaining -= accepted;
			if (!batch.isEmpty()) {
				break; // Inventory is full
			}
		}

		if (totalInserted > 0) {
			player.take(self, totalInserted);
			player.awardStat(Stats.ITEM_PICKED_UP.get(physical.getItem()), totalInserted);
			player.onItemPickup(self);

			if (remaining <= 0) {
				this.jarstacker$stackCount = 0;
				self.discard();
			} else {
				this.jarstacker$stackCount = remaining;
				physical.setCount(Math.min(physical.getMaxStackSize(), remaining));
				ItemStackingManager.updateLabel(self, remaining, ModConfig.getInstance().getItemStacking().isShowLabel());
			}
			ci.cancel();
		}
	}
}