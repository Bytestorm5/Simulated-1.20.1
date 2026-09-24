package dev.simulated_team.simulated.registrate.simulated_tab;

import dev.simulated_team.simulated.Simulated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return new ShelfSpriteLoader(atlas).loadAndStitch(resourceManager, ATLAS_INFO_LOCATION, 0, backgroundExecutor)
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

    /**
     * 1.20.1's {@code Stitcher} can silently drop a sprite: when it grows the atlas, it ignores whether the new region
     * could actually hold the sprite. With several 162x18 section banners this loses one of them (the Aeronautics
     * banner in practice), so this atlas is packed into simple rows instead.
     */
    private static final class ShelfSpriteLoader extends SpriteLoader {

        private final ResourceLocation location;

        private ShelfSpriteLoader(final TextureAtlas atlas) {
            super(atlas.location(), atlas.maxSupportedTextureSize(), 0, 0);
            this.location = atlas.location();
        }

        @Override
        public Preparations stitch(final List<SpriteContents> contents, final int mipLevel, final Executor executor) {
            final List<SpriteContents> sorted = new ArrayList<>(contents);
            sorted.sort(Comparator.comparingInt(SpriteContents::height).reversed().thenComparing(sprite -> sprite.name().toString()));

            int width = 256;
            for (final SpriteContents sprite : sorted) {
                width = Math.max(width, Mth.smallestEncompassingPowerOfTwo(sprite.width()));
            }

            final int[] xs = new int[sorted.size()];
            final int[] ys = new int[sorted.size()];
            int x = 0;
            int y = 0;
            int rowHeight = 0;
            for (int i = 0; i < sorted.size(); i++) {
                final SpriteContents sprite = sorted.get(i);
                if (x + sprite.width() > width) {
                    x = 0;
                    y += rowHeight;
                    rowHeight = 0;
                }
                xs[i] = x;
                ys[i] = y;
                x += sprite.width();
                rowHeight = Math.max(rowHeight, sprite.height());
            }
            final int height = Mth.smallestEncompassingPowerOfTwo(Math.max(1, y + rowHeight));

            final Map<ResourceLocation, TextureAtlasSprite> regions = new HashMap<>();
            for (int i = 0; i < sorted.size(); i++) {
                final SpriteContents sprite = sorted.get(i);
                regions.put(sprite.name(), new TextureAtlasSprite(this.location, sprite, width, height, xs[i], ys[i]) {
                });
            }

            final TextureAtlasSprite missing = regions.get(MissingTextureAtlasSprite.getLocation());
            return new Preparations(width, height, 0, missing, regions, CompletableFuture.completedFuture(null));
        }
    }

    @Override
    public String getName() {
        return Simulated.MOD_ID + " gui sprites";
    }
}
