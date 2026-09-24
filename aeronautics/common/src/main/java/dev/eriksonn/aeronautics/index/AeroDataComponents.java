package dev.eriksonn.aeronautics.index;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.components.Converter;
import dev.eriksonn.aeronautics.content.components.Levitating;
import dev.simulated_team.simulated.backport.DataComponentType;

import java.util.function.UnaryOperator;

public class AeroDataComponents {

	public static final DataComponentType<Levitating> LEVITATING = create("levitating",
			builder -> builder.persistent(Levitating.CODEC));

	public static final DataComponentType<Converter> CONVERTER = create("converter",
			builder -> builder.persistent(Converter.CODEC));

	private static <T> DataComponentType<T> create(final String name, final UnaryOperator<DataComponentType.Builder<T>> builder) {
		final DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		return type.bind(Aeronautics.path(name));
	}

	public static void init() {

	}

}
