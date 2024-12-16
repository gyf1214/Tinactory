package org.shsts.tinactory.core.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeBuilder<R extends IRecipe<?>, S extends RecipeBuilder<R, S>>
    extends Builder<R, IRecipeType<S>, S>
    implements IRecipeBuilder<R, S> {
    public final ResourceLocation loc;

    protected RecipeBuilder(IRecipeType<S> parent, ResourceLocation loc) {
        super(parent);
        this.loc = loc;
    }
}
