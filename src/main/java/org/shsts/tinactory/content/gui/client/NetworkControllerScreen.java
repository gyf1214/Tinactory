package org.shsts.tinactory.content.gui.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.electric.Voltage;
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
import org.shsts.tinactory.core.util.MathUtil;
import org.slf4j.Logger;

import java.util.function.Consumer;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
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
    public static final int WIDTH = TechPanel.LEFT_OFFSET + TechPanel.RIGHT_WIDTH;
    public static final int HEIGHT = TechPanel.BUTTON_SIZE * 6 + FONT_HEIGHT +
        MARGIN_VERTICAL * 3 + MARGIN_TOP;

    private final Panel welcomePanel;
    private final EditBox welcomeEdit;
    private final Tab tabs;
    private final Label stateLabel;
    public final TechPanel techPanel;
    private final Consumer<ITeamProfile> onTechChange = $ -> refreshTeam();

    public static Component tr(String key, Object... args) {
        return I18n.tr("tinactory.gui.networkController." + key, args);
    }

    public NetworkControllerScreen(NetworkControllerMenu menu, Inventory inventory,
        Component title, int syncSlot) {
        super(menu, inventory, title);

        this.welcomePanel = new Panel(this);
        var welcomeLabel = new Label(menu, tr("welcome"));
        welcomeLabel.horizontalAlign = Label.Alignment.END;
        this.welcomeEdit = Widgets.editBox();
        var welcomeButton = Widgets.simpleButton(menu, tr("welcomeButton"), null, this::onWelcomePressed);
        welcomePanel.addWidget(welcomeLabel);
        welcomePanel.addVanillaWidget(new Rect(0, -1, 64, EDIT_BOX_LINE_HEIGHT), welcomeEdit);
        welcomePanel.addWidget(new Rect(-WELCOME_BUTTON_WIDTH / 2, 20, WELCOME_BUTTON_WIDTH, BUTTON_HEIGHT),
            welcomeButton);

        var statePanel = new Panel(this);
        this.stateLabel = new Label(menu);
        statePanel.addWidget(stateLabel);

        this.techPanel = new TechPanel(this);

        this.tabs = new Tab(this, statePanel, AllItems.CABLE.get(Voltage.LV),
            techPanel, AllItems.RESEARCH_EQUIPMENT.get(Voltage.LV));

        rootPanel.addPanel(RectD.corners(0.5, 0d, 0.5, 1d), Rect.ZERO, welcomePanel);
        rootPanel.addPanel(statePanel);
        rootPanel.addPanel(techPanel);
        rootPanel.addPanel(new Rect(-MARGIN_HORIZONTAL, -MARGIN_TOP, 0, 0), tabs);

        menu.onSyncPacket(syncSlot, this::refresh);
        TechManager.client().onProgressChange(onTechChange);
        statePanel.setActive(false);
        welcomePanel.setActive(false);

        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        leftPos = (width - WIDTH) / 2;
        topPos = (height - HEIGHT - Tab.BUTTON_OFFSET) / 2 + Tab.BUTTON_OFFSET;
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
        if (menu.player instanceof LocalPlayer player) {
            var name = welcomeEdit.getValue();
            var command = "/" + Tinactory.ID + " createTeam " + StringArgumentType.escapeIfRequired(name);
            player.chat(command);
        }
    }
}
