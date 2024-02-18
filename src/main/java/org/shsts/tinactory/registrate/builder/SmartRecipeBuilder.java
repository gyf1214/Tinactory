package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipeBuilder<U extends SmartRecipe<?, U>, S extends SmartRecipeBuilder<U, S>>
        extends Builder<U, RecipeTypeEntry<U, S>, S> {

    @FunctionalInterface
    public interface Factory<U1 extends Recipe<?>, S1> {
        S1 create(Registrate registrate, RecipeTypeEntry<U1, S1> parent, ResourceLocation loc);
    }

    protected SmartRecipeBuilder(Registrate registrate, RecipeTypeEntry<U, S> parent, ResourceLocation loc) {
        super(registrate, parent, loc);
    }

    @Override
    public RecipeTypeEntry<U, S> build() {
        this.registrate.recipeDataHandler.addCallback(prov -> prov.addRecipe(this.buildObject().toFinished()));
        return this.parent;
    }
}
