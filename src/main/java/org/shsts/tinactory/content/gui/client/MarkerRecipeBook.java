package org.shsts.tinactory.content.gui.client;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MarkerRecipeBook extends ProcessingRecipeBook {
    public MarkerRecipeBook(MenuScreen<? extends Menu<?, ?>> screen,
                            RecipeType<? extends ProcessingRecipe> recipeType, Layout layout) {
        super(screen, recipeType, layout);
    }

    @Override
    protected void doRefreshRecipes() {
        var be = screen.getMenu().blockEntity;
        var voltage = (long) AllCapabilities.ELECTRIC_MACHINE.tryGet(be)
                .map(IElectricMachine::getVoltage)
                .orElse(0L);
        for (var recipe : ClientUtil.getRecipeManager().getAllRecipesFor(AllRecipes.MARKER.get())) {
            if (recipe.baseType != recipeType || !recipe.canCraftInVoltage(voltage)) {
                continue;
            }
            recipes.put(recipe.getId(), recipe);
        }
    }
}
