package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.gui.Layout;

import static org.shsts.tinactory.api.logistics.SlotType.ITEM_INPUT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.CRAFTING_ARROW;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_CLEANROOM;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllLayouts {
    public static final Layout WORKBENCH;
    public static final Layout CLEANROOM;

    static {
        WORKBENCH = Layout.builder()
            .dummySlot(9 + 6 * SLOT_SIZE, SLOT_SIZE)
            .image(16 + 4 * SLOT_SIZE, 20, CRAFTING_ARROW)
            .port(ITEM_INPUT)
            .slots(0, 3 * SLOT_SIZE + SPACING, 1, 9)
            .slots(9 + SLOT_SIZE, 0, 3, 3)
            .buildLayout();

        CLEANROOM = Layout.builder()
            .progressBar(PROGRESS_CLEANROOM, 0, SLOT_SIZE / 2)
            .buildLayout();
    }
}
