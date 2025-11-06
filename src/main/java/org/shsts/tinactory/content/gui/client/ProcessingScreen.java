package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.LayoutScreen;
import org.shsts.tinactory.core.gui.client.StaticWidget;

import static org.shsts.tinactory.core.gui.ProcessingMenu.FLUID_SYNC;
import static org.shsts.tinactory.core.gui.Texture.FLUID_SLOT_BG;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingScreen extends LayoutScreen<ProcessingMenu> {
    public ProcessingScreen(ProcessingMenu menu, Component title) {
        super(menu, title);

        for (var slot : layout.slots) {
            if (slot.type().portType == PortType.FLUID) {
                var syncSlot = FLUID_SYNC + slot.index();
                var rectBg = new Rect(slot.x(), slot.y(), Menu.SLOT_SIZE, Menu.SLOT_SIZE);
                var rect = rectBg.offset(1, 1).enlarge(-2, -2);
                layoutBg.addWidget(rectBg, new StaticWidget(menu, FLUID_SLOT_BG));
                layoutPanel.addWidget(rect, new FluidSlot(menu, slot.index(), syncSlot));
            }
        }

        for (var image : layout.images) {
            layoutBg.addWidget(image.rect(), new StaticWidget(menu, image.texture()));
        }

        addProgressBar();
    }
}
