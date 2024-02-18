package org.shsts.tinactory.integration.jei.category;

import com.google.common.collect.ArrayListMultimap;
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
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingCategory<T extends ProcessingRecipe<T>> extends RecipeCategory<T> {
    protected final ArrayListMultimap<Integer, Layout.SlotInfo> portSlots;

    public ProcessingCategory(RecipeType<T> type, IJeiHelpers helpers, Layout layout, ItemLike icon) {
        super(type, helpers, layout, new ItemStack(icon));
        this.portSlots = ArrayListMultimap.create();
        var slots = layout.getStackSlots();
        for (var slot : slots) {
            this.portSlots.put(slot.port(), slot);
        }
    }

    protected <I> void addIngredient(IRecipeLayoutBuilder builder, Layout.SlotInfo slot,
                                     I ingredient, RecipeIngredientRole role) {
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

    protected <I> void addIngredient(IRecipeLayoutBuilder builder, Map<Integer, Integer> currentSlotIndex,
                                     int port, I ingredient) {
        var slotIndex = currentSlotIndex.getOrDefault(port, 0);
        var slots = this.portSlots.get(port);
        if (slotIndex < slots.size()) {
            var slot = slots.get(slotIndex);
            var role = slot.type().output ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;
            this.addIngredient(builder, slot, ingredient, role);
            currentSlotIndex.put(port, slotIndex + 1);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        Map<Integer, Integer> currentSlotIndex = new HashMap<>();
        for (var input : recipe.inputs) {
            this.addIngredient(builder, currentSlotIndex, input.port(), input.ingredient());
        }
        for (var output : recipe.outputs) {
            this.addIngredient(builder, currentSlotIndex, output.port(), output.result());
        }
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        this.drawProgressBar(stack, (int) recipe.workTicks);
    }
}
