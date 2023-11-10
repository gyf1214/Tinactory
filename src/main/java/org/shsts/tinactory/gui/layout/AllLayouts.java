package org.shsts.tinactory.gui.layout;

import static org.shsts.tinactory.gui.ContainerMenu.SLOT_SIZE;
import static org.shsts.tinactory.gui.ContainerMenu.SPACING_VERTICAL;

public final class AllLayouts {
    public static final Layout STONE_GENERATOR;
    public static final Layout WORKBENCH;

    static {
        STONE_GENERATOR = Layout.builder()
                .slot(0, SLOT_SIZE * 2, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8, 0)
                .build();

        var workbenchBuilder = Layout.builder()
                .slot(0, 6 * SLOT_SIZE, SLOT_SIZE);
        for (var j = 0; j < 9; j++) {
            workbenchBuilder.slot(1 + j, j * SLOT_SIZE, 3 * SLOT_SIZE + SPACING_VERTICAL);
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 3; j++) {
                workbenchBuilder.slot(10 + i * 3 + j, (2 + j) * SLOT_SIZE, i * SLOT_SIZE);
            }
        }
        WORKBENCH = workbenchBuilder.build();
    }
}
