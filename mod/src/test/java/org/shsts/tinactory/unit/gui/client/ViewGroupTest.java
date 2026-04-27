package org.shsts.tinactory.unit.gui.client;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.IViewNode;
import org.shsts.tinactory.core.gui.client.ViewGroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewGroupTest {
    @Test
    void shouldResolveChildRectsAndApplyLifecycleInZOrder() {
        var group = new ViewGroup();
        var later = new TestViewNode();
        var earlier = new TestViewNode();

        group.addChild(RectD.corners(0d, 0d, 1d, 1d), new Rect(1, 2, -3, -4), 10, later);
        group.addChild(RectD.corners(0.5d, 0d, 1d, 1d), new Rect(2, 3, -1, -2), -5, earlier);

        group.initView();
        group.setRect(new Rect(10, 20, 40, 30));
        group.setActive(false);

        assertEquals(1, earlier.initCount);
        assertEquals(1, later.initCount);
        assertTrue(earlier.initOrder < later.initOrder);
        assertEquals(Rect.corners(32, 23, 51, 51), earlier.rect);
        assertEquals(Rect.corners(11, 22, 48, 48), later.rect);
        assertEquals(Boolean.FALSE, earlier.active);
        assertEquals(Boolean.FALSE, later.active);
    }

    private static final class TestViewNode implements IViewNode {
        private static int nextInitOrder = 0;

        private int initCount = 0;
        private int initOrder = -1;
        private Rect rect = Rect.ZERO;
        private Boolean active = null;

        @Override
        public void initView() {
            initCount++;
            initOrder = nextInitOrder++;
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
