package org.shsts.tinactory.content.gui.client;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MarkerRecipeBook extends MachineRecipeBook {
    private final boolean includeNormal;

    public MarkerRecipeBook(MenuScreen<? extends ProcessingMenu> screen,
                            RecipeType<? extends ProcessingRecipe> recipeType,
                            boolean includeNormal) {
        super(screen, recipeType);
        this.includeNormal = includeNormal;
    }

    @Override
    protected void doRefreshRecipes() {
        var voltage = getMachineVoltage();
        for (var recipe : ClientUtil.getRecipeManager().getAllRecipesFor(AllRecipes.MARKER.get())) {
            if (recipe.baseType != recipeType || !recipe.canCraftInVoltage(voltage)) {
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
