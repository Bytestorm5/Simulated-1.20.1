package dev.ryanhcode.offroad.neoforge;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.OffroadClient;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-side initialization, called from {@link OffroadNeoForge} on the physical client.
 */
public class OffroadNeoForgeClient {

	public static void init(final IEventBus modBus) {
		listenClientEvents(modBus);
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new BaseConfigScreen(screen, Offroad.MOD_ID)));

		OffroadClient.init();
	}

	private static void listenClientEvents(final IEventBus modBus) {

	}
}
