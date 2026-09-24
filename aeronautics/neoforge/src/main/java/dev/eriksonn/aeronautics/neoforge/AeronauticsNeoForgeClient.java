package dev.eriksonn.aeronautics.neoforge;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.AeronauticsClient;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import dev.eriksonn.aeronautics.neoforge.events.AeroNeoForgeClientEvents;
import dev.eriksonn.aeronautics.neoforge.index.AeroParticleTypesNeoForge;
import foundry.veil.forge.event.ForgeVeilRegisterBlockLayersEvent;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-side initialization, called from {@link AeronauticsNeoForge} on the physical client.
 */
public class AeronauticsNeoForgeClient {
	public static void init(final IEventBus modBus) {
		MinecraftForge.EVENT_BUS.register(AeroNeoForgeClientEvents.class);
		modBus.register(AeroNeoForgeClientEvents.ModBusEvents.class);
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new BaseConfigScreen(screen, Aeronautics.MOD_ID)));

		modBus.addListener(AeroParticleTypesNeoForge::registerParticleProviders);
		modBus.<ForgeVeilRegisterBlockLayersEvent>addListener(event -> event.registerBlockLayer(AeroRenderTypes.levitite()));

		AeronauticsClient.init();
	}
}
