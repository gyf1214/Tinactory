package org.shsts.tinactory.unit.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.worldgen.ore.IOreShape;
import org.shsts.tinactory.core.worldgen.ore.OreShapeDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreShapeInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OreShapeContractTest {
    @Test
    void typedWrappersRetainShapeAndPayload() {
        var shape = new TestShape();
        var definition = new OreShapeDefinition<>(shape, "definition");
        var instance = new OreShapeInstance<>(shape, 42);

        assertEquals(shape, definition.shape());
        assertEquals("definition", definition.definition());
        assertEquals(shape, instance.shape());
        assertEquals(42, instance.instance());
    }

    private static final class TestShape implements IOreShape<String, Integer> {
        @Override
        public MapCodec<String> definitionCodec() {
            return Codec.STRING.fieldOf("definition");
        }

        @Override
        public MapCodec<Integer> instanceCodec() {
            return Codec.INT.fieldOf("instance");
        }

        @Override
        public Integer sample(String definition, long veinSeed) {
            return definition.length() + (int) veinSeed;
        }

        @Override
        public BoundingBox bounds(BlockPos center, Integer instance) {
            return new BoundingBox(center);
        }

        @Override
        public double fillFactor(long veinSeed, BlockPos center, BlockPos position, Integer instance) {
            return instance;
        }
    }
}
