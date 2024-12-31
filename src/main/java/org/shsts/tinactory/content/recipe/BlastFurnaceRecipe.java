package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.multiblock.BlastFurnace;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.OptionalInt;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceRecipe extends ProcessingRecipe {
    public final int temperature;

    private BlastFurnaceRecipe(Builder builder) {
        super(builder);
        this.temperature = builder.temperature;
    }

    private OptionalInt getTemperature(IMachine machine) {
        if (!(machine instanceof MultiBlockInterface multiBlockInterface)) {
            return OptionalInt.empty();
        }
        return multiBlockInterface.getMultiBlock()
            .filter($ -> $ instanceof BlastFurnace)
            .map($ -> ((BlastFurnace) $).getTemperature())
            .orElse(OptionalInt.empty());
    }

    @Override
    public boolean canCraft(IMachine machine) {
        var machineTemp = getTemperature(machine);
        return super.canCraft(machine) && machineTemp.isPresent() &&
            temperature <= machineTemp.getAsInt();
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
