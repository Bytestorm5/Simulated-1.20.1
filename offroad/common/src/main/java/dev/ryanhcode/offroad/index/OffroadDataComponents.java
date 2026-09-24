package dev.ryanhcode.offroad.index;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.simulated_team.simulated.backport.DataComponentType;

import java.util.function.UnaryOperator;

public class OffroadDataComponents {

	public static final DataComponentType<TireLike> TIRE = create("tire",
			builder -> builder.persistent(TireLike.CODEC));


	private static <T> DataComponentType<T> create(final String name, final UnaryOperator<DataComponentType.Builder<T>> builder) {
		final DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		return type.bind(Offroad.path(name));
	}

	public static void init() {
		// no-op
	}
}
