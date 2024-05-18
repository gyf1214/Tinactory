package org.shsts.tinactory.core.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipeSerializer<T extends SmartRecipe<?, T>, B>
        extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {

    protected final Gson gson = new Gson();

    protected final RecipeTypeEntry<T, B> type;

    @FunctionalInterface
    public interface Factory<T1 extends SmartRecipe<?, T1>, B1> {
        SmartRecipeSerializer<T1, B1> create(RecipeTypeEntry<T1, B1> type);
    }

    protected SmartRecipeSerializer(RecipeTypeEntry<T, B> type) {
        this.type = type;
    }

    @Override
    public abstract T fromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context);

    public abstract void toJson(JsonObject jo, T recipe);

    @Override
    public T fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
        var jo = gson.fromJson(buf.readUtf(), JsonObject.class);
        return fromJson(loc, jo);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, T recipe) {
        var jo = new JsonObject();
        toJson(jo, recipe);
        buf.writeUtf(gson.toJson(jo));
    }

    @Override
    public T fromJson(ResourceLocation loc, JsonObject jo) {
        return fromJson(loc, jo, ICondition.IContext.EMPTY);
    }
}
