package org.shsts.tinactory.integration.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllLayouts;
import org.shsts.tinactory.AllRecipes;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.recipe.ToolRecipe;

import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ToolCategory extends RecipeCategory<ToolRecipe> {
    private ToolCategory(Block workbench) {
        super(AllRecipes.TOOL_CRAFTING, AllLayouts.WORKBENCH, Ingredient.of(workbench),
            new ItemStack(workbench));
    }

    public ToolCategory() {
        this(AllBlockEntities.WORKBENCH.get());
    }

    @Override
    protected void setRecipe(ToolRecipe recipe, IIngredientBuilder builder) {
        var shaped = recipe.shapedRecipe;
        var slots = layout.slots;

        builder.itemOutput(slots.get(0).setType(SlotType.ITEM_OUTPUT), shaped.getResultItem());
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 3; j++) {
                var slot = slots.get(10 + i * 3 + j);
                if (i < shaped.getHeight() && j < shaped.getWidth()) {
                    var ingredient = recipe.shapedRecipe.getIngredients().get(i * shaped.getWidth() + j);
                    builder.ingredientInput(slot, ingredient);
                } else {
                    builder.itemInput(slot, Collections.emptyList());
                }
            }
        }
    }

    @Override
    protected void extraLayout(ToolRecipe recipe, IRecipeLayoutBuilder builder) {
        var k = 0;
        for (var toolIngredient : recipe.toolIngredients) {
            var slot = layout.slots.get(1 + k);
            var x = slot.x() + 1 + xOffset;
            var y = slot.y() + 1;

            builder.addSlot(RecipeIngredientRole.CATALYST, x, y)
                .addIngredients(VanillaTypes.ITEM_STACK, List.of(toolIngredient.getItems()));

            if (++k >= 9) {
                break;
            }
        }
    }
}
