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

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.logistics.MEDrive.STORAGE_DEFAULT;
import static org.shsts.tinactory.content.logistics.MEDrive.STORAGE_KEY;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.ALLOW_ARROW_BUTTON;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEDriveScreen extends LayoutScreen<LayoutMenu> {
    public MEDriveScreen(LayoutMenu menu, Component title) {
        super(menu, title);

        var config = MACHINE.get(menu.blockEntity()).config();
        var buttonY = menu.layout().rect.endY() + SPACING;
        addWidget(RectD.corners(1d, 0d, 1d, 0d), new Rect(-19, buttonY - 1, 20, 20),
            new MachineConfigButton(menu, config, STORAGE_KEY, STORAGE_DEFAULT,
                ALLOW_ARROW_BUTTON, 0, 20, "chestNotStorage", "chestStorage"));
    }
}
