package dev.simulated_team.simulated.network.packets;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelBlockEntity;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimStats;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.core.BlockPos;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public record SteeringWheelPacket(boolean shouldStop, float targetAngle, BlockPos pos) implements CustomPacketPayload {

    public static Type<SteeringWheelPacket> TYPE = new Type<>(Simulated.path("steering_wheel_update"));

    public static StreamCodec <RegistryFriendlyByteBuf, SteeringWheelPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SteeringWheelPacket::shouldStop,
            ByteBufCodecs.FLOAT, SteeringWheelPacket::targetAngle,
            VanillaStreamCodecs.BLOCK_POS, SteeringWheelPacket::pos,
            SteeringWheelPacket::new);

    public void handle(final ServerPacketContext context) {
        final ServerPlayer player = context.player();

        if (player.canReach(this.pos, 4) &&
                player.level().getBlockEntity(this.pos) instanceof final SteeringWheelBlockEntity be) {
            be.targetAngleToUpdate = this.targetAngle();

            if (this.shouldStop()) {
                be.stopHolding();
            } else {
                be.startHolding();
                SimStats.INTERACT_WITH_STEERING_WHEEL.awardTo(player);
                SimAdvancements.UNPOWERED_STEERING.awardTo(player);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
