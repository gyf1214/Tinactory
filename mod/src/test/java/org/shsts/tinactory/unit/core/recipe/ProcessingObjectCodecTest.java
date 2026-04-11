package org.shsts.tinactory.unit.core.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.unit.fixture.TestStack;
import org.shsts.tinycorelib.api.recipe.IRecipeDataConsumer;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ProcessingObjectCodecTest {
    @Test
    void shouldRoundTripProcessingRecipeWithInjectedCodecs() {
        var type = new TestRecipeType<>("test_processing_recipe", InjectedRecipe.Builder::new);
        var loc = new ResourceLocation("tinactory", "codec_processing_recipe");
        var serializer = new ProcessingRecipe.Serializer<InjectedRecipe, InjectedRecipe.Builder>(
            TEST_INGREDIENT_CODEC, TEST_RESULT_CODEC);
        var recipe = type.getBuilder(loc)
            .input(0, new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
                TestStack.item("ore", 2), TestStack.ADAPTER))
            .output(1, new StackResult<>("test_stack_result", PortType.ITEM, 1d,
                TestStack.item("ingot", 3), TestStack.ADAPTER))
            .workTicks(40)
            .voltage(120)
            .power(16)
            .buildObject();

        var json = new JsonObject();
        serializer.toJson(json, recipe);
        var roundTrip = serializer.fromJson(type, loc, json, ICondition.IContext.EMPTY);

        assertEquals(recipe.inputs, roundTrip.inputs);
        assertEquals(recipe.outputs, roundTrip.outputs);
        assertEquals(recipe.workTicks, roundTrip.workTicks);
        assertEquals(recipe.voltage, roundTrip.voltage);
        assertEquals(recipe.power, roundTrip.power);
    }

    @Test
    void shouldRoundTripResearchRecipeWithInjectedIngredientCodec() {
        var type = new TestRecipeType<ResearchRecipe.Builder>("test_research_recipe", ResearchRecipe.Builder::new);
        var loc = new ResourceLocation("tinactory", "codec_research_recipe");
        var serializer = new ResearchRecipe.Serializer(TEST_INGREDIENT_CODEC);
        var recipe = type.getBuilder(loc)
            .input(new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
                TestStack.item("scan", 1), TestStack.ADAPTER))
            .target(new ResourceLocation("tinactory", "research_target"))
            .progress(7)
            .workTicks(50)
            .voltage(32)
            .power(4)
            .buildObject();

        var json = new JsonObject();
        serializer.toJson(json, recipe);
        var roundTrip = serializer.fromJson(type, loc, json, ICondition.IContext.EMPTY);

        assertEquals(recipe.inputs, roundTrip.inputs);
        assertEquals(recipe.target, roundTrip.target);
        assertEquals(recipe.progress, roundTrip.progress);
    }

    @Test
    void shouldRoundTripMarkerRecipeWithInjectedIngredientCodec() {
        var type = new TestRecipeType<MarkerRecipe.Builder>("test_marker_recipe", TestMarkerBuilder::new);
        var loc = new ResourceLocation("tinactory", "codec_marker_recipe");
        var serializer = new MarkerRecipe.Serializer(TEST_INGREDIENT_CODEC, TEST_RESULT_CODEC);
        var recipe = type.getBuilder(loc)
            .baseType(new ResourceLocation("minecraft", "smelting"))
            .display(new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
                TestStack.item("display", 1), TestStack.ADAPTER))
            .output(2, new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
                TestStack.item("marker", 3), TestStack.ADAPTER))
            .buildObject();

        var json = new JsonObject();
        serializer.toJson(json, recipe);
        var roundTrip = serializer.fromJson(type, loc, json, ICondition.IContext.EMPTY);

        assertEquals(recipe.markerOutputs, roundTrip.markerOutputs);
        assertInstanceOf(StackIngredient.class, roundTrip.displayIngredient().orElseThrow());
    }

    private static final Codec<IProcessingIngredient> TEST_INGREDIENT_CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, name -> {
            if ("test_stack_ingredient".equals(name)) {
                return StackIngredient.codec(
                    "test_stack_ingredient", PortType.ITEM, TestStack.CODEC, TestStack.ADAPTER);
            }
            throw new IllegalArgumentException("Unknown ingredient codec: " + name);
        });

    private static final Codec<IProcessingResult> TEST_RESULT_CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, name -> {
            if ("test_stack_result".equals(name)) {
                return StackResult.codec("test_stack_result", PortType.ITEM, TestStack.CODEC, TestStack.ADAPTER);
            }
            throw new IllegalArgumentException("Unknown result codec: " + name);
        });

    private static final class InjectedRecipe extends ProcessingRecipe {
        private InjectedRecipe(Builder builder) {
            super(builder);
        }

        private static final class Builder extends BuilderBase<InjectedRecipe, Builder> {
            private Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
                super(parent, loc);
            }

            @Override
            protected InjectedRecipe createObject() {
                return new InjectedRecipe(this);
            }
        }
    }

    private static final class TestMarkerBuilder extends MarkerRecipe.Builder {
        @SuppressWarnings("unchecked")
        private TestMarkerBuilder(IRecipeType<MarkerRecipe.Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        @Override
        public RecipeType<?> getBaseType() {
            return null;
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
            return Object.class;
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
        public RecipeType<?> get() {
            throw new UnsupportedOperationException();
        }
    }
}
