package dev.simulated_team.simulated.neoforge.mixin.self_mixins;

import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 1.20.1: extends {@link Entity} so that {@link #getAddEntityPacket()} is a real override. As a plain mixin method it
 * kept its dev name in the reobfuscated jar while {@code Entity#getAddEntityPacket} is {@code m_5654_}, so in
 * production it overrode nothing, Forge's spawn packet was never used and the spawn data never reached clients.
 */
@Mixin(DiagramEntity.class)
public abstract class DiagramEntityMixin extends Entity implements IEntityAdditionalSpawnData {

    private DiagramEntityMixin(final EntityType<?> type, final Level level) {
        super(type, level);
    }

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
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
