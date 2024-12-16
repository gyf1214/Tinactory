package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllRecipes.MARKER;

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
        var machine = MACHINE.get(blockEntity);
        for (var recipe : CORE.clientRecipeManager().getAllRecipesFor(MARKER)) {
            if (recipe.baseType == recipeType && recipe.canCraft(machine)) {
                recipes.put(recipe.loc(), recipe);
            }
        }
        if (includeNormal) {
            super.doRefreshRecipes();
        }
    }

    @Override
    protected int compareRecipes(ProcessingRecipe r1, ProcessingRecipe r2) {
        var marker1 = r1 instanceof MarkerRecipe;
        var marker2 = r2 instanceof MarkerRecipe;
        if (marker1 != marker2) {
            return marker1 ? -1 : 1;
        }
        return super.compareRecipes(r1, r2);
    }
}
