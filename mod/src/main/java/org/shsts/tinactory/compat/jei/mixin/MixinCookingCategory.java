package org.shsts.tinactory.compat.jei.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.plugins.vanilla.cooking.AbstractCookingCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.shsts.tinactory.compat.jei.ingredient.RecipeMarker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCookingCategory.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MixinCookingCategory {
    @Inject(method = "setRecipe", at = @At("RETURN"), remap = false)
    private void injectSetRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AbstractCookingRecipe> recipe,
        IFocusGroup focuses, CallbackInfo ci) {
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
            .addIngredient(RecipeMarker.TYPE, new RecipeMarker(recipe.id()));
    }
}
