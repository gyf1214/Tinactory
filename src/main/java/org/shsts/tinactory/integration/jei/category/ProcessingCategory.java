package org.shsts.tinactory.integration.jei.category;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.gui.layout.Layout;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingCategory<T extends ProcessingRecipe<T>> extends RecipeCategory<T> {
    protected final ArrayListMultimap<Integer, Layout.SlotInfo> slotsMap;

    public ProcessingCategory(RecipeType<T> type, IJeiHelpers helpers, Layout layout, ItemLike icon) {
        super(type, helpers, layout, new ItemStack(icon));
        this.slotsMap = ArrayListMultimap.create();
        for (var slot : layout.slots) {
            this.slotsMap.put(slot.port(), slot);
        }
    }

    protected void addIngredient(IRecipeLayoutBuilder builder, Map<Integer, Integer> currentSlotIndex,
                                 int port, Either<Ingredient, FluidStack> ingredient) {
        var slotIndex = currentSlotIndex.getOrDefault(port, 0);
        var slots = this.slotsMap.get(port);
        if (slotIndex < slots.size()) {
            var slot = slots.get(slotIndex);
            var role = slot.type().output ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;
            this.addIngredient(builder, slot.index(), ingredient, role);
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
            this.addIngredient(builder, currentSlotIndex, output.port(), output.result().mapLeft(Ingredient::of));
        }
    }
}
