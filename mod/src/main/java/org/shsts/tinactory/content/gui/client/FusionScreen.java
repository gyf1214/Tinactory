package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.gui.ProcessingMenu;
import org.shsts.tinactory.integration.gui.client.ProgressBar;

import static org.shsts.tinactory.content.gui.FusionMenu.ENERGY_SYNC;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_FUSION_ENERGY;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FusionScreen extends MachineScreen {
    public FusionScreen(ProcessingMenu menu, Component title) {
        super(menu, title);

        var startupBar = new ProgressBar(menu, PROGRESS_FUSION_ENERGY, ENERGY_SYNC);
        layoutPanel.addChild(new Rect(0, 0, 94, 7), startupBar);
    }
}
