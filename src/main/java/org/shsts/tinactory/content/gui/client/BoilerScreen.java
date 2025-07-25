package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.ProgressBar;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.HEAT_EMPTY;
import static org.shsts.tinactory.core.gui.Texture.HEAT_FULL;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_BURN;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerScreen extends ProcessingScreen {
    public BoilerScreen(IMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        var burnBar = new ProgressBar(menu, PROGRESS_BURN, "burn");
        burnBar.direction = ProgressBar.Direction.VERTICAL;
        layoutPanel.addWidget(new Rect(1, 1 + SLOT_SIZE, 16, 16), burnBar);

        var heatBar = new ProgressBar(menu, HEAT_EMPTY, HEAT_FULL, "heat");
        heatBar.direction = ProgressBar.Direction.VERTICAL;
        var rect = new Rect(SLOT_SIZE * 2, 1, HEAT_EMPTY.width(), HEAT_EMPTY.height());
        layoutPanel.addWidget(rect, heatBar);
    }
}
