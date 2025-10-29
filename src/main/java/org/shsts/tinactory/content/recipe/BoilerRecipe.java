package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.core.builder.RecipeBuilder;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerRecipe implements IRecipe<Boiler> {
    private final ResourceLocation loc;
    private final FluidStack input;
    private final FluidStack output;
    private final double minHeat;
    private final double optimalHeat;
    private final double maxHeat;
    private final double reactionRate;
    private final double absorbRate;

    private BoilerRecipe(Builder builder) {
        this.loc = builder.loc;
        this.input = Objects.requireNonNull(builder.input);
        this.output = Objects.requireNonNull(builder.output);
        this.minHeat = builder.minHeat;
        this.optimalHeat = builder.optimalHeat;
        this.maxHeat = builder.maxHeat;
        this.reactionRate = builder.reactionRate;
        this.absorbRate = builder.absorbRate;

        assert minHeat > 0 && optimalHeat > minHeat && maxHeat > optimalHeat &&
            reactionRate > 0 && absorbRate > 0;
    }

    @Override
    public ResourceLocation loc() {
        return loc;
    }

    @Override
    public boolean matches(Boiler boiler, Level world) {
        return boiler.getHeat() > minHeat &&
            boiler.getInput().drain(input, true).getAmount() >= input.getAmount();
    }

    public double absorbHeat(Boiler boiler, double parallel) {
        var heat = boiler.getHeat();

        var reaction = (int) Math.floor((heat - minHeat) * reactionRate * parallel);
        if (reaction <= 0) {
            return 0;
        }
        var inputStack = StackHelper.copyWithAmount(input, input.getAmount() * reaction);
        var drained = boiler.getInput().drain(inputStack, true);
        var reaction1 = drained.getAmount() / inputStack.getAmount();
        if (reaction1 <= 0) {
            return 0;
        }

        var inputStack1 = StackHelper.copyWithAmount(input, input.getAmount() * reaction1);
        var decay = MathUtil.clamp(1 - (heat - optimalHeat) / (maxHeat - optimalHeat), 0, 1);
        var outputAmount = (int) Math.floor(output.getAmount() * reaction1 * decay);
        var outputStack = StackHelper.copyWithAmount(output, outputAmount);
        boiler.getInput().drain(inputStack1, false);
        boiler.getOutput().fill(outputStack, false);

        return absorbRate * reaction1;
    }

    public static class Builder extends RecipeBuilder<BoilerRecipe, Builder> {
        @Nullable
        private FluidStack input = null;
        @Nullable
        private FluidStack output = null;
        private double minHeat = 0;
        private double reactionRate = 0;
        private double absorbRate = 0;
        private double optimalHeat = 0;
        private double maxHeat = 0;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder input(FluidStack val) {
            input = val;
            return this;
        }

        public Builder output(FluidStack val) {
            output = val;
            return this;
        }

        public Builder heat(double minHeat, double optimalHeat, double maxHeat) {
            this.minHeat = minHeat;
            this.optimalHeat = optimalHeat;
            this.maxHeat = maxHeat;
            return this;
        }

        public Builder reaction(double reactionRate, double absorbRate) {
            this.reactionRate = reactionRate;
            this.absorbRate = absorbRate;
            return this;
        }

        @Override
        protected BoilerRecipe createObject() {
            return new BoilerRecipe(this);
        }
    }

    private static class Serializer implements IRecipeSerializer<BoilerRecipe, BoilerRecipe.Builder> {
        @Override
        public BoilerRecipe fromJson(IRecipeType<Builder> type, ResourceLocation loc,
            JsonObject jo, ICondition.IContext context) {
            return type.getBuilder(loc)
                .input(CodecHelper.parseJson(FluidStack.CODEC,
                    GsonHelper.getAsJsonObject(jo, "input")))
                .output(CodecHelper.parseJson(FluidStack.CODEC,
                    GsonHelper.getAsJsonObject(jo, "output")))
                .heat(GsonHelper.getAsDouble(jo, "minHeat"),
                    GsonHelper.getAsDouble(jo, "optimalHeat"),
                    GsonHelper.getAsDouble(jo, "maxHeat"))
                .reaction(GsonHelper.getAsDouble(jo, "reactionRate"),
                    GsonHelper.getAsDouble(jo, "absorbRate"))
                .buildObject();
        }

        @Override
        public void toJson(JsonObject jo, BoilerRecipe recipe) {
            jo.add("input", CodecHelper.encodeJson(FluidStack.CODEC, recipe.input));
            jo.add("output", CodecHelper.encodeJson(FluidStack.CODEC, recipe.output));
            jo.addProperty("minHeat", recipe.minHeat);
            jo.addProperty("optimalHeat", recipe.optimalHeat);
            jo.addProperty("maxHeat", recipe.maxHeat);
            jo.addProperty("reactionRate", recipe.reactionRate);
            jo.addProperty("absorbRate", recipe.absorbRate);
        }
    }

    public static final IRecipeSerializer<BoilerRecipe, BoilerRecipe.Builder> SERIALIZER = new Serializer();
}
