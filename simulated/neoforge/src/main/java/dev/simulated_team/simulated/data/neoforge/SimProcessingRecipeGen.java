package dev.simulated_team.simulated.data.neoforge;

import com.simibubi.create.api.data.recipe.BaseRecipeProvider;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SimProcessingRecipeGen extends BaseRecipeProvider {
    protected static final List<BaseRecipeProvider> GENERATORS = new ArrayList<>();
    public static DataProvider registerAll(final PackOutput output) {
        GENERATORS.add(new SimFillingRecipes(output));
        GENERATORS.add(new SimMechanicalCraftingRecipes(output));
        GENERATORS.add(new SimSequencedAssemblyRecipes(output));
        GENERATORS.add(new SimStandardRecipeGen(output));
        return new DataProvider() {
            @Override
            public CompletableFuture<?> run(final CachedOutput arg) {
                return CompletableFuture.allOf(GENERATORS.stream()
                        .map(gen -> gen.run(arg))
                        .toArray(CompletableFuture[]::new));
            }

            @Override
            public String getName() {
                return "Simulated's Peculiar Processing Recipes";
            }
        };
    }
    public SimProcessingRecipeGen(final PackOutput output) {
        super(output, Simulated.MOD_ID);
    }
}
