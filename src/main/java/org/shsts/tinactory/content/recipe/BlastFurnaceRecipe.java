package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceRecipe extends ProcessingRecipe {
    public final int temperature;

    private BlastFurnaceRecipe(Builder builder) {
        super(builder);
        this.temperature = builder.temperature;
    }

    public static class Builder extends BuilderBase<BlastFurnaceRecipe, Builder> {
        private int temperature = 0;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
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

    private static class Serializer extends ProcessingRecipe.Serializer<BlastFurnaceRecipe, Builder> {
        @Override
        protected Builder buildFromJson(IRecipeType<Builder> type, ResourceLocation loc, JsonObject jo) {
            return super.buildFromJson(type, loc, jo)
                .temperature(GsonHelper.getAsInt(jo, "temperature"));
        }

        @Override
        public void toJson(JsonObject jo, BlastFurnaceRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("temperature", recipe.temperature);
        }
    }

    public static final IRecipeSerializer<BlastFurnaceRecipe, Builder> SERIALIZER = new Serializer();
}
