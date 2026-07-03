package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.ILimitedPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingDisplay;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinycorelib.api.recipe.IRecipe;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.machine.ProcessingRuntime.VOID_DEFAULT;
import static org.shsts.tinactory.core.machine.ProcessingRuntime.VOID_KEY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRecipe implements IRecipe<IMachine> {
    public record Input(int port, IProcessingIngredient ingredient) {}

    public record Output(int port, IProcessingResult result) {}

    @FunctionalInterface
    public interface Factory<R extends ProcessingRecipe> {
        R create(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power);
    }

    @Nullable
    protected final ResourceLocation loc;
    public final List<Input> inputs;
    public final List<Output> outputs;

    public final long workTicks;
    public final long voltage;
    public final long power;

    public ProcessingRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power) {
        this(null, inputs, outputs, workTicks, voltage, power);
    }

    protected ProcessingRecipe(@Nullable ResourceLocation loc, List<Input> inputs, List<Output> outputs,
        long workTicks, long voltage, long power) {
        this.loc = loc;
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.workTicks = workTicks;
        this.voltage = voltage;
        this.power = power;
        assert power > 0;
        assert workTicks > 0;
    }

    protected Optional<IProcessingIngredient> consumeInput(IContainer container, Input input,
        int parallel, boolean simulate) {
        if (!container.hasPort(input.port)) {
            return Optional.empty();
        }
        var port = container.getPort(input.port, ContainerAccess.INTERNAL);
        return input.ingredient.consumePort(port, parallel, simulate);
    }

    protected boolean canConsumeInput(IContainer container, Input input, int parallel) {
        return consumeInput(container, input, parallel, true).isPresent();
    }

    protected Optional<IProcessingResult> insertOutput(IContainer container, Output output, int parallel,
        RandomSource random, boolean simulate) {
        var port = container.getPort(output.port, ContainerAccess.INTERNAL);
        return output.result.insertPort(port, parallel, random, simulate);
    }

    protected boolean canInsertOutput(IContainer container, Output output, int parallel, RandomSource random) {
        return insertOutput(container, output, parallel, random, true).isPresent();
    }

    protected boolean canInsertOutput(IContainer container, Output output, int parallel,
        RandomSource random, Map<Integer, Integer> outputSlots) {
        var port = container.getPort(output.port, ContainerAccess.INTERNAL);
        var limit = port instanceof ILimitedPort limitedPort ?
            limitedPort.getPortLimit() : Integer.MAX_VALUE;
        if (outputSlots.getOrDefault(output.port, 0) >= limit) {
            return true;
        }
        outputSlots.merge(output.port, 1, Integer::sum);

        return output.result.insertPort(port, parallel, random, true).isPresent();
    }

    protected boolean matchInputs(IMachine machine, IContainer container, int parallel) {
        return inputs.stream().allMatch(input -> canConsumeInput(container, input, parallel));
    }

    protected boolean matchOutputs(IMachine machine, IContainer container,
        int parallel, RandomSource random) {
        var outputSlots = new HashMap<Integer, Integer>();
        return outputs.stream().allMatch(output ->
            canInsertOutput(container, output, parallel, random, outputSlots));
    }

    protected boolean matchTeam(Optional<ITeamProfile> team) {
        return true;
    }

    protected boolean matchElectric(Optional<IElectricMachine> electric) {
        return voltage <= electric.map(IElectricMachine::getVoltage).orElse(0L);
    }

    /**
     * Whether the machine can craft the recipe regardless of input or output;
     */
    public boolean canCraft(IMachine machine) {
        return matchTeam(machine.owner()) && matchElectric(machine.electric());
    }

    @Override
    public boolean matches(IMachine machine) {
        return matches(machine, 1);
    }

    public boolean matches(IMachine machine, int parallel) {
        var container = machine.container();
        var autoVoid = machine.config().getBoolean(VOID_KEY, VOID_DEFAULT);
        return canCraft(machine) && container
            .filter($ -> matchInputs(machine, $, parallel) &&
                (autoVoid || matchOutputs(machine, $, parallel, machine.random())))
            .isPresent();
    }

    public void consumeInputs(IContainer container, int parallel, Consumer<ProcessingInfo> callback) {
        for (var input : inputs) {
            consumeInput(container, input, parallel, false)
                .ifPresent($ -> callback.accept(new ProcessingInfo(input.port, $)));
        }
    }

    public void insertOutputs(IMachine machine, int parallel, RandomSource random,
        Consumer<IProcessingResult> callback) {
        machine.container().ifPresent(container -> insertOutputs(container, parallel, random, callback));
    }

    public void insertOutputs(IContainer container, int parallel, RandomSource random,
        Consumer<IProcessingResult> callback) {
        for (var output : outputs) {
            insertOutput(container, output, parallel, random, false).ifPresent(callback);
        }
    }

    public IRenderDescriptor display() {
        return getDisplayObject()
            .filter(IProcessingDisplay.class::isInstance)
            .map(IProcessingDisplay.class::cast)
            .map(IProcessingDisplay::display)
            .orElse(EmptyRenderDescriptor.INSTANCE);
    }

    public Optional<List<Component>> tooltip() {
        return getDisplayObject()
            .filter(IProcessingDisplay.class::isInstance)
            .map(IProcessingDisplay.class::cast)
            .flatMap(IProcessingDisplay::tooltip);
    }

    protected Optional<IProcessingObject> getDisplayObject() {
        if (!outputs.isEmpty()) {
            return outputs.stream()
                .min(Comparator.comparingInt(Output::port))
                .map(Output::result);
        }
        return inputs.stream()
            .min(Comparator.comparingInt(Input::port))
            .map(Input::ingredient);
    }

    public ResourceLocation loc() {
        return Objects.requireNonNull(loc);
    }

    public static String getDescriptionId(ResourceLocation loc) {
        return loc.getNamespace() + ".recipe." + loc.getPath().replace('/', '.');
    }

    @Override
    public String toString() {
        return loc == null ? getClass().getSimpleName() : getClass().getSimpleName() + "[" + loc + "]";
    }

    public static Codec<Input> inputCodec(Codec<IProcessingIngredient> ingredientCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("port").forGetter(Input::port),
            ingredientCodec.fieldOf("ingredient").forGetter(Input::ingredient)
        ).apply(instance, Input::new));
    }

    public static Codec<Output> outputCodec(Codec<IProcessingResult> resultCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("port").forGetter(Output::port),
            resultCodec.fieldOf("result").forGetter(Output::result)
        ).apply(instance, Output::new));
    }

    public static <R extends ProcessingRecipe> MapCodec<R> codec(Codec<IProcessingIngredient> ingredientCodec,
        Codec<IProcessingResult> resultCodec, Factory<R> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            inputCodec(ingredientCodec).listOf().fieldOf("inputs").forGetter($ -> $.inputs),
            outputCodec(resultCodec).listOf().fieldOf("outputs").forGetter($ -> $.outputs),
            Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
            Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
            Codec.LONG.fieldOf("power").forGetter($ -> $.power)
        ).apply(instance, factory::create));
    }
}
