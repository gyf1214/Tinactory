package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.recipe.IRecipeDataConsumer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.function.BiFunction;

public final class TestRecipeType<B extends IRecipeBuilderBase<?>> implements IRecipeType<B> {
    private final ResourceLocation loc;
    private final Class<?> recipeClass;
    private final BiFunction<IRecipeType<B>, ResourceLocation, B> builderFactory;

    public TestRecipeType(String path, Class<?> recipeClass,
        BiFunction<IRecipeType<B>, ResourceLocation, B> builderFactory) {
        this.loc = new ResourceLocation("tinactory", path);
        this.recipeClass = recipeClass;
        this.builderFactory = builderFactory;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> recipeClass() {
        return recipeClass;
    }

    @Override
    public B getBuilder(ResourceLocation loc) {
        return builderFactory.apply(this, loc);
    }

    @Override
    public B recipe(IRecipeDataConsumer consumer, ResourceLocation loc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceLocation loc() {
        return loc;
    }

    @Override
    public RecipeType<?> get() {
        throw new UnsupportedOperationException();
    }
}
