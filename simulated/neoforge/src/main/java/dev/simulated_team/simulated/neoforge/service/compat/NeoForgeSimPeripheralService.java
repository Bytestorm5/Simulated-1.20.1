package dev.simulated_team.simulated.neoforge.service.compat;

import dan200.computercraft.api.network.wired.WiredElement;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.simulated_team.simulated.neoforge.capability.SimBlockEntityCapabilities;
import dev.simulated_team.simulated.service.compat.SimPeripheralService;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeSimPeripheralService implements SimPeripheralService {

    // 1.20.1: CC: Tweaked on Forge looks peripherals and wired elements up through these capabilities (capabilities
    // are identified by their type, so this is the same instance CC: Tweaked uses)
    private static final Capability<IPeripheral> PERIPHERAL = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final Capability<WiredElement> WIRED_ELEMENT = CapabilityManager.get(new CapabilityToken<>() {
    });

    @Override
    public <T extends BlockEntity> void addPeripheral(final Supplier<BlockEntityType<T>> typeSupplier, final CapabilityGetter<T, IPeripheral> getter) {
        SimBlockEntityCapabilities.<T, IPeripheral, IPeripheral>register(typeSupplier, PERIPHERAL, getter::get, Function.identity());
    }

    @Override
    public <T extends BlockEntity> void addWired(final Supplier<BlockEntityType<T>> typeSupplier, final CapabilityGetter<T, WiredElement> getter) {
        SimBlockEntityCapabilities.<T, WiredElement, WiredElement>register(typeSupplier, WIRED_ELEMENT, getter::get, Function.identity());
    }
}
