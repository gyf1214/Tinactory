package org.shsts.tinactory.integration.jei.category;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.core.recipe.ToolRecipe;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ToolCategory extends RecipeCategory<ToolRecipe, WorkbenchMenu> {
    private ToolCategory(Block workbench) {
        super(AllRecipes.TOOL, AllLayouts.WORKBENCH, Ingredient.of(workbench),
                new ItemStack(workbench), WorkbenchMenu.class);
    }

    public ToolCategory() {
        this(AllBlockEntities.WORKBENCH.block());
    }

    @Override
    protected void addRecipe(ToolRecipe recipe, IIngredientBuilder builder) {
        var shaped = recipe.shapedRecipe;
        var slots = layout.slots;

        builder.item(slots.get(0).setType(SlotType.ITEM_OUTPUT), shaped.getResultItem());

        var k = 0;
        for (var toolIngredient : recipe.toolIngredients) {
            builder.ingredient(slots.get(1 + k), toolIngredient);
            if (++k >= 9) {
                break;
            }
        }
        for (var i = 0; i < shaped.getHeight(); i++) {
            for (var j = 0; j < shaped.getWidth(); j++) {
                var ingredient = recipe.shapedRecipe.getIngredients().get(i * shaped.getWidth() + j);
                builder.ingredient(slots.get(10 + i * 3 + j), ingredient);
            }
        }
    }
}
