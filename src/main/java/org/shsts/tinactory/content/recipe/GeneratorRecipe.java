package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeneratorRecipe extends DisplayInputRecipe {
    // this is used to distinguish generator recipes that can be overclocked
    private final boolean exactVoltage;

    private GeneratorRecipe(Builder builder) {
        super(builder);
        this.exactVoltage = builder.exactVoltage;
    }

    @Override
    protected boolean matchOutputs(IContainer container, int parallel, Random random) {
        // no check output
        return true;
    }

    @Override
    protected boolean matchElectric(Optional<IElectricMachine> electric) {
        if (exactVoltage) {
            return electric.filter($ -> $.getVoltage() == voltage).isPresent();
        }
        return super.matchElectric(electric);
    }

    public static class Builder extends BuilderBase<GeneratorRecipe, Builder> {
        private boolean exactVoltage = false;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder exactVoltage(boolean value) {
            exactVoltage = value;
            return this;
        }

        @Override
        protected void validate() {
            assert power > 0 : loc;
            assert workTicks > 0 : loc;
        }

        @Override
        protected GeneratorRecipe createObject() {
            return new GeneratorRecipe(this);
        }
    }

    private static class Serializer extends ProcessingRecipe.Serializer<GeneratorRecipe, Builder> {
        @Override
        protected Builder buildFromJson(IRecipeType<Builder> type, ResourceLocation loc, JsonObject jo) {
            return super.buildFromJson(type, loc, jo)
                .exactVoltage(GsonHelper.getAsBoolean(jo, "exactVoltage", false));
        }

        @Override
        public void toJson(JsonObject jo, GeneratorRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("exactVoltage", recipe.exactVoltage);
        }
    }

    public static IRecipeSerializer<GeneratorRecipe, Builder> SERIALIZER = new Serializer();
}
