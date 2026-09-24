package dev.simulated_team.simulated.neoforge;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.neoforge.NeoForgeSimStats;
import dev.simulated_team.simulated.index.neoforge.SimNeoForgeRecipeTypes;
import dev.simulated_team.simulated.index.neoforge.SimParticleTypesImpl;
import dev.simulated_team.simulated.neoforge.events.SimNeoForgeCommonEvents;
import dev.simulated_team.simulated.neoforge.service.NeoForgeSimConfigService;
import dev.simulated_team.simulated.neoforge.service.NeoForgeSimEntityDataSerialization;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;

@Mod(Simulated.MOD_ID)
public final class SimulatedNeoForge {
    public static final CreativeModeTab TAB = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Simulated.MOD_ID + ".group"))
            .icon(() -> new ItemStack(SimBlocks.PHYSICS_ASSEMBLER.get()))
            .build();

    public SimulatedNeoForge() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // deferred register tab
        final DeferredRegister<CreativeModeTab> tabRegister = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Simulated.MOD_ID);
        tabRegister.register("main_tab", () -> TAB);
        tabRegister.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(SimNeoForgeCommonEvents.class);
        modEventBus.register(SimNeoForgeCommonEvents.ModBusEvents.class);

        SimParticleTypesImpl.register(modEventBus);
        SimNeoForgeRecipeTypes.register(modEventBus);

        NeoForgeSimEntityDataSerialization.register(modEventBus);
        Simulated.getRegistrate().registerEventListeners(modEventBus);

        NeoForgeSimStats.register(modEventBus);

        Simulated.init();
        NeoForgeSimConfigService.register(ModLoadingContext.get());

        // 1.20.1: Forge has no client-only @Mod entrypoints, so the client entrypoint is called from here
        if (FMLEnvironment.dist == Dist.CLIENT) {
            SimulatedNeoForgeClient.init(modEventBus);
        }
    }
}
