package org.shsts.tinactory.unit.gui.client;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.GridViewGroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridViewGroupTest {
    @Test
    void shouldCalculateGridPagingAndPageButtonState() {
        var group = new GridViewGroup(10, 8, 2, 21, new RectD(0.5d, 1d, 0d, 0d),
            new Rect(0, -18, 12, 18), 12);

        group.setItemCount(10);
        group.setRect(new Rect(0, 0, 35, 31));

        assertEquals(3, group.getColumnCount());
        assertEquals(1, group.getRowCount());
        assertEquals(2, group.getHorizontalSpacing());
        assertEquals(3, group.getButtonCount());
        assertEquals(new Rect(0, 0, 10, 8), group.getButtonRect(0));
        assertEquals(new Rect(12, 0, 10, 8), group.getButtonRect(1));
        assertEquals(new Rect(24, 0, 10, 8), group.getButtonRect(2));
        assertEquals(RectD.corners(0.5d, 1d, 0.5d, 1d), group.getPageButtonAnchor());
        assertEquals(new Rect(-24, -18, 12, 18), group.getLeftPageButtonOffset());
        assertEquals(new Rect(12, -18, 12, 18), group.getRightPageButtonOffset());
        assertFalse(group.isLeftPageEnabled());
        assertTrue(group.isRightPageEnabled());
        assertEquals(0, group.getVisibleIndex(0));
        assertEquals(1, group.getVisibleIndex(1));
        assertEquals(2, group.getVisibleIndex(2));

        group.setPage(10);

        assertEquals(3, group.getPage());
        assertTrue(group.isLeftPageEnabled());
        assertFalse(group.isRightPageEnabled());
        assertEquals(9, group.getVisibleIndex(0));
        assertEquals(-1, group.getVisibleIndex(1));
        assertEquals(-1, group.getVisibleIndex(2));
    }
}
