package dev.simulated_team.simulated.content.navigation_targets;

import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.index.SimTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MapNavigationTarget implements NavigationTarget {
	@Override
	public @Nullable Vec3 getTarget(final NavTableBlockEntity navBE, final ItemStack self) {
		final Level level = navBE.getLevel();
		final Vec3 pos = navBE.getProjectedSelfPos();
		return getNearestDecorationPos(level, pos, self);
	}

	private static Vec3 getNearestDecorationPos(final Level level, final Vec3 pos, final ItemStack stack) {
		// 1.20.1: map decorations live in the stack's "Decorations" NBT list instead of a data component
		final CompoundTag tag = stack.getTag();
		final Integer mapId = MapItem.getMapId(stack);
		if(tag != null && tag.contains("Decorations", Tag.TAG_LIST) && mapId != null) {
			final ListTag decorations = tag.getList("Decorations", Tag.TAG_COMPOUND);

			double closestDist = Double.POSITIVE_INFINITY;
			Vec3 closestPos = null;
			for (int i = 0; i < decorations.size(); i++) {
				final CompoundTag decoration = decorations.getCompound(i);
				final MapDecoration.Type type = MapDecoration.Type.byIcon(decoration.getByte("type"));
				if(!SimTags.Misc.NAV_TABLE_FINDABLE.contains(type))
					continue;

				final double x = decoration.getDouble("x");
				final double z = decoration.getDouble("z");
				final double dist = pos.distanceToSqr(x, pos.y(), z);
				if(dist < closestDist) {
					closestPos = new Vec3(x, pos.y(), z);
					closestDist = dist;
				}
			}

			final MapItemSavedData mapData = MapItem.getSavedData(mapId, level);
			if (mapData != null) {
				final Collection<MapBanner> banners = mapData.getBanners();
				for (final MapBanner banner : banners) {
					final Vec3 bannerPos = banner.getPos().getCenter();
					final double dist = pos.distanceToSqr(bannerPos.x(), pos.y(), bannerPos.z());
					if(dist < closestDist) {
						closestPos = bannerPos;
						closestDist = dist;
					}
				}
			}

			return closestPos;
		}

		return null;
	}
}
