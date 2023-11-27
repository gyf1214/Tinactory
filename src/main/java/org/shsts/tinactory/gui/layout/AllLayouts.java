package org.shsts.tinactory.gui.layout;

import static org.shsts.tinactory.gui.ContainerMenu.SLOT_SIZE;
import static org.shsts.tinactory.gui.ContainerMenu.SPACING_VERTICAL;

public final class AllLayouts {
    public static final Layout STONE_GENERATOR;
    public static final Layout ORE_ANALYZER;
    public static final Layout WORKBENCH;

    static {
        STONE_GENERATOR = Layout.builder()
                .port(Layout.SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 2, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8, 0)
                .build();

        ORE_ANALYZER = Layout.builder()
                .port(Layout.SlotType.ITEM_INPUT)
                .slot(0, 1)
                .port(Layout.SlotType.ITEM_OUTPUT)
                .slot(SLOT_SIZE * 3, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .build();

        var workbenchBuilder = Layout.builder()
                .dummySlot(6 * SLOT_SIZE, SLOT_SIZE)
                .port(Layout.SlotType.ITEM_INPUT);
        for (var j = 0; j < 9; j++) {
            workbenchBuilder.slot(j * SLOT_SIZE, 3 * SLOT_SIZE + SPACING_VERTICAL);
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 3; j++) {
                workbenchBuilder.slot((2 + j) * SLOT_SIZE, i * SLOT_SIZE);
            }
        }
        WORKBENCH = workbenchBuilder.build();
    }
}
