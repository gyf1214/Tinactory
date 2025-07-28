package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.StaticWidget;

import static org.shsts.tinactory.content.AllLayouts.WORKBENCH;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchScreen extends MenuScreen<WorkbenchMenu> {
    public WorkbenchScreen(WorkbenchMenu menu, Component title) {
        super(menu, title);
        var panel = new Panel(this);
        for (var image : WORKBENCH.images) {
            panel.addWidget(image.rect(), new StaticWidget(menu, image.texture()));
        }
        addPanel(new Rect(WORKBENCH.getXOffset(), 0, 0, 0), panel);
    }
}
