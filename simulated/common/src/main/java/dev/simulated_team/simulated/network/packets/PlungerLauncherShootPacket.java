package dev.simulated_team.simulated.network.packets;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.content.items.plunger_launcher.PlungerLauncherItemRenderer;
import foundry.veil.api.network.handler.PacketContext;
import io.netty.buffer.ByteBuf;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import net.minecraft.client.Minecraft;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

public class PlungerLauncherShootPacket implements CustomPacketPayload {

    public static Type<PlungerLauncherShootPacket> TYPE = new Type<>(Simulated.path("plunger_launcher_shoot"));

    // 1.20.1: equivalent of Catnip's CatnipStreamCodecs.HAND, which Ponder 1.0.91 doesn't have
    private static final StreamCodec<ByteBuf, InteractionHand> HAND_CODEC = ByteBufCodecs.idMapper(i -> InteractionHand.values()[i], InteractionHand::ordinal);

    public static final StreamCodec<ByteBuf, PlungerLauncherShootPacket> CODEC = StreamCodec.composite(
            HAND_CODEC, packet -> packet.hand,
            PlungerLauncherShootPacket::new
    );

    protected final InteractionHand hand;

    public PlungerLauncherShootPacket(final InteractionHand hand) {
        this.hand = hand;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final PacketContext context) {
        final Entity renderViewEntity = Minecraft.getInstance().getCameraEntity();
        if (renderViewEntity == null) {
            return;
        }

        final PlungerLauncherItemRenderer.RenderHandler handler = SimulatedClient.PLUNGER_LAUNCHER_RENDER_HANDLER;
        handler.basicShoot(this.hand);
        handler.playSound(this.hand, context.player().position());
    }
}
