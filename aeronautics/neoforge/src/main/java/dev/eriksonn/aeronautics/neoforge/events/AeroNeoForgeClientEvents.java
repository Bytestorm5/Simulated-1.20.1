package dev.eriksonn.aeronautics.neoforge.events;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.events.AeronauticsClientEvents;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import dev.eriksonn.aeronautics.mixin.levitite.ChunkRenderTypeSetAccessor;
import dev.eriksonn.aeronautics.neoforge.content.fluids.AeroFluidType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

/**
 * Registered on the Forge event bus by {@link dev.eriksonn.aeronautics.neoforge.AeronauticsNeoForgeClient}.
 */
public class AeroNeoForgeClientEvents {

    @SubscribeEvent
    public static void clientTick(final TickEvent.ClientTickEvent event) {
        AeronauticsClientEvents.clientLevelTick(event.phase == TickEvent.Phase.END);
    }

    /**
     * Registered on the mod event bus by {@link dev.eriksonn.aeronautics.neoforge.AeronauticsNeoForgeClient}.
     * <p>
     * 1.20.1: the levitite blend's client fluid type extensions are supplied by {@link AeroFluidType#initializeClient}
     */
    public static class ModBusEvents {

        @SubscribeEvent
        public static void clientSetup(final FMLClientSetupEvent event) {
            final ChunkRenderTypeSet set = ChunkRenderTypeSet.of(RenderType.solid(), AeroRenderTypes.levitite(), AeroRenderTypes.levititeGhosts());
            ItemBlockRenderTypes.setRenderLayer(AeroBlocks.LEVITITE.get(), set);
            ItemBlockRenderTypes.setRenderLayer(AeroBlocks.PEARLESCENT_LEVITITE.get(), set);

            fixChunkRenderTypeSet();
        }

        /**
         * Certain mods (like Bookshelf) cause the ChunkRenderTypeSet class in Forge to get initialized early,
         * cementing the chunk render layers inside it. We do this as an unfortunate safety measure to "fix" the
         * static collections in ChunkRenderTypeSet to include the Levitite layers, if the class is loaded before
         * us.
         */
        private static void fixChunkRenderTypeSet() {
            final List<RenderType> list = RenderType.chunkBufferLayers();

            ChunkRenderTypeSetAccessor.setChunkRenderTypesList(list);
            ChunkRenderTypeSetAccessor.setChunkRenderTypes(list.toArray(new RenderType[0]));
            ((ChunkRenderTypeSetAccessor) (Object) ChunkRenderTypeSet.all()).getBits().set(0, list.size());
        }

        @SubscribeEvent
        public static void registerRegisterStageEvent(final RenderLevelStageEvent.RegisterStageEvent event) {
            event.register(Aeronautics.path("levitite"), AeroRenderTypes.levitite());
            event.register(Aeronautics.path("levitite_ghosts"), AeroRenderTypes.levititeGhosts());
        }
    }
}
