package dev.eriksonn.aeronautics.neoforge;


import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.neoforge.events.AeroNeoForgeCommonEvents;
import dev.eriksonn.aeronautics.neoforge.index.AeroFluidsNeoForge;
import dev.eriksonn.aeronautics.neoforge.index.AeroParticleTypesNeoForge;
import dev.eriksonn.aeronautics.neoforge.service.NeoForgeAeroConfigService;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Aeronautics.MOD_ID)
public class AeronauticsNeoForge {
    public AeronauticsNeoForge() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(AeroNeoForgeCommonEvents.class);
        modBus.register(AeroNeoForgeCommonEvents.ModBusEvents.class);

        AeroParticleTypesNeoForge.register(modBus);
        Aeronautics.getRegistrate().registerEventListeners(modBus);

        Aeronautics.init();
        AeroFluidsNeoForge.init();

        NeoForgeAeroConfigService.register(ModLoadingContext.get());

        // 1.20.1: Forge has no client-only @Mod entrypoints, so the client entrypoint is called from here
        if (FMLEnvironment.dist == Dist.CLIENT) {
            AeronauticsNeoForgeClient.init(modBus);
        }
    }
}
