package org.shsts.tinactory.integration.jei.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.common.ingredients.fluid.FluidIngredientHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.common.CellItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.shsts.tinactory.AllRegistries.ITEMS;

@Mixin(FluidIngredientHelper.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MixinFluidIngredientHelper {
    private static final String CHEAT_FLUID_CELL = "tool/fluid_cell/iron";

    @Shadow(remap = false)
    private IIngredientTypeWithSubtypes<Fluid, Object> fluidType;

    @Inject(method = "getCheatItemStack", at = @At("RETURN"), remap = false, cancellable = true)
    private void injectGetCheatItemStack(Object ingredient, CallbackInfoReturnable<ItemStack> ci) {
        if (ci.getReturnValue().isEmpty()) {
            var fluid = fluidType.getBase(ingredient);
            var cell = ITEMS.<CellItem>getEntry(CHEAT_FLUID_CELL).get();
            var fluidStack = new FluidStack(fluid, cell.capacity);
            ci.setReturnValue(cell.create(fluidStack));
        }
    }
}
