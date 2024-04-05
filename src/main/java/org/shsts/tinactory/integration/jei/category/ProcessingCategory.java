package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ProcessingResults;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingCategory<T extends ProcessingRecipe<T>> extends RecipeCategory<T> {
    public ProcessingCategory(RecipeType<T> type, IJeiHelpers helpers, Layout layout, ItemLike icon) {
        super(type, helpers, layout, new ItemStack(icon));
    }

    private <I> void addIngredient(IRecipeLayoutBuilder builder, Layout.SlotInfo slot, I ingredient) {
        var role = slot.type().output ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;
        var slotBuilder = builder.addSlot(role, slot.x() + 1, slot.y() + 1);
        if (ingredient instanceof ProcessingIngredients.SimpleItemIngredient simpleItemIngredient) {
            slotBuilder.addItemStack(simpleItemIngredient.stack());
        } else if (ingredient instanceof ProcessingIngredients.ItemIngredient itemIngredient) {
            slotBuilder.addIngredients(itemIngredient.ingredient());
        } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluidIngredient) {
            slotBuilder.addIngredient(ForgeTypes.FLUID_STACK, fluidIngredient.fluid());
        } else if (ingredient instanceof ProcessingResults.ItemResult itemResult) {
            slotBuilder.addItemStack(itemResult.stack);
        } else if (ingredient instanceof ProcessingResults.FluidResult fluidResult) {
            slotBuilder.addIngredient(ForgeTypes.FLUID_STACK, fluidResult.stack);
        } else {
            throw new IllegalArgumentException("Unknown processing ingredient type %s"
                    .formatted(ingredient.getClass()));
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        var inputs = layout.getProcessingInputs(recipe);
        var outputs = layout.getProcessingOutputs(recipe);

        for (var input : inputs) {
            addIngredient(builder, input.slot(), input.val());
        }
        for (var output : outputs) {
            addIngredient(builder, output.slot(), output.val());
        }
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        drawProgressBar(stack, (int) recipe.workTicks);
    }
}
