package dev.eriksonn.aeronautics.neoforge;

import com.tterrag.registrate.util.OneTimeEventReceiver;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.AeronauticsClient;
import dev.eriksonn.aeronautics.events.AeronauticsClientEvents;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import dev.eriksonn.aeronautics.neoforge.events.AeroNeoForgeClientEvents;
import foundry.veil.forge.event.ForgeVeilRegisterBlockLayersEvent;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;

import java.util.Set;
import java.util.stream.Collectors;

@Mod(value = Aeronautics.MOD_ID, dist = Dist.CLIENT)
public class AeronauticsNeoForgeClient {
	public AeronauticsNeoForgeClient(final IEventBus modBus, final ModContainer container) {
		MinecraftForge.EVENT_BUS.register(AeroNeoForgeClientEvents.class);
		modBus.register(AeroNeoForgeClientEvents.ModBusEvents.class);
		container.registerExtensionPoint(IConfigScreenFactory.class, ((c, l) -> new BaseConfigScreen(l, Aeronautics.MOD_ID)));

		modBus.<ForgeVeilRegisterBlockLayersEvent>addListener(event -> event.registerBlockLayer(AeroRenderTypes.levitite()));

		AeronauticsClient.init();
	}
}
