package dev.simulated_team.simulated.network.packets;

import com.simibubi.create.content.kinetics.chainConveyor.ServerChainConveyorHandler;
import dev.simulated_team.simulated.Simulated;
import foundry.veil.api.network.handler.ServerPacketContext;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public record RopeRidingPacket(UUID uuid, boolean stop) implements CustomPacketPayload {
    public static Type<RopeRidingPacket> TYPE = new Type<>(Simulated.path("ride_rope"));

    public static StreamCodec<RegistryFriendlyByteBuf, RopeRidingPacket> CODEC = StreamCodec.composite(
            VanillaStreamCodecs.UUID, RopeRidingPacket::uuid,
            ByteBufCodecs.BOOL, RopeRidingPacket::stop,
            RopeRidingPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final ServerPacketContext ctx) {
        final ServerPlayer player = ctx.player();

        player.connection.aboveGroundTickCount = 0;
        player.connection.aboveGroundVehicleTickCount = 0;
        player.fallDistance = 0.0f;

        if (this.stop)
            ServerChainConveyorHandler.handleStopRidingPacket(player);
        else
            ServerChainConveyorHandler.handleTTLPacket(player);

    }
}
