package org.shsts.tinactory.content.gui.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.gui.NetworkControllerMenu;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.Widgets;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static org.shsts.tinactory.core.gui.client.Widgets.BUTTON_HEIGHT;
import static org.shsts.tinactory.core.gui.client.Widgets.EDIT_BOX_LINE_HEIGHT;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkControllerScreen extends MenuScreen<NetworkControllerMenu> {
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#.##");
    private static final int HEIGHT = 120;
    private static final int BUTTON_WIDTH = 72;

    private final Panel welcomePanel;
    private final Panel configPanel;
    private final EditBox welcomeEdit;
    private final Label stateLabel;

    public NetworkControllerScreen(NetworkControllerMenu menu, Inventory inventory,
                                   Component title, int syncSlot) {
        super(menu, inventory, title);
        this.imageHeight = HEIGHT;

        var welcomeLabel = new Label(menu, Label.Alignment.END,
                new TranslatableComponent("tinactory.gui.networkController.welcome"));
        this.welcomeEdit = Widgets.editBox();
        var welcomeButton = Widgets.simpleButton(menu,
                new TranslatableComponent("tinactory.gui.networkController.welcome.button"),
                null, this::onWelcomePressed);
        this.stateLabel = new Label(menu);

        this.welcomePanel = new Panel(this);
        welcomePanel.addWidget(welcomeLabel);
        welcomePanel.addVanillaWidget(new Rect(0, -1, 64, EDIT_BOX_LINE_HEIGHT), welcomeEdit);
        welcomePanel.addWidget(new Rect(-BUTTON_WIDTH / 2, 20, BUTTON_WIDTH, BUTTON_HEIGHT), welcomeButton);

        this.configPanel = new Panel(this);
        configPanel.addWidget(new Rect(0, Label.LINE_HEIGHT, 0, 0), stateLabel);

        var offset = Rect.corners(0, 10, 0, -10);
        rootPanel.addPanel(RectD.corners(0.5d, 0d, 0.5d, 1d), offset, welcomePanel);
        rootPanel.addPanel(offset, configPanel);

        menu.onSyncPacket(syncSlot, this::refresh);
        configPanel.setActive(false);
        welcomePanel.setActive(false);
    }

    private void refresh(NetworkControllerSyncPacket packet) {
        if (!packet.isPresent()) {
            welcomePanel.setActive(true);
            configPanel.setActive(false);
        } else {
            var workFactor = NUMBER_FORMAT.format(packet.getWorkFactor());
            stateLabel.setLines(
                    I18n.tr("tinactory.gui.networkController.teamNameLabel", packet.getTeamName()),
                    I18n.tr("tinactory.gui.networkController.stateLabel", packet.getState()),
                    I18n.tr("tinactory.gui.networkController.workFactorLabel", workFactor));
            welcomePanel.setActive(false);
            configPanel.setActive(true);
        }
    }

    private void onWelcomePressed() {
        if (menu.player instanceof LocalPlayer player) {
            var name = welcomeEdit.getValue();
            var command = "/" + Tinactory.ID + " createTeam " + StringArgumentType.escapeIfRequired(name);
            player.chat(command);
        }
    }
}
