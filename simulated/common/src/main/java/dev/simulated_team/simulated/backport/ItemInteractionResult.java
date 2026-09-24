package dev.simulated_team.simulated.backport;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import java.util.function.Supplier;

/**
 * Backport of {@code net.minecraft.world.ItemInteractionResult}.
 * <p>
 * 1.21 splits {@code Block#use} into {@code useItemOn}, which returns this type, and {@code useWithoutItem}, which is
 * only called when {@code useItemOn} returns {@link #PASS_TO_DEFAULT_BLOCK_INTERACTION}. Blocks ported to 1.20.1 keep
 * both methods and bridge them from {@code use} with {@link #use(ItemInteractionResult, InteractionHand, Supplier)}.
 */
public enum ItemInteractionResult {
    SUCCESS,
    CONSUME,
    CONSUME_PARTIAL,
    PASS_TO_DEFAULT_BLOCK_INTERACTION,
    SKIP_DEFAULT_BLOCK_INTERACTION,
    FAIL;

    public boolean consumesAction() {
        return this.result().consumesAction();
    }

    public static ItemInteractionResult sidedSuccess(final boolean clientSide) {
        return clientSide ? SUCCESS : CONSUME;
    }

    public InteractionResult result() {
        return switch (this) {
            case SUCCESS -> InteractionResult.SUCCESS;
            case CONSUME -> InteractionResult.CONSUME;
            case CONSUME_PARTIAL -> InteractionResult.CONSUME_PARTIAL;
            case PASS_TO_DEFAULT_BLOCK_INTERACTION, SKIP_DEFAULT_BLOCK_INTERACTION -> InteractionResult.PASS;
            case FAIL -> InteractionResult.FAIL;
        };
    }

    /**
     * Converts a 1.21 {@code InteractionResult} returned by an item-less interaction into the item result that passes
     * it on unchanged.
     */
    public static ItemInteractionResult of(final InteractionResult result) {
        return switch (result) {
            case SUCCESS -> SUCCESS;
            case CONSUME -> CONSUME;
            case CONSUME_PARTIAL -> CONSUME_PARTIAL;
            case FAIL -> FAIL;
            default -> PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    /**
     * Resolves a 1.21-style block interaction the way 1.21's {@code ServerPlayerGameMode#useItemOn} does.
     *
     * @param itemResult  The result of {@code useItemOn}
     * @param hand        The hand used
     * @param withoutItem Runs {@code useWithoutItem}, or the 1.20.1 super implementation of {@code use}
     * @return The result of the combined 1.20.1 {@code Block#use}
     */
    public static InteractionResult use(final ItemInteractionResult itemResult, final InteractionHand hand, final Supplier<InteractionResult> withoutItem) {
        if (itemResult.consumesAction()) {
            return itemResult.result();
        }

        if (itemResult == PASS_TO_DEFAULT_BLOCK_INTERACTION && hand == InteractionHand.MAIN_HAND) {
            return withoutItem.get();
        }

        return InteractionResult.PASS;
    }
}
