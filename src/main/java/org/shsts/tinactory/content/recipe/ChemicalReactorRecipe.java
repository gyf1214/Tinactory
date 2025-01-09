package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalReactorRecipe extends AssemblyRecipe {
    public final boolean requireLarge;

    private ChemicalReactorRecipe(Builder builder) {
        super(builder);
        this.requireLarge = builder.requireLarge;
    }

    public static class Builder extends BuilderBase<ChemicalReactorRecipe, Builder> {
        private boolean requireLarge = false;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder input(MaterialSet material, String sub, float amount) {
            if (material.hasItem(sub)) {
                return inputItem(material.tag(sub), (int) amount);
            } else {
                return inputFluid(material.fluid(sub), material.fluidAmount(sub, amount));
            }
        }

        public Builder input(MaterialSet material, float amount) {
            return material.hasItem("dust") ? input(material, "dust", amount) : input(material, "fluid", amount);
        }

        public Builder input(MaterialSet material) {
            return input(material, 1f);
        }

        public Builder output(MaterialSet material, String sub, float amount) {
            if (material.hasItem(sub)) {
                return inputItem(material.tag(sub), (int) amount);
            } else {
                return inputFluid(material.fluid(sub), material.fluidAmount(sub, amount));
            }
        }

        public Builder output(MaterialSet material, float amount) {
            return material.hasItem("dust") ? output(material, "dust", amount) : output(material, "fluid", amount);
        }

        public Builder output(MaterialSet material) {
            return output(material, 1f);
        }

        public Builder requireLarge(boolean val) {
            requireLarge = val;
            return this;
        }

        public Builder requireLarge() {
            return requireLarge(true);
        }

        @Override
        protected ChemicalReactorRecipe createObject() {
            return new ChemicalReactorRecipe(this);
        }
    }

    protected static class Serializer extends AssemblyRecipe.Serializer<ChemicalReactorRecipe, Builder> {
        @Override
        protected Builder buildFromJson(IRecipeType<Builder> type, ResourceLocation loc, JsonObject jo) {
            return super.buildFromJson(type, loc, jo)
                .requireLarge(GsonHelper.getAsBoolean(jo, "require_large", false));
        }

        @Override
        public void toJson(JsonObject jo, ChemicalReactorRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("require_large", recipe.requireLarge);
        }
    }

    public static IRecipeSerializer<ChemicalReactorRecipe, Builder> SERIALIZER = new Serializer();
}
