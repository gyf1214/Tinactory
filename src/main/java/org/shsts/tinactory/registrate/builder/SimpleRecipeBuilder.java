package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.registrate.Registrate;

@MethodsReturnNonnullByDefault
public abstract class SimpleRecipeBuilder<P, S extends SimpleRecipeBuilder<P, S>>
        extends Builder<FinishedRecipe, P, S> {

    public SimpleRecipeBuilder(Registrate registrate, P parent, ResourceLocation loc) {
        super(registrate, parent, loc);
    }

    @Override
    public P build() {
        this.registrate.recipeDataHandler.addCallback(prov -> prov.addRecipe(this.buildObject()));
        return this.parent;
    }
}
