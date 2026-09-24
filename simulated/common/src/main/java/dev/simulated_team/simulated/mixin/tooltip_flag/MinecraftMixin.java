package dev.simulated_team.simulated.mixin.tooltip_flag;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.simulated_team.simulated.mixin_interface.tooltip_flag.TooltipFlagExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * 1.20.1: there is no SessionSearchTrees; the creative search tree collects tooltips inside a lambda of
 * {@link Minecraft#createSearchTrees()} using {@code TooltipFlag.Default.NORMAL.asCreative()}. The recipe-book tree
 * uses a non-creative flag, so only creative flags are marked, matching 1.21's updateCreativeTooltips hook.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"))
    private static List<Component> simulated$markAsCreativeSearch(final ItemStack instance, final Player player, final TooltipFlag flag, final Operation<List<Component>> original) {
        if (flag instanceof final TooltipFlag.Default defaultFlag && defaultFlag.isCreative()) {
            ((TooltipFlagExtension) (Object) defaultFlag).simulated$setCreativeSearch(true);
        }
        return original.call(instance, player, flag);
    }
}
