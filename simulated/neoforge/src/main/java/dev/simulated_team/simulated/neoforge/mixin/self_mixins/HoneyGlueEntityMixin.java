package dev.simulated_team.simulated.neoforge.mixin.self_mixins;

import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueEntity;
import net.minecraft.nbt.CompoundTag;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HoneyGlueEntity.class)
public abstract class HoneyGlueEntityMixin implements IEntityAdditionalSpawnData {

    @Shadow
    public abstract void addAdditionalSaveData(CompoundTag tag);

    @Shadow
    public abstract void readAdditionalSaveData(CompoundTag tag);

    @Override
    public void writeSpawnData(final RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        final CompoundTag compound = new CompoundTag();
        this.addAdditionalSaveData(compound);
        registryFriendlyByteBuf.writeNbt(compound);
    }

    @Override
    public void readSpawnData(final RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.readAdditionalSaveData(registryFriendlyByteBuf.readNbt());
    }
    
}
