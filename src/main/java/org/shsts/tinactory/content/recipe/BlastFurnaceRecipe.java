package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.recipe.IRecipeDataConsumer;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceRecipe extends ProcessingRecipe {
    public final int temperature;

    protected BlastFurnaceRecipe(Builder builder) {
        super(builder);
        this.temperature = builder.temperature;
    }

    public static class Builder extends BuilderBase<BlastFurnaceRecipe, Builder> {
        private int temperature = 0;

        public Builder(IRecipeDataConsumer consumer,
                       RecipeTypeEntry<BlastFurnaceRecipe, Builder> parent,
                       ResourceLocation loc) {
            super(consumer, parent, loc);
        }

        public Builder temperature(int value) {
            this.temperature = value;
            return this;
        }

        @Override
        protected BlastFurnaceRecipe createObject() {
            return new BlastFurnaceRecipe(this);
        }
    }

    private static class Serializer extends
            ProcessingRecipe.Serializer<BlastFurnaceRecipe, BlastFurnaceRecipe.Builder> {
        private Serializer(RecipeTypeEntry<BlastFurnaceRecipe, Builder> type) {
            super(type);
        }

        @Override
        protected Builder buildFromJson(ResourceLocation loc, JsonObject jo) {
            return super.buildFromJson(loc, jo)
                    .temperature(GsonHelper.getAsInt(jo, "temperature"));
        }

        @Override
        public void toJson(JsonObject jo, BlastFurnaceRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("temperature", recipe.temperature);
        }
    }

    public static final SmartRecipeSerializer.Factory<BlastFurnaceRecipe, Builder> SERIALIZER =
            Serializer::new;
}
