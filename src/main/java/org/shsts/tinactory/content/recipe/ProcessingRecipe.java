package org.shsts.tinactory.content.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.content.machine.ProcessingContainer;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.core.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.SmartRecipeBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRecipe<S extends ProcessingRecipe<S>> extends SmartRecipe<ProcessingContainer, S> {
    protected record Input(int port, Ingredient ingredient, int amount) {}

    protected record WithPort<U>(int port, U object) {}

    protected final List<Input> inputs;
    protected final List<WithPort<ItemStack>> outputs;

    public final long workTicks;

    public ProcessingRecipe(RecipeTypeEntry<S, ?> type, ResourceLocation loc,
                            List<Input> inputs, List<WithPort<ItemStack>> outputs, long workTicks) {
        super(type, loc);
        this.inputs = inputs;
        this.outputs = outputs;
        this.workTicks = workTicks;
    }

    @Override
    public boolean matches(ProcessingContainer container, Level world) {
        for (var input : this.inputs) {
            var collection = container.getPort(input.port, true);
            // TODO: there is a problem here when two ingredients overlap
            if (!ItemHelper.consumeItemCollection(collection, input.ingredient, input.amount, true)) {
                return false;
            }
        }
        for (var output : this.outputs) {
            var collection = container.getPort(output.port, true);
            if (!collection.insertItem(output.object, true).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void consumeInputs(ProcessingContainer container) {
        for (var input : this.inputs) {
            var collection = container.getPort(input.port, true);
            // TODO: there is a problem here when two ingredients overlap
            ItemHelper.consumeItemCollection(collection, input.ingredient, input.amount, false);
        }
    }

    public void insertOutputs(ProcessingContainer container) {
        for (var output : this.outputs) {
            var collection = container.getPort(output.port, true);
            collection.insertItem(output.object, false);
        }
    }

    @Override
    public ItemStack assemble(ProcessingContainer container) {
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
                      List<Input> inputs, List<WithPort<ItemStack>> outputs,
                      long workTicks) {
            super(type, loc, inputs, outputs, workTicks);
        }
    }

    public abstract static class Builder<U extends ProcessingRecipe<U>, S extends Builder<U, S>>
            extends SmartRecipeBuilder<U, S> {
        protected final List<Supplier<Input>> inputs = new ArrayList<>();
        protected final List<Supplier<WithPort<ItemStack>>> outputs = new ArrayList<>();
        protected long workTicks = 0;

        public Builder(Registrate registrate, RecipeTypeEntry<U, S> parent, ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        public S input(int port, Supplier<Ingredient> ingredient, int amount) {
            this.inputs.add(() -> new Input(port, ingredient.get(), amount));
            return self();
        }

        public S input(int port, Ingredient ingredient, int amount) {
            return this.input(port, () -> ingredient, amount);
        }

        public S inputItem(int port, Supplier<Item> item, int amount) {
            return this.input(port, () -> Ingredient.of(item.get()), amount);
        }

        public S inputItem(int port, Item item, int amount) {
            return this.inputItem(port, () -> item, amount);
        }

        public S output(int port, Supplier<ItemStack> itemStack) {
            this.outputs.add(() -> new WithPort<>(port, itemStack.get()));
            return self();
        }

        public S output(int port, ItemStack itemStack) {
            return this.output(port, () -> itemStack);
        }

        public S output(int port, Supplier<Item> item, int amount) {
            return this.output(port, () -> new ItemStack(item.get(), amount));
        }

        public S output(int port, Item item, int amount) {
            return this.output(port, () -> item, amount);
        }

        public S workTicks(long workTicks) {
            this.workTicks = workTicks;
            return self();
        }

        protected List<Input> getInputs() {
            return this.inputs.stream().map(Supplier::get).toList();
        }

        protected List<WithPort<ItemStack>> getOutputs() {
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

        public B buildFromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context) {
            var builder = this.type.recipe(loc);
            Streams.stream(GsonHelper.getAsJsonArray(jo, "inputs"))
                    .map(JsonElement::getAsJsonObject)
                    .forEach(je -> builder.input(
                            GsonHelper.getAsInt(je, "port"),
                            Ingredient.fromJson(GsonHelper.getAsJsonObject(je, "ingredient")),
                            GsonHelper.getAsInt(je, "amount")));
            Streams.stream(GsonHelper.getAsJsonArray(jo, "outputs"))
                    .map(JsonElement::getAsJsonObject)
                    .forEach(je -> builder.output(
                            GsonHelper.getAsInt(je, "port"),
                            ItemStack.CODEC.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(je, "item"))
                                    .getOrThrow(false, $ -> {})));
            return builder.workTicks(GsonHelper.getAsLong(jo, "workTicks"));
        }

        @Override
        public T fromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context) {
            return this.buildFromJson(loc, jo, context).buildObject();
        }

        @Override
        public void toJson(JsonObject jo, T recipe) {
            var inputs = new JsonArray();
            recipe.inputs.stream()
                    .map(input -> {
                        var je = new JsonObject();
                        je.addProperty("port", input.port);
                        je.add("ingredient", input.ingredient.toJson());
                        je.addProperty("amount", input.amount);
                        return je;
                    }).forEach(inputs::add);
            var outputs = new JsonArray();
            recipe.outputs.stream()
                    .map(output -> {
                        var je = new JsonObject();
                        je.addProperty("port", output.port);
                        je.add("item", ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, output.object)
                                .getOrThrow(false, $ -> {}));
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
                    Ingredient.fromNetwork(buf1),
                    buf1.readVarInt()));
            buf.readWithCount(buf1 -> builder.output(
                    buf1.readVarInt(),
                    buf1.readItem()));
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
                input.ingredient.toNetwork(buf1);
                buf1.writeVarInt(input.amount);
            });
            buf.writeCollection(recipe.outputs, (buf1, output) -> {
                buf1.writeVarInt(output.port);
                buf1.writeItem(output.object);
            });
            buf.writeVarLong(recipe.workTicks);
        }
    }

    public static final SmartRecipeSerializer.SimpleFactory<Simple, SimpleBuilder>
            SIMPLE_SERIALIZER = Serializer::new;

}
