package dev.simulated_team.simulated.content.item_attributes;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.simulated_team.simulated.index.SimItemAttributeTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockFrictionItemAttribute implements ItemAttribute {
	// 1.20.1: Create's item attributes are mutable and (de)serialized through save/load instead of codecs
	private double friction;

	public BlockFrictionItemAttribute(final double friction) {
		this.friction = friction;
	}

	public double friction() {
		return this.friction;
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof final BlockFrictionItemAttribute other && Double.compare(this.friction, other.friction) == 0;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(this.friction);
	}

	@Override
	public void save(final CompoundTag nbt) {
		nbt.putDouble("value", this.friction);
	}

	@Override
	public void load(final CompoundTag nbt) {
		this.friction = nbt.getDouble("value");
	}

	@Override
	public boolean appliesTo(final ItemStack stack, final Level world) {
		if(stack.getItem() instanceof final BlockItem item) {
			final BlockStateExtension extension = (BlockStateExtension) item.getBlock().defaultBlockState();
			return extension.sable$getProperty(PhysicsBlockPropertyTypes.FRICTION.get()) == this.friction();
		}
		return false;
	}

	@Override
	public ItemAttributeType getType() {
		return SimItemAttributeTypes.BLOCK_FRICTION.get();
	}

	@Override
	public String getTranslationKey() {
		return "block_friction";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{ this.friction() };
	}

	public static class Type implements ItemAttributeType {

		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new BlockFrictionItemAttribute(1.0);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(final ItemStack stack, final Level level) {
			if(stack.getItem() instanceof final BlockItem item) {
				final BlockStateExtension extension = (BlockStateExtension) item.getBlock().defaultBlockState();
				final double mass = extension.sable$getProperty(PhysicsBlockPropertyTypes.FRICTION.get());
				return List.of(new BlockFrictionItemAttribute(mass));
			}
			return List.of();
		}
	}
}
