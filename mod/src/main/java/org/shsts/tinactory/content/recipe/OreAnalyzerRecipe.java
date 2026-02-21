package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzerRecipe extends AssemblyRecipe {
    public final double rate;

    private OreAnalyzerRecipe(Builder builder) {
        super(builder);
        this.rate = builder.rate;
    }

    public static class Builder extends AssemblyRecipe.BuilderBase<OreAnalyzerRecipe, Builder> {
        public double rate = 0d;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder rate(double value) {
            rate = value;
            return this;
        }

        @Override
        protected OreAnalyzerRecipe createObject() {
            assert rate > 0d;
            return new OreAnalyzerRecipe(this);
        }
    }

    private static class Serializer extends AssemblyRecipe.Serializer<OreAnalyzerRecipe, Builder> {
        @Override
        protected Builder buildFromJson(IRecipeType<Builder> type, ResourceLocation loc, JsonObject jo) {
            return super.buildFromJson(type, loc, jo)
                .rate(GsonHelper.getAsDouble(jo, "rate"));
        }

        @Override
        public void toJson(JsonObject jo, OreAnalyzerRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("rate", recipe.rate);
        }
    }

    public static final IRecipeSerializer<OreAnalyzerRecipe, Builder> SERIALIZER = new Serializer();
}
