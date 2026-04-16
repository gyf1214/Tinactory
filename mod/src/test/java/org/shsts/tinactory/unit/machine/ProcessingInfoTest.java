package org.shsts.tinactory.unit.machine;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.machine.ProcessingInfo;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessingInfoTest {
    private static final Codec<IProcessingIngredient> INGREDIENT_CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, name -> {
            if ("test_ingredient".equals(name)) {
                return TestIngredient.CODEC;
            }
            throw new IllegalArgumentException("Unknown ingredient codec: " + name);
        });
    private static final Codec<IProcessingResult> RESULT_CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, name -> {
            if ("test_result".equals(name)) {
                return TestResult.CODEC;
            }
            throw new IllegalArgumentException("Unknown result codec: " + name);
        });
    private static final Codec<ProcessingInfo> CODEC = ProcessingInfo.codec(INGREDIENT_CODEC, RESULT_CODEC);

    @Test
    void shouldRoundTripIngredientInfoThroughInjectedCodec() {
        var info = new ProcessingInfo(2, new TestIngredient("ore", 3));

        var tag = CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, info).getOrThrow(false, $ -> {});
        var roundTrip = CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).getOrThrow(false, $ -> {});

        assertEquals(info, roundTrip);
        assertEquals(2, ((CompoundTag) tag).getInt("port"));
        assertEquals("ore:3", ((CompoundTag) ((CompoundTag) tag).get("ingredient")).getString("value"));
    }

    @Test
    void shouldRoundTripResultInfoThroughInjectedCodec() {
        var info = new ProcessingInfo(4, new TestResult("dust", 5));

        var tag = CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, info).getOrThrow(false, $ -> {});
        var roundTrip = CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).getOrThrow(false, $ -> {});

        assertEquals(info, roundTrip);
        assertEquals(4, ((CompoundTag) tag).getInt("port"));
        assertEquals("dust:5", ((CompoundTag) ((CompoundTag) tag).get("result")).getString("value"));
    }

    @Test
    void shouldRejectInvalidTagsWithoutIngredientOrResult() {
        var tag = new CompoundTag();
        tag.putInt("port", 1);

        assertThrows(RuntimeException.class, () -> CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag)
            .getOrThrow(false, $ -> {}));
    }

    private record TestIngredient(String name, int amount) implements IProcessingIngredient {
        private static final Codec<TestIngredient> CODEC = Codec.STRING.xmap(
            value -> {
                var parts = value.split(":");
                return new TestIngredient(parts[0], Integer.parseInt(parts[1]));
            },
            value -> value.name + ":" + value.amount);

        @Override
        public String codecName() {
            return "test_ingredient";
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
        public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
            throw new UnsupportedOperationException();
        }
    }

    private record TestResult(String name, int amount) implements IProcessingResult {
        private static final Codec<TestResult> CODEC = Codec.STRING.xmap(
            value -> {
                var parts = value.split(":");
                return new TestResult(parts[0], Integer.parseInt(parts[1]));
            },
            value -> value.name + ":" + value.amount);

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
}
