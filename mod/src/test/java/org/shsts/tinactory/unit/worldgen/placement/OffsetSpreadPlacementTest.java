package org.shsts.tinactory.unit.worldgen.placement;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.worldgen.placement.OffsetSpreadPlacement;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OffsetSpreadPlacementTest {
    @Test
    void potentialChunkShouldTranslateVanillaRandomSpreadByTheConfiguredOffset() {
        var placement = placement(RandomSpreadType.LINEAR, 0.75F, 3, -2);
        var vanilla = vanilla(RandomSpreadType.LINEAR, 0.75F);
        var seed = 12345L;
        var chunkX = -19;
        var chunkZ = 27;

        var expected = vanilla.getPotentialStructureChunk(seed, chunkX - 3, chunkZ + 2);
        var actual = placement.getPotentialStructureChunk(seed, chunkX, chunkZ);

        assertEquals(new ChunkPos(expected.x + 3, expected.z - 2), actual);
    }

    @Test
    void potentialChunkShouldDelegateBothRandomSpreadTypesDeterministically() {
        for (var spreadType : RandomSpreadType.values()) {
            var placement = placement(spreadType, 1F, 1, 1);
            var first = placement.getPotentialStructureChunk(-987654321L, -19, 27);
            var second = placement.getPotentialStructureChunk(-987654321L, -19, 27);

            assertEquals(first, second);
        }
    }

    @Test
    void frequencyRestrictionShouldUseVanillaImplementation() {
        var placement = placement(RandomSpreadType.LINEAR, 0.25F, 1, 1);
        var vanilla = vanilla(RandomSpreadType.LINEAR, 0.25F);

        assertEquals(vanilla.applyAdditionalChunkRestrictions(-4, 7, 12345L),
            placement.applyAdditionalChunkRestrictions(-4, 7, 12345L));
    }

    @Test
    void codecShouldRoundTripInheritedFieldsAndOffsets() {
        var json = new JsonObject();
        json.addProperty("frequency", 0.75F);
        json.addProperty("salt", 12001);
        json.addProperty("spacing", 8);
        json.addProperty("separation", 1);
        json.addProperty("spread_type", "triangular");
        json.addProperty("offset_x", -3);
        json.addProperty("offset_z", 5);

        var decoded = OffsetSpreadPlacement.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
        var vanilla = vanilla(RandomSpreadType.TRIANGULAR, 0.75F, 8, 1);
        var expected = vanilla.getPotentialStructureChunk(12345L, 10 + 3, 11 - 5);
        var actual = decoded.getPotentialStructureChunk(12345L, 10, 11);

        assertEquals(new ChunkPos(expected.x - 3, expected.z + 5), actual);
        assertEquals(8, decoded.spacing());
        assertEquals(1, decoded.separation());
        assertEquals(RandomSpreadType.TRIANGULAR, decoded.spreadType());
        assertEquals(-3, decoded.offsetX());
        assertEquals(5, decoded.offsetZ());
    }

    @Test
    void codecShouldRejectSpacingNotLargerThanSeparation() {
        var json = new JsonObject();
        json.addProperty("salt", 12001);
        json.addProperty("spacing", 4);
        json.addProperty("separation", 4);
        json.addProperty("offset_x", 1);
        json.addProperty("offset_z", 1);

        assertFalse(OffsetSpreadPlacement.CODEC.codec().parse(JsonOps.INSTANCE, json).result().isPresent());
    }

    private static OffsetSpreadPlacement placement(RandomSpreadType spreadType, float frequency,
        int offsetX, int offsetZ) {
        return new OffsetSpreadPlacement(
            Vec3i.ZERO,
            StructurePlacement.FrequencyReductionMethod.DEFAULT,
            frequency,
            12001,
            Optional.empty(),
            4,
            1,
            spreadType,
            offsetX,
            offsetZ);
    }

    private static RandomSpreadStructurePlacement vanilla(RandomSpreadType spreadType, float frequency) {
        return vanilla(spreadType, frequency, 4, 1);
    }

    private static RandomSpreadStructurePlacement vanilla(RandomSpreadType spreadType, float frequency,
        int spacing, int separation) {
        return new RandomSpreadStructurePlacement(
            Vec3i.ZERO,
            StructurePlacement.FrequencyReductionMethod.DEFAULT,
            frequency,
            12001,
            Optional.empty(),
            spacing,
            separation,
            spreadType);
    }
}
