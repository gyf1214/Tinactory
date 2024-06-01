package org.shsts.tinactory.content;

import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Texture;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING_VERTICAL;

public final class AllLayouts {
    public static final Layout WORKBENCH;
    public static final Layout BOILER;

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

        BOILER = Layout.builder()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1)
                .port(SlotType.FLUID_INPUT)
                .slot(SLOT_SIZE, 1)
                .port(SlotType.FLUID_OUTPUT)
                .slot(4 * SLOT_SIZE, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8 + 2 * SLOT_SIZE, 0)
                .buildLayout();
    }
}
