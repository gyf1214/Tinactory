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

    public ProcessingRecipe(RecipeTypeEntry<S, ?> type, ResourceLocation loc,
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

    @Override
    public boolean matches(IContainer container, Level world) {
        return this.inputs.stream().allMatch(input -> this.consumeInput(container, input, true)) &&
                this.outputs.stream().allMatch(output -> this.insertOutput(container, output, world.random, true));
    }

    public void consumeInputs(IContainer container) {
        for (var input : this.inputs) {
            this.consumeInput(container, input, false);
        }
    }

    public void insertOutputs(IContainer container, Random random) {
        for (var output : this.outputs) {
            this.insertOutput(container, output, random, false);
        }
    }

    @Override
    public ItemStack assemble(IContainer container) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public static class Simple extends ProcessingRecipe<Simple> {
        public Simple(RecipeTypeEntry<Simple, ?> type, ResourceLocation loc,
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
            this.inputs.add(() -> new Input(port, ingredient.get()));
            return self();
        }

        public S input(int port, IProcessingIngredient ingredient) {
            return this.input(port, () -> ingredient);
        }

        public S inputItem(int port, Supplier<Item> item, int amount) {
            return this.input(port, () -> new ProcessingIngredients.SimpleItemIngredient(
                    new ItemStack(item.get(), amount)));
        }

        public S inputFluid(int port, Fluid fluid, int amount) {
            return this.input(port, () -> new ProcessingIngredients.FluidIngredient(
                    new FluidStack(fluid, amount)));
        }

        public S output(int port, Supplier<IProcessingResult> result) {
            this.outputs.add(() -> new Output(port, result.get()));
            return self();
        }

        public S output(int port, IProcessingResult result) {
            return this.output(port, () -> result);
        }

        public S outputItem(int port, Supplier<Item> item, int amount, float rate) {
            return this.output(port, () ->
                    new ProcessingResults.ItemResult(true, rate, new ItemStack(item.get(), amount)));
        }

        public S outputItem(int port, Supplier<Item> item, int amount) {
            return this.outputItem(port, item, amount, 1.0f);
        }

        public S outputItem(int port, Item item, int amount) {
            return this.outputItem(port, () -> item, amount);
        }

        public S outputFluid(int port, Fluid fluid, int amount, float rate) {
            return this.output(port, new ProcessingResults.FluidResult(true, rate, new FluidStack(fluid, amount)));
        }

        public S outputFluid(int port, Fluid fluid, int amount) {
            return this.outputFluid(port, fluid, amount, 1.0f);
        }

        public S workTicks(long workTicks) {
            this.workTicks = workTicks;
            return self();
        }

        public S primitive() {
            this.voltage = 0;
            return self();
        }

        public S voltage(Voltage voltage) {
            this.voltage = voltage.val;
            return self();
        }

        public S voltage(long voltage) {
            this.voltage = voltage;
            return self();
        }

        public S power(long power) {
            this.power = power;
            return self();
        }

        public S amperage(double amperage) {
            this.power = (long) (amperage * this.voltage);
            return self();
        }

        protected List<Input> getInputs() {
            return this.inputs.stream().map(Supplier::get).toList();
        }

        protected List<Output> getOutputs() {
            return this.outputs.stream().map(Supplier::get).toList();
        }
    }

    public static class SimpleBuilder extends Builder<Simple, SimpleBuilder> {
        public SimpleBuilder(Registrate registrate, RecipeTypeEntry<Simple, SimpleBuilder> parent,
                             ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        @Override
        public Simple createObject() {
            return new Simple(this.parent, this.loc,
                    this.getInputs(), this.getOutputs(),
                    this.workTicks, this.voltage, this.power);
        }
    }

    protected static class Serializer<T extends ProcessingRecipe<T>, B extends Builder<T, B>>
            extends SmartRecipeSerializer<T, B> {
        protected Serializer(RecipeTypeEntry<T, B> type) {
            super(type);
        }

        protected B buildFromJson(ResourceLocation loc, JsonObject jo) {
            var builder = this.type.getBuilder(loc);
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
                    .workTicks(GsonHelper.getAsLong(jo, "workTicks"))
                    .voltage(GsonHelper.getAsLong(jo, "voltage", 0))
                    .power(GsonHelper.getAsLong(jo, "power", 0));
        }

        @Override
        public T fromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context) {
            return this.buildFromJson(loc, jo).buildObject();
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
            jo.addProperty("workTicks", recipe.workTicks);
            if (recipe.voltage > 0) {
                jo.addProperty("voltage", recipe.voltage);
                jo.addProperty("power", recipe.power);
            }
        }

        public B buildFromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
            var builder = this.type.getBuilder(loc);
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
            return this.buildFromNetwork(loc, buf).buildObject();
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
