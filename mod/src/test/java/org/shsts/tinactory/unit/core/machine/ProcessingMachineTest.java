package org.shsts.tinactory.unit.core.machine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.machine.ProcessingInfo;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.recipe.IRecipeDataConsumer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessingMachineTest {
    @Test
    void shouldUseScaledPreviewForGenericResultsWhenBuildingOutputInfo() {
        var recipe = new TestRecipe.Builder(new ResourceLocation("tinactory", "test_recipe"))
            .output(2, new TestResult("dust", 2))
            .workTicks(20)
            .power(8)
            .buildObject();
        var info = new ArrayList<ProcessingInfo>();

        new TestProcessingMachine().addOutputInfoForTest(recipe, 3, info::add);

        assertEquals(List.of(new ProcessingInfo(2, new TestResult("dust", 6))), info);
    }

    private static final class TestProcessingMachine extends ProcessingMachine<TestRecipe> {
        private TestProcessingMachine() {
            super(new TestRecipeType<>("test_machine", (type, loc) -> new TestRecipe.Builder(loc)));
        }

        private void addOutputInfoForTest(TestRecipe recipe, int parallel,
            java.util.function.Consumer<ProcessingInfo> info) {
            addOutputInfo(recipe, parallel, info);
        }
    }

    private static final class TestRecipe extends ProcessingRecipe {
        private TestRecipe(Builder builder) {
            super(builder);
        }

        private static final class Builder extends BuilderBase<TestRecipe, Builder> {
            private Builder(ResourceLocation loc) {
                super(null, loc);
            }

            @Override
            protected TestRecipe createObject() {
                return new TestRecipe(this);
            }
        }
    }

    private record TestResult(String name, int amount) implements IProcessingResult {
        @Override
        public String codecName() {
            return "test_result";
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public Predicate<?> filter() {
            return (Predicate<TestResult>) other -> Objects.equals(name, other.name);
        }

        @Override
        public Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, Random random, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IProcessingResult scaledPreview(int parallel) {
            return new TestResult(name, amount * parallel);
        }
    }

    private static final class TestRecipeType<B extends IRecipeBuilderBase<?>> implements IRecipeType<B> {
        private final ResourceLocation loc;
        private final BiFunction<IRecipeType<B>, ResourceLocation, B> builderFactory;

        private TestRecipeType(String path, BiFunction<IRecipeType<B>, ResourceLocation, B> builderFactory) {
            this.loc = new ResourceLocation("tinactory", path);
            this.builderFactory = builderFactory;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<?> recipeClass() {
            return TestRecipe.class;
        }

        @Override
        public B getBuilder(ResourceLocation loc) {
            return builderFactory.apply(this, loc);
        }

        @Override
        public B recipe(IRecipeDataConsumer consumer, ResourceLocation loc) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResourceLocation loc() {
            return loc;
        }

        @Override
        public net.minecraft.world.item.crafting.RecipeType<?> get() {
            throw new UnsupportedOperationException();
        }
    }
}
