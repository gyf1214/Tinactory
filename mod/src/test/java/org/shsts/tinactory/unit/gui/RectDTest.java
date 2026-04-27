package org.shsts.tinactory.unit.gui;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.RectD;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RectDTest {
    @Test
    void shouldBuildFromCornersAndReportEndpoints() {
        var rect = RectD.corners(0.25d, 1.5d, 3.75d, 8.5d);

        assertEquals(0.25d, rect.x());
        assertEquals(1.5d, rect.y());
        assertEquals(3.5d, rect.width());
        assertEquals(7d, rect.height());
        assertEquals(3.75d, rect.endX());
        assertEquals(8.5d, rect.endY());
    }

    @Test
    void shouldExposeZeroAndFullConstants() {
        assertEquals(new RectD(0d, 0d, 0d, 0d), RectD.ZERO);
        assertEquals(new RectD(0d, 0d, 1d, 1d), RectD.FULL);
        assertEquals(1d, RectD.FULL.endX());
        assertEquals(1d, RectD.FULL.endY());
    }
}
