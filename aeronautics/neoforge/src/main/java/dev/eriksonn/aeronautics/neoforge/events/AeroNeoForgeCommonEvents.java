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
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Registered on the Forge event bus by {@link dev.eriksonn.aeronautics.neoforge.AeronauticsNeoForge}.
 */
public class AeroNeoForgeCommonEvents {

	/**
	 * Envelopes burn for 100 ticks.
	 * <p>
	 * 1.20.1: there are no data maps, so this replaces NeoForge's {@code furnace_fuels} data map entry
	 */
	private static final int ENVELOPE_BURN_TIME = 100;

	@SubscribeEvent
	public static void serverStop(final ServerStoppedEvent event) {
		AeronauticsCommonEvents.onServerStopped(event.getServer());
	}

	@SubscribeEvent
	public static void postServerTick(final TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		final MinecraftServer server = event.getServer();
		for (final ServerLevel level : server.getAllLevels()) {
			AeronauticsCommonEvents.onServerTickEnd(level);
		}
	}

	@SubscribeEvent
	public static void furnaceFuelBurnTime(final FurnaceFuelBurnTimeEvent event) {
		if (event.getItemStack().is(AeroTags.ItemTags.ENVELOPE)) {
			event.setBurnTime(ENVELOPE_BURN_TIME);
		}
	}

	/**
	 * Registered on the mod event bus by {@link dev.eriksonn.aeronautics.neoforge.AeronauticsNeoForge}.
	 */
	public static class ModBusEvents {

		@SubscribeEvent
		public static void registerEvent(final RegisterEvent event) {
			AeroArmInteractionPoints.init();
		}

		@SubscribeEvent(priority = EventPriority.HIGH)
		public static void gatherDataHighPriority(final GatherDataEvent event) {
			if (event.getModContainer().getModId().equals(Aeronautics.MOD_ID)) {
				AeroTags.addGenerators();
			}
		}

		@SubscribeEvent
		public static void gatherData(final GatherDataEvent event) {
			final DataGenerator generator = event.getGenerator();
			final PackOutput output = generator.getPackOutput();
			final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

			generator.addProvider(event.includeServer(), new AeroAdvancements(output, lookupProvider));
			generator.addProvider(event.includeServer(), AeroProcessingRecipeGen.registerAll(output));
			generator.addProvider(event.includeClient(), AeroSoundEvents.REGISTRY.getProvider(output));
		}

		@SubscribeEvent
		public static void commonSetup(final FMLCommonSetupEvent event) {
			// 1.20.1: criterion triggers have no registry, they're registered into CriteriaTriggers once registration
			// has finished (like Create does)
			event.enqueueWork(() -> {
				AeroAdvancements.init();
				AeroAdvancementTriggers.register();
				AeroFluidsNeoForge.registerFluidInteractions();
			});
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
