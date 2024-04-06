package org.shsts.tinactory.content.gui.client;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.ContainerMenuScreen;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.network.NetworkController;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkControllerScreen extends ContainerMenuScreen<ContainerMenu<NetworkController>> {
    private static final int HEIGHT = 200;

    private final Panel welcomePanel;

    public NetworkControllerScreen(ContainerMenu<NetworkController> menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = HEIGHT;

        this.welcomePanel = new Panel(menu);
        var welcomeLabel = new Label(menu, RectD.corners(0.5, 0.5, 0.5, 0.5), Rect.ZERO);
        welcomeLabel.verticalAlign = Label.Alignment.MIDDLE;
        welcomeLabel.horizontalAlign = Label.Alignment.END;
        welcomeLabel.setText(new TranslatableComponent("tinactory.gui.networkController.welcome"));
        welcomePanel.addWidget(welcomeLabel);

        addWidget(welcomePanel);
    }
}
