package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.effect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.eriksonn.aeronautics.index.AeroTags;
import dev.ryanhcode.sable.render.region.SimpleCulledRenderRegionBuilder;
import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class HeatedCulledRenderRegionBuilder extends SimpleCulledRenderRegionBuilder {
    private final BlockPos worldOrigin;
    private final LevelAccelerator accelerator;

    public HeatedCulledRenderRegionBuilder(final BlockPos worldOrigin, final LevelAccelerator accelerator, final int gridSize) {
        super(gridSize);
        this.worldOrigin = worldOrigin;
        this.accelerator = accelerator;
    }

    /**
     * Renders all cubes into the specified consumer.
     *
     * @param consumer The consumer to draw cubes into
     */
    public void render(@NotNull final Matrix4f matrix4f, @NotNull final VertexConsumer consumer) {
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (final Cube cube : this.getCubes()) {
            final int x0 = cube.x();
            final int y0 = cube.y();
            final int z0 = cube.z();
            final int x1 = cube.x() + cube.sizeX();
            final int y1 = cube.y() + cube.sizeY();
            final int z1 = cube.z() + cube.sizeZ();

            if (this.shouldFaceRender(cube, Direction.NORTH)) {
                final Direction dir = Direction.NORTH;
                consumer.vertex(matrix4f, x0, y0, z0).uv(0, 0).color(this.getColor(x0, y0, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x0, y1, z0).uv(0, 1).color(this.getColor(x0, y1, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x1, y1, z0).uv(1, 1).color(this.getColor(x1, y1, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x1, y0, z0).uv(1, 0).color(this.getColor(x1, y0, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
            }

            if (this.shouldFaceRender(cube, Direction.EAST)) {
                final Direction dir = Direction.NORTH;
                consumer.vertex(matrix4f, x1, y0, z0).uv(0, 0).color(this.getColor(x1, y0, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x1, y1, z0).uv(0, 1).color(this.getColor(x1, y1, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x1, y1, z1).uv(1, 1).color(this.getColor(x1, y1, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x1, y0, z1).uv(1, 0).color(this.getColor(x1, y0, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
            }

            if (this.shouldFaceRender(cube, Direction.SOUTH)) {
                final Direction dir = Direction.NORTH;
                consumer.vertex(matrix4f, x1, y0, z1).uv(1, 0).color(this.getColor(x1, y0, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x1, y1, z1).uv(1, 1).color(this.getColor(x1, y1, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x0, y1, z1).uv(0, 1).color(this.getColor(x0, y1, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x0, y0, z1).uv(0, 0).color(this.getColor(x0, y0, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
            }

            if (this.shouldFaceRender(cube, Direction.WEST)) {
                final Direction dir = Direction.NORTH;
                consumer.vertex(matrix4f, x0, y0, z1).uv(1, 0).color(this.getColor(x0, y0, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x0, y1, z1).uv(1, 1).color(this.getColor(x0, y1, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x0, y1, z0).uv(0, 1).color(this.getColor(x0, y1, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x0, y0, z0).uv(0, 0).color(this.getColor(x0, y0, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
            }

            if (this.shouldFaceRender(cube, Direction.DOWN)) {
                final Direction dir = Direction.DOWN;
                if (this.accelerator.getBlockState(pos.set(cube.x(), cube.y() - 1, cube.z()).offset(this.worldOrigin)).is(AeroTags.BlockTags.AIRTIGHT)) {
                    consumer.vertex(matrix4f, x0, y0, z0).uv(0, 1).color(this.getColor(x0, y0, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                    consumer.vertex(matrix4f, x1, y0, z0).uv(1, 1).color(this.getColor(x1, y0, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                    consumer.vertex(matrix4f, x1, y0, z1).uv(1, 0).color(this.getColor(x1, y0, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                    consumer.vertex(matrix4f, x0, y0, z1).uv(0, 0).color(this.getColor(x0, y0, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                }
            }

            if (this.shouldFaceRender(cube, Direction.UP)) {
                final Direction dir = Direction.UP;
                consumer.vertex(matrix4f, x0, y1, z1).uv(0, 1).color(this.getColor(x0, y1, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x1, y1, z1).uv(1, 1).color(this.getColor(x1, y1, z1)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x1, y1, z0).uv(1, 0).color(this.getColor(x1, y1, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
                consumer.vertex(matrix4f, x0, y1, z0).uv(0, 0).color(this.getColor(x0, y1, z0)).normal(dir.getStepX(), dir.getStepY(), dir.getStepZ()).endVertex();
            }
        }
    }

    private int getColor(final int x, final int y, final int z) {
//        if (y == 0) {
//            return 0x77ffffff;
//        }
        return 0xffffffff;
    }
}
