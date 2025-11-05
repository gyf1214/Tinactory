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
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.gui.TechMenu;
import org.shsts.tinactory.content.gui.sync.RenameEventPacket;
import org.shsts.tinactory.core.electric.Voltage;
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
import org.slf4j.Logger;

import java.util.function.Consumer;

import static org.shsts.tinactory.content.AllItems.getComponent;
import static org.shsts.tinactory.content.AllMenus.RENAME;
import static org.shsts.tinactory.content.gui.TechMenu.HEIGHT;
import static org.shsts.tinactory.content.gui.TechMenu.RENAME_BASE_MARGIN;
import static org.shsts.tinactory.content.gui.TechMenu.RENAME_BASE_WIDTH;
import static org.shsts.tinactory.content.gui.TechMenu.RENAME_BASE_Y;
import static org.shsts.tinactory.content.gui.TechMenu.WIDTH;
import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Texture.CRAFTING_ARROW;
import static org.shsts.tinactory.core.gui.client.Widgets.BUTTON_HEIGHT;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TechScreen extends MenuScreen<TechMenu> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WELCOME_BUTTON_WIDTH = 72;

    private final Panel welcomePanel;
    private final EditBox welcomeEdit;
    private final Tab tabs;
    private final TechPanel techPanel;
    private final Consumer<ITeamProfile> onTechChange = $ -> refreshTeam();

    public static Component tr(String key, Object... args) {
        return I18n.tr("tinactory.gui.techMenu." + key, args);
    }

    private class RenamePanel extends Panel {
        public RenamePanel() {
            super(TechScreen.this);
        }

        @Override
        public void setActive(boolean value) {
            super.setActive(value);
            menu().setRenameActive(value);
        }
    }

    public TechScreen(TechMenu menu, Component title) {
        super(menu, title);

        this.welcomePanel = new Panel(this);
        var welcomeLabel = new Label(menu, tr("welcome"));
        welcomeLabel.horizontalAlign = Label.Alignment.END;
        this.welcomeEdit = Widgets.editBox();
        var welcomeButton = Widgets.simpleButton(menu, tr("welcomeButton"), null, this::onWelcomePressed);
        welcomePanel.addWidget(welcomeLabel);
        welcomePanel.addWidget(new Rect(0, -1, 64, EDIT_HEIGHT), welcomeEdit);
        welcomePanel.addWidget(new Rect(-WELCOME_BUTTON_WIDTH / 2, 20, WELCOME_BUTTON_WIDTH, BUTTON_HEIGHT),
            welcomeButton);

        this.techPanel = new TechPanel(this);

        var renamePanel = new RenamePanel();
        var rect = new Rect(RENAME_BASE_MARGIN, RENAME_BASE_Y, RENAME_BASE_WIDTH, 0);
        var renameEdit = Widgets.editBox();
        renameEdit.setResponder(name -> menu.triggerEvent(RENAME, () -> new RenameEventPacket(name)));
        renamePanel.addWidget(rect.enlarge(0, FONT_HEIGHT), new Label(menu, tr("rename")));
        renamePanel.addWidget(rect.offset(0, FONT_HEIGHT + MARGIN_VERTICAL)
            .enlarge(0, EDIT_HEIGHT), renameEdit);
        renamePanel.addWidget(rect.offset(34, FONT_HEIGHT + EDIT_HEIGHT + MARGIN_VERTICAL * 2 + 1)
                .enlarge(-RENAME_BASE_WIDTH + CRAFTING_ARROW.width(), CRAFTING_ARROW.height()),
            new StaticWidget(menu, CRAFTING_ARROW));
        menu.onRefreshName(renameEdit::setValue);

        this.tabs = new Tab(this,
            techPanel, getComponent("research_equipment").get(Voltage.LV),
            renamePanel, Items.NAME_TAG);

        rootPanel.addPanel(RectD.corners(0.5, 0d, 0.5, 1d), Rect.ZERO, welcomePanel);
        rootPanel.addPanel(techPanel);
        rootPanel.addPanel(renamePanel);
        rootPanel.addPanel(new Rect(-MARGIN_X, -MARGIN_TOP, 0, 0), tabs);

        TechManager.client().onProgressChange(onTechChange);

        this.contentWidth = WIDTH;
        this.contentHeight = HEIGHT;
    }

    @Override
    protected void centerWindow() {
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight - Tab.BUTTON_OFFSET) / 2 + Tab.BUTTON_OFFSET;
    }

    @Override
    protected void init() {
        super.init();
        refreshTeam();
    }

    @Override
    public void removed() {
        TechManager.client().removeProgressChangeListener(onTechChange);
        super.removed();
    }

    private void refreshTeam() {
        var localTeam = TechManager.localTeam();
        LOGGER.trace("refresh team {}", localTeam);
        if (localTeam.isPresent()) {
            welcomePanel.setActive(false);
            tabs.setActive(true);
            techPanel.refreshTech(localTeam.get());
        } else {
            welcomePanel.setActive(true);
            tabs.setActive(false);
        }
    }

    private void onWelcomePressed() {
        if (menu.player() instanceof LocalPlayer player) {
            var name = welcomeEdit.getValue();
            var command = "/" + TinactoryKeys.ID + " createTeam " + StringArgumentType.escapeIfRequired(name);
            player.chat(command);
        }
    }
}
