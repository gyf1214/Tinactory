package org.shsts.tinactory.unit.worldgen.ore;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.OreShapeDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreShapeInstance;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;
import org.shsts.tinactory.unit.fixture.TestCodecHelper;
import org.shsts.tinactory.unit.fixture.TestOreHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestOreHelper.ELLIPSOID;

class OreShapeCodecTest {
    private static final RegistryAccess REGISTRY = TestCodecHelper.createRegistry(TestOreHelper.SHAPES);

    @Test
    void definitionCodecShouldDispatchToAFlatTypedPayload() {
        var value = new OreShapeDefinition<>(ELLIPSOID, new EllipsoidShape.Definition(100d, 200d, 0.6d, 2d, 5d));
        var json = CodecHelper.encodeJson(REGISTRY, OreVeinUtil.DEFINITION_CODEC.codec(), value)
            .getAsJsonObject();

        assertEquals(modLoc("ellipsoid").toString(), json.get("type").getAsString());
        assertEquals(100d, json.get("min_area").getAsDouble());
        assertEquals(200d, json.get("max_area").getAsDouble());
        assertEquals(0.6d, json.get("max_eccentric").getAsDouble());
        assertFalse(json.has("min_radius_x"));
        assertFalse(json.has("config"));
    }

    @Test
    void instanceCodecShouldRoundTripThroughJsonAndNbt() {
        var value = new OreShapeInstance<>(ELLIPSOID, new EllipsoidShape.Instance(6, 2, 4, 1.25d));
        var codec = OreVeinUtil.INSTANCE_CODEC.codec();
        var json = CodecHelper.encodeJson(REGISTRY, codec, value);
        var tag = CodecHelper.encodeTag(REGISTRY, codec, value);

        assertEquals(value, CodecHelper.parseJson(REGISTRY, codec, json));
        assertEquals(value, CodecHelper.parseTag(REGISTRY, codec, tag));
        assertEquals(modLoc("ellipsoid").toString(), json.getAsJsonObject().get("type").getAsString());
        assertEquals(6d, json.getAsJsonObject().get("radius_long").getAsDouble());
        assertEquals(4d, json.getAsJsonObject().get("radius_short").getAsDouble());
        assertEquals(1.25d, json.getAsJsonObject().get("angle").getAsDouble());
        assertFalse(json.getAsJsonObject().has("radius_x"));
        assertFalse(json.getAsJsonObject().has("radius_z"));
    }

    @Test
    void codecShouldRejectUnknownShapeIds() {
        var json = new JsonObject();
        json.addProperty("type", modLoc("missing").toString());
        json.addProperty("radius_long", 4);
        json.addProperty("radius_y", 2);
        json.addProperty("radius_short", 2);
        json.addProperty("angle", 0);

        assertThrows(RuntimeException.class,
            () -> CodecHelper.parseJson(REGISTRY, OreVeinUtil.INSTANCE_CODEC.codec(), json));
    }

    @Test
    void codecShouldRejectLegacyEllipsoidInstanceFields() {
        var json = new JsonObject();
        json.addProperty("type", modLoc("ellipsoid").toString());
        json.addProperty("radius_x", 4);
        json.addProperty("radius_y", 2);
        json.addProperty("radius_z", 6);

        assertThrows(RuntimeException.class,
            () -> CodecHelper.parseJson(REGISTRY, OreVeinUtil.INSTANCE_CODEC.codec(), json));
    }
}
