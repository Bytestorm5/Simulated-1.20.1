package dev.simulated_team.simulated.index;

import com.mojang.serialization.Codec;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import dev.simulated_team.simulated.backport.DataComponentType;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import java.util.function.UnaryOperator;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public class SimDataComponents {

    public static final DataComponentType<BlockPos> ROPE_FIRST_CONNECTION = register(
            "rope_first_connection",
            builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(VanillaStreamCodecs.BLOCK_POS)
    );

	public static final DataComponentType<UUID> LODESTONE_COMPASS_SUBLEVEL_TRACKER = register("lodestone_compass_tracker",
			uuidBuilder -> uuidBuilder.persistent(UUIDUtil.CODEC));

    public static final DataComponentType<UUID> COMPASS_PLACER_UUID = register("compass_placer",
            builder -> builder.persistent(UUIDUtil.STRING_CODEC));
    public static final DataComponentType<GlobalPos> LAST_PLAYER_DEATH_LOCATION = register("last_player_death_location",
            builder -> builder.persistent(GlobalPos.CODEC));

    public static final DataComponentType<NavigationTarget> TARGET = register("target", builder -> builder
            .persistent(SimRegistries.NAVIGATION_TARGET.byNameCodec())
            .networkSynchronized(VanillaStreamCodecs.RESOURCE_LOCATION
                    .map(SimRegistries.NAVIGATION_TARGET::get, SimRegistries.NAVIGATION_TARGET::getKey))
    );

    public static final DataComponentType<Float> BOUNCINESS = register("bounciness", builder -> builder
            .persistent(Codec.FLOAT)
            .networkSynchronized(ByteBufCodecs.FLOAT)
    );

    /**
     * 1.20.1: items have no default components, so the spring's default bounciness of 1 (set as a default component
     * on 1.21) is applied here when the stack has no explicit value.
     *
     * @return The bounciness of the stack, or null if it isn't bouncy
     */
    public static @Nullable Float getBounciness(final ItemStack stack) {
        final Float bounciness = BOUNCINESS.get(stack);
        if (bounciness == null && SimItems.SPRING.isIn(stack)) {
            return 1f;
        }
        return bounciness;
    }

    private static <T> DataComponentType<T> register(final String name, final UnaryOperator<DataComponentType.Builder<T>> builder) {
        final DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        return type.bind(Simulated.path(name));
    }

    public static void register() {}
}
