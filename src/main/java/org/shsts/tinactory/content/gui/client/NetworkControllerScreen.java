package org.shsts.tinactory.content.gui.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.NetworkControllerMenu;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.RenameEventPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.client.Tab;
import org.shsts.tinactory.core.gui.client.Widgets;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.core.util.MathUtil;
import org.slf4j.Logger;

import java.util.function.Consumer;

import static org.shsts.tinactory.content.AllItems.CABLE;
import static org.shsts.tinactory.content.AllItems.RESEARCH_EQUIPMENT;
import static org.shsts.tinactory.content.AllMenus.RENAME;
import static org.shsts.tinactory.content.gui.NetworkControllerMenu.HEIGHT;
import static org.shsts.tinactory.content.gui.NetworkControllerMenu.RENAME_BASE_MARGIN;
import static org.shsts.tinactory.content.gui.NetworkControllerMenu.RENAME_BASE_WIDTH;
import static org.shsts.tinactory.content.gui.NetworkControllerMenu.RENAME_BASE_Y;
import static org.shsts.tinactory.content.gui.NetworkControllerMenu.WIDTH;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Texture.CRAFTING_ARROW;
import static org.shsts.tinactory.core.gui.client.Widgets.BUTTON_HEIGHT;
import static org.shsts.tinactory.core.gui.client.Widgets.EDIT_BOX_LINE_HEIGHT;
import static org.shsts.tinactory.core.util.ClientUtil.INTEGER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.PERCENTAGE_FORMAT;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkControllerScreen extends MenuScreen<NetworkControllerMenu> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WELCOME_BUTTON_WIDTH = 72;

    private final Panel welcomePanel;
    private final EditBox welcomeEdit;
    private final Tab tabs;
    private final Label stateLabel;
    private final TechPanel techPanel;
    private final Consumer<ITeamProfile> onTechChange = $ -> refreshTeam();

    public static Component tr(String key, Object... args) {
        return I18n.tr("tinactory.gui.networkController." + key, args);
    }

    private class RenamePanel extends Panel {
        public RenamePanel() {
            super(NetworkControllerScreen.this);
        }

        @Override
        public void setActive(boolean value) {
            super.setActive(value);
            menu().setRenameActive(value);
        }
    }

    public NetworkControllerScreen(NetworkControllerMenu menu, Component title) {
        super(menu, title);

        this.welcomePanel = new Panel(this);
        var welcomeLabel = new Label(menu, tr("welcome"));
        welcomeLabel.horizontalAlign = Label.Alignment.END;
        this.welcomeEdit = Widgets.editBox();
        var welcomeButton = Widgets.simpleButton(menu, tr("welcomeButton"), null, this::onWelcomePressed);
        welcomePanel.addWidget(welcomeLabel);
        welcomePanel.addWidget(new Rect(0, -1, 64, EDIT_BOX_LINE_HEIGHT), welcomeEdit);
        welcomePanel.addWidget(new Rect(-WELCOME_BUTTON_WIDTH / 2, 20, WELCOME_BUTTON_WIDTH, BUTTON_HEIGHT),
            welcomeButton);

        var statePanel = new Panel(this);
        this.stateLabel = new Label(menu);
        statePanel.addWidget(stateLabel);

        this.techPanel = new TechPanel(this);

        var renamePanel = new RenamePanel();
        var rect = new Rect(RENAME_BASE_MARGIN, RENAME_BASE_Y, RENAME_BASE_WIDTH, 0);
        var renameEdit = Widgets.editBox();
        renameEdit.setResponder(name -> menu.triggerEvent(RENAME, () -> new RenameEventPacket(name)));
        renamePanel.addWidget(rect.enlarge(0, FONT_HEIGHT), new Label(menu, tr("rename")));
        renamePanel.addWidget(rect.offset(0, FONT_HEIGHT + MARGIN_VERTICAL)
            .enlarge(0, EDIT_BOX_LINE_HEIGHT), renameEdit);
        renamePanel.addWidget(rect.offset(34, FONT_HEIGHT + EDIT_BOX_LINE_HEIGHT + MARGIN_VERTICAL * 2 + 1)
                .enlarge(-RENAME_BASE_WIDTH + CRAFTING_ARROW.width(), CRAFTING_ARROW.height()),
            new StaticWidget(menu, CRAFTING_ARROW));
        menu.onRefreshName(renameEdit::setValue);

        this.tabs = new Tab(this, statePanel, CABLE.get(Voltage.LV),
            techPanel, RESEARCH_EQUIPMENT.get(Voltage.LV),
            renamePanel, Items.NAME_TAG);

        rootPanel.addPanel(RectD.corners(0.5, 0d, 0.5, 1d), Rect.ZERO, welcomePanel);
        rootPanel.addPanel(statePanel);
        rootPanel.addPanel(techPanel);
        rootPanel.addPanel(renamePanel);
        rootPanel.addPanel(new Rect(-MARGIN_X, -MARGIN_TOP, 0, 0), tabs);

        menu.onSyncPacket("info", this::refresh);
        TechManager.client().onProgressChange(onTechChange);
        statePanel.setActive(false);
        welcomePanel.setActive(false);

        this.contentWidth = WIDTH;
        this.contentHeight = HEIGHT;
    }

    @Override
    protected void centerWindow() {
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight - Tab.BUTTON_OFFSET) / 2 + Tab.BUTTON_OFFSET;
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
        stateLabel.setLine(0, tr("teamNameLabel", teamName));
        localTeam.ifPresent(techPanel::refreshTech);
    }

    private void refresh(NetworkControllerSyncPacket packet) {
        if (!packet.isPresent()) {
            welcomePanel.setActive(true);
            tabs.setActive(false);
        } else {
            refreshTeam();
            stateLabel.setLine(1, tr("stateLabel", packet.getState()));
            var metric = packet.getElectricMetrics();
            stateLabel.setLine(2, tr("workFactorLabel",
                PERCENTAGE_FORMAT.format(metric.workSpeed())));
            stateLabel.setLine(3, tr("efficiencyLabel",
                PERCENTAGE_FORMAT.format(metric.efficiency())));
            var comp = MathUtil.compare(metric.buffer());
            if (comp == 0) {
                stateLabel.setLine(4, tr("powerLabel0",
                    INTEGER_FORMAT.format(metric.workCons()),
                    INTEGER_FORMAT.format(metric.gen())));
            } else if (comp > 0) {
                stateLabel.setLine(4, tr("powerLabel1",
                    INTEGER_FORMAT.format(metric.workCons()),
                    INTEGER_FORMAT.format(metric.buffer()),
                    INTEGER_FORMAT.format(metric.gen())));
            } else {
                stateLabel.setLine(4, tr("powerLabel2",
                    INTEGER_FORMAT.format(metric.workCons()),
                    INTEGER_FORMAT.format(metric.gen()),
                    INTEGER_FORMAT.format(-metric.buffer())));
            }
            welcomePanel.setActive(false);
            tabs.setActive(true);
        }
    }

    private void onWelcomePressed() {
        if (menu.player() instanceof LocalPlayer player) {
            var name = welcomeEdit.getValue();
            var command = "/" + Tinactory.ID + " createTeam " + StringArgumentType.escapeIfRequired(name);
            player.chat(command);
        }
    }
}
