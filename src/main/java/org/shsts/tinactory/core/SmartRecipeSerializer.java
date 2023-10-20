package org.shsts.tinactory.core;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.shsts.tinactory.registrate.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipeSerializer<T extends Recipe<?>> extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<T> {

    @FunctionalInterface
    public interface Factory<T extends Recipe<?>, S extends SmartRecipeSerializer<T>> {
        S create(RecipeTypeEntry<T> type);
    }

    protected final RecipeTypeEntry<T> type;

    protected SmartRecipeSerializer(RecipeTypeEntry<T> type) {
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
