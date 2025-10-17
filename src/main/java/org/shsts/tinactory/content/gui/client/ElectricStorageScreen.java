package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.ElectricStorageMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.LayoutScreen;

import static org.shsts.tinactory.content.logistics.ElectricStorage.GLOBAL_DEFAULT;
import static org.shsts.tinactory.content.logistics.ElectricStorage.GLOBAL_KEY;
import static org.shsts.tinactory.content.logistics.ElectricStorage.STORAGE_DEFAULT;
import static org.shsts.tinactory.content.logistics.ElectricStorage.STORAGE_KEY;
import static org.shsts.tinactory.content.logistics.ElectricStorage.UNLOCK_DEFAULT;
import static org.shsts.tinactory.content.logistics.ElectricStorage.UNLOCK_KEY;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.ALLOW_ARROW_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.GLOBAL_PORT_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.LOCK_BUTTON;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricStorageScreen<M extends ElectricStorageMenu> extends LayoutScreen<M> {
    public ElectricStorageScreen(M menu, Component title) {
        super(menu, title);

        var config = menu.machineConfig();
        var buttonY = menu.layout().rect.endY() + SPACING;
        var offset = new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE);
        var anchor = RectD.corners(1d, 0d, 1d, 0d);
        addWidget(anchor, offset, new MachineConfigButton(menu, config, UNLOCK_KEY, UNLOCK_DEFAULT,
            LOCK_BUTTON, 18, 0, "chestLock", "chestUnlock"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        addWidget(anchor, offset.enlarge(2, 2).offset(-1, -1),
            new MachineConfigButton(menu, config, STORAGE_KEY, STORAGE_DEFAULT,
                ALLOW_ARROW_BUTTON, 0, 20, "chestNotStorage", "chestStorage"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        addWidget(anchor, offset, new MachineConfigButton(menu, config, GLOBAL_KEY, GLOBAL_DEFAULT,
            GLOBAL_PORT_BUTTON, 0, 18, "chestLocal", "chestGlobal"));
    }
}
