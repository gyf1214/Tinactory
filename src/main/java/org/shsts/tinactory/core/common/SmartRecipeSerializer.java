package org.shsts.tinactory.core.common;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipeSerializer<T extends SmartRecipe<?>, B extends BuilderBase<?, ?, B>>
    extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {
    protected final RecipeTypeEntry<T, B> type;

    @FunctionalInterface
    public interface Factory<T1 extends SmartRecipe<?>, B1 extends BuilderBase<?, ?, B1>> {
        SmartRecipeSerializer<T1, B1> create(RecipeTypeEntry<T1, B1> type);
    }

    protected SmartRecipeSerializer(RecipeTypeEntry<T, B> type) {
        this.type = type;
    }

    public RecipeTypeEntry<T, B> getType() {
        return type;
    }

    @Override
    public abstract T fromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context);

    public abstract void toJson(JsonObject jo, T recipe);

    public void recipeToJson(JsonObject jo, SmartRecipe<?> recipe) {
        toJson(jo, type.clazz.cast(recipe));
    }

    @Override
    public T fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
        var jo = CodecHelper.jsonFromStr(buf.readUtf());
        return fromJson(loc, jo);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, T recipe) {
        var jo = new JsonObject();
        toJson(jo, recipe);
        buf.writeUtf(CodecHelper.jsonToStr(jo));
    }

    @Override
    public T fromJson(ResourceLocation loc, JsonObject jo) {
        return fromJson(loc, jo, ICondition.IContext.EMPTY);
    }
}
