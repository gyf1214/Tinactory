package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.MEPatternTerminalMenu;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.MenuScreen;

import static org.shsts.tinactory.content.gui.MEPatternTerminalMenu.PANEL_HEIGHT;
import static org.shsts.tinactory.content.gui.MEPatternTerminalMenu.PATTERN_RESULT_SYNC;
import static org.shsts.tinactory.content.gui.MEPatternTerminalMenu.PATTERN_SYNC;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternTerminalScreen extends MenuScreen<MEPatternTerminalMenu> {
    private final MEPatternBrowserPanel browserPanel;
    private final MEPatternEditorPanel editorPanel;

    public MEPatternTerminalScreen(MEPatternTerminalMenu menu, Component title) {
        super(menu, title);
        this.browserPanel = new MEPatternBrowserPanel(this, this::showEditor, this::showCreate);
        this.editorPanel = new MEPatternEditorPanel(this, this::showBrowser);

        rootPanel.addGroup(browserPanel);
        rootPanel.addChild(RectD.corners(0d, 0d, 1d, 0d), new Rect(0, 0, 0, PANEL_HEIGHT), editorPanel);
        this.contentHeight = MEPatternTerminalMenu.INVENTORY_BAR_Y + SLOT_SIZE;

        editorPanel.setActive(false);
        menu.setEditorActive(false);

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
        browserPanel.setActive(false);
        editorPanel.setActive(true);
        editorPanel.create();
        menu.setEditorActive(true);
    }

    private void showEditor(CraftPattern pattern) {
        browserPanel.setActive(false);
        editorPanel.setActive(true);
        editorPanel.edit(pattern);
        menu.setEditorActive(true);
    }

    private void onPatternResultSync(SyncPackets.LongPacket packet) {
        var result = MEPatternTerminalMenu.resultOf(packet.getData());
        if (result == MEPatternTerminalMenu.Result.SUCCESS) {
            showBrowser();
        } else {
            editorPanel.showFeedback(tr("result." + result.id));
        }
    }
}
