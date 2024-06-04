package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.client.PortConfigPanel;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.SimpleButton;
import org.shsts.tinactory.core.gui.client.StaticWidget;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.PANEL_ANCHOR;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.PANEL_OFFSET;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingPlugin<M extends Menu<?, M>> implements IMenuPlugin<M> {
    private final Layout layout;
    private final int buttonY;

    public ProcessingPlugin(M menu, Layout layout) {
        this.layout = layout;
        menu.onEventPacket(SET_MACHINE, p -> AllCapabilities.MACHINE.get(menu.blockEntity).setConfig(p));
        this.buttonY = menu.getHeight() - SLOT_SIZE;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMenuScreen(MenuScreen<M> screen) {
        var menu = screen.getMenu();
        var portConfigPanel = new PortConfigPanel(screen, layout);
        var button = new SimpleButton(menu, Texture.SWITCH_BUTTON, null, 0, 0, 0, 0) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                portConfigPanel.setActive(!portConfigPanel.isActive());
            }
        };
        var buttonOverlay = new StaticWidget(menu, Texture.GREGTECH);

        screen.addPanel(PANEL_ANCHOR, PANEL_OFFSET, portConfigPanel);
        portConfigPanel.setActive(false);
        var buttonAnchor = RectD.corners(1d, 0d, 1d, 0d);
        var buttonOffset = new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE);
        screen.addWidget(buttonAnchor, buttonOffset, button);
        screen.addWidget(buttonAnchor, buttonOffset.offset(1, 1).enlarge(-1, -1), buttonOverlay);
    }

    public static <M extends Menu<?, M>> Function<M, IMenuPlugin<M>> builder(Layout layout) {
        return menu -> new ProcessingPlugin<>(menu, layout);
    }
}
