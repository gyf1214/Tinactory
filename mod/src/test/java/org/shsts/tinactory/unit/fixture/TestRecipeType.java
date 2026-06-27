package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

public final class TestRecipeType<R extends IRecipe<?>> implements IRecipeType<R> {
    private final ResourceLocation loc;
    private final Class<R> recipeClass;

    public TestRecipeType(String path, Class<R> recipeClass) {
        this.loc = new ResourceLocation("tinactory", path);
        this.recipeClass = recipeClass;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<R> recipeClass() {
        return recipeClass;
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
