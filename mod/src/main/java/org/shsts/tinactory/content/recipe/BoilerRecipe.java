package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.core.builder.RecipeBuilder;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Objects;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerRecipe implements IRecipe<Boiler> {
    public static final MapCodec<BoilerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        FluidStack.CODEC.fieldOf("input").forGetter($ -> $.input),
        FluidStack.CODEC.fieldOf("output").forGetter($ -> $.output),
        Codec.DOUBLE.fieldOf("minHeat").forGetter($ -> $.minHeat),
        Codec.DOUBLE.fieldOf("optimalHeat").forGetter($ -> $.optimalHeat),
        Codec.DOUBLE.fieldOf("maxHeat").forGetter($ -> $.maxHeat),
        Codec.DOUBLE.fieldOf("reactionRate").forGetter($ -> $.reactionRate),
        Codec.DOUBLE.fieldOf("absorbRate").forGetter($ -> $.absorbRate)
    ).apply(instance, BoilerRecipe::new));

    @Nullable
    private final ResourceLocation loc;
    public final FluidStack input;
    public final FluidStack output;
    private final double minHeat;
    private final double optimalHeat;
    private final double maxHeat;
    private final double reactionRate;
    private final double absorbRate;

    private BoilerRecipe(Builder builder) {
        this(builder.loc, Objects.requireNonNull(builder.input), Objects.requireNonNull(builder.output),
            builder.minHeat, builder.optimalHeat, builder.maxHeat, builder.reactionRate, builder.absorbRate);
    }

    public BoilerRecipe(FluidStack input, FluidStack output, double minHeat, double optimalHeat, double maxHeat,
        double reactionRate, double absorbRate) {
        this(null, input, output, minHeat, optimalHeat, maxHeat, reactionRate, absorbRate);
    }

    private BoilerRecipe(@Nullable ResourceLocation loc, FluidStack input, FluidStack output, double minHeat,
        double optimalHeat, double maxHeat, double reactionRate, double absorbRate) {
        this.loc = loc;
        this.input = input;
        this.output = output;
        this.minHeat = minHeat;
        this.optimalHeat = optimalHeat;
        this.maxHeat = maxHeat;
        this.reactionRate = reactionRate;
        this.absorbRate = absorbRate;

        assert minHeat > 0 && optimalHeat > minHeat && maxHeat > optimalHeat &&
            reactionRate > 0 && absorbRate > 0;
    }

    public ResourceLocation loc() {
        return Objects.requireNonNull(loc);
    }

    @Override
    public boolean matches(Boiler boiler) {
        return boiler.heat() > minHeat && boiler.getInput()
            .filter($ -> $.extract(input, true).getAmount() >= input.getAmount())
            .isPresent();
    }

    public double getReaction(double heat, double parallel) {
        return (heat - minHeat) * reactionRate * parallel;
    }

    public double absorbHeat(IPort<FluidStack> inputPort, IPort<FluidStack> outputPort,
        int reaction, double heat, BiConsumer<FluidStack, FluidStack> callback) {
        var inputStack = StackHelper.copyWithAmount(input, input.getAmount() * reaction);
        var drained = inputPort.extract(inputStack, true);
        var reaction1 = drained.getAmount() / input.getAmount();
        if (reaction1 <= 0) {
            return 0;
        }

        var inputStack1 = StackHelper.copyWithAmount(input, input.getAmount() * reaction1);
        var decay = MathUtil.clamp(1 - (heat - optimalHeat) / (maxHeat - optimalHeat), 0, 1);
        var outputAmount = (int) Math.floor(output.getAmount() * reaction1 * decay);
        var outputStack = StackHelper.copyWithAmount(output, outputAmount);
        inputPort.extract(inputStack1, false);
        outputPort.insert(outputStack, false);
        callback.accept(inputStack1, outputStack);

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

        public Builder(IRecipeType<?> parent, ResourceLocation loc) {
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
}
