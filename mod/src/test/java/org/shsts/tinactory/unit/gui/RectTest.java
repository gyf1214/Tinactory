package org.shsts.tinactory.unit.gui;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.Rect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RectTest {
    @Test
    void shouldBuildFromCornersAndReportEndpoints() {
        var rect = Rect.corners(3, 5, 13, 19);

        assertEquals(3, rect.x());
        assertEquals(5, rect.y());
        assertEquals(10, rect.width());
        assertEquals(14, rect.height());
        assertEquals(13, rect.endX());
        assertEquals(19, rect.endY());
    }

    @Test
    void shouldOffsetResizeAndEnlargeWithoutMutatingOriginalRect() {
        var rect = new Rect(2, 4, 6, 8);

        assertEquals(new Rect(5, 2, 6, 8), rect.offset(3, -2));
        assertEquals(new Rect(3, 6, 10, 12), rect.offsetLike(new Rect(1, 2, 10, 12)));
        assertEquals(new Rect(2, 4, 9, 11), rect.resize(9, 11));
        assertEquals(new Rect(2, 4, 11, 13), rect.enlarge(5, 5));
        assertEquals(new Rect(2, 4, 6, 8), rect);
    }

    @Test
    void shouldProjectRelativeCoordinatesInsideRect() {
        var rect = new Rect(10, 20, 9, 11);

        assertEquals(10, rect.inX(0d));
        assertEquals(14, rect.inX(0.5d));
        assertEquals(19, rect.inX(1d));
        assertEquals(20, rect.inY(0d));
        assertEquals(25, rect.inY(0.5d));
        assertEquals(31, rect.inY(1d));
    }

    @Test
    void shouldUseInclusiveStartAndExclusiveEndForContainment() {
        var rect = new Rect(10, 20, 5, 7);

        assertTrue(rect.in(10d, 20d));
        assertTrue(rect.in(14.999d, 26.999d));
        assertFalse(rect.in(9.999d, 20d));
        assertFalse(rect.in(10d, 19.999d));
        assertFalse(rect.in(15d, 20d));
        assertFalse(rect.in(10d, 27d));
    }
}
