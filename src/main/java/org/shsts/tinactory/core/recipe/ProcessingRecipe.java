package org.shsts.tinactory.core.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.builder.RecipeBuilder;
import org.shsts.tinactory.core.machine.ProcessingInfo;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRecipe implements IRecipe<IMachine> {
    public record Input(int port, IProcessingIngredient ingredient) {}

    public record Output(int port, IProcessingResult result) {}

    protected final ResourceLocation loc;
    public final List<Input> inputs;
    public final List<Output> outputs;

    public final long workTicks;
    public final long voltage;
    public final long power;

    protected ProcessingRecipe(BuilderBase<?, ?> builder) {
        this.loc = builder.loc;
        this.inputs = builder.getInputs();
        this.outputs = builder.getOutputs();
        this.workTicks = builder.workTicks;
        this.voltage = builder.voltage;
        this.power = builder.power;
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

    protected boolean insertOutput(IContainer container, Output output, int parallel,
        Random random, boolean simulate) {
        var port = container.getPort(output.port, ContainerAccess.INTERNAL);
        return output.result.insertPort(port, parallel, random, simulate);
    }

    protected boolean matchInputs(IContainer container, int parallel) {
        return inputs.stream().allMatch(input -> canConsumeInput(container, input, parallel));
    }

    protected boolean matchOutputs(IContainer container, int parallel, Random random) {
        return outputs.stream().allMatch(output -> insertOutput(container, output, parallel, random, true));
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
    public boolean matches(IMachine machine, Level world) {
        return matches(machine, world, 1);
    }

    public boolean matches(IMachine machine, Level world, int parallel) {
        var container = machine.container();
        return canCraft(machine) && container
            .filter($ -> matchInputs($, parallel) && matchOutputs($, parallel, world.random))
            .isPresent();
    }

    public void consumeInputs(IContainer container, int parallel, Consumer<ProcessingInfo> cons) {
        for (var input : inputs) {
            consumeInput(container, input, parallel, false)
                .ifPresent($ -> cons.accept(new ProcessingInfo(input.port, $)));
        }
    }

    public void insertOutputs(IMachine machine, int parallel, Random random) {
        machine.container().ifPresent(container -> insertOutputs(container, parallel, random));
    }

    public void insertOutputs(IContainer container, int parallel, Random random) {
        for (var output : outputs) {
            insertOutput(container, output, parallel, random, false);
        }
    }

    @Override
    public ResourceLocation loc() {
        return loc;
    }

    public static String getDescriptionId(ResourceLocation loc) {
        return loc.getNamespace() + ".recipe." + loc.getPath().replace('/', '.');
    }

    public Optional<String> getDescriptionId() {
        return Optional.empty();
    }

    public Optional<Component> getDescription() {
        return getDescriptionId().map(I18n::tr);
    }

    public IProcessingObject getDisplay() {
        if (!outputs.isEmpty()) {
            return outputs.stream().min(Comparator.comparingInt(Output::port))
                .get().result;
        } else if (!inputs.isEmpty()) {
            return inputs.stream().min(Comparator.comparingInt(Input::port))
                .get().ingredient;
        } else {
            return ProcessingResults.EMPTY;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + loc + "]";
    }

    public abstract static class BuilderBase<R extends ProcessingRecipe, S extends BuilderBase<R, S>>
        extends RecipeBuilder<R, S> {
        protected final List<Supplier<Input>> inputs = new ArrayList<>();
        protected final List<Supplier<Output>> outputs = new ArrayList<>();
        protected long workTicks = 0;
        protected long voltage = 0;
        protected long power = 0;
        protected List<Input> resolvedInputs = null;
        protected List<Output> resolvedOutputs = null;

        protected BuilderBase(IRecipeType<S> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public S input(int port, Supplier<IProcessingIngredient> ingredient) {
            assert port >= 0;
            inputs.add(() -> new Input(port, ingredient.get()));
            return self();
        }

        public S input(int port, IProcessingIngredient ingredient) {
            return input(port, () -> ingredient);
        }

        public S output(int port, Supplier<IProcessingResult> result) {
            outputs.add(() -> new Output(port, result.get()));
            return self();
        }

        public S output(int port, IProcessingResult result) {
            return output(port, () -> result);
        }

        public S workTicks(long value) {
            workTicks = value;
            return self();
        }

        public S voltage(long value) {
            voltage = value;
            return self();
        }

        public S power(long value) {
            power = value;
            return self();
        }

        public List<Input> getInputs() {
            if (resolvedInputs == null) {
                resolvedInputs = inputs.stream().map(Supplier::get).toList();
            }
            return resolvedInputs;
        }

        public List<Output> getOutputs() {
            if (resolvedOutputs == null) {
                resolvedOutputs = outputs.stream().map(Supplier::get).toList();
            }
            return resolvedOutputs;
        }

        protected void validate() {
            assert power > 0 : loc;
            assert workTicks > 0 : loc;
            assert !outputs.isEmpty() : loc;
        }

        @Override
        public R buildObject() {
            validate();
            return super.buildObject();
        }
    }

    public static class Builder extends BuilderBase<ProcessingRecipe, Builder> {
        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        @Override
        protected ProcessingRecipe createObject() {
            return new ProcessingRecipe(this);
        }
    }

    protected static class Serializer<R extends ProcessingRecipe, B extends BuilderBase<R, B>>
        implements IRecipeSerializer<R, B> {
        protected B buildFromJson(IRecipeType<B> type, ResourceLocation loc, JsonObject jo) {
            var builder = type.getBuilder(loc);
            Streams.stream(GsonHelper.getAsJsonArray(jo, "inputs"))
                .map(JsonElement::getAsJsonObject)
                .forEach(je -> builder.input(
                    GsonHelper.getAsInt(je, "port"),
                    ProcessingIngredients.fromJson(GsonHelper.getAsJsonObject(je, "ingredient"))));
            Streams.stream(GsonHelper.getAsJsonArray(jo, "outputs"))
                .map(JsonElement::getAsJsonObject)
                .forEach(je -> builder.output(
                    GsonHelper.getAsInt(je, "port"),
                    ProcessingResults.fromJson(GsonHelper.getAsJsonObject(je, "result"))));
            return builder
                .workTicks(GsonHelper.getAsLong(jo, "work_ticks"))
                .voltage(GsonHelper.getAsLong(jo, "voltage"))
                .power(GsonHelper.getAsLong(jo, "power"));
        }

        @Override
        public R fromJson(IRecipeType<B> type, ResourceLocation loc, JsonObject jo, ICondition.IContext context) {
            return buildFromJson(type, loc, jo).buildObject();
        }

        @Override
        public void toJson(JsonObject jo, R recipe) {
            var inputs = new JsonArray();
            recipe.inputs.stream()
                .map(input -> {
                    var je = new JsonObject();
                    je.addProperty("port", input.port);
                    je.add("ingredient", ProcessingIngredients.toJson(input.ingredient));
                    return je;
                }).forEach(inputs::add);
            var outputs = new JsonArray();
            recipe.outputs.stream()
                .map(output -> {
                    var je = new JsonObject();
                    je.addProperty("port", output.port);
                    je.add("result", ProcessingResults.toJson(output.result));
                    return je;
                }).forEach(outputs::add);
            jo.add("inputs", inputs);
            jo.add("outputs", outputs);
            jo.addProperty("work_ticks", recipe.workTicks);
            jo.addProperty("voltage", recipe.voltage);
            jo.addProperty("power", recipe.power);
        }
    }

    public static final IRecipeSerializer<ProcessingRecipe, Builder> SERIALIZER = new Serializer<>();
}
