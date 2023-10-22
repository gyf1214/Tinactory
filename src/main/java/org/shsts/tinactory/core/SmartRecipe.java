package org.shsts.tinactory.core;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.registrate.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipe<C extends Container, T extends SmartRecipe<C, T>>
        implements Recipe<C>, ISelf<T> {

    @FunctionalInterface
    public interface Factory<T extends SmartRecipe<?, T>> {
        T create(RecipeTypeEntry<T, ?> type, ResourceLocation loc);
    }

    protected final ResourceLocation loc;
    protected final RecipeType<? super T> type;
    protected final SmartRecipeSerializer<T, ?> serializer;

    protected SmartRecipe(RecipeTypeEntry<T, ?> type, ResourceLocation loc) {
        this.loc = loc;
        this.type = type.get();
        this.serializer = (SmartRecipeSerializer<T, ?>) type.getSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public ResourceLocation getId() {
        return this.loc;
    }

    @Override
    public abstract boolean matches(C container, Level world);

    @Override
    public abstract ItemStack assemble(C container);

    @Override
    public abstract boolean canCraftInDimensions(int width, int height);

    @Override
    public NonNullList<ItemStack> getRemainingItems(C container) {
        return Recipe.super.getRemainingItems(container);
    }

    private class SimpleFinished implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject jo) {
            serializer.toJson(jo, SmartRecipe.this.self());
        }

        @Override
        public ResourceLocation getId() {
            return loc;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return serializer;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

    public FinishedRecipe toFinished() {
        return new SimpleFinished();
    }
}
