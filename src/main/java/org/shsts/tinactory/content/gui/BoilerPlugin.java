package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.MenuScreen1;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerPlugin implements IMenuPlugin<ProcessingMenu> {
    private static final double MAX_HEAT = 500d;

    private final int burnSlot;
    private final int heatSlot;

    public BoilerPlugin(ProcessingMenu menu) {
        this.burnSlot = menu.addSyncSlot(MenuSyncPacket.Double::new,
            be -> Machine.getProcessor(be).map(IProcessor::getProgress).orElse(0d));
        this.heatSlot = menu.addSyncSlot(MenuSyncPacket.Double::new,
            be -> Machine.getProcessor(be).map($ -> ((Boiler) $).getHeat() / MAX_HEAT).orElse(0d));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMenuScreen(MenuScreen1<ProcessingMenu> screen) {
        var menu = screen.getMenu();

//        if (menu.layout != null) {
//            var xOffset = menu.layout.getXOffset();
//            var burnBar = new ProgressBar(menu, Texture.PROGRESS_BURN, burnSlot);
//            burnBar.direction = ProgressBar.Direction.VERTICAL;
//            screen.addWidget(new Rect(xOffset + 1, 1 + SLOT_SIZE, 16, 16), burnBar);
//
//            var heatBar = new ProgressBar(menu, Texture.HEAT_EMPTY, Texture.HEAT_FULL, heatSlot);
//            heatBar.direction = ProgressBar.Direction.VERTICAL;
//            var rect = new Rect(xOffset + SLOT_SIZE * 2, 1, Texture.HEAT_EMPTY.width(), Texture.HEAT_EMPTY.height());
//            screen.addWidget(rect, heatBar);
//        }
    }
}
