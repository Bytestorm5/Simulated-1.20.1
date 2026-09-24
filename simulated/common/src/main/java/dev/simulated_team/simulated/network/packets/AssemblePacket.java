package dev.simulated_team.simulated.network.packets;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity;
import dev.simulated_team.simulated.index.SimStats;
import foundry.veil.api.network.handler.ServerPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public record AssemblePacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<AssemblePacket> TYPE = new Type<>(Simulated.path("assemble"));

    public static final StreamCodec<ByteBuf, AssemblePacket> CODEC = StreamCodec.composite(
            VanillaStreamCodecs.BLOCK_POS, packet -> packet.pos,
            AssemblePacket::new);

    @Override
    public Type<AssemblePacket> type() {
        return TYPE;
    }

    public void handle(final ServerPacketContext context) {
        final ServerPlayer player = context.player();
        final ServerLevel level = player.serverLevel();

        if (player.canReach(this.pos, 4) &&
                level.getBlockEntity(this.pos) instanceof final PhysicsAssemblerBlockEntity assembler) {
            assembler.assembleOrDisassemble();
            SimStats.INTERACT_WITH_ASSEMBLER.awardTo(context.player());
        }
    }
}
