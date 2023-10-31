package org.shsts.tinactory.content.recipe;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.content.machine.ProcessingContainer;
import org.shsts.tinactory.core.SmartRecipe;
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
            var collection = container.getPort(input.port);
            // TODO: there is a problem here when two ingredients overlap
            if (!ItemHelper.consumeItemCollection(collection, input.ingredient, input.amount, true)) {
                return false;
            }
        }
        for (var output : this.outputs) {
            var collection = container.getPort(output.port);
            if (!collection.insertItem(output.object, true).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void consumeInputs(ProcessingContainer container) {
        for (var input : this.inputs) {
            var collection = container.getPort(input.port);
            // TODO: there is a problem here when two ingredients overlap
            ItemHelper.consumeItemCollection(collection, input.ingredient, input.amount, false);
        }
    }

    public void insertOutputs(ProcessingContainer container) {
        for (var output : this.outputs) {
            var collection = container.getPort(output.port);
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
        protected int workTicks = 0;

        public Builder(Registrate registrate, RecipeTypeEntry<U, S> parent, ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        public S input(int port, Supplier<Ingredient> ingredient, int amount) {
            this.inputs.add(() -> new Input(port, ingredient.get(), amount));
            return self();
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

        public S output(int port, Supplier<Item> item, int amount) {
            return this.output(port, () -> new ItemStack(item.get(), amount));
        }

        public S output(int port, Item item, int amount) {
            return this.output(port, () -> item, amount);
        }

        public S workTicks(int workTicks) {
            this.workTicks = workTicks;
            return self();
        }
    }

    public static class SimpleBuilder extends Builder<Simple, SimpleBuilder> {
        public SimpleBuilder(Registrate registrate, RecipeTypeEntry<Simple, SimpleBuilder> parent,
                             ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        @Override
        public Simple createObject() {
            var inputs = this.inputs.stream().map(Supplier::get).toList();
            var outputs = this.outputs.stream().map(Supplier::get).toList();
            return new Simple(this.parent, this.loc, inputs, outputs, this.workTicks);
        }
    }
}
