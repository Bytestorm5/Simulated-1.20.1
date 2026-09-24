package dev.simulated_team.simulated.neoforge.service;

import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import dev.simulated_team.simulated.multiloader.energy.SingleBattery;
import dev.simulated_team.simulated.multiloader.energy.SingleBatteryWrapper;
import dev.simulated_team.simulated.multiloader.inventory.AbstractContainer;
import dev.simulated_team.simulated.multiloader.inventory.InventoryLoaderWrapper;
import dev.simulated_team.simulated.multiloader.inventory.neoforge.ContainerWrapper;
import dev.simulated_team.simulated.multiloader.inventory.neoforge.InventoryLoaderWrapperImpl;
import dev.simulated_team.simulated.multiloader.tanks.SingleTank;
import dev.simulated_team.simulated.multiloader.tanks.neoforge.SingleTankWrapper;
import dev.simulated_team.simulated.neoforge.capability.SimBlockEntityCapabilities;
import dev.simulated_team.simulated.service.SimInventoryService;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class NeoForgeSimInventoryService implements SimInventoryService {

    // 1.20.1: the capabilities are attached through AttachCapabilitiesEvent (see SimBlockEntityCapabilities)
    @Override
    public <T extends BlockEntity> NonNullConsumer<BlockEntityType<T>> registerInventory(final BiFunction<T, Direction, AbstractContainer> getter) {
        return (type) -> SimBlockEntityCapabilities.register(() -> type, ForgeCapabilities.ITEM_HANDLER, getter, ContainerWrapper::new);
    }

    @Override
    public <T extends BlockEntity> NonNullConsumer<BlockEntityType<T>> registerTank(final BiFunction<T, Direction, SingleTank> getter) {
        return (type) -> SimBlockEntityCapabilities.register(() -> type, ForgeCapabilities.FLUID_HANDLER, getter, SingleTankWrapper::new);
    }

    @Override
    public <T extends BlockEntity> NonNullConsumer<BlockEntityType<T>> registerBattery(final BiFunction<T, Direction, SingleBattery> getter) {
        return (type) -> SimBlockEntityCapabilities.register(() -> type, ForgeCapabilities.ENERGY, getter, SingleBatteryWrapper::new);
    }

    @Override
    public <T extends InventoryLoaderWrapper> T getInventory(@Nullable final BlockEntity be, @Nullable final Direction dir) {
        if (be != null) {
            final IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).orElse(null);
            if (handler != null) {
                return (T) new InventoryLoaderWrapperImpl(handler);
            }
        }

        return null;
    }

    @Override
    public <T extends InventoryLoaderWrapper> T getWrappedAllItemsFromContraption(final MountedStorageManager manager) {
        return (T) new InventoryLoaderWrapperImpl(manager.getAllItems());
    }

    @Override
    public <T extends InventoryLoaderWrapper> T getWrappedMountedItemsFromContraption(final MountedStorageManager manager) {
        return (T) new InventoryLoaderWrapperImpl(manager.getMountedItems());
    }
}
