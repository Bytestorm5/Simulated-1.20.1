package dev.simulated_team.simulated.neoforge.capability;

import dev.simulated_team.simulated.Simulated;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Exposes capabilities on block entity types that Simulated (or its addons) register through the loader services.
 * <p>
 * 1.20.1: Forge has no {@code RegisterCapabilitiesEvent}; capabilities are attached to every matching block entity
 * through {@link AttachCapabilitiesEvent}. Like NeoForge's block capability providers, the getter is queried on every
 * lookup. The returned {@link LazyOptional} is cached for as long as the getter keeps returning the same object, and is
 * invalidated when the getter's result changes or the block entity's capabilities are invalidated.
 */
public final class SimBlockEntityCapabilities {

    private static final ResourceLocation KEY = Simulated.path("block_entity_capabilities");
    private static final List<Entry<?, ?>> ENTRIES = new ArrayList<>();

    private SimBlockEntityCapabilities() {
    }

    /**
     * Registers a capability for a block entity type.
     *
     * @param type       the block entity type (resolved lazily)
     * @param capability the capability to expose
     * @param getter     gets the backing object for the given side, or null if the capability isn't available
     * @param wrapper    wraps the backing object into the capability's interface
     */
    public static <T extends BlockEntity, S, C> void register(final Supplier<? extends BlockEntityType<?>> type,
                                                              final Capability<C> capability,
                                                              final BiFunction<T, Direction, S> getter,
                                                              final Function<S, ? extends C> wrapper) {
        synchronized (ENTRIES) {
            ENTRIES.add(new Entry<>(type, capability, getter, wrapper));
        }
    }

    public static void attach(final AttachCapabilitiesEvent<BlockEntity> event) {
        final BlockEntity be = event.getObject();
        final BlockEntityType<?> type = be.getType();

        List<Entry<?, ?>> matching = null;
        synchronized (ENTRIES) {
            for (final Entry<?, ?> entry : ENTRIES) {
                if (entry.type.get() == type) {
                    if (matching == null) {
                        matching = new ArrayList<>();
                    }
                    matching.add(entry);
                }
            }
        }

        if (matching == null) {
            return;
        }

        final Provider provider = new Provider(be, matching);
        event.addCapability(KEY, provider);
        event.addListener(provider::invalidate);
    }

    private record Entry<S, C>(Supplier<? extends BlockEntityType<?>> type, Capability<C> capability,
                               BiFunction<? extends BlockEntity, Direction, S> getter,
                               Function<S, ? extends C> wrapper) {

        @SuppressWarnings("unchecked")
        @Nullable
        S getSource(final BlockEntity be, @Nullable final Direction side) {
            return ((BiFunction<BlockEntity, Direction, S>) this.getter).apply(be, side);
        }
    }

    private record CacheKey(Entry<?, ?> entry, @Nullable Direction side) {
    }

    private record Cached(Object source, LazyOptional<?> optional) {
    }

    private static final class Provider implements ICapabilityProvider {
        private final BlockEntity be;
        private final List<Entry<?, ?>> entries;
        private final Map<CacheKey, Cached> cache = new HashMap<>();

        private Provider(final BlockEntity be, final List<Entry<?, ?>> entries) {
            this.be = be;
            this.entries = entries;
        }

        @Override
        public <C> @NotNull LazyOptional<C> getCapability(@NotNull final Capability<C> cap, @Nullable final Direction side) {
            for (final Entry<?, ?> entry : this.entries) {
                if (entry.capability != cap) {
                    continue;
                }

                final LazyOptional<?> optional = this.get(entry, side);
                if (optional.isPresent()) {
                    return optional.cast();
                }
            }

            return LazyOptional.empty();
        }

        private <S, C> LazyOptional<?> get(final Entry<S, C> entry, @Nullable final Direction side) {
            final CacheKey key = new CacheKey(entry, side);
            final S source = entry.getSource(this.be, side);
            final Cached cached = this.cache.get(key);

            if (cached != null) {
                if (cached.source == source) {
                    return cached.optional;
                }

                this.cache.remove(key);
                cached.optional.invalidate();
            }

            if (source == null) {
                return LazyOptional.empty();
            }

            final C value = Objects.requireNonNull(entry.wrapper.apply(source));
            final LazyOptional<C> optional = LazyOptional.of(() -> value);
            this.cache.put(key, new Cached(source, optional));
            return optional;
        }

        private void invalidate() {
            final List<Cached> values = new ArrayList<>(this.cache.values());
            this.cache.clear();
            for (final Cached cached : values) {
                cached.optional.invalidate();
            }
        }
    }
}
