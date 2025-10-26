package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutMenu;
import org.shsts.tinactory.core.gui.Rect;

import static org.shsts.tinactory.core.gui.LayoutMenu.PROGRESS_SYNC;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayoutScreen<M extends LayoutMenu> extends MenuScreen<M> {
    protected final Layout layout;
    protected final Panel layoutPanel;

    protected LayoutScreen(M menu, Component title) {
        super(menu, title);
        this.contentHeight = menu.endY();
        this.layout = menu.layout();
        this.layoutPanel = new Panel(this);

        addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);
    }

    /**
     * Called during constructor.
     */
    protected void addProgressBar() {
        var progressBar = layout.progressBar;
        if (progressBar != null) {
            var widget = new ProgressBar(menu, progressBar.texture(), PROGRESS_SYNC);
            layoutPanel.addWidget(progressBar.rect(), widget);
        }
    }
}
