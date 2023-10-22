package org.shsts.tinactory.core;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.builder.RecipeBuilder;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipeSerializer<T extends SmartRecipe<?, T>, B extends RecipeBuilder<T, B>>
        extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {

    @FunctionalInterface
    public interface Factory<T1 extends SmartRecipe<?, T1>, B1 extends RecipeBuilder<T1, B1>,
            S extends SmartRecipeSerializer<T1, B1>> {
        S create(RecipeTypeEntry<T1, B1> type);
    }

    protected final RecipeTypeEntry<T, B> type;

    protected SmartRecipeSerializer(RecipeTypeEntry<T, B> type) {
        this.type = type;
    }

    public abstract void toJson(JsonObject jo, T recipe);

    @Override
    public T fromJson(ResourceLocation loc, JsonObject jo) {
        return this.fromJson(loc, jo, ICondition.IContext.EMPTY);
    }

    @Override
    public abstract T fromNetwork(ResourceLocation loc, FriendlyByteBuf buf);

    @Override
    public abstract void toNetwork(FriendlyByteBuf buf, T recipe);

    @Override
    public abstract T fromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context);
}
