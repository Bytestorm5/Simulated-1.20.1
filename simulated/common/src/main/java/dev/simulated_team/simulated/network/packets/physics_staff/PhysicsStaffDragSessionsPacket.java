package dev.simulated_team.simulated.network.packets.physics_staff;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.util.SimCodecUtil;
import foundry.veil.api.network.handler.PacketContext;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import net.createmod.catnip.data.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public record PhysicsStaffDragSessionsPacket(ResourceKey<Level> dimension, List<Pair<UUID, Vector3d>> sessions) implements CustomPacketPayload {
    public static Type<PhysicsStaffDragSessionsPacket> TYPE = new Type<>(Simulated.path("physics_staff_drag_sessions"));

    // 1.20.1: Catnip's Pair.streamCodec doesn't exist in Ponder 1.0.91
    private static final StreamCodec<ByteBuf, Pair<UUID, Vector3d>> SESSION_CODEC = StreamCodec.composite(
            VanillaStreamCodecs.UUID, Pair::getFirst,
            SimCodecUtil.STREAM_VECTOR3D, Pair::getSecond,
            Pair::of
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PhysicsStaffDragSessionsPacket> CODEC = StreamCodec.composite(
            VanillaStreamCodecs.RESOURCE_LOCATION.map(id -> ResourceKey.create(Registries.DIMENSION, id), ResourceKey::location), i -> i.dimension,
            ByteBufCodecs.collection(ArrayList::new, SESSION_CODEC), i -> i.sessions,
            PhysicsStaffDragSessionsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final PacketContext context) {
        SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER.setServerDragSessions(this.dimension, this.sessions);
    }
}
