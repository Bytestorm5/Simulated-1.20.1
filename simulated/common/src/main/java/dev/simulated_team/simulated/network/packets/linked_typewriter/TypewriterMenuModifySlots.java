package dev.simulated_team.simulated.network.packets.linked_typewriter;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterMenuCommon;
import foundry.veil.api.network.handler.ServerPacketContext;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public record TypewriterMenuModifySlots(ItemStack first, ItemStack second) implements CustomPacketPayload {

    public static Type<TypewriterMenuModifySlots> TYPE = new Type<>(Simulated.path("entry_modify"));

    public static StreamCodec<RegistryFriendlyByteBuf, TypewriterMenuModifySlots> CODEC = StreamCodec.composite(
            VanillaStreamCodecs.ITEM_STACK, TypewriterMenuModifySlots::first,
            VanillaStreamCodecs.ITEM_STACK, TypewriterMenuModifySlots::second,
            TypewriterMenuModifySlots::new
    );

    public void handle(final ServerPacketContext context) {
        final ServerPlayer player = context.player();

        if (player.containerMenu instanceof final LinkedTypewriterMenuCommon menu) {
            menu.ghostInventory.setStackInSlot(0, this.first);
            menu.ghostInventory.setStackInSlot(1, this.second);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
