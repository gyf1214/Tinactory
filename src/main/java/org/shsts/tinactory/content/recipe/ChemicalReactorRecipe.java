package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;
import org.slf4j.Logger;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalReactorRecipe extends AssemblyRecipe {
    private static final Logger LOGGER = LogUtils.getLogger();

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

    public static class Builder extends BuilderBase<ChemicalReactorRecipe, Builder>
        implements IMaterialRecipeBuilder<Builder> {
        private boolean requireMultiblock = false;
        private boolean requireMultiBlockSet = false;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder requireMultiblock(boolean val) {
            requireMultiblock = val;
            requireMultiBlockSet = true;
            return this;
        }

        public Builder requireMultiblock() {
            return requireMultiblock(true);
        }

        private boolean needMultiblock() {
            var inputs = getInputs();
            var outputs = getOutputs();

            int itemInputs = 0;
            int fluidInputs = 0;
            int itemOutputs = 0;
            int fluidOutputs = 0;

            for (var input : inputs) {
                if (input.ingredient().type() == PortType.ITEM) {
                    itemInputs++;
                } else if (input.ingredient().type() == PortType.FLUID) {
                    fluidInputs++;
                }
            }

            for (var output : outputs) {
                if (output.result().type() == PortType.ITEM) {
                    itemOutputs++;
                } else if (output.result().type() == PortType.FLUID) {
                    fluidOutputs++;
                }
            }

            return itemInputs > 2 || fluidInputs > 2 || itemOutputs > 2 || fluidOutputs > 2;
        }

        @Override
        protected ChemicalReactorRecipe createObject() {
            if (!requireMultiBlockSet && needMultiblock()) {
                LOGGER.debug("{} recipe need multiblock", loc);
                requireMultiblock = true;
            }
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
