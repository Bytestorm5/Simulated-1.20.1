package dev.ryanhcode.offroad.neoforge;


import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.data.OffroadTags;
import dev.ryanhcode.offroad.events.OffroadCommonEvents;
import dev.ryanhcode.offroad.neoforge.data.OffroadDatagen;
import dev.ryanhcode.offroad.neoforge.service.NeoForgeOffroadConfigService;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.ModifyDefaultComponentsEvent;
import net.minecraftforge.event.TickEvent;

@Mod(Offroad.MOD_ID)
public class OffroadNeoForge {
    public OffroadNeoForge(final IEventBus modBus, final ModContainer modContainer) {
        this.modBusRegistry(modBus);
        this.listenCommonEvents(MinecraftForge.EVENT_BUS);

        Offroad.init();

        NeoForgeOffroadConfigService.register(modContainer);
    }

    private void listenCommonEvents(final IEventBus eventBus) {

    }

    private void modBusRegistry(final IEventBus modBus) {
        modBus.register(NeoForgeOffroadConfigService.class);

        modBus.addListener(OffroadNeoForge::init);
        modBus.addListener(EventPriority.HIGHEST, OffroadDatagen::gatherDataHighPriority);
        modBus.addListener(EventPriority.LOWEST, OffroadDatagen::gatherData);
        modBus.addListener(OffroadDatagen::registerEvent);
        modBus.addListener((ModifyDefaultComponentsEvent event) -> OffroadCommonEvents.modifyDefaultComponents(event::modify));
        MinecraftForge.EVENT_BUS.addListener((LevelTickEvent.Post event) -> OffroadCommonEvents.tickLevelEvent(event.getLevel()));

        modBus.addListener((final GatherDataEvent event) -> {
            if (event.getMods().contains(Offroad.MOD_ID)) {
                OffroadTags.addGenerators();
            }
        });

        Offroad.getRegistrate().registerEventListeners(modBus);
    }

    private static void init(final FMLCommonSetupEvent event) {

    }
}
