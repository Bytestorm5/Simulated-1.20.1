package dev.simulated_team.simulated.data.neoforge;

import com.simibubi.create.AllTags;
import com.simibubi.create.api.data.recipe.FillingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimItems;
import net.minecraft.data.PackOutput;


public class SimFillingRecipes extends FillingRecipeGen {
    private final GeneratedRecipe HONEY_GLUE = this.create("honey_glue",
            b -> b.require(AllTags.AllFluidTags.HONEY.tag, 500)
                  .require(CommonMetal.IRON.plates)
                  .output(SimItems.HONEY_GLUE));

    public SimFillingRecipes(final PackOutput output) {
        super(output, Simulated.MOD_ID);
    }

    @Override
    public String getName() {
        return "Simulated's Fantastic Filling Recipes";
    }

}
