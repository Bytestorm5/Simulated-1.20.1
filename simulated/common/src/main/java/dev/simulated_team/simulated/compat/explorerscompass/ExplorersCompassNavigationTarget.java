package dev.simulated_team.simulated.compat.explorerscompass;

import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ExplorersCompassNavigationTarget implements NavigationTarget {
	@Override
	public @Nullable Vec3 getTarget(final NavTableBlockEntity navBE, final ItemStack self) {
		// 1.20.1: Explorer's Compass stores the found position in NBT, only valid while the compass is in the FOUND state
		if (self.getItem() instanceof final ExplorersCompassItem compass && compass.getState(self) == CompassState.FOUND) {
			final int x = compass.getFoundStructureX(self);
			final int z = compass.getFoundStructureZ(self);
			final Vec3 pos = navBE.getProjectedSelfPos();
			return new Vec3(x, pos.y(), z);
		}

		return null;
	}
}
