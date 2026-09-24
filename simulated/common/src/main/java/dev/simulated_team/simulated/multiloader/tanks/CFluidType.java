package dev.simulated_team.simulated.multiloader.tanks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A loader-independent representation of a fluid
 *
 * @param data The fluid's NBT data, or null if it has none (1.20.1: fluids carry NBT instead of data components)
 */
public record CFluidType(Fluid fluid, @Nullable CompoundTag data) {
    public CFluidType {
        // an empty tag means the same as no tag, like an empty component patch did
        if (data != null && data.isEmpty()) {
            data = null;
        }
    }

    public boolean isBlank() {
        return this.equals(BLANK);
    }

    public static final CFluidType BLANK = new CFluidType(Fluids.EMPTY, null);

    public CompoundTag write() {
        final CompoundTag tag = new CompoundTag();
        tag.putString("Fluid", BuiltInRegistries.FLUID.getKey(this.fluid).toString());

        if (this.data != null) {
            tag.put("data", this.data.copy());
        }

        return tag;
    }

    public static CFluidType read(final CompoundTag tag) {
        final Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString("Fluid")));
        CompoundTag data = null;
        if (tag.contains("data")) {
            data = tag.getCompound("data").copy();
        }

        return new CFluidType(fluid, data);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof final CFluidType other) {
            // both haves tag, or both no haves tag
            return this.fluid.isSame(other.fluid()) && Objects.equals(this.data, other.data());
        }
        return false;
    }
}
