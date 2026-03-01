package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.compat.jei.ingredient.IngredientRenderers;
import org.shsts.tinactory.core.gui.Layout;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayoutIngredientBuilder implements IIngredientBuilder {
    private final int xOffset;
    private final IRecipeLayoutBuilder builder;

    public LayoutIngredientBuilder(IRecipeLayoutBuilder builder, int xOffset) {
        this.xOffset = xOffset;
        this.builder = builder;
    }

    private IRecipeSlotBuilder addSlot(Layout.SlotInfo slot, RecipeIngredientRole role) {
        var x = slot.x() + 1 + xOffset;
        var y = slot.y() + 1;
        return builder.addSlot(role, x, y);
    }

    @Override
    public void itemInput(Layout.SlotInfo slot, List<ItemStack> item) {
        addSlot(slot, RecipeIngredientRole.INPUT)
            .addIngredients(VanillaTypes.ITEM_STACK, item);
    }

    @Override
    public void itemNotConsumedInput(Layout.SlotInfo slot, List<ItemStack> item) {
        addSlot(slot, RecipeIngredientRole.INPUT)
            .addIngredients(VanillaTypes.ITEM_STACK, item)
            .setCustomRenderer(VanillaTypes.ITEM_STACK, IngredientRenderers.ITEM_NOT_CONSUMED);
    }

    @Override
    public void fluidInput(Layout.SlotInfo slot, FluidStack fluid) {
        addSlot(slot, RecipeIngredientRole.INPUT)
            .addIngredient(ForgeTypes.FLUID_STACK, fluid)
            .setCustomRenderer(ForgeTypes.FLUID_STACK, IngredientRenderers.FLUID);
    }

    @Override
    public void itemOutput(Layout.SlotInfo slot, ItemStack item) {
        addSlot(slot, RecipeIngredientRole.OUTPUT)
            .addIngredient(VanillaTypes.ITEM_STACK, item);
    }

    @Override
    public void ratedItemOutput(Layout.SlotInfo slot, ItemStack item, double rate) {
        addSlot(slot, RecipeIngredientRole.OUTPUT)
            .addIngredient(VanillaTypes.ITEM_STACK, item)
            .setCustomRenderer(VanillaTypes.ITEM_STACK, IngredientRenderers.ratedItem(rate));
    }

    @Override
    public void fluidOutput(Layout.SlotInfo slot, FluidStack fluid) {
        addSlot(slot, RecipeIngredientRole.OUTPUT)
            .addIngredient(ForgeTypes.FLUID_STACK, fluid)
            .setCustomRenderer(ForgeTypes.FLUID_STACK, IngredientRenderers.FLUID);
    }

    @Override
    public void ratedFluidOutput(Layout.SlotInfo slot, FluidStack fluid, double rate) {
        addSlot(slot, RecipeIngredientRole.OUTPUT)
            .addIngredient(ForgeTypes.FLUID_STACK, fluid)
            .setCustomRenderer(ForgeTypes.FLUID_STACK, IngredientRenderers.ratedFluid(rate));
    }
}
