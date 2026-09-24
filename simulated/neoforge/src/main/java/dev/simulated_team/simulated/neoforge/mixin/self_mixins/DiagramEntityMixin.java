package dev.simulated_team.simulated.neoforge.mixin.self_mixins;

import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DiagramEntity.class)
public abstract class DiagramEntityMixin implements IEntityAdditionalSpawnData {
    @Shadow public abstract void addAdditionalSaveData(CompoundTag tag);

    @Shadow public abstract void readAdditionalSaveData(CompoundTag tag);

    @Override
    public void writeSpawnData(final FriendlyByteBuf buf) {
        final CompoundTag compound = new CompoundTag();
        this.addAdditionalSaveData(compound);
        buf.writeNbt(compound);
    }

    @Override
    public void readSpawnData(final FriendlyByteBuf buf) {
        this.readAdditionalSaveData(buf.readNbt());
    }

    /**
     * 1.20.1: Forge only sends the additional spawn data with its own spawn packet
     */
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity) (Object) this);
    }
}
