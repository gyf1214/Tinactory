package org.shsts.tinactory.content.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.content.machine.IProcessingMachine;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.core.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.SmartRecipeBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRecipe<S extends ProcessingRecipe<S>> extends SmartRecipe<IProcessingMachine, S> {
    public record Input(int port, Either<Ingredient, FluidStack> ingredient, int amount) {}

    public record Output(int port, Either<ItemStack, FluidStack> result, float rate) {}

    public final List<Input> inputs;
    public final List<Output> outputs;

    public final long workTicks;

    public ProcessingRecipe(RecipeTypeEntry<S, ?> type, ResourceLocation loc,
                            List<Input> inputs, List<Output> outputs, long workTicks) {
        super(type, loc);
        this.inputs = inputs;
        this.outputs = outputs;
        this.workTicks = workTicks;
    }

    protected boolean consumeInput(IProcessingMachine container, Input input, boolean simulate) {
        return container.hasPort(input.port) && container.getPort(input.port, true).map(
                collection -> input.ingredient.left()
                        .map(item -> ItemHelper.consumeItemCollection(collection, item, input.amount, simulate))
                        .orElse(false),
                collection -> input.ingredient.right()
                        .map(fluid -> collection.drain(fluid, simulate).getAmount() >= fluid.getAmount())
                        .orElse(false));
    }

    protected boolean insertOutput(IProcessingMachine container, Output output, boolean simulate) {
        if (!container.hasPort(output.port) || (simulate && output.rate < 1.0d)) {
            return true;
        }
        return container.getPort(output.port, true).map(
                collection -> output.result.left()
                        .map(item -> collection.insertItem(item.copy(), simulate).isEmpty())
                        .orElse(false),
                collection -> output.result.right()
                        .map(fluid -> collection.fill(fluid, simulate) == fluid.getAmount())
                        .orElse(false));
    }

    @Override
    public boolean matches(IProcessingMachine container, Level world) {
        return this.inputs.stream().allMatch(input -> this.consumeInput(container, input, true)) &&
                this.outputs.stream().allMatch(output -> this.insertOutput(container, output, true));
    }

    public void consumeInputs(IProcessingMachine container) {
        for (var input : this.inputs) {
            this.consumeInput(container, input, false);
        }
    }

    public void insertOutputs(IProcessingMachine container, Random random) {
        for (var output : this.outputs) {
            if (random.nextDouble() > output.rate) {
                continue;
            }
            this.insertOutput(container, output, false);
        }
    }

    @Override
    public ItemStack assemble(IProcessingMachine container) {
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
                      long workTicks) {
            super(type, loc, inputs, outputs, workTicks);
        }
    }

    public abstract static class Builder<U extends ProcessingRecipe<U>, S extends Builder<U, S>>
            extends SmartRecipeBuilder<U, S> {
        protected final List<Supplier<Input>> inputs = new ArrayList<>();
        protected final List<Supplier<Output>> outputs = new ArrayList<>();
        protected long workTicks = 0;

        public Builder(Registrate registrate, RecipeTypeEntry<U, S> parent, ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        public S input(int port, Supplier<Either<Ingredient, FluidStack>> ingredient, int amount) {
            this.inputs.add(() -> new Input(port, ingredient.get(), amount));
            return self();
        }

        public S input(int port, Either<Ingredient, FluidStack> ingredient, int amount) {
            return this.input(port, () -> ingredient, amount);
        }

        public S inputItem(int port, Supplier<Item> item, int amount) {
            return this.input(port, () -> Either.left(Ingredient.of(item.get())), amount);
        }

        public S output(int port, Supplier<Either<ItemStack, FluidStack>> result, float rate) {
            this.outputs.add(() -> new Output(port, result.get(), rate));
            return self();
        }

        public S output(int port, Either<ItemStack, FluidStack> ingredient, float rate) {
            return this.output(port, () -> ingredient, rate);
        }

        public S outputItem(int port, Supplier<Item> item, int amount, float rate) {
            return this.output(port, () -> Either.left(new ItemStack(item.get(), amount)), rate);
        }

        public S outputItem(int port, Supplier<Item> item, int amount) {
            return this.outputItem(port, item, amount, 1.0f);
        }

        public S outputItem(int port, Item item, int amount) {
            return this.outputItem(port, () -> item, amount);
        }

        public S outputFluid(int port, Fluid fluid, int amount, float rate) {
            return this.output(port, () -> Either.right(new FluidStack(fluid, amount)), rate);
        }

        public S outputFluid(int port, Fluid fluid, int amount) {
            return this.outputFluid(port, fluid, amount, 1.0f);
        }

        public S workTicks(long workTicks) {
            this.workTicks = workTicks;
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
            return new Simple(this.parent, this.loc, this.getInputs(), this.getOutputs(), this.workTicks);
        }
    }

    protected static class Serializer<T extends ProcessingRecipe<T>, B extends Builder<T, B>>
            extends SmartRecipeSerializer<T, B> {
        protected Serializer(RecipeTypeEntry<T, B> type) {
            super(type);
        }

        protected <L, R> Either<L, R> ifThenElse(boolean cond, Supplier<L> left, Supplier<R> right) {
            return cond ? Either.left(left.get()) : Either.right(right.get());
        }

        protected <P> P parseFromJson(Decoder<P> codec, JsonElement je) {
            return codec.parse(JsonOps.INSTANCE, je).getOrThrow(false, $ -> {});
        }

        protected static <T> Function<T, JsonElement> encodeToJson(Encoder<T> codec) {
            return x -> codec.encodeStart(JsonOps.INSTANCE, x).getOrThrow(false, $ -> {});
        }

        protected B buildFromJson(ResourceLocation loc, JsonObject jo) {
            var builder = this.type.recipe(loc);
            Streams.stream(GsonHelper.getAsJsonArray(jo, "inputs"))
                    .map(JsonElement::getAsJsonObject)
                    .forEach(je -> builder.input(
                            GsonHelper.getAsInt(je, "port"),
                            ifThenElse(je.has("item"),
                                    () -> Ingredient.fromJson(GsonHelper.getAsJsonObject(je, "item")),
                                    () -> parseFromJson(FluidStack.CODEC, GsonHelper.getAsJsonObject(je, "fluid"))),
                            GsonHelper.getAsInt(je, "amount")));
            Streams.stream(GsonHelper.getAsJsonArray(jo, "outputs"))
                    .map(JsonElement::getAsJsonObject)
                    .forEach(je -> builder.output(
                            GsonHelper.getAsInt(je, "port"),
                            ifThenElse(je.has("item"),
                                    () -> parseFromJson(ItemStack.CODEC, GsonHelper.getAsJsonObject(je, "item")),
                                    () -> parseFromJson(FluidStack.CODEC, GsonHelper.getAsJsonObject(je, "fluid"))),
                            GsonHelper.getAsFloat(je, "rate", 1.0f)));
            return builder.workTicks(GsonHelper.getAsLong(jo, "workTicks"));
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
                        input.ingredient.mapBoth(Ingredient::toJson, encodeToJson(FluidStack.CODEC))
                                .ifLeft(je1 -> je.add("item", je1))
                                .ifRight(je1 -> je.add("fluid", je1));
                        je.addProperty("amount", input.amount);
                        return je;
                    }).forEach(inputs::add);
            var outputs = new JsonArray();
            recipe.outputs.stream()
                    .map(output -> {
                        var je = new JsonObject();
                        je.addProperty("port", output.port);
                        output.result.mapBoth(encodeToJson(ItemStack.CODEC), encodeToJson(FluidStack.CODEC))
                                .ifLeft(je1 -> je.add("item", je1))
                                .ifRight(je1 -> je.add("fluid", je1));
                        if (output.rate < 1.0f) {
                            je.addProperty("rate", output.rate);
                        }
                        return je;
                    }).forEach(outputs::add);
            jo.add("inputs", inputs);
            jo.add("outputs", outputs);
            jo.addProperty("workTicks", recipe.workTicks);
        }

        public B buildFromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
            var builder = this.type.recipe(loc);
            buf.readWithCount(buf1 -> builder.input(
                    buf1.readVarInt(),
                    buf1.readBoolean() ?
                            Either.left(Ingredient.fromNetwork(buf1)) :
                            Either.right(FluidStack.readFromPacket(buf1)),
                    buf1.readVarInt()));
            buf.readWithCount(buf1 -> builder.output(
                    buf1.readVarInt(),
                    buf1.readBoolean() ?
                            Either.left(buf1.readItem()) :
                            Either.right(buf1.readFluidStack()),
                    buf1.readFloat()));
            return builder.workTicks(buf.readVarLong());
        }

        @Override
        public T fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
            return this.buildFromNetwork(loc, buf).buildObject();
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, T recipe) {
            buf.writeCollection(recipe.inputs, (buf1, input) -> {
                buf1.writeVarInt(input.port);
                buf1.writeBoolean(input.ingredient.left().isPresent());
                input.ingredient.ifLeft(ingredient -> ingredient.toNetwork(buf1)).ifRight(buf1::writeFluidStack);
                buf1.writeVarInt(input.amount);
            });
            buf.writeCollection(recipe.outputs, (buf1, output) -> {
                buf1.writeVarInt(output.port);
                buf1.writeBoolean(output.result.left().isPresent());
                output.result.ifLeft(buf1::writeItem).ifRight(buf1::writeFluidStack);
                buf1.writeFloat(output.rate);
            });
            buf.writeVarLong(recipe.workTicks);
        }
    }

    public static final SmartRecipeSerializer.SimpleFactory<Simple, SimpleBuilder>
            SIMPLE_SERIALIZER = Serializer::new;

}
