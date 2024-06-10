package org.shsts.tinactory.core.recipe;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.BuilderBase;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SimpleRecipeBuilder<P, S extends SimpleRecipeBuilder<P, S>>
        extends BuilderBase<FinishedRecipe, P, S> {
    public final ResourceLocation loc;

    public SimpleRecipeBuilder(IRecipeDataConsumer consumer, P parent, ResourceLocation loc) {
        super(parent);
        this.loc = loc;
        onBuild.add(() -> consumer.addRecipe(loc, this::buildObject));
    }
}
