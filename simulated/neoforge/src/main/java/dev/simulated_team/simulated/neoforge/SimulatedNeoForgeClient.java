package dev.simulated_team.simulated.neoforge;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.neoforge.events.SimNeoForgeClientEvents;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;

@Mod(value = Simulated.MOD_ID, dist = Dist.CLIENT)
public class SimulatedNeoForgeClient {

	public SimulatedNeoForgeClient(final IEventBus modEventBus, final ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, ((c, l) -> new BaseConfigScreen(l, Simulated.MOD_ID)));

		MinecraftForge.EVENT_BUS.register(SimNeoForgeClientEvents.class);
		modEventBus.register(SimNeoForgeClientEvents.ModBusEvents.class);
		SimulatedClient.PLUNGER_LAUNCHER_RENDER_HANDLER.registerListeners(MinecraftForge.EVENT_BUS);

		SimulatedClient.init();
	}

}