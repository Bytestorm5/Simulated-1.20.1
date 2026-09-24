package dev.eriksonn.aeronautics.neoforge;


import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.neoforge.events.AeroNeoForgeCommonEvents;
import dev.eriksonn.aeronautics.neoforge.index.AeroFluidsNeoForge;
import dev.eriksonn.aeronautics.neoforge.index.AeroParticleTypesNeoForge;
import dev.eriksonn.aeronautics.neoforge.service.NeoForgeAeroConfigService;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;

@Mod(Aeronautics.MOD_ID)
public class AeronauticsNeoForge {
    public AeronauticsNeoForge(final IEventBus modBus, final ModContainer modContainer) {
        MinecraftForge.EVENT_BUS.register(AeroNeoForgeCommonEvents.class);
        modBus.register(AeroNeoForgeCommonEvents.ModBusEvents.class);

        AeroParticleTypesNeoForge.registerEventListeners(modBus);
        Aeronautics.getRegistrate().registerEventListeners(modBus);

        Aeronautics.init();
        AeroFluidsNeoForge.init();

        NeoForgeAeroConfigService.register(modContainer);
    }
}
