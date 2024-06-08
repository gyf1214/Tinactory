package org.shsts.tinactory.content.gui.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.gui.NetworkControllerMenu;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.Tab;
import org.shsts.tinactory.core.gui.client.Widgets;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.client.Widgets.BUTTON_HEIGHT;
import static org.shsts.tinactory.core.gui.client.Widgets.EDIT_BOX_LINE_HEIGHT;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkControllerScreen extends MenuScreen<NetworkControllerMenu> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LEFT_MARGIN = 28;
    private static final int TOP_MARGIN = 48;
    private static final int BOTTOM_MARGIN = 28;
    private static final int BUTTON_WIDTH = 72;

    private final Panel welcomePanel;
    private final EditBox welcomeEdit;
    private final Tab tabs;
    private final Label stateLabel;
    private final Label techLabel;
    private final Consumer<ITeamProfile> onTechChange = $ -> refreshTeam();

    private static Component tr(String key, Object... args) {
        return I18n.tr("tinactory.gui.networkController." + key, args);
    }

    public NetworkControllerScreen(NetworkControllerMenu menu, Inventory inventory,
                                   Component title, int syncSlot) {
        super(menu, inventory, title);

        this.welcomePanel = new Panel(this);
        var welcomeLabel = new Label(menu, Label.Alignment.END, tr("welcome"));
        this.welcomeEdit = Widgets.editBox();
        var welcomeButton = Widgets.simpleButton(menu, tr("welcome.button"), null, this::onWelcomePressed);
        welcomePanel.addWidget(welcomeLabel);
        welcomePanel.addVanillaWidget(new Rect(0, -1, 64, EDIT_BOX_LINE_HEIGHT), welcomeEdit);
        welcomePanel.addWidget(new Rect(-BUTTON_WIDTH / 2, 20, BUTTON_WIDTH, BUTTON_HEIGHT), welcomeButton);

        var statePanel = new Panel(this);
        this.stateLabel = new Label(menu);
        statePanel.addWidget(stateLabel);

        var techPanel = new Panel(this);
        this.techLabel = new Label(menu);
        techPanel.addWidget(techLabel);

        this.tabs = new Tab(this, statePanel, techPanel);

        rootPanel.addPanel(RectD.corners(0.5, 0d, 0.5, 1d), Rect.ZERO, welcomePanel);
        rootPanel.addPanel(statePanel);
        rootPanel.addPanel(techPanel);
        rootPanel.addPanel(new Rect(-MARGIN_HORIZONTAL, -MARGIN_TOP, 0, 0), tabs);

        menu.onSyncPacket(syncSlot, this::refresh);
        TechManager.client().onProgressChange(onTechChange);
        statePanel.setActive(false);
        welcomePanel.setActive(false);
    }

    @Override
    protected void init() {
        imageWidth = width - 2 * LEFT_MARGIN;
        leftPos = LEFT_MARGIN;
        imageHeight = height - TOP_MARGIN - BOTTOM_MARGIN;
        topPos = TOP_MARGIN;
        initRect();
    }

    @Override
    public void removed() {
        TechManager.client().removeProgressChangeListener(onTechChange);
        super.removed();
    }

    private void refreshTeam() {
        var localTeam = TechManager.localTeam();
        LOGGER.debug("refresh team {}", localTeam);
        var teamName = localTeam.map(ITeamProfile::getName).orElse("<null>");
        var targetTech = localTeam
                .flatMap(team -> {
                    var tech = team.getTargetTech().orElse(null);
                    if (tech == null) {
                        return Optional.empty();
                    }
                    return Optional.of(tr("researchLabel", team.getName(),
                            "%4d".formatted(team.getTechProgress(tech)),
                            "%4d".formatted(tech.getMaxProgress())));
                }).orElse(tr("noneResearchLabel"));
        stateLabel.setLine(0, tr("teamNameLabel", teamName));
        techLabel.setLine(0, targetTech);
    }

    private void refresh(NetworkControllerSyncPacket packet) {
        if (!packet.isPresent()) {
            welcomePanel.setActive(true);
            tabs.setActive(false);
        } else {
            refreshTeam();
            stateLabel.setLine(1, tr("stateLabel", packet.getState()));
            stateLabel.setLine(2, tr("workFactorLabel", "%.0f%%".formatted(packet.getWorkFactor() * 100d)));
            welcomePanel.setActive(false);
            tabs.setActive(true);
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
