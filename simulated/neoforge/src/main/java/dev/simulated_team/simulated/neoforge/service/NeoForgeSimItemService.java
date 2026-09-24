package dev.simulated_team.simulated.neoforge.service;

import com.simibubi.create.AllTags;
import dev.simulated_team.simulated.service.SimItemService;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class NeoForgeSimItemService implements SimItemService {

    // 1.20.1: Create has no superheated blaze burner fuel data map; its blaze burner gives special fuels a fixed
    // 3200 tick burn time (see BlazeBurnerBlockEntity#tryUpdateFuel)
    private static final int SPECIAL_FUEL_BURN_TIME = 3200;

    public int getBurnTime(final ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING);
    }

    @Override
    public int getSuperheatedBurnTime(final ItemStack stack) {
        if (AllTags.AllItemTags.BLAZE_BURNER_FUEL_SPECIAL.matches(stack)) {
            return SPECIAL_FUEL_BURN_TIME;
        }
        return 0;
    }
}
