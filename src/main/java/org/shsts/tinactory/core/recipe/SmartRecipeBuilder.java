package org.shsts.tinactory.core.recipe;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.BuilderBase;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipeBuilder<U extends SmartRecipe<?>, S extends SmartRecipeBuilder<U, S>>
        extends BuilderBase<U, RecipeTypeEntry<U, S>, S> {
    public final ResourceLocation loc;

    @FunctionalInterface
    public interface Factory<U1 extends SmartRecipe<?>, S1 extends BuilderBase<?, ?, S1>> {
        S1 create(IRecipeDataConsumer consumer, RecipeTypeEntry<U1, S1> parent, ResourceLocation loc);
    }

    protected SmartRecipeBuilder(IRecipeDataConsumer consumer, RecipeTypeEntry<U, S> parent, ResourceLocation loc) {
        super(parent);
        this.loc = loc;
        onBuild.add(() -> consumer.registerRecipe(loc, () -> buildObject().toFinished()));
    }
}
