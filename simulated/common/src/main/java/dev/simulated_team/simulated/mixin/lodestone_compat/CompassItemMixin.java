package dev.simulated_team.simulated.mixin.lodestone_compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability.LodestoneTrackingMap;
import dev.simulated_team.simulated.index.SimDataComponents;
import net.minecraft.core.BlockPos;
import dev.simulated_team.simulated.backport.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(CompassItem.class)
public abstract class CompassItemMixin extends Item {
	public CompassItemMixin(final Properties properties) {
		super(properties);
	}

	// 1.20.1: lodestone data is plain NBT, there is no component lookup to anchor on; the handler only acts server-side
	@Inject(method = "inventoryTick", at = @At("HEAD"))
	private void simulated$checkID(final ItemStack stack, final Level level, final Entity entity, final int itemSlot, final boolean isSelected, final CallbackInfo ci) {
		if (!level.isClientSide) {
			if (SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER.has(stack)) {
				final UUID trackerID = SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER.get(stack);
				final LodestoneTrackingMap map = LodestoneTrackingMap.getOrLoad(level);
				if (map != null && entity instanceof final ServerPlayer sp) {
					map.sendUpdateForPlayer(trackerID, sp);
				}
			}
		}
	}

	// 1.20.1: the lodestone target is written to NBT by addLodestoneTags (for both the held stack and the split-off copy)
	@WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CompassItem;addLodestoneTags(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;Lnet/minecraft/nbt/CompoundTag;)V"))
	public void simulated$setLodestoneData(final CompassItem instance, final ResourceKey<Level> lodestoneDimension, final BlockPos lodestonePos, final CompoundTag compoundTag, final Operation<Void> original, @Local(argsOnly = true) final UseOnContext context) {
		final BlockPos pos = context.getClickedPos();
		final LodestoneTrackingMap map = LodestoneTrackingMap.getOrLoad(context.getLevel());
		if (map != null) {
			final UUID uuid = map.addOrGetLodestoneTrackingPoint(pos);
			if (uuid != null) {
				final DataComponentType<UUID> tracker = SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER;
				tracker.codec().encodeStart(NbtOps.INSTANCE, uuid).result().ifPresent(tag -> compoundTag.put(tracker.key(), tag));
			}
		}
		original.call(instance, lodestoneDimension, lodestonePos, compoundTag);
	}
}
