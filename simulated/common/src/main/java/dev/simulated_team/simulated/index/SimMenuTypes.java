package dev.simulated_team.simulated.index;

import com.tterrag.registrate.builders.MenuBuilder.ForgeMenuFactory;
import com.tterrag.registrate.builders.MenuBuilder.ScreenFactory;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterMenuCommon;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterScreen;
import dev.simulated_team.simulated.service.SimMenuService;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class SimMenuTypes {

    public static final MenuEntry<LinkedTypewriterMenuCommon> LINKED_TYPEWRITER =
            register("linked_typewriter", (type, id, inv, buf) -> SimMenuService.INSTANCE.getLoaderLinkedTypewriter(type, id, inv,
                    buf instanceof final RegistryFriendlyByteBuf registryBuf ? registryBuf : new RegistryFriendlyByteBuf(buf, inv.player.level().registryAccess())),
                    () -> LinkedTypewriterScreen::new);

    private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(
            final String name, final ForgeMenuFactory<C> factory, final NonNullSupplier<ScreenFactory<C, S>> screenFactory) {

        return Simulated.getRegistrate()
                .menu(name, factory, screenFactory)
                .register();
    }
    
    public static void register() {}
}
