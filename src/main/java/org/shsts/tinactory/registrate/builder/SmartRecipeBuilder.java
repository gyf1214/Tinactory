package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipeBuilder<U extends SmartRecipe<?>, S extends SmartRecipeBuilder<U, S>>
        extends Builder<U, RecipeTypeEntry<U, S>, S> {

    @FunctionalInterface
    public interface Factory<U1 extends SmartRecipe<?>, S1 extends Builder<?, ?, S1>> {
        S1 create(Registrate registrate, RecipeTypeEntry<U1, S1> parent, ResourceLocation loc);
    }

    protected SmartRecipeBuilder(Registrate registrate, RecipeTypeEntry<U, S> parent, ResourceLocation loc) {
        super(registrate, parent, loc);
        onBuild.add(() -> registrate.recipeDataHandler
                .addCallback(prov -> prov.addRecipe(buildObject().toFinished())));
    }
}
