package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.LayoutScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.ProgressBar;
import org.shsts.tinactory.core.gui.client.StaticWidget;

import static org.shsts.tinactory.core.gui.ProcessingMenu.FLUID_SLOT;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingScreen extends LayoutScreen<ProcessingMenu> {
    protected final Layout layout;
    protected final Panel layoutPanel;

    public ProcessingScreen(ProcessingMenu menu, Component title) {
        super(menu, title);

        this.layout = menu.layout();
        this.layoutPanel = new Panel(this);

        for (var slot : layout.slots) {
            if (slot.type().portType == PortType.FLUID) {
                var syncSlot = FLUID_SLOT + slot.index();
                var rect = new Rect(slot.x() + 1, slot.y() + 1, Menu.SLOT_SIZE - 2, Menu.SLOT_SIZE - 2);
                layoutPanel.addWidget(rect, new FluidSlot(menu, slot.index(), syncSlot));
            }
        }

        for (var image : layout.images) {
            layoutPanel.addWidget(image.rect(), new StaticWidget(menu, image.texture()));
        }

        var progressBar = layout.progressBar;
        if (progressBar != null) {
            var widget = new ProgressBar(menu, progressBar.texture(), "progress");
            layoutPanel.addWidget(progressBar.rect(), widget);
        }

        addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);
    }
}
