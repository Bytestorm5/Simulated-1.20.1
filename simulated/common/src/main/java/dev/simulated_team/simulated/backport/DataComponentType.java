package dev.simulated_team.simulated.backport;

import com.mojang.serialization.Codec;
import foundry.veil.backport.network.codec.StreamCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Stand-in for 1.21's {@code net.minecraft.core.component.DataComponentType} on 1.20.1, where items have no data
 * components. The value is stored in the item's NBT under the component id, encoded with the component's codec.
 * <p>
 * 1.21 code reads {@code stack.get(TYPE)}; on 1.20.1 that is {@code TYPE.get(stack)}.
 */
public final class DataComponentType<T> {

    @Nullable
    private final Codec<T> codec;
    private String key;

    private DataComponentType(@Nullable final Codec<T> codec) {
        this.codec = codec;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Assigns the id this component is stored under. Called once, when the owning mod creates its components.
     */
    public DataComponentType<T> bind(final ResourceLocation id) {
        if (this.key != null) {
            throw new IllegalStateException("Component " + this.key + " is already bound");
        }
        this.key = id.toString();
        return this;
    }

    public String key() {
        return Objects.requireNonNull(this.key, "Unbound data component");
    }

    @Nullable
    public Codec<T> codec() {
        return this.codec;
    }

    public boolean has(final ItemStack stack) {
        final CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(this.key());
    }

    @Nullable
    public T get(final ItemStack stack) {
        final CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(this.key()) || this.codec == null) {
            return null;
        }
        return this.codec.parse(NbtOps.INSTANCE, tag.get(this.key())).result().orElse(null);
    }

    public T getOrDefault(final ItemStack stack, final T fallback) {
        final T value = this.get(stack);
        return value != null ? value : fallback;
    }

    /**
     * Sets the value, or removes it when {@code value} is null.
     *
     * @return The previous value
     */
    @Nullable
    public T set(final ItemStack stack, @Nullable final T value) {
        final T previous = this.get(stack);
        if (value == null) {
            this.remove(stack);
            return previous;
        }
        if (this.codec == null) {
            throw new IllegalStateException("Component " + this.key() + " is not persistent");
        }
        final Tag encoded = this.codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, error -> { });
        stack.getOrCreateTag().put(this.key(), encoded);
        return previous;
    }

    @Nullable
    public T update(final ItemStack stack, final T fallback, final UnaryOperator<T> updater) {
        return this.set(stack, updater.apply(this.getOrDefault(stack, fallback)));
    }

    @Nullable
    public T remove(final ItemStack stack) {
        final T previous = this.get(stack);
        final CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(this.key());
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
        return previous;
    }

    /**
     * Copies this component from one stack to another.
     */
    public void copy(final ItemStack from, final ItemStack to) {
        final CompoundTag tag = from.getTag();
        if (tag != null && tag.contains(this.key())) {
            to.getOrCreateTag().put(this.key(), tag.get(this.key()).copy());
        } else {
            this.remove(to);
        }
    }

    @Override
    public String toString() {
        return "DataComponentType[" + this.key + "]";
    }

    public static final class Builder<T> {
        @Nullable
        private Codec<T> codec;

        public Builder<T> persistent(final Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        /**
         * Components are stored in item NBT, which is synchronized with the item, so the stream codec isn't needed.
         */
        public Builder<T> networkSynchronized(final StreamCodec<?, T> codec) {
            return this;
        }

        public Builder<T> cacheEncoding() {
            return this;
        }

        public DataComponentType<T> build() {
            return new DataComponentType<>(this.codec);
        }
    }
}
