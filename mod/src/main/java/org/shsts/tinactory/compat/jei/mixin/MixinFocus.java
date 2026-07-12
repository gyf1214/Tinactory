package org.shsts.tinactory.compat.jei.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.focus.Focus;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
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
    private static void injectCreateFromApi(IIngredientManager manager,
        RecipeIngredientRole role, IIngredientType<?> ingredientType, Object value,
        CallbackInfoReturnable<Focus<?>> ci) {
        if (ingredientType == VanillaTypes.ITEM_STACK && value instanceof ItemStack item) {
            var fluid = StackHelper.getFluidFromItem(item);
            if (!fluid.isEmpty()) {
                ci.setReturnValue(Focus.createFromApi(manager, role,
                    NeoForgeTypes.FLUID_STACK, fluid));
            }
        }
    }
}
