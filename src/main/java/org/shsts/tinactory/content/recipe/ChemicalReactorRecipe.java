package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalReactorRecipe extends AssemblyRecipe {
    public final boolean requireMultiblock;

    private ChemicalReactorRecipe(Builder builder) {
        super(builder);
        this.requireMultiblock = builder.requireMultiblock;
    }

    @Override
    public boolean canCraft(IMachine machine) {
        return super.canCraft(machine) &&
            (!requireMultiblock || machine instanceof MultiblockInterface);
    }

    public static class Builder extends BuilderBase<ChemicalReactorRecipe, Builder> {
        private boolean requireMultiblock = false;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder requireMultiblock(boolean val) {
            requireMultiblock = val;
            return this;
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
                .requireMultiblock(GsonHelper.getAsBoolean(jo, "require_multiblock", false));
        }

        @Override
        public void toJson(JsonObject jo, ChemicalReactorRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("require_multiblock", recipe.requireMultiblock);
        }
    }

    public static IRecipeSerializer<ChemicalReactorRecipe, Builder> SERIALIZER = new Serializer();
}
