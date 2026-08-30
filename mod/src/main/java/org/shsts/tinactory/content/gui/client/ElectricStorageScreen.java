package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.ElectricStorageMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;

import static org.shsts.tinactory.content.gui.StorageMenu.PANEL_HEIGHT;
import static org.shsts.tinactory.content.logistics.ElectricStorage.PRIORITY_DEFAULT;
import static org.shsts.tinactory.content.logistics.ElectricStorage.PRIORITY_KEY;
import static org.shsts.tinactory.content.logistics.ElectricStorage.UNLOCK_DEFAULT;
import static org.shsts.tinactory.content.logistics.ElectricStorage.UNLOCK_KEY;
import static org.shsts.tinactory.content.logistics.ElectricStorage.VOID_DEFAULT;
import static org.shsts.tinactory.content.logistics.ElectricStorage.VOID_KEY;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.LOCK_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.VOID_BUTTON;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ElectricStorageScreen extends StorageScreen<ElectricStorageMenu> {
    public ElectricStorageScreen(ElectricStorageMenu menu, Component title) {
        super(menu, title);

        var config = menu.machineConfig();
        var offset = new Rect(-SLOT_SIZE, PANEL_HEIGHT - SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        var anchor = RectD.corners(1d, 0d, 1d, 0d);
        rootPanel.addChild(anchor, offset, new MachineConfigButton(menu, config, UNLOCK_KEY, UNLOCK_DEFAULT,
            LOCK_BUTTON, 18, 0, "chestLock", "chestUnlock"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        rootPanel.addChild(anchor, offset, new MachineConfigButton(menu, config, VOID_KEY, VOID_DEFAULT,
            VOID_BUTTON, 18, 0, "noAutoVoid", "autoVoid"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        rootPanel.addChild(anchor, offset, new StoragePriorityButton(menu, config, PRIORITY_KEY, PRIORITY_DEFAULT));
    }
}
