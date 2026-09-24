package dev.simulated_team.simulated.network.packets.name_plate;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.nameplate.NameplateBlockEntity;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.core.BlockPos;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public record NameplateChangeNamePacket(BlockPos controllerPos, @Nullable String name) implements CustomPacketPayload {

    public static Type<NameplateChangeNamePacket> TYPE = new Type<>(Simulated.path("nameplate_change_name"));

    public static StreamCodec<RegistryFriendlyByteBuf, NameplateChangeNamePacket> CODEC = StreamCodec.composite(
            VanillaStreamCodecs.BLOCK_POS, NameplateChangeNamePacket::controllerPos,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), (packet) -> Optional.ofNullable(packet.name()),
            NameplateChangeNamePacket::fromCodec);

    public static NameplateChangeNamePacket fromCodec(final BlockPos controllerPos, final Optional<String> name) {
        return new NameplateChangeNamePacket(controllerPos, name.orElse(null));
    }

    public void handle(final ServerPacketContext context) {
        final Level level = context.level();
        if (level.isLoaded(this.controllerPos()) &&
                level.getBlockEntity(this.controllerPos()) instanceof final NameplateBlockEntity nbe &&
                nbe.allowsEditing()) {
            nbe.setName(this.name, true, context.player());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
