package dev.simulated_team.simulated.multiloader.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An info wrapper that holds an item type, and its associated NBT data. Primarly used for Simulated's multiloader inventory structure. <p>
 * In order to generate a wrapper from a given item easily, <b>{@link ItemInfoWrapper#generateFromStack(ItemStack) generateFromStack()}</b> can be used. <p>
 * In order to generate a new item from a given wrapper easily, <b>{@link ItemInfoWrapper#generateFromInfo(ItemInfoWrapper) generateFromInfo()}</b> can be used.
 *
 * @param type     The item type of this wrapper
 * @param patchMap The NBT data of this wrapper, or null if the item has none (1.20.1: items store data in NBT instead of components)
 */
public record ItemInfoWrapper(Item type, @Nullable CompoundTag patchMap) {

    /**
     * Generates a new wrapper from the given item
     *
     * @param stack The item stack to gather information from.
     * @return A <b>new</b> {@link ItemInfoWrapper} containing the type and NBT data from the given item.
     */
    public static ItemInfoWrapper generateFromStack(final ItemStack stack) {
        return new ItemInfoWrapper(stack.getItem(), stack.hasTag() ? stack.getTag().copy() : null);
    }

    /**
     * Generates a new {@link ItemStack} from the given wrapper.
     *
     * @param info The {@link ItemInfoWrapper} to use information from.
     * @return A <b>new</b> {@link ItemStack} containing data from the given wrapper.
     */
    public static @NotNull ItemStack generateFromInfo(final ItemInfoWrapper info) {
        final ItemStack newStack = new ItemStack(info.type());
        if (info.patchMap() != null) {
            newStack.setTag(info.patchMap().copy());
        }
        return newStack;
    }
}
