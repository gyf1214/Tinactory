package org.shsts.tinactory.unit.recipe;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.TEST_REGISTRY;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.INFO_CODEC;

class ProcessingInfoTest {
    @Test
    void shouldRoundTripIngredientInfoThroughInjectedCodec() {
        var info = new ProcessingInfo(2, new TestIngredient("ore", 3));

        var tag = CodecHelper.encodeTag(TEST_REGISTRY, INFO_CODEC, info);
        var roundTrip = CodecHelper.parseTag(TEST_REGISTRY, INFO_CODEC, tag);
        var tag1 = (CompoundTag) tag;
        var tag2 = tag1.getCompound("object");

        assertEquals(info, roundTrip);
        assertEquals(2, tag1.getInt("port"));
        assertEquals("test_ingredient", tag2.getString("type"));
        assertEquals("ore", tag2.getString("key"));
        assertEquals(3, tag2.getInt("amount"));
    }

    @Test
    void shouldRoundTripResultInfoThroughInjectedCodec() {
        var info = new ProcessingInfo(4, new TestResult("dust", 5));

        var tag = CodecHelper.encodeTag(TEST_REGISTRY, INFO_CODEC, info);
        var roundTrip = CodecHelper.parseTag(TEST_REGISTRY, INFO_CODEC, tag);
        var tag1 = (CompoundTag) tag;
        var tag2 = tag1.getCompound("object");

        assertEquals(info, roundTrip);
        assertEquals(4, tag1.getInt("port"));
        assertEquals("test_result", tag2.getString("type"));
        assertEquals("dust", tag2.getString("key"));
        assertEquals(5, tag2.getInt("amount"));
    }

    @Test
    void shouldRejectInvalidTagsWithoutIngredientOrResult() {
        var tag = new CompoundTag();
        tag.putInt("port", 1);

        assertThrows(RuntimeException.class, () -> CodecHelper.parseTag(TEST_REGISTRY, INFO_CODEC, tag));
    }
}
