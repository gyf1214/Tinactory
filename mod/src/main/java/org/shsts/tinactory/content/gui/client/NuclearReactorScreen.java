package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.ProgressBar;

import static org.shsts.tinactory.content.gui.BoilerMenu.HEAT_SYNC;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.HEAT_EMPTY;
import static org.shsts.tinactory.core.gui.Texture.HEAT_FULL;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NuclearReactorScreen extends MachineScreen {
    public NuclearReactorScreen(ProcessingMenu menu, Component title) {
        super(menu, title, false);

        var heatBar = new ProgressBar(menu, HEAT_EMPTY, HEAT_FULL, HEAT_SYNC);
        heatBar.direction = ProgressBar.Direction.VERTICAL;
        var x = SLOT_SIZE * 4 + (SLOT_SIZE - HEAT_EMPTY.width()) / 2;
        var y = (SLOT_SIZE * 5 - HEAT_EMPTY.height()) / 2;
        var rect = new Rect(x, y, HEAT_EMPTY.width(), HEAT_EMPTY.height());
        layoutPanel.addWidget(rect, heatBar);
    }
}
