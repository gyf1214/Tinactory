package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SimpleRecipeBuilder<P, S extends SimpleRecipeBuilder<P, S>>
        extends Builder<FinishedRecipe, P, S> {

    public SimpleRecipeBuilder(Registrate registrate, P parent, ResourceLocation loc) {
        super(registrate, parent, loc);
        onBuild.add(() -> {
            registrate.recipeDataHandler.addCallback(prov -> prov.addRecipe(buildObject()));
            registrate.languageHandler.track(SmartRecipe.getDescriptionId(loc));
        });
    }
}
