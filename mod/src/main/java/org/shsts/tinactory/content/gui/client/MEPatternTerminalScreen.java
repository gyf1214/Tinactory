package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.MEPatternTerminalMenu;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.MenuScreen;

import static org.shsts.tinactory.content.gui.MEPatternTerminalMenu.PANEL_HEIGHT;
import static org.shsts.tinactory.content.gui.MEPatternTerminalMenu.PATTERN_RESULT_SYNC;
import static org.shsts.tinactory.content.gui.MEPatternTerminalMenu.PATTERN_SYNC;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternTerminalScreen extends MenuScreen<MEPatternTerminalMenu> {
    private static final RectD PANEL_ANCHOR = RectD.corners(0d, 0d, 1d, 0d);
    private static final Rect PANEL_OFFSET = new Rect(0, 0, 0, PANEL_HEIGHT);

    private final MEPatternBrowserPanel browserPanel;
    private final MEPatternEditorPanel editorPanel;

    public MEPatternTerminalScreen(MEPatternTerminalMenu menu, Component title) {
        super(menu, title);
        this.browserPanel = new MEPatternBrowserPanel(this, this::showEditor, this::showCreate);
        this.editorPanel = new MEPatternEditorPanel(this, this::showBrowser);

        rootPanel.addChild(PANEL_ANCHOR, PANEL_OFFSET, browserPanel);
        rootPanel.addChild(PANEL_ANCHOR, PANEL_OFFSET, editorPanel);
        this.contentHeight = menu.endY();

        editorPanel.setActive(false);

        menu.onSyncPacket(PATTERN_SYNC, this::onPatternSync);
        menu.onSyncPacket(PATTERN_RESULT_SYNC, $ -> showBrowser());
        menu.setRecipeDraftImporter(this::importRecipeDraft);
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
    }

    private void showCreate() {
        browserPanel.setActive(false);
        editorPanel.setActive(true);
        editorPanel.create(menu.getCarried());
    }

    public boolean canImportRecipeDraft() {
        return !editorPanel.isActive();
    }

    public void createFromDraft(MEPatternDraft draft) {
        browserPanel.setActive(false);
        editorPanel.setActive(true);
        editorPanel.createFromDraft(draft);
    }

    private boolean importRecipeDraft(MEPatternDraft draft, boolean doImport) {
        if (!canImportRecipeDraft()) {
            return false;
        }
        if (doImport) {
            createFromDraft(draft);
        }
        return true;
    }

    private void showEditor(CraftPattern pattern) {
        browserPanel.setActive(false);
        editorPanel.setActive(true);
        editorPanel.edit(pattern);
    }
}
