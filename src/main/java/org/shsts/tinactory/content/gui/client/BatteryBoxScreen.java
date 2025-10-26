package org.shsts.tinactory.content.gui.client;

import net.minecraft.network.chat.Component;
import org.shsts.tinactory.core.gui.LayoutMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.LayoutScreen;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.electric.BatteryBox.DISCHARGE_DEFAULT;
import static org.shsts.tinactory.content.electric.BatteryBox.DISCHARGE_KEY;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.CHARGE_DISCHARGE_BUTTON;

public class BatteryBoxScreen extends LayoutScreen<LayoutMenu> {
    public BatteryBoxScreen(LayoutMenu menu, Component title) {
        super(menu, title);

        var config = MACHINE.get(menu.blockEntity()).config();
        var buttonY = menu.layout().rect.endY() + SPACING;
        var button = new MachineConfigButton(menu, config, DISCHARGE_KEY, DISCHARGE_DEFAULT,
            CHARGE_DISCHARGE_BUTTON, 0, 18, "batteryMode", "dischargeMode");
        addWidget(RectD.corners(1d, 0d, 1d, 0d),
            new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE), button);
    }
}
