package org.shsts.tinactory.unit.util;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.MathUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilTest {
    @Test
    void compareAndClampRespectThresholdsAndBounds() {
        assertEquals(0, MathUtil.compare(1e-7d));
        assertEquals(1, MathUtil.compare(0.2d));
        assertEquals(-1, MathUtil.compare(-0.2d));
        assertEquals(0, MathUtil.compare(0.3d, 0.5d));
        assertEquals(10d, MathUtil.clamp(11d, 0d, 10d));
        assertEquals(2, MathUtil.clamp(1, 2, 5));
        assertEquals(7L, MathUtil.clamp(9L, 0L, 7L));
    }

    @Test
    void safePowReturnsZeroForNonPositiveValuesAndPowerOtherwise() {
        assertEquals(0d, MathUtil.safePow(0d, 3d));
        assertEquals(0d, MathUtil.safePow(1e-7d, 2d));
        assertEquals(9d, MathUtil.safePow(3d, 2d));
    }

    @Test
    void toSignalClampsToRedstoneRange() {
        assertEquals(0, MathUtil.toSignal(-1d));
        assertEquals(0, MathUtil.toSignal(0d));
        assertEquals(1, MathUtil.toSignal(0.01d));
        assertEquals(8, MathUtil.toSignal(0.5d));
        assertEquals(15, MathUtil.toSignal(1d));
        assertEquals(15, MathUtil.toSignal(2d));
    }

    @Test
    void sampleBinomialHandlesDirectPoissonNormalAndHighProbabilityBranchesDeterministically() {
        assertEquals(3, MathUtil.sampleBinomial(5, 0.5d, RandomSource.create(1L)));
        assertEquals(2, MathUtil.sampleBinomial(100, 0.01d, RandomSource.create(2L)));
        assertEquals(39, MathUtil.sampleBinomial(50, 0.8d, RandomSource.create(3L)));
        assertEquals(98, MathUtil.sampleBinomial(100, 0.95d, RandomSource.create(4L)));
        assertEquals(99, MathUtil.sampleBinomial(100, 0.99d, RandomSource.create(5L)));
    }
}
