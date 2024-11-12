package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;

import java.util.List;

import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Tab extends Panel {
    private static final Texture BUTTON_TEX = new Texture(
        gregtech("gui/tab/tabs_top"), 84, 64);
    private static final int BUTTON_WIDTH = 28;
    private static final int BUTTON_HEIGHT = 32;
    public static final int BUTTON_OFFSET = BUTTON_HEIGHT - 4;

    private class TabButton extends Button {
        private final int index;

        public TabButton(Menu<?, ?> menu, int index) {
            super(menu);
            this.index = index;
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var tx = index == 0 ? 0 : BUTTON_WIDTH;
            var ty = index == currentTab ? BUTTON_HEIGHT : 0;
            var z = getBlitOffset();
            RenderUtil.blit(poseStack, BUTTON_TEX, z, rect, tx, ty);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            select(index);
        }
    }

    private final List<Panel> tabPanels;
    private int currentTab = 0;

    public Tab(MenuScreen<?> screen, Panel... tabPanels) {
        super(screen);
        this.tabPanels = List.of(tabPanels);

        for (var i = 0; i < tabPanels.length; i++) {
            var button = new TabButton(menu, i);
            addWidget(new Rect(i * BUTTON_WIDTH, -BUTTON_OFFSET, BUTTON_WIDTH, BUTTON_HEIGHT), button);
        }
    }

    public void select(int tab) {
        currentTab = tab;
        for (var i = 0; i < tabPanels.size(); i++) {
            tabPanels.get(i).setActive(i == tab);
        }
    }

    @Override
    public void setActive(boolean value) {
        super.setActive(value);
        select(value ? currentTab : -1);
    }
}
