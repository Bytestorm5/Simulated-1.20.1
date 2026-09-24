package dev.simulated_team.simulated.data.neoforge;

import com.simibubi.create.api.data.recipe.BaseRecipeProvider;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.neoforge.SimNeoForgeRecipeTypes;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public class SimStandardRecipeGen extends BaseRecipeProvider {

    GeneratedRecipe PORTABLE_ENGINE_DYEING = this.createSpecial(SimNeoForgeRecipeTypes.PORTABLE_ENGINE_DYEING::getSerializer, "crafting", "portable_engine_dyeing");

    public SimStandardRecipeGen(final PackOutput output) {
        super(output, Simulated.MOD_ID);
    }

    @Override
    public String getName() {
        return "Simulated's Surprisingly Standard Recipes";
    }

    // 1.20.1: special recipes are built from their serializer instead of a recipe factory
    private GeneratedRecipe createSpecial(final Supplier<RecipeSerializer<? extends CraftingRecipe>> serializer, final String recipeType, final String path) {
        final ResourceLocation location = Simulated.path(recipeType + "/" + path);

        return this.register(consumer -> {
            final SpecialRecipeBuilder b = SpecialRecipeBuilder.special(serializer.get());
            b.save(consumer, location.toString());
        });
    }
}
