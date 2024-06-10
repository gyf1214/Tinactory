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
    public MarkerRecipeBook(MenuScreen<? extends ProcessingMenu> screen,
                            RecipeType<? extends ProcessingRecipe> recipeType) {
        super(screen, recipeType);
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
    }
}
