package org.shsts.tinactory.core.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.BuilderBase;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SimpleRecipeBuilder<P, S extends SimpleRecipeBuilder<P, S>>
    extends BuilderBase<FinishedRecipe, P, S> {
    public final ResourceLocation loc;

    public SimpleRecipeBuilder(IRecipeDataConsumer consumer, P parent, ResourceLocation loc) {
        super(parent);
        this.loc = loc;
        onBuild.add(() -> consumer.registerRecipe(loc, this::buildObject));
    }
}
