package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.MEPatternTerminalMenu;
import org.shsts.tinactory.content.gui.sync.MEPatternResultSyncPacket;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.MenuScreen;

import static org.shsts.tinactory.content.gui.MEPatternTerminalMenu.PATTERN_RESULT_SYNC;
import static org.shsts.tinactory.content.gui.MEPatternTerminalMenu.PATTERN_SYNC;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternTerminalScreen extends MenuScreen<MEPatternTerminalMenu> {
    public static final int BUTTON_WIDTH = 48;

    private final MEPatternBrowserPanel browserPanel;
    private final MEPatternEditorPanel editorPanel;

    public MEPatternTerminalScreen(MEPatternTerminalMenu menu, Component title) {
        super(menu, title);
        this.browserPanel = new MEPatternBrowserPanel(this, this::showEditor, this::showCreate);
        this.editorPanel = new MEPatternEditorPanel(this, this::showBrowser);

        rootPanel.addGroup(browserPanel);
        rootPanel.addGroup(editorPanel);
        editorPanel.setActive(false);
        menu.setEditorActive(false);
        this.contentHeight = MEPatternTerminalMenu.INVENTORY_BAR_Y + 18;

        menu.onSyncPacket(PATTERN_SYNC, this::onPatternSync);
        menu.onSyncPacket(PATTERN_RESULT_SYNC, this::onPatternResultSync);
    }

    public static TranslatableComponent tr(String key, Object... args) {
        return I18n.tr("tinactory.gui.pattern." + key, args);
    }

    private void onPatternSync(MEPatternSyncPacket packet) {
        browserPanel.updatePatterns(packet);
    }

    public void showBrowser() {
        browserPanel.setActive(true);
        editorPanel.setActive(false);
        menu.setEditorActive(false);
    }

    private void showCreate() {
        editorPanel.create();
        browserPanel.setActive(false);
        editorPanel.setActive(true);
        menu.setEditorActive(true);
    }

    private void showEditor(CraftPattern pattern) {
        editorPanel.edit(pattern);
        browserPanel.setActive(false);
        editorPanel.setActive(true);
        menu.setEditorActive(true);
    }

    private void onPatternResultSync(MEPatternResultSyncPacket packet) {
        if (packet.result() == MEPatternResultSyncPacket.ResultCode.SUCCESS) {
            showBrowser();
        } else {
            editorPanel.showFeedback(resultMessage(packet.result()));
        }
    }

    private static Component resultMessage(MEPatternResultSyncPacket.ResultCode result) {
        return switch (result) {
            case SUCCESS -> tr("result.success");
            case DUPLICATE_PATTERN_ID -> tr("result.duplicatePatternId");
            case PATTERN_NOT_FOUND -> tr("result.patternNotFound");
            case NO_CAPACITY -> tr("result.noCapacity");
            case INVALID_PATTERN -> tr("result.invalidPattern");
            case STALE_PATTERN -> tr("result.stalePattern");
        };
    }
}
