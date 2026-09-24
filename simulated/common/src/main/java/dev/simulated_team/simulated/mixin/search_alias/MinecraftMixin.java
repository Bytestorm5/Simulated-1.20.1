package dev.simulated_team.simulated.mixin.search_alias;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.simulated_team.simulated.client.SearchAlias;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * 1.20.1: there is no SessionSearchTrees; the creative-search and recipe-book search trees are built from item tooltips
 * inside the lambdas of {@link Minecraft#createSearchTrees()}, which are the only tooltip lookups in this class.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"))
    private static List<Component> simulated$getTooltipLines(final ItemStack instance, final Player player, final TooltipFlag flag, final Operation<List<Component>> original) {
        final List<Component> tooltipLines = original.call(instance, player, flag);
        tooltipLines.addAll(SearchAlias.getAliases(instance).stream().map(Component::literal).toList());
        return tooltipLines;
    }
}
