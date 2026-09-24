package dev.simulated_team.simulated.network.packets.rope;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import foundry.veil.api.network.handler.ClientPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public record ClientboundRopeStoppedPacket(BlockPos ownerPos) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundRopeStoppedPacket> CODEC = VanillaStreamCodecs.BLOCK_POS.map(ClientboundRopeStoppedPacket::new, ClientboundRopeStoppedPacket::ownerPos);
    public static Type<ClientboundRopeStoppedPacket> TYPE = new Type<>(Simulated.path("rope_stopped"));

    public void handle(final ClientPacketContext context) {
        final LocalPlayer player = context.player();
        final Level level = player.level();

        final BlockEntity blockEntity = level.getBlockEntity(this.ownerPos);

        if (!(blockEntity instanceof final SmartBlockEntity smartBlockEntity)) {
            return;
        }

        final RopeStrandHolderBehavior ropeHolder = smartBlockEntity.getBehaviour(RopeStrandHolderBehavior.TYPE);

        if (ropeHolder == null) {
            return;
        }

        ropeHolder.receiveClientStrandStopped();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
