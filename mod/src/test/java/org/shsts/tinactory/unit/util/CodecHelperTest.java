package org.shsts.tinactory.unit.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodecHelperTest {
    @Test
    void jsonHelpersRoundTripSimpleObjects() {
        var json = CodecHelper.jsonFromStr("{\"value\":3}");
        var fromReader = CodecHelper.jsonFromReader(new StringReader("{\"name\":\"tinactory\"}"));

        assertEquals(3, json.get("value").getAsInt());
        assertEquals("tinactory", fromReader.get("name").getAsString());
        assertEquals("{\"value\":3}", CodecHelper.jsonToStr(json));
    }

    @Test
    void codecHelpersRoundTripJsonAndNbtValues() {
        var encodedJson = CodecHelper.encodeJson(Codec.INT, 17);
        var encodedTag = CodecHelper.encodeTag(Codec.INT, 23);

        assertEquals(17, CodecHelper.parseJson(Codec.INT, encodedJson));
        assertEquals(23, CodecHelper.parseTag(Codec.INT, encodedTag));
    }

    @Test
    void blockPosCodecRoundTripsCoordinates() {
        var pos = new BlockPos(4, -2, 19);
        var tag = CodecHelper.encodeBlockPos(pos);

        assertEquals(4, tag.getInt("x"));
        assertEquals(-2, tag.getInt("y"));
        assertEquals(19, tag.getInt("z"));
        assertEquals(pos, CodecHelper.parseBlockPos(tag));
    }

    @Test
    void componentCodecRoundTripsTextAndThrowsOnInvalidJson() {
        var component = Component.literal("core util");
        var encoded = CodecHelper.encodeComponent(component);

        assertEquals(component, CodecHelper.parseComponent(encoded));
        assertThrows(JsonParseException.class, () -> CodecHelper.parseComponent("{not valid json"));
    }

    @Test
    void listAndArrayHelpersDecodeElementsInOrder() {
        var encoded = CodecHelper.encodeList(List.of(1, 2, 3), IntTag::valueOf);
        var decoded = new ArrayList<Integer>();
        CodecHelper.parseList(encoded, tag -> ((IntTag) tag).getAsInt(), decoded::add);

        var jsonArray = new JsonArray();
        jsonArray.add(5);
        jsonArray.add(-7);
        jsonArray.add(11);

        assertEquals(List.of(1, 2, 3), decoded);
        assertArrayEquals(new int[]{5, -7, 11}, CodecHelper.parseIntArray(jsonArray));
    }
}
