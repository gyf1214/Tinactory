package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.recipe.IRecipe;

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

    public final FluidStack input;
    public final FluidStack output;
    private final double minHeat;
    private final double optimalHeat;
    private final double maxHeat;
    private final double reactionRate;
    private final double absorbRate;

    public BoilerRecipe(FluidStack input, FluidStack output, double minHeat, double optimalHeat, double maxHeat,
        double reactionRate, double absorbRate) {
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

}
