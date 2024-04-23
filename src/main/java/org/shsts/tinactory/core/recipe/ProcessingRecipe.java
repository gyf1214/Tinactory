package org.shsts.tinactory.core.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.SmartRecipeBuilder;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRecipe<S extends ProcessingRecipe<S>> extends SmartRecipe<IContainer, S> {
    public record Input(int port, IProcessingIngredient ingredient) {}

    public record Output(int port, IProcessingResult result) {}

    public final List<Input> inputs;
    public final List<Output> outputs;

    public final long workTicks;
    public final long voltage;
    public final long power;

    protected ProcessingRecipe(RecipeTypeEntry<S, ?> type, ResourceLocation loc,
                               List<Input> inputs, List<Output> outputs,
                               long workTicks, long voltage, long power) {
        super(type, loc);
        this.inputs = inputs;
        this.outputs = outputs;
        this.workTicks = workTicks;
        this.voltage = voltage;
        this.power = power;
    }

    protected boolean consumeInput(IContainer container, Input input, boolean simulate) {
        return container.hasPort(input.port) &&
                input.ingredient.consumePort(container.getPort(input.port, true), simulate);
    }

    protected boolean insertOutput(IContainer container, Output output, Random random, boolean simulate) {
        return output.result.insertPort(container.getPort(output.port, true), random, simulate);
    }

    /**
     * Return whether this recipe is available in this container.
     */
    public boolean canCraftIn(IContainer container) {
        return true;
    }

    @Override
    public boolean matches(IContainer container, Level world) {
        return canCraftIn(container) &&
                inputs.stream().allMatch(input -> consumeInput(container, input, true)) &&
                outputs.stream().allMatch(output -> insertOutput(container, output, world.random, true));
    }

    public void consumeInputs(IContainer container) {
        for (var input : inputs) {
            consumeInput(container, input, false);
        }
    }

    public void insertOutputs(IContainer container, Random random) {
        for (var output : outputs) {
            insertOutput(container, output, random, false);
        }
    }

    @Override
    public ItemStack assemble(IContainer container) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        var output = getResult();
        if (output instanceof ProcessingResults.ItemResult item) {
            return item.stack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public IProcessingResult getResult() {
        return outputs.stream().min(Comparator.comparingInt(a -> a.port))
                .map(Output::result)
                .orElse(new ProcessingResults.ItemResult(true, 0f, ItemStack.EMPTY));
    }

    public static class Simple extends ProcessingRecipe<Simple> {
        private Simple(RecipeTypeEntry<Simple, ?> type, ResourceLocation loc,
                       List<Input> inputs, List<Output> outputs,
                       long workTicks, long voltage, long power) {
            super(type, loc, inputs, outputs, workTicks, voltage, power);
        }
    }

    public abstract static class Builder<U extends ProcessingRecipe<U>, S extends Builder<U, S>>
            extends SmartRecipeBuilder<U, S> {
        protected final List<Supplier<Input>> inputs = new ArrayList<>();
        protected final List<Supplier<Output>> outputs = new ArrayList<>();
        protected long workTicks = 0;
        protected long voltage = 0;
        protected long power = 0;

        public Builder(Registrate registrate, RecipeTypeEntry<U, S> parent, ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        public S input(int port, Supplier<IProcessingIngredient> ingredient) {
            inputs.add(() -> new Input(port, ingredient.get()));
            return self();
        }

        public S input(int port, IProcessingIngredient ingredient) {
            return input(port, () -> ingredient);
        }

        public S inputItem(int port, Supplier<? extends Item> item, int amount) {
            return input(port, () -> new ProcessingIngredients.SimpleItemIngredient(
                    new ItemStack(item.get(), amount)));
        }

        public S inputItem(int port, Item item, int amount) {
            return input(port, new ProcessingIngredients.SimpleItemIngredient(
                    new ItemStack(item, amount)));
        }

        public S inputFluid(int port, Supplier<? extends Fluid> fluid, int amount) {
            return input(port, () -> new ProcessingIngredients.FluidIngredient(
                    new FluidStack(fluid.get(), amount)));
        }

        public S inputFluid(int port, Fluid fluid, int amount) {
            return input(port, new ProcessingIngredients.FluidIngredient(
                    new FluidStack(fluid, amount)));
        }

        public S output(int port, Supplier<IProcessingResult> result) {
            outputs.add(() -> new Output(port, result.get()));
            return self();
        }

        public S output(int port, IProcessingResult result) {
            return output(port, () -> result);
        }

        public S outputItem(int port, Supplier<? extends Item> item, int amount, float rate) {
            return output(port, () ->
                    new ProcessingResults.ItemResult(true, rate, new ItemStack(item.get(), amount)));
        }

        public S outputItem(int port, Supplier<? extends Item> item, int amount) {
            return outputItem(port, item, amount, 1.0f);
        }

        public S outputItem(int port, Item item, int amount) {
            return outputItem(port, () -> item, amount);
        }

        public S outputFluid(int port, Supplier<? extends Fluid> fluid, int amount, float rate) {
            return output(port, () -> new ProcessingResults.FluidResult(
                    true, rate, new FluidStack(fluid.get(), amount)));
        }

        public S outputFluid(int port, Fluid fluid, int amount, float rate) {
            return output(port, new ProcessingResults.FluidResult(
                    true, rate, new FluidStack(fluid, amount)));
        }

        public S outputFluid(int port, Supplier<? extends Fluid> fluid, int amount) {
            return outputFluid(port, fluid, amount, 1.0f);
        }

        public S outputFluid(int port, Fluid fluid, int amount) {
            return outputFluid(port, fluid, amount, 1.0f);
        }

        public S workTicks(long value) {
            workTicks = value;
            return self();
        }

        public S primitive() {
            voltage = 0;
            return self();
        }

        public S voltage(Voltage value) {
            voltage = value.value;
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

        protected List<Input> getInputs() {
            return inputs.stream().map(Supplier::get).toList();
        }

        protected List<Output> getOutputs() {
            return outputs.stream().map(Supplier::get).toList();
        }
    }

    public static class SimpleBuilder extends Builder<Simple, SimpleBuilder> {
        public SimpleBuilder(Registrate registrate, RecipeTypeEntry<Simple, SimpleBuilder> parent,
                             ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        @Override
        public Simple createObject() {
            return new Simple(parent, loc, getInputs(), getOutputs(), workTicks, voltage, power);
        }
    }

    protected static class Serializer<T extends ProcessingRecipe<T>, B extends Builder<T, B>>
            extends SmartRecipeSerializer<T, B> {
        protected Serializer(RecipeTypeEntry<T, B> type) {
            super(type);
        }

        protected B buildFromJson(ResourceLocation loc, JsonObject jo) {
            var builder = type.getBuilder(loc);
            Streams.stream(GsonHelper.getAsJsonArray(jo, "inputs"))
                    .map(JsonElement::getAsJsonObject)
                    .forEach(je -> builder.input(
                            GsonHelper.getAsInt(je, "port"),
                            ProcessingIngredients.SERIALIZER.fromJson(GsonHelper.getAsJsonObject(je, "ingredient"))));
            Streams.stream(GsonHelper.getAsJsonArray(jo, "outputs"))
                    .map(JsonElement::getAsJsonObject)
                    .forEach(je -> builder.output(
                            GsonHelper.getAsInt(je, "port"),
                            ProcessingResults.SERIALIZER.fromJson(GsonHelper.getAsJsonObject(je, "result"))));
            return builder
                    .workTicks(GsonHelper.getAsLong(jo, "work_ticks"))
                    .voltage(GsonHelper.getAsLong(jo, "voltage", 0))
                    .power(GsonHelper.getAsLong(jo, "power", 0));
        }

        @Override
        public T fromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context) {
            return buildFromJson(loc, jo).buildObject();
        }

        @Override
        public void toJson(JsonObject jo, T recipe) {
            var inputs = new JsonArray();
            recipe.inputs.stream()
                    .map(input -> {
                        var je = new JsonObject();
                        je.addProperty("port", input.port);
                        je.add("ingredient", ProcessingIngredients.SERIALIZER.toJson(input.ingredient));
                        return je;
                    }).forEach(inputs::add);
            var outputs = new JsonArray();
            recipe.outputs.stream()
                    .map(output -> {
                        var je = new JsonObject();
                        je.addProperty("port", output.port);
                        je.add("result", ProcessingResults.SERIALIZER.toJson(output.result));
                        return je;
                    }).forEach(outputs::add);
            jo.add("inputs", inputs);
            jo.add("outputs", outputs);
            jo.addProperty("work_ticks", recipe.workTicks);
            if (recipe.voltage > 0) {
                jo.addProperty("voltage", recipe.voltage);
                jo.addProperty("power", recipe.power);
            }
        }

        public B buildFromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
            var builder = type.getBuilder(loc);
            buf.readWithCount(buf1 -> builder.input(
                    buf1.readVarInt(),
                    ProcessingIngredients.SERIALIZER.fromNetwork(buf1)));
            buf.readWithCount(buf1 -> builder.output(
                    buf1.readVarInt(),
                    ProcessingResults.SERIALIZER.fromNetwork(buf1)));
            return builder
                    .workTicks(buf.readVarLong())
                    .voltage(buf.readVarLong())
                    .power(buf.readVarLong());
        }

        @Override
        public T fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
            return buildFromNetwork(loc, buf).buildObject();
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, T recipe) {
            buf.writeCollection(recipe.inputs, (buf1, input) -> {
                buf1.writeVarInt(input.port);
                ProcessingIngredients.SERIALIZER.toNetwork(input.ingredient, buf);
            });
            buf.writeCollection(recipe.outputs, (buf1, output) -> {
                buf1.writeVarInt(output.port);
                ProcessingResults.SERIALIZER.toNetwork(output.result, buf1);
            });
            buf.writeVarLong(recipe.workTicks);
            buf.writeVarLong(recipe.voltage);
            buf.writeVarLong(recipe.power);
        }
    }

    public static final SmartRecipeSerializer.SimpleFactory<Simple, SimpleBuilder>
            SIMPLE_SERIALIZER = Serializer::new;

}
