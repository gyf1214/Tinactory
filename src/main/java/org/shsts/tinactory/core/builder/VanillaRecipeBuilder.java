package org.shsts.tinactory.core.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IVanillaRecipeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class VanillaRecipeBuilder<R extends IRecipe<?>, S extends VanillaRecipeBuilder<R, S>>
    extends Builder<FinishedRecipe, IRecipeType<S>, S>
    implements IVanillaRecipeBuilder<R, S> {
    public final ResourceLocation loc;

    protected VanillaRecipeBuilder(IRecipeType<S> parent, ResourceLocation loc) {
        super(parent);
        this.loc = loc;
    }
}
