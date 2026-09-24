package dev.eriksonn.aeronautics.network.packets;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.CrystalPropagationContext;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.LevititeBlendHelper;
import dev.eriksonn.aeronautics.index.AeroLevititeBlendPropagationContexts;
import dev.eriksonn.aeronautics.index.AeroTags;
import dev.eriksonn.aeronautics.util.CatalyzerHelper;
import foundry.veil.api.network.handler.ServerPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
public record LevititeCatalystCrystallizationPacket(BlockPos pos, InteractionHand hand) implements CustomPacketPayload {
	public static final Type<LevititeCatalystCrystallizationPacket> TYPE = new Type<>(Aeronautics.path("levitite_blend_crystallize"));

	private static final StreamCodec<ByteBuf, InteractionHand> HAND_CODEC = ByteBufCodecs.idMapper(i -> InteractionHand.values()[i], InteractionHand::ordinal);

	public static final StreamCodec<RegistryFriendlyByteBuf, LevititeCatalystCrystallizationPacket> STREAM_CODEC = StreamCodec.composite(
			VanillaStreamCodecs.BLOCK_POS, LevititeCatalystCrystallizationPacket::pos,
			HAND_CODEC, LevititeCatalystCrystallizationPacket::hand,
			LevititeCatalystCrystallizationPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(final ServerPacketContext context) {
		final ServerPlayer player = context.player();

		final ItemStack item = player.getItemInHand(this.hand);

		if (!CatalyzerHelper.isCatalyzer(item)) {
			return;
		}

		if (!item.is(AeroTags.ItemTags.LEVITITE_CATALYZER_NO_CONSUME)) {
			if (item.isDamageableItem()) {
				item.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(this.hand));
			} else if (item.isStackable() && !context.player().getAbilities().instabuild) {
				item.shrink(1);
			}
		}
		player.swing(this.hand);
		final CrystalPropagationContext itemContext = item.is(AeroTags.ItemTags.LEVITITE_SOUL_CATALYZER) ?
				AeroLevititeBlendPropagationContexts.SOUL_CONTEXT.get() :
				AeroLevititeBlendPropagationContexts.STANDARD_CONTEXT.get();
		LevititeBlendHelper.addLevititeBlendTicker(context.level(), this.pos, false, false,
				itemContext.getContextForSpread(context.level(), this.pos)
		);
	}
}
