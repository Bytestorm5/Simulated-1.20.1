package dev.simulated_team.simulated.neoforge;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.index.neoforge.SimParticleTypesImpl;
import dev.simulated_team.simulated.neoforge.events.SimNeoForgeClientEvents;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-side initialization, called from {@link SimulatedNeoForge} on the physical client.
 */
public class SimulatedNeoForgeClient {

	public static void init(final IEventBus modEventBus) {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new BaseConfigScreen(screen, Simulated.MOD_ID)));

		MinecraftForge.EVENT_BUS.register(SimNeoForgeClientEvents.class);
		modEventBus.register(SimNeoForgeClientEvents.ModBusEvents.class);
		modEventBus.addListener(SimParticleTypesImpl::registerParticleProviders);
		SimulatedClient.PLUNGER_LAUNCHER_RENDER_HANDLER.registerListeners(MinecraftForge.EVENT_BUS);

		SimulatedClient.init();
	}

}
