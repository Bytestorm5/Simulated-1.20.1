package dev.eriksonn.aeronautics.neoforge.data.recipe;

import com.simibubi.create.api.data.recipe.BaseRecipeProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AeroProcessingRecipeGen {
	protected static List<BaseRecipeProvider> GENERATORS = new ArrayList<>();

	public static DataProvider registerAll(PackOutput output) {
		GENERATORS.add(new AeroMixingRecipes(output));
		GENERATORS.add(new AeroCrushingRecipes(output));
		GENERATORS.add(new AeroMechanicalCraftingRecipes(output));
		GENERATORS.add(new AeroWashingRecipes(output));
		GENERATORS.add(new AeroDeployingRecipes(output));

		return new DataProvider() {
			@Override
			public CompletableFuture<?> run(CachedOutput cachedOutput) {
				return CompletableFuture.allOf(GENERATORS.stream().map(gen -> gen.run(cachedOutput)).toArray(CompletableFuture[]::new));
			}

			@Override
			public String getName() {
				return "Aero's Perfect Processing Recipes";
			}
		};
	}
}
