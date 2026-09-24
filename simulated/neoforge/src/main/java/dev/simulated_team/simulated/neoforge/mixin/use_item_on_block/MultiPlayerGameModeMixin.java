package dev.simulated_team.simulated.neoforge.mixin.use_item_on_block;

import dev.simulated_team.simulated.events.SimulatedCommonClientEvents;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.20.1: Forge has no {@code UseItemOnBlockEvent}. This replaces its {@code ITEM_AFTER_BLOCK} phase on the client:
 * it runs after the targeted block didn't consume the interaction, right before the held item would be used on it.
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "performUseItemOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$RightClickBlock;getUseItem()Lnet/minecraftforge/eventbus/api/Event$Result;", ordinal = 1, remap = false),
            cancellable = true)
    private void simulated$useItemAfterBlock(final LocalPlayer player, final InteractionHand hand, final BlockHitResult hitResult, final CallbackInfoReturnable<InteractionResult> cir) {
        if (SimulatedCommonClientEvents.useItemOnBlockEvent(player.level(), player, player.getItemInHand(hand), hand)) {
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }
}
