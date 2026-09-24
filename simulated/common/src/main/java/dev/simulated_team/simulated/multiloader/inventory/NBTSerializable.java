package dev.simulated_team.simulated.multiloader.inventory;

import net.minecraft.nbt.CompoundTag;

public interface NBTSerializable {
    CompoundTag write();

    void read(CompoundTag nbt);
}

