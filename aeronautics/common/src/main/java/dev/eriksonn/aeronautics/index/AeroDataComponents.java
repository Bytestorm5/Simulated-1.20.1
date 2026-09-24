package dev.eriksonn.aeronautics.index;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.components.Converter;
import dev.eriksonn.aeronautics.content.components.Levitating;
import dev.simulated_team.simulated.backport.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class AeroDataComponents {

	public static final DataComponentType<Levitating> LEVITATING = create("levitating",
			builder -> builder.persistent(Levitating.CODEC));

	public static final DataComponentType<Converter> CONVERTER = create("converter",
			builder -> builder.persistent(Converter.CODEC));

	/**
	 * 1.20.1: items have no default data components, so the defaults 1.21 set through {@code Item.Properties#component}
	 * are kept here and used when a stack has no value of its own.
	 */
	private static final Map<Item, Levitating> DEFAULT_LEVITATING = new IdentityHashMap<>();

	private static <T> DataComponentType<T> create(final String name, final UnaryOperator<DataComponentType.Builder<T>> builder) {
		final DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		return type.bind(Aeronautics.path(name));
	}

	public static void setDefaultLevitating(final Item item, final Levitating levitating) {
		DEFAULT_LEVITATING.put(item, levitating);
	}

	/**
	 * @return The levitating component of the stack, falling back to the default of its item
	 */
	@Nullable
	public static Levitating getLevitating(final ItemStack stack) {
		final Levitating levitating = LEVITATING.get(stack);
		return levitating != null ? levitating : DEFAULT_LEVITATING.get(stack.getItem());
	}

	public static void init() {

	}

}
