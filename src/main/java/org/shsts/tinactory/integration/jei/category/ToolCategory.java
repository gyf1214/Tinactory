package org.shsts.tinactory.integration.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
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
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ToolCategory extends RecipeCategory<ToolRecipe, WorkbenchMenu> {
    private ToolCategory(Block workbench) {
        super(AllRecipes.TOOL_CRAFTING, AllLayouts.WORKBENCH, Ingredient.of(workbench),
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
            var items = Arrays.asList(toolIngredient.getItems());
            builder.addIngredients(slots.get(1 + k), RecipeIngredientRole.CATALYST,
                    VanillaTypes.ITEM_STACK, items);
            if (++k >= 9) {
                break;
            }
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 3; j++) {
                var slot = slots.get(10 + i * 3 + j);
                if (i < shaped.getHeight() && j < shaped.getWidth()) {
                    var ingredient = recipe.shapedRecipe.getIngredients().get(i * shaped.getWidth() + j);
                    builder.ingredient(slot, ingredient);
                } else {
                    builder.items(slot, List.of());
                }
            }
        }
    }
}
