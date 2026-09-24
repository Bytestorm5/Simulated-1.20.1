package dev.simulated_team.simulated.mixin.accessor;

import com.simibubi.create.foundation.data.CreateBlockEntityBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.NonNullPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(value = CreateBlockEntityBuilder.class, remap = false)
public interface CreateBlockEntityBuilderAccessor<T extends BlockEntity, P> {

    @Accessor
    NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> getVisualFactory();

    @Accessor
    // 1.20.1: Create 6.0.8 stores this as a Forge NonNullPredicate
    NonNullPredicate<T> getRenderNormally();

}
