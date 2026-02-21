package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Tab extends Panel {
    private static final Texture BUTTON_TEX = new Texture(
        gregtech("gui/tab/tabs_top"), 84, 64);
    private static final int BUTTON_WIDTH = 28;
    private static final int BUTTON_HEIGHT = 32;
    private static final int ICON_X_OFFSET = (BUTTON_WIDTH - 16) / 2;
    private static final int ICON_Y_OFFSET = ICON_X_OFFSET + 2;
    public static final int BUTTON_OFFSET = BUTTON_HEIGHT - 4;

    private class TabButton extends Button {
        private final int index;
        private final ItemStack icon;

        public TabButton(MenuBase menu, int index, ItemStack icon) {
            super(menu);
            this.index = index;
            this.icon = icon;
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var tx = index == 0 ? 0 : BUTTON_WIDTH;
            var ty = index == currentTab ? BUTTON_HEIGHT : 0;
            var z = getBlitOffset();
            RenderUtil.blit(poseStack, BUTTON_TEX, z, rect, tx, ty);
            if (!icon.isEmpty()) {
                RenderUtil.renderItem(icon, rect.x() + ICON_X_OFFSET, rect.y() + ICON_Y_OFFSET);
            }
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            select(index);
        }
    }

    private final List<Panel> tabPanels = new ArrayList<>();
    private int currentTab = 0;

    public Tab(MenuScreen<?> screen, Object... args) {
        super(screen);

        for (var i = 0; i < args.length; i++) {
            var k = tabPanels.size();
            tabPanels.add((Panel) args[i]);

            var icon = ItemStack.EMPTY;
            if (i + 1 < args.length) {
                if (args[i + 1] instanceof ItemLike item) {
                    icon = new ItemStack(item);
                } else if (args[i + 1] instanceof IEntry<?> entry) {
                    icon = new ItemStack((ItemLike) entry.get());
                }
                i++;
            }

            var button = new TabButton(menu, k, icon);
            addWidget(new Rect(k * BUTTON_WIDTH, -BUTTON_OFFSET, BUTTON_WIDTH, BUTTON_HEIGHT), button);
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
        select(value ? (currentTab >= 0 && currentTab < tabPanels.size() ? currentTab : 0) : -1);
    }
}
