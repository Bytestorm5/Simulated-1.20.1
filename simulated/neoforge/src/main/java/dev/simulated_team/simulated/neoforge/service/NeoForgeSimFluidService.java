package dev.simulated_team.simulated.neoforge.service;

import dev.simulated_team.simulated.service.SimFluidService;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class NeoForgeSimFluidService implements SimFluidService {
    public long mbToLoaderUnits(final long mb) {
        return mb;
    }

    @Override
    public Fluid getFluidInItem(final ItemStack stack) {
        final IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FluidHandler.ITEM);
        if(handler != null) {
            final FluidStack fluid = handler.getFluidInTank(0);
            if(!fluid.isEmpty()) {
                return fluid.getFluid();
            }
        }
        return null;
    }
}
