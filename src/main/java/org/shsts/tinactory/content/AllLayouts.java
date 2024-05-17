package org.shsts.tinactory.content;

import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Texture;

import java.util.Map;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING_VERTICAL;

public final class AllLayouts {
    public static final Layout WORKBENCH;
    public static final Map<Voltage, Layout> ORE_WASHER;

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

        ORE_WASHER = Layout.builder()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1)
                .port(SlotType.FLUID_INPUT)
                .slot(0, 2 + SLOT_SIZE * 2)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 4 + 4, 1, Voltage.LV)
                .port(SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 5 + 8, 1, Voltage.HV)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .buildObject();
    }
}
