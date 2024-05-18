package org.shsts.tinactory.content;

import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.gui.Layout;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING_VERTICAL;

public final class AllLayouts {
    public static final Layout WORKBENCH;

    static {
        var workbenchBuilder = Layout.builder()
                .dummySlot(6 * SLOT_SIZE, SLOT_SIZE)
                .port(SlotType.ITEM_INPUT);
        for (var j = 0; j < 9; j++) {
            workbenchBuilder.slot(j * SLOT_SIZE, 3 * SLOT_SIZE + SPACING_VERTICAL);
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 3; j++) {
                workbenchBuilder.slot((2 + j) * SLOT_SIZE, i * SLOT_SIZE);
            }
        }
        WORKBENCH = workbenchBuilder.buildLayout();
    }
}
