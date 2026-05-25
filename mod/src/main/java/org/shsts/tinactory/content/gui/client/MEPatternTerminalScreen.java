package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.MEPatternTerminalMenu;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.MenuScreen;

import static org.shsts.tinactory.content.gui.MEPatternTerminalMenu.PATTERN_SYNC;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternTerminalScreen extends MenuScreen<MEPatternTerminalMenu> {
    private final MEPatternBrowserPanel browserPanel;

    public MEPatternTerminalScreen(MEPatternTerminalMenu menu, Component title) {
        super(menu, title);
        this.browserPanel = new MEPatternBrowserPanel(this);

        rootPanel.addGroup(new Rect(-MARGIN_X, -MARGIN_TOP, 0, 0), browserPanel);
        this.contentHeight = 144;

        menu.onSyncPacket(PATTERN_SYNC, this::onPatternSync);
    }

    public static TranslatableComponent tr(String key, Object... args) {
        return I18n.tr("tinactory.gui.pattern." + key, args);
    }

    private void onPatternSync(MEPatternSyncPacket packet) {
        browserPanel.updatePatterns(packet);
    }
}
