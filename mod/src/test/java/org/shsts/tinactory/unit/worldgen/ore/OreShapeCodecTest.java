package org.shsts.tinactory.unit.worldgen.ore;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.OreShapeDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreShapeInstance;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;
import org.shsts.tinactory.unit.fixture.OreShapeTestHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.OreShapeTestHelper.ELLIPSOID;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.createRegistry;

class OreShapeCodecTest {
    @Test
    void definitionCodecShouldDispatchToAFlatTypedPayload() {
        var value = new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(2, 5, 1, 4, 3, 7));
        var registryAccess = createRegistry(OreShapeTestHelper.SHAPES);
        var json = CodecHelper.encodeJson(registryAccess, OreVeinUtil.DEFINITION_CODEC.codec(), value)
            .getAsJsonObject();

        assertEquals(modLoc("ellipsoid").toString(), json.get("type").getAsString());
        assertEquals(2, json.get("min_radius_x").getAsInt());
        assertFalse(json.has("config"));
    }

    @Test
    void instanceCodecShouldRoundTripThroughJsonAndNbt() {
        var value = new OreShapeInstance<>(ELLIPSOID, new EllipsoidShape.Instance(4, 2, 6));
        var registryAccess = createRegistry(OreShapeTestHelper.SHAPES);
        var codec = OreVeinUtil.INSTANCE_CODEC.codec();
        var json = CodecHelper.encodeJson(registryAccess, codec, value);
        var tag = CodecHelper.encodeTag(registryAccess, codec, value);

        assertEquals(value, CodecHelper.parseJson(registryAccess, codec, json));
        assertEquals(value, CodecHelper.parseTag(registryAccess, codec, tag));
        assertEquals(modLoc("ellipsoid").toString(), json.getAsJsonObject().get("type").getAsString());
    }

    @Test
    void codecShouldRejectUnknownShapeIds() {
        var json = new JsonObject();
        json.addProperty("type", modLoc("missing").toString());
        json.addProperty("radius_x", 4);
        json.addProperty("radius_y", 2);
        json.addProperty("radius_z", 6);
        var registryAccess = createRegistry(OreShapeTestHelper.SHAPES);

        assertThrows(RuntimeException.class,
            () -> CodecHelper.parseJson(registryAccess, OreVeinUtil.INSTANCE_CODEC.codec(), json));
    }

    @Test
    void codecShouldRejectAnUnregisteredShapeOnEncode() {
        var value = new OreShapeDefinition<>(new EllipsoidShape(), new EllipsoidShape.Definition(1, 1, 1, 1, 1, 1));
        var registryAccess = createRegistry(OreShapeTestHelper.SHAPES);

        assertThrows(RuntimeException.class,
            () -> CodecHelper.encodeJson(registryAccess, OreVeinUtil.DEFINITION_CODEC.codec(), value));
        assertNotSame(ELLIPSOID, value.shape());
    }
}
