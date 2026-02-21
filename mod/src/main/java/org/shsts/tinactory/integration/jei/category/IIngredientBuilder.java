package org.shsts.tinactory.integration.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.Layout;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IIngredientBuilder {
    void itemInput(Layout.SlotInfo slot, List<ItemStack> item);

    default void itemInput(Layout.SlotInfo slot, ItemStack item) {
        itemInput(slot, List.of(item));
    }

    default void ingredientInput(Layout.SlotInfo slot, Ingredient ingredient) {
        itemInput(slot, List.of(ingredient.getItems()));
    }

    void itemNotConsumedInput(Layout.SlotInfo slot, List<ItemStack> item);

    void fluidInput(Layout.SlotInfo slot, FluidStack fluid);

    void itemOutput(Layout.SlotInfo slot, ItemStack item);

    void ratedItemOutput(Layout.SlotInfo slot, ItemStack item, double rate);

    default void itemOutput(Layout.SlotInfo slot, ItemStack item, double rate) {
        if (rate >= 1d) {
            itemOutput(slot, item);
        } else {
            ratedItemOutput(slot, item, rate);
        }
    }

    void fluidOutput(Layout.SlotInfo slot, FluidStack fluid);

    void ratedFluidOutput(Layout.SlotInfo slot, FluidStack fluid, double rate);

    default void fluidOutput(Layout.SlotInfo slot, FluidStack fluid, double rate) {
        if (rate >= 1d) {
            fluidOutput(slot, fluid);
        } else {
            ratedFluidOutput(slot, fluid, rate);
        }
    }
}
