package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.content.machine.OreAnalyzerProcessor;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.IRecipeDataConsumer;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzerRecipe extends AssemblyRecipe {
    public final double rate;

    private OreAnalyzerRecipe(Builder builder) {
        super(builder);
        this.rate = builder.rate;
    }

    /**
     * This is only called in primitive processor.
     */
    @Override
    public void insertOutputs(IContainer container, Random random) {
        if (random.nextDouble() <= rate) {
            super.insertOutputs(container, random);
        }
    }

    /**
     * This is called in {@link OreAnalyzerProcessor}.
     */
    public void doInsertOutputs(IContainer container, Random random) {
        super.insertOutputs(container, random);
    }

    public static class Builder extends AssemblyRecipe.BuilderBase<OreAnalyzerRecipe, Builder> {
        public double rate = 0d;

        public Builder(IRecipeDataConsumer consumer, RecipeTypeEntry<OreAnalyzerRecipe, Builder> parent,
                       ResourceLocation loc) {
            super(consumer, parent, loc);
        }

        public Builder rate(double value) {
            rate = value;
            return this;
        }

        public Builder inputOre(OreVariant variant) {
            return inputItem(0, () -> variant.baseItem, 1);
        }

        @Override
        protected OreAnalyzerRecipe createObject() {
            assert rate > 0d;
            return new OreAnalyzerRecipe(this);
        }
    }

    public static class Serializer extends AssemblyRecipe.Serializer<OreAnalyzerRecipe, Builder> {
        protected Serializer(RecipeTypeEntry<OreAnalyzerRecipe, Builder> type) {
            super(type);
        }

        @Override
        protected Builder buildFromJson(ResourceLocation loc, JsonObject jo) {
            return super.buildFromJson(loc, jo)
                    .rate(GsonHelper.getAsDouble(jo, "rate"));
        }

        @Override
        public void toJson(JsonObject jo, OreAnalyzerRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("rate", recipe.rate);
        }
    }

    public static final SmartRecipeSerializer.Factory<OreAnalyzerRecipe, OreAnalyzerRecipe.Builder>
            SERIALIZER = Serializer::new;
}
