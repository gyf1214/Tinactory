package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MarkerRecipeBook extends MachineRecipeBook {
    private final boolean includeNormal;

    public MarkerRecipeBook(ProcessingScreen screen, Layout layout, boolean includeNormal) {
        super(screen, layout);
        this.includeNormal = includeNormal;
    }

    @Override
    protected void doRefreshRecipes() {
        for (var recipe : ClientUtil.getRecipeManager().getAllRecipesFor(AllRecipes.MARKER.get())) {
            if (recipe.baseType != recipeType || !canCraft(recipe)) {
                continue;
            }
            recipes.put(recipe.getId(), recipe);
        }
        if (includeNormal) {
            super.doRefreshRecipes();
        }
    }

    @Override
    protected int compareRecipes(ProcessingRecipe r1, ProcessingRecipe r2) {
        var marker1 = r1.getType() == AllRecipes.MARKER.get();
        var marker2 = r2.getType() == AllRecipes.MARKER.get();
        if (marker1 != marker2) {
            return marker1 ? -1 : 1;
        }
        return super.compareRecipes(r1, r2);
    }
}
