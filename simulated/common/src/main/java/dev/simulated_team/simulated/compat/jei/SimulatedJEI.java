package dev.simulated_team.simulated.compat.jei;

import com.simibubi.create.compat.jei.GhostIngredientHandler;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.client.SearchAlias;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterScreen;
import dev.simulated_team.simulated.index.SimResourceManagers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class SimulatedJEI implements IModPlugin {

    private static final ResourceLocation ID = Simulated.path("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(LinkedTypewriterScreen.class, new GhostIngredientHandler());
    }

    // 1.20.1: JEI 15 has no IModInfoRegistration, so the mod search aliases between the Simulated mods
    // (SimulatedRegistrate.MODS) can't be registered here.

    @Override
    public void registerIngredientAliases(final IIngredientAliasRegistration registration) {
        for (final SearchAlias searchAlias : SimResourceManagers.SEARCH_ALIAS.entries()) {
            registration.addAliases(VanillaTypes.ITEM_STACK, searchAlias.getItems(), searchAlias.terms());
        }
    }
}
