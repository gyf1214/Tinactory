package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.core.gui.client.LayoutScreen;
import org.shsts.tinactory.core.gui.client.StaticWidget;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchScreen extends LayoutScreen<WorkbenchMenu> {
    public WorkbenchScreen(WorkbenchMenu menu, Component title) {
        super(menu, title);
        for (var image : layout.images) {
            layoutPanel.addWidget(image.rect(), new StaticWidget(menu, image.texture()));
        }
    }
}
