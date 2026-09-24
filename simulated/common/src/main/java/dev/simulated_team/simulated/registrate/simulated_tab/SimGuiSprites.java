package dev.simulated_team.simulated.registrate.simulated_tab;

import dev.simulated_team.simulated.Simulated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 1.20.1: stand-in for 1.20.2+'s GUI sprite atlas ({@code Minecraft#getGuiSprites()}), which 1.20.1 doesn't have.
 * Stitches the (animated) sprites in {@code textures/gui/sprites} of every namespace, as listed by
 * {@code assets/simulated/atlases/gui.json}, into {@link #ATLAS_LOCATION}. Modelled on vanilla's {@code TextureAtlasHolder},
 * but the atlas is created on the first reload, once the texture manager exists.
 */
public final class SimGuiSprites implements PreparableReloadListener {

    public static final ResourceLocation ATLAS_LOCATION = Simulated.path("textures/atlas/gui.png");
    public static final ResourceLocation ATLAS_INFO_LOCATION = Simulated.path("gui");

    public static final SimGuiSprites INSTANCE = new SimGuiSprites();

    private TextureAtlas textureAtlas;

    private SimGuiSprites() {
    }

    private TextureAtlas atlas() {
        if (this.textureAtlas == null) {
            this.textureAtlas = new TextureAtlas(ATLAS_LOCATION);
            Minecraft.getInstance().getTextureManager().register(this.textureAtlas.location(), this.textureAtlas);
        }
        return this.textureAtlas;
    }

    public TextureAtlasSprite getSprite(final ResourceLocation location) {
        return this.atlas().getSprite(location);
    }

    @Override
    public CompletableFuture<Void> reload(final PreparationBarrier preparationBarrier, final ResourceManager resourceManager,
                                          final ProfilerFiller preparationsProfiler, final ProfilerFiller reloadProfiler,
                                          final Executor backgroundExecutor, final Executor gameExecutor) {
        // reload listeners are started on the render thread, where the atlas can be registered
        final TextureAtlas atlas = this.atlas();
        return SpriteLoader.create(atlas).loadAndStitch(resourceManager, ATLAS_INFO_LOCATION, 0, backgroundExecutor)
                .thenCompose(SpriteLoader.Preparations::waitForUpload)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(preparations -> {
                    reloadProfiler.startTick();
                    reloadProfiler.push("upload");
                    atlas.upload(preparations);
                    reloadProfiler.pop();
                    reloadProfiler.endTick();
                }, gameExecutor);
    }

    @Override
    public String getName() {
        return Simulated.MOD_ID + " gui sprites";
    }
}
