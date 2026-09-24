package dev.simulated_team.simulated.neoforge.events;


import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.command.SimCommand;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import dev.simulated_team.simulated.data.advancements.SimAdvancementTriggers;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.data.neoforge.SimProcessingRecipeGen;
import dev.simulated_team.simulated.events.SimulatedCommonClientEvents;
import dev.simulated_team.simulated.events.SimulatedCommonEvents;
import dev.simulated_team.simulated.index.SimArmInteractions;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.index.neoforge.NeoForgeSimStats;
import dev.simulated_team.simulated.neoforge.capability.SimBlockEntityCapabilities;
import dev.simulated_team.simulated.neoforge.service.NeoForgeSimConfigService;
import net.createmod.catnip.config.ConfigBase;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Registered on the Forge event bus by {@link dev.simulated_team.simulated.neoforge.SimulatedNeoForge}.
 */
public class SimNeoForgeCommonEvents {

	@SubscribeEvent
	public static void loadChunk(final ChunkEvent.Load event) {
		SimulatedCommonEvents.onChunkLoad(event.getLevel(), event.getChunk(), event.isNewChunk());
	}

	@SubscribeEvent
	public static void playerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
		final Player player = event.getEntity();
		SimulatedCommonEvents.onPlayerLoggedIn(player);
	}

	@SubscribeEvent
	public static void registerCommands(final RegisterCommandsEvent event) {
		SimCommand.register(event.getDispatcher(), event.getBuildContext());
	}

	@SubscribeEvent
	public static void serverStopped(final ServerStoppedEvent event) {
		SimulatedCommonEvents.onServerStopped(event.getServer());
	}

	@SubscribeEvent
	public static void postServerTick(final TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		final MinecraftServer server = event.getServer();
		for (final ServerLevel level : server.getAllLevels()) {
			SimulatedCommonEvents.onServerTickEnd(level);
		}
	}

	@SubscribeEvent
	public static void syncDataPack(final OnDatapackSyncEvent event) {
		final List<ServerPlayer> players = event.getPlayer() != null ? List.of(event.getPlayer()) : event.getPlayerList().getPlayers();
		EndSeaPhysicsData.syncDataPacket(packet -> players.forEach(player -> player.connection.send(packet)));
	}

	@SubscribeEvent
	public static void addReloadListeners(final AddReloadListenerEvent event) {
		event.addListener(EndSeaPhysicsData.ReloadListener.INSTANCE);
	}

	@SubscribeEvent
	public static void rightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		final InteractionResult result = SimulatedCommonEvents.rightClickBlock(event.getLevel(), event.getPos(), event.getEntity(), event.getItemStack());
		if (result != null) {
			event.setCancellationResult(result);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onLivingEntityUseItem(final PlayerInteractEvent.RightClickItem event) {
		final LivingEntity entity = event.getEntity();
		if (entity instanceof final Player player && player.isLocalPlayer()) {
			SimulatedCommonClientEvents.useItemOnAirEvent(entity.level(), player, event.getItemStack(), event.getHand());
		}
	}

	@SubscribeEvent
	public static void modifyItemAttributes(final ItemAttributeModifierEvent event) {
		SimulatedCommonEvents.modifyItemAttributes(event.getItemStack(), event.getSlotType(), event::addModifier);
	}

	@SubscribeEvent
	public static void attachBlockEntityCapabilities(final AttachCapabilitiesEvent<BlockEntity> event) {
		SimBlockEntityCapabilities.attach(event);
	}

	/**
	 * Registered on the mod event bus by {@link dev.simulated_team.simulated.neoforge.SimulatedNeoForge}.
	 */
	public static class ModBusEvents {

		@SubscribeEvent
		public static void register(final RegisterEvent event) {
			SimArmInteractions.init();
		}

		@SubscribeEvent
		public static void commonSetup(final FMLCommonSetupEvent event) {
			// 1.20.1: criterion triggers have no registry, they're registered into CriteriaTriggers once registration
			// has finished (like Create does)
			event.enqueueWork(() -> {
				SimAdvancements.register();
				SimAdvancementTriggers.register();
			});
		}

		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void gatherDataHighPriority(final GatherDataEvent event) {
			if (event.getModContainer().getModId().equals(Simulated.MOD_ID))
				SimTags.addGenerators();
		}

		@SubscribeEvent
		public static void gatherData(final GatherDataEvent event) {
			final DataGenerator generator = event.getGenerator();

			final PackOutput output = generator.getPackOutput();
			final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

			generator.addProvider(event.includeClient(), SimSoundEvents.REGISTRY.getProvider(output));
			generator.addProvider(event.includeServer(), new SimAdvancements(output, lookupProvider));
			generator.addProvider(event.includeServer(), SimProcessingRecipeGen.registerAll(output));
		}

		@SubscribeEvent
		public static void loadConfig(final ModConfigEvent.Loading event) {
			for (final ConfigBase config : NeoForgeSimConfigService.CONFIGS.values()) {
				if (config.specification == event.getConfig().getSpec()) {
					config.onLoad();
				}
			}

		}

		@SubscribeEvent
		public static void reloadConfig(final ModConfigEvent.Reloading event) {
			for (final ConfigBase config : NeoForgeSimConfigService.CONFIGS.values()) {
				if (config.specification == event.getConfig().getSpec()) {
					config.onReload();
				}
			}

		}

		@SubscribeEvent
		public static void postRegister(final FMLLoadCompleteEvent event) {
			NeoForgeSimStats.bootstrap();
		}
	}

}
