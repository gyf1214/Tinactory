package org.shsts.tinactory.compat.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static org.shsts.tinactory.core.util.ClientUtil.PERCENTAGE_FORMAT;
import static org.shsts.tinactory.core.util.I18n.tr;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class IngredientRenderers {
    public static <V> IIngredientRenderer<V> empty() {
        return new EmptyRenderer<>();
    }

    public static final IIngredientRenderer<FluidStack> FLUID = new FluidIngredientRenderer();

    public static IIngredientRenderer<FluidStack> ratedFluid(double rate) {
        return new FluidIngredientRenderer() {
            @Override
            public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
                return addRate(super.getTooltip(ingredient, tooltipFlag), rate);
            }
        };
    }

    public static IIngredientRenderer<ItemStack> ratedItem(double rate) {
        return new ItemIngredientRenderer() {
            @Override
            public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
                return addRate(super.getTooltip(ingredient, tooltipFlag), rate);
            }
        };
    }

    public static final IIngredientRenderer<ItemStack> ITEM_NOT_CONSUMED = new ItemIngredientRenderer() {
        @Override
        public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
            return addNotConsumed(super.getTooltip(ingredient, tooltipFlag));
        }
    };

    public static IIngredientRenderer<FluidStack> FLUID_NOT_CONSUMED = new FluidIngredientRenderer() {
        @Override
        public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
            return addNotConsumed(super.getTooltip(ingredient, tooltipFlag));
        }
    };

    private static List<Component> addRate(List<Component> lines, double rate) {
        lines.add(tr("tinactory.jei.rate", PERCENTAGE_FORMAT.format(rate)));
        return lines;
    }

    private static List<Component> addNotConsumed(List<Component> lines) {
        lines.add(tr("tinactory.jei.not_consumed"));
        return lines;
    }
}
