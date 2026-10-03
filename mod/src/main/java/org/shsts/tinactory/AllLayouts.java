package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinycorelib.api.core.Transformer;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.CRAFTING_ARROW;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllLayouts {
    public static final Layout WORKBENCH;

    static {
        WORKBENCH = Layout.builder()
            .dummySlot(9 + 6 * SLOT_SIZE, SLOT_SIZE)
            .image(16 + 4 * SLOT_SIZE, 20, CRAFTING_ARROW)
            .transform(slots(0, SlotType.ITEM_INPUT, 0, 3 * SLOT_SIZE + SPACING, 1, 9))
            .transform(slots(0, SlotType.ITEM_INPUT, 9 + SLOT_SIZE, 0, 3, 3))
            .buildLayout();
    }

    private static <P> Transformer<LayoutSetBuilder<P>> slots(int port, SlotType type, int x, int y,
        int rows, int columns) {
        return $ -> {
            for (var i = 0; i < rows; i++) {
                for (var j = 0; j < columns; j++) {
                    $.slot(port, type, x + j * SLOT_SIZE, y + i * SLOT_SIZE);
                }
            }
            return $;
        };
    }
}
