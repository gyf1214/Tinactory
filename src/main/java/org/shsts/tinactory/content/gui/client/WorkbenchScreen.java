package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.content.AllLayouts.WORKBENCH;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchScreen extends MenuScreen {
    public WorkbenchScreen(IMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        var panel = new Panel(this);
        for (var image : WORKBENCH.images) {
            panel.addWidget(image.rect(), new StaticWidget(menu, image.texture()));
        }
        addPanel(new Rect(WORKBENCH.getXOffset(), 0, 0, 0), panel);
    }
}
