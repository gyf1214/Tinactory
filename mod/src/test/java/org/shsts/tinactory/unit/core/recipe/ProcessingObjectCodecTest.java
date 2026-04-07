package org.shsts.tinactory.unit.core.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeDataConsumer;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ProcessingObjectCodecTest {
    @Test
    void shouldRoundTripProcessingRecipeWithInjectedCodecs() {
        var type = new TestRecipeType<>("test_processing_recipe", InjectedRecipe.Builder::new);
        var loc = new ResourceLocation("tinactory", "codec_processing_recipe");
        var serializer = new InjectedRecipe.Serializer();
        var recipe = type.getBuilder(loc)
            .input(0, new FakeIngredient("ore", 2))
            .output(1, new FakeResult("ingot", 3))
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
        var serializer = new InjectedResearchSerializer();
        var recipe = type.getBuilder(loc)
            .input(new FakeIngredient("scan", 1))
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
        var serializer = new InjectedMarkerSerializer();
        var recipe = type.getBuilder(loc)
            .baseType(new ResourceLocation("minecraft", "smelting"))
            .display(new FakeIngredient("display", 1))
            .output(2, new FakeIngredient("marker", 3))
            .buildObject();

        var json = new JsonObject();
        serializer.toJson(json, recipe);
        var roundTrip = serializer.fromJson(type, loc, json, ICondition.IContext.EMPTY);

        assertEquals(recipe.markerOutputs, roundTrip.markerOutputs);
        assertInstanceOf(FakeIngredient.class, roundTrip.displayIngredient().orElseThrow());
    }

    private record FakeIngredient(String key, int amount) implements IProcessingIngredient {
        private static final String CODEC_NAME = "fake_ingredient";
        private static final Codec<FakeIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(FakeIngredient::key),
            Codec.INT.fieldOf("amount").forGetter(FakeIngredient::amount)
        ).apply(instance, FakeIngredient::new));

        @Override
        public String codecName() {
            return CODEC_NAME;
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
            return Optional.empty();
        }
    }

    private record FakeResult(String key, int amount) implements IProcessingResult {
        private static final String CODEC_NAME = "fake_result";
        private static final Codec<FakeResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(FakeResult::key),
            Codec.INT.fieldOf("amount").forGetter(FakeResult::amount)
        ).apply(instance, FakeResult::new));

        @Override
        public String codecName() {
            return CODEC_NAME;
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, Random random,
            boolean simulate) {
            return Optional.empty();
        }
    }

    private static final Codec<IProcessingIngredient> TEST_INGREDIENT_CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, name -> {
            if (FakeIngredient.CODEC_NAME.equals(name)) {
                return FakeIngredient.CODEC;
            }
            throw new IllegalArgumentException("Unknown ingredient codec: " + name);
        });

    private static final Codec<IProcessingResult> TEST_RESULT_CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, name -> {
            if (FakeResult.CODEC_NAME.equals(name)) {
                return FakeResult.CODEC;
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

        private static final class Serializer extends ProcessingRecipe.Serializer<InjectedRecipe, Builder> {
            @Override
            protected Codec<IProcessingIngredient> ingredientCodec() {
                return TEST_INGREDIENT_CODEC;
            }

            @Override
            protected Codec<IProcessingResult> resultCodec() {
                return TEST_RESULT_CODEC;
            }
        }
    }

    private static final class InjectedResearchSerializer extends ResearchRecipe.Serializer {
        @Override
        protected Codec<IProcessingIngredient> ingredientCodec() {
            return TEST_INGREDIENT_CODEC;
        }
    }

    private static final class InjectedMarkerSerializer extends MarkerRecipe.Serializer {
        @Override
        protected Codec<IProcessingIngredient> ingredientCodec() {
            return TEST_INGREDIENT_CODEC;
        }

        @Override
        protected Codec<IProcessingResult> resultCodec() {
            return TEST_RESULT_CODEC;
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
