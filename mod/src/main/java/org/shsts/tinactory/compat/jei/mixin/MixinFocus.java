package org.shsts.tinactory.compat.jei.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.focus.Focus;
import mezz.jei.common.ingredients.RegisteredIngredients;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The main purpose of this is to redirect itemStack ingredient to fluidStack ones when the item contains fluid.
 * In AbstractContainerScreen this is handled by FluidScreenHandler. However, when some mods call JEI APIs directly,
 * we also want to do that.
 */
@Mixin(Focus.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MixinFocus {
    @Inject(method = "createFromApi", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectCreateFromApi(RegisteredIngredients registeredIngredients,
        RecipeIngredientRole role, IIngredientType<?> ingredientType, Object value,
        CallbackInfoReturnable<Focus<?>> ci) {
        if (ingredientType == VanillaTypes.ITEM_STACK && value instanceof ItemStack item) {
            var fluid = StackHelper.getFluidHandlerFromItem(item)
                .map(h -> h.getFluidInTank(0))
                .orElse(FluidStack.EMPTY);
            if (!fluid.isEmpty()) {
                ci.setReturnValue(Focus.createFromApi(registeredIngredients, role,
                    ForgeTypes.FLUID_STACK, fluid));
            }
        }
    }
}
