package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.LayoutMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.LayoutScreen;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.logistics.MEDrive.PRIORITY_DEFAULT;
import static org.shsts.tinactory.content.logistics.MEDrive.PRIORITY_KEY;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEDriveScreen extends LayoutScreen<LayoutMenu> {
    public MEDriveScreen(LayoutMenu menu, Component title) {
        super(menu, title);

        var config = MACHINE.get(menu.blockEntity()).config();
        var buttonY = menu.layout().rect.endY() + SPACING;
        addWidget(RectD.corners(1d, 0d, 1d, 0d), new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE),
            new StoragePriorityButton(menu, config, PRIORITY_KEY, PRIORITY_DEFAULT));
    }
}
