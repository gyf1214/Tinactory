package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class RecipeBuilder<U extends SmartRecipe<?, ?>, P, S extends RecipeBuilder<U, P, S>>
        extends Builder<U, P, S> {
    protected RecipeBuilder(Registrate registrate, P parent, String id) {
        super(registrate, parent, id);
    }

    @Override
    public P build() {
        var object = this.buildObject();
        this.registrate.recipeDataHandler.addCallback(prov -> prov.addRecipe(object));
        return this.parent;
    }
}
