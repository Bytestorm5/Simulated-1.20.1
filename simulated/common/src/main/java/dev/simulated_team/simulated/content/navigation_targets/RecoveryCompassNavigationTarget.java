package dev.simulated_team.simulated.content.navigation_targets;

import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.index.SimDataComponents;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.UUID;

public class RecoveryCompassNavigationTarget implements NavigationTarget {
    @Override
    public @Nullable Vec3 getTarget(final NavTableBlockEntity navBE, final ItemStack self) {
        final UUID lastPlayer = SimDataComponents.COMPASS_PLACER_UUID.get(self);
        if (lastPlayer != null) {
            GlobalPos lastDeathLocation;

            final Player player = navBE.getLevel().getPlayerByUUID(lastPlayer);
            if (player != null) {
                Optional<GlobalPos> lastDeathLocationOptional = player.getLastDeathLocation();
                if (lastDeathLocationOptional.isEmpty()) {
                    SimDataComponents.LAST_PLAYER_DEATH_LOCATION.remove(self);
                    return null;
                }

                lastDeathLocation = lastDeathLocationOptional.get();
                SimDataComponents.LAST_PLAYER_DEATH_LOCATION.set(self, lastDeathLocation);
            } else {
                lastDeathLocation = SimDataComponents.LAST_PLAYER_DEATH_LOCATION.get(self);
            }

            if (lastDeathLocation == null) {
                return null;
            }

            final ResourceKey<Level> dimension = navBE.getLevel().dimension();
            if (!lastDeathLocation.dimension().equals(dimension)) {
                return null;
            }

            return lastDeathLocation.pos().getCenter();
        }

        return null;
    }

    @Override
    public void onInsert(final ItemStack itemStack, final NavTableBlockEntity be, @Nullable final Player player) {
        if (player != null) {
            DataComponentMap.Builder builder = SimDataComponents.COMPASS_PLACER_UUID.set(DataComponentMap.builder(), player.getUUID());
            player.getLastDeathLocation().ifPresent(globalPos -> SimDataComponents.LAST_PLAYER_DEATH_LOCATION.set(builder, globalPos));
            itemStack.applyComponents(builder.build());
        }
    }

    @Override
    public void onExtract(final ItemStack itemStack, final NavTableBlockEntity be, @Nullable final Player player) {
        SimDataComponents.COMPASS_PLACER_UUID.remove(itemStack);
        SimDataComponents.LAST_PLAYER_DEATH_LOCATION.remove(itemStack);
    }
}
