package dev.eriksonn.aeronautics.neoforge.events;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.data.AeroAdvancementTriggers;
import dev.eriksonn.aeronautics.events.AeronauticsCommonEvents;
import dev.eriksonn.aeronautics.index.*;
import dev.eriksonn.aeronautics.neoforge.data.recipe.AeroProcessingRecipeGen;
import dev.eriksonn.aeronautics.neoforge.index.AeroFluidsNeoForge;
import dev.eriksonn.aeronautics.neoforge.service.NeoForgeAeroConfigService;
import net.createmod.catnip.config.ConfigBase;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Aeronautics.MOD_ID)
public class AeroNeoForgeCommonEvents {

	@SubscribeEvent
	public static void serverStop(ServerStoppedEvent event) {
		AeronauticsCommonEvents.onServerStopped(event.getServer());
	}

	@SubscribeEvent
	public static void postServerTick(ServerTickEvent.Post event) {
		final MinecraftServer server = event.getServer();
		for (final ServerLevel level : server.getAllLevels()) {
			AeronauticsCommonEvents.onServerTickEnd(level);
		}
	}

	@Mod.EventBusSubscriber(modid = Aeronautics.MOD_ID)
	public static class ModBusEvents {

		@SubscribeEvent
		public static void registerEvent(RegisterEvent event) {
			AeroArmInteractionPoints.init();

			if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES) {
				AeroAdvancements.init();
				AeroAdvancementTriggers.register();
			}
		}

		@SubscribeEvent(priority = EventPriority.HIGH)
		public static void gatherDataHighPriority(GatherDataEvent event) {
			if(event.getMods().contains(Aeronautics.MOD_ID)) {
				AeroTags.addGenerators();
			}
		}

		@SubscribeEvent
		public static void gatherData(GatherDataEvent event) {
			final DataGenerator generator = event.getGenerator();
			final PackOutput output = generator.getPackOutput();
			final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

			generator.addProvider(event.includeServer(), new AeroAdvancements(output, lookupProvider));
			generator.addProvider(event.includeServer(), AeroProcessingRecipeGen.registerAll(output, lookupProvider));
			event.addProvider(AeroSoundEvents.REGISTRY.getProvider(output));
		}

		@SubscribeEvent
		public static void commonSetup(FMLCommonSetupEvent event) {
			AeroFluidsNeoForge.registerFluidInteractions();
		}

		@SubscribeEvent
		public static void loadConfig(final ModConfigEvent.Loading event) {
			for (final ConfigBase config : NeoForgeAeroConfigService.CONFIGS.values()) {
				if (config.specification == event.getConfig().getSpec()) {
					config.onLoad();
				}
			}
		}

		@SubscribeEvent
		public static void reloadConfig(final ModConfigEvent.Reloading event) {
			for (final ConfigBase config : NeoForgeAeroConfigService.CONFIGS.values()) {
				if (config.specification == event.getConfig().getSpec()) {
					config.onReload();
				}
			}
		}
	}
}
