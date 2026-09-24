package dev.simulated_team.simulated.compat.naturescompass;

import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.CompassState;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class NaturesCompassNavigationTarget implements NavigationTarget {
	@Override
	public @Nullable Vec3 getTarget(final NavTableBlockEntity navBE, final ItemStack self) {
		// 1.20.1: Nature's Compass stores the found position in NBT, only valid while the compass is in the FOUND state
		if (self.getItem() instanceof final NaturesCompassItem compass && compass.getState(self) == CompassState.FOUND) {
			final int x = compass.getFoundBiomeX(self);
			final int z = compass.getFoundBiomeZ(self);
			final Vec3 pos = navBE.getProjectedSelfPos();
			return new Vec3(x, pos.y(), z);
		}

		return null;
	}
}
