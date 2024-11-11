package org.shsts.tinactory.content;

import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Texture;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

public final class AllLayouts {
    public static final Layout WORKBENCH;
    public static final Layout BOILER;
    public static final Layout BLAST_FURNACE;
    public static final Layout SIFTER;

    static {
        WORKBENCH = Layout.builder()
                .dummySlot(9 + 6 * SLOT_SIZE, SLOT_SIZE)
                .image(16 + 4 * SLOT_SIZE, 20, Texture.CRAFTING_ARROW)
                .port(SlotType.ITEM_INPUT)
                .slots(0, 3 * SLOT_SIZE + SPACING, 1, 9)
                .slots(9 + SLOT_SIZE, 0, 3, 3)
                .buildLayout();

        BOILER = Layout.builder()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE * 2)
                .port(SlotType.FLUID_INPUT)
                .slot(0, 1)
                .port(SlotType.FLUID_OUTPUT)
                .slot(3 * SLOT_SIZE, 1 + SLOT_SIZE)
                .buildLayout();

        BLAST_FURNACE = Layout.builder()
                .port(SlotType.ITEM_INPUT)
                .slots(0, 1, 1, 3)
                .port(SlotType.FLUID_INPUT)
                .slot(2 * SLOT_SIZE, 1 + SLOT_SIZE)
                .port(SlotType.ITEM_OUTPUT)
                .slots(5 * SLOT_SIZE, 1, 1, 3)
                .port(SlotType.FLUID_OUTPUT)
                .slot(5 * SLOT_SIZE, 1 + SLOT_SIZE)
                .progressBar(Texture.PROGRESS_ARROW, 8 + 3 * SLOT_SIZE, 1 + SLOT_SIZE / 2)
                .buildLayout();

        SIFTER = Layout.builder()
                .port(SlotType.ITEM_INPUT)
                .slot(0, 1 + SLOT_SIZE / 2)
                .port(SlotType.ITEM_OUTPUT)
                .slots(3 * SLOT_SIZE, 1, 2, 3)
                .progressBar(Texture.PROGRESS_SIFT, 8 + SLOT_SIZE, 1 + SLOT_SIZE / 2)
                .buildLayout();
    }
}
