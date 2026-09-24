package dev.ryanhcode.offroad.index;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.simulated_team.simulated.backport.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class OffroadDataComponents {

	public static final DataComponentType<TireLike> TIRE = create("tire",
			builder -> builder.persistent(TireLike.CODEC));

	// 1.20.1: items have no default data components, so default tire values are kept per item here and used when the
	// stack's NBT doesn't carry a tire component.
	private static final List<DefaultTire> PENDING_DEFAULT_TIRES = new ArrayList<>();
	private static final Map<Item, TireLike> DEFAULT_TIRES = new IdentityHashMap<>();

	private static <T> DataComponentType<T> create(final String name, final UnaryOperator<DataComponentType.Builder<T>> builder) {
		final DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		return type.bind(Offroad.path(name));
	}

	/**
	 * Registers the default {@link #TIRE} value of an item. The item is resolved lazily, so this can be called before
	 * registration has finished.
	 */
	public static synchronized void registerDefaultTire(final ItemLike item, final TireLike tire) {
		PENDING_DEFAULT_TIRES.add(new DefaultTire(item, tire));
	}

	@Nullable
	private static synchronized TireLike getDefaultTire(final Item item) {
		if (!PENDING_DEFAULT_TIRES.isEmpty()) {
			for (final DefaultTire pending : PENDING_DEFAULT_TIRES) {
				DEFAULT_TIRES.put(pending.item().asItem(), pending.tire());
			}
			PENDING_DEFAULT_TIRES.clear();
		}
		return DEFAULT_TIRES.get(item);
	}

	/**
	 * The tire of a stack: its {@link #TIRE} component, or the item's default tire.
	 */
	@Nullable
	public static TireLike getTire(final ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return null;
		}
		final TireLike tire = TIRE.get(stack);
		return tire != null ? tire : getDefaultTire(stack.getItem());
	}

	public static boolean hasTire(final ItemStack stack) {
		return getTire(stack) != null;
	}

	public static void init() {
		// no-op
	}

	private record DefaultTire(ItemLike item, TireLike tire) {
	}
}
