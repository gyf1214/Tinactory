package org.shsts.tinactory.unit.recipe;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.INFO_CODEC;

class ProcessingInfoTest {
    @Test
    void shouldRoundTripIngredientInfoThroughInjectedCodec() {
        var info = new ProcessingInfo(2, new TestIngredient("ore", 3));

        var tag = CodecHelper.encodeTag(INFO_CODEC, info);
        var roundTrip = CodecHelper.parseTag(INFO_CODEC, tag);

        assertEquals(info, roundTrip);
        assertEquals(2, ((CompoundTag) tag).getInt("port"));
        assertEquals("ore:3", ((CompoundTag) ((CompoundTag) tag).get("ingredient")).getString("value"));
    }

    @Test
    void shouldRoundTripResultInfoThroughInjectedCodec() {
        var info = new ProcessingInfo(4, new TestResult("dust", 5));

        var tag = CodecHelper.encodeTag(INFO_CODEC, info);
        var roundTrip = CodecHelper.parseTag(INFO_CODEC, tag);

        assertEquals(info, roundTrip);
        assertEquals(4, ((CompoundTag) tag).getInt("port"));
        assertEquals("dust:5", ((CompoundTag) ((CompoundTag) tag).get("result")).getString("value"));
    }

    @Test
    void shouldRejectInvalidTagsWithoutIngredientOrResult() {
        var tag = new CompoundTag();
        tag.putInt("port", 1);

        assertThrows(RuntimeException.class, () -> CodecHelper.parseTag(INFO_CODEC, tag));
    }
}
