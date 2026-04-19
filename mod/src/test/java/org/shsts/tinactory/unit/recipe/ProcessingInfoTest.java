package org.shsts.tinactory.unit.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestProcessingObject;
import org.shsts.tinactory.unit.fixture.TestResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertProcessingInfo(2, TestIngredient.class, "ore", 3, roundTrip);
        assertEquals(2, ((CompoundTag) tag).getInt("port"));
        assertEquals("ore:3", ((CompoundTag) ((CompoundTag) tag).get("ingredient")).getString("value"));
    }

    @Test
    void shouldRoundTripResultInfoThroughInjectedCodec() {
        var info = new ProcessingInfo(4, new TestResult("dust", 5));

        var tag = CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, info).getOrThrow(false, $ -> {});
        var roundTrip = CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).getOrThrow(false, $ -> {});

        assertProcessingInfo(4, TestResult.class, "dust", 5, roundTrip);
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

    private static void assertProcessingInfo(int port, Class<? extends TestProcessingObject> type,
        String key, int amount, ProcessingInfo info) {
        assertEquals(port, info.port());
        var object = (TestProcessingObject) info.object();
        assertTrue(type.isInstance(object));
        assertEquals(key, object.key());
        assertEquals(amount, object.amount());
    }
}
