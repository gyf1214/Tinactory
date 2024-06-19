package org.shsts.tinactory.integration.jei.ingredient;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidStackType implements IIngredientTypeWithSubtypes<Fluid, FluidStackWrapper> {
    private FluidStackType() {}

    @Override
    public Class<? extends FluidStackWrapper> getIngredientClass() {
        return FluidStackWrapper.class;
    }

    @Override
    public Class<? extends Fluid> getIngredientBaseClass() {
        return Fluid.class;
    }

    @Override
    public Fluid getBase(FluidStackWrapper ingredient) {
        return ingredient.stack().getFluid();
    }

    public static FluidStackType INSTANCE = new FluidStackType();
}
