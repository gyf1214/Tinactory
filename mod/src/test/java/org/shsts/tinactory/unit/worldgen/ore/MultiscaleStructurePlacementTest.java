package org.shsts.tinactory.unit.worldgen.ore;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.worldgen.ore.MultiscaleStructurePlacement;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiscaleStructurePlacementTest {
    @Test
    void shouldPlaceShouldBeDeterministicForTheSameSeedAndCoordinates() {
        var placement = placement(0.75F);

        assertEquals(placement.shouldPlace(12345L, 3, 3), placement.shouldPlace(12345L, 3, 3));
        assertEquals(placement.shouldPlace(-987654321L, -19, 27), placement.shouldPlace(-987654321L, -19, 27));
    }

    @Test
    void shouldPlaceShouldApplyAllThreeScaledLevelsAndNegativeFloorGeometry() {
        var placement = placement(1F);

        assertTrue(placement.shouldPlace(12345L, 3, 3));
        assertTrue(placement.shouldPlace(12345L, 6, 6));
        assertTrue(placement.shouldPlace(12345L, 18, 13));
        assertTrue(placement.shouldPlace(12345L, -1, -2));
    }

    @Test
    void shouldPlaceShouldSelectAtMostOneCandidateInTheLevelZeroRegion() {
        var placement = placement(1F);
        var candidates = 0;
        for (var chunkX = 1; chunkX <= 4; chunkX++) {
            for (var chunkZ = 1; chunkZ <= 4; chunkZ++) {
                if (placement.shouldPlace(12345L, chunkX, chunkZ)) {
                    candidates++;
                }
            }
        }

        assertEquals(1, candidates);
    }

    @Test
    void shouldPlaceShouldRespectExactFrequencyBounds() {
        assertFalse(placement(0F).shouldPlace(12345L, 3, 3));
        assertTrue(placement(1F).shouldPlace(12345L, 3, 3));
    }

    @Test
    void additionalChunkRestrictionsShouldNotApplyVanillaFrequencyReduction() {
        assertTrue(placement(0.25F).applyAdditionalChunkRestrictions(0, 0, 12345L));
    }

    @Test
    void codecShouldRejectInvalidSpacingSeparationAndScale() {
        assertFalse(parse(placementJson(4, 4, 2)).result().isPresent());
        assertFalse(parse(placementJson(4, 1, 0)).result().isPresent());
        assertFalse(parse(placementJson(4, 1, -1)).result().isPresent());
    }

    private static MultiscaleStructurePlacement placement(float frequency) {
        return new MultiscaleStructurePlacement(
            Vec3i.ZERO,
            StructurePlacement.FrequencyReductionMethod.DEFAULT,
            frequency,
            12001,
            Optional.empty(),
            1, 1, 4, 1, 2, 3);
    }

    private static JsonObject placementJson(int spacing, int separation, int scale) {
        var json = new JsonObject();
        json.addProperty("frequency", 0.75F);
        json.addProperty("salt", 12001);
        json.addProperty("offset_x", 1);
        json.addProperty("offset_z", 1);
        json.addProperty("spacing", spacing);
        json.addProperty("separation", separation);
        json.addProperty("scale", scale);
        return json;
    }

    private static DataResult<MultiscaleStructurePlacement> parse(JsonObject json) {
        return MultiscaleStructurePlacement.CODEC.codec().parse(JsonOps.INSTANCE, json);
    }
}
