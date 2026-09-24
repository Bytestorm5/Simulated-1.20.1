package dev.simulated_team.simulated.network.packets;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver.ModulatingLinkedReceiverBlockEntity;
import dev.simulated_team.simulated.network.packets.helpers.SimBlockEntityConfigurationPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public class ConfigureModulatingLinkedRecieverPacket extends SimBlockEntityConfigurationPacket<ModulatingLinkedReceiverBlockEntity> {
    public static final Type<ConfigureModulatingLinkedRecieverPacket> TYPE = new Type<>(Simulated.path("configure_modulating_linked_reciever"));
    public static final StreamCodec<ByteBuf, ConfigureModulatingLinkedRecieverPacket> CODEC = StreamCodec.composite(
            VanillaStreamCodecs.BLOCK_POS, SimBlockEntityConfigurationPacket::getPos,
            ByteBufCodecs.INT, ConfigureModulatingLinkedRecieverPacket::getMinRange,
            ByteBufCodecs.INT, ConfigureModulatingLinkedRecieverPacket::getMaxRange,
            ConfigureModulatingLinkedRecieverPacket::new);

    private final int minRange;
    private final int maxRange;

    public ConfigureModulatingLinkedRecieverPacket(final BlockPos pos, final int minRange, final int maxRange) {
        super(pos);
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    public int getMinRange() {
        return this.minRange;
    }

    public int getMaxRange() {
        return this.maxRange;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    protected void applySettings(final ServerPlayer serverPlayer, final ModulatingLinkedReceiverBlockEntity be) {
        final ModulatingLinkedReceiverBlockEntity abe = be;
        if (abe != null) {
            abe.minRange = this.minRange;
            abe.maxRange = this.maxRange;

            abe.notifyUpdate();
        }
    }
}
