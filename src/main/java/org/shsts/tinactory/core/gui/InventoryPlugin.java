package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinycorelib.api.gui.IMenu;
import org.shsts.tinycorelib.api.gui.IMenuPlugin;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class InventoryPlugin<S extends MenuScreen> implements IMenuPlugin<S> {
    protected final IMenu menu;
    private final int endY;

    public InventoryPlugin(IMenu menu, int y) {
        this.menu = menu;
        var inventory = menu.inventory();
        var barY = y + 3 * SLOT_SIZE + SPACING;
        var barY1 = barY + MARGIN_TOP;
        for (var j = 0; j < 9; j++) {
            var x = MARGIN_HORIZONTAL + j * SLOT_SIZE;
            menu.addSlot(new Slot(inventory, j, x + 1, barY1 + 1));
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                var x = MARGIN_HORIZONTAL + j * SLOT_SIZE;
                var y1 = y + i * SLOT_SIZE + MARGIN_TOP;
                menu.addSlot(new Slot(inventory, 9 + i * 9 + j, x + 1, y1 + 1));
            }
        }
        this.endY = barY + SLOT_SIZE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public abstract Class<S> menuScreenClass();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(S screen) {
        if (screen.contentHeight < endY) {
            screen.contentHeight = endY;
        }
    }
}
