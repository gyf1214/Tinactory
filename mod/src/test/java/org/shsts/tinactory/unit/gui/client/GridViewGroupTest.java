package org.shsts.tinactory.unit.gui.client;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.GridViewGroup;
import org.shsts.tinactory.core.gui.client.IViewNode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridViewGroupTest {
    @Test
    void shouldCalculateGridPagingAndPageButtonState() {
        var group = new GridViewGroup<RecordingNode>(10, 8, 2,
            Rect.corners(0, 0, 0, -21), index -> new RecordingNode());

        group.setItemCount(10);
        group.setRect(new Rect(0, 0, 35, 31));

        assertEquals(3, group.getColumnCount());
        assertEquals(1, group.getRowCount());
        assertEquals(2, group.getHorizontalSpacing());
        assertEquals(3, group.getSlotCount());
        assertEquals(new Rect(0, 0, 10, 8), group.getSlotRect(0));
        assertEquals(new Rect(12, 0, 10, 8), group.getSlotRect(1));
        assertEquals(new Rect(24, 0, 10, 8), group.getSlotRect(2));
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

    @Test
    void shouldOwnSlotChildrenCreatedDuringLayout() {
        var created = new ArrayList<RecordingNode>();
        var group = new GridViewGroup<RecordingNode>(10, 8, 2, Rect.ZERO, index -> {
            assertEquals(created.size(), index);
            var node = new RecordingNode();
            created.add(node);
            return node;
        });

        group.setActive(false);
        group.initView();
        group.setRect(new Rect(0, 0, 35, 18));

        assertEquals(6, group.getSlotCount());
        assertEquals(6, created.size());
        assertSame(created.get(2), group.getSlot(2));
        assertEquals(new Rect(24, 0, 10, 8), created.get(2).rect);
        assertEquals(new Rect(0, 10, 10, 8), created.get(3).rect);
        assertTrue(created.stream().allMatch(node -> node.initialized));
        assertTrue(created.stream().noneMatch(node -> node.active));

        group.setRect(new Rect(0, 0, 10, 8));

        assertEquals(1, group.getSlotCount());
        var children = new ArrayList<IViewNode>();
        group.forEachChild(children::add);
        assertEquals(List.of(created.get(0)), children);
    }

    private static class RecordingNode implements IViewNode {
        private Rect rect = Rect.ZERO;
        private boolean initialized = false;
        private boolean active = true;

        @Override
        public void initView() {
            initialized = true;
        }

        @Override
        public void setRect(Rect rect) {
            this.rect = rect;
        }

        @Override
        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
