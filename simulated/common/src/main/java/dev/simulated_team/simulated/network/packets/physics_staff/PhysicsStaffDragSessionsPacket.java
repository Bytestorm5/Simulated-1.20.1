package dev.simulated_team.simulated.network.packets.physics_staff;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.util.SimCodecUtil;
import foundry.veil.api.network.handler.PacketContext;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.registries.Registries;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

import java.util.List;
import java.util.UUID;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public record PhysicsStaffDragSessionsPacket(ResourceKey<Level> dimension, List<Pair<UUID, Vector3d>> sessions) implements CustomPacketPayload {
    public static Type<PhysicsStaffDragSessionsPacket> TYPE = new Type<>(Simulated.path("physics_staff_drag_sessions"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PhysicsStaffDragSessionsPacket> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), i -> i.dimension,
            CatnipStreamCodecBuilders.list(Pair.streamCodec(VanillaStreamCodecs.UUID, SimCodecUtil.STREAM_VECTOR3D)), i -> i.sessions,
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
