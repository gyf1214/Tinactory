package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingPlugin;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.List;

import static org.shsts.tinactory.content.gui.client.AbstractRecipeBook.BACKGROUND_TEX_RECT;
import static org.shsts.tinactory.content.gui.client.AbstractRecipeBook.BUTTON_TOP_MARGIN;
import static org.shsts.tinactory.content.gui.client.AbstractRecipeBook.PANEL_BORDER;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PortPanel extends Panel {
    private static final Rect LABEL_RECT =
        new Rect(PANEL_BORDER + SPACING, BUTTON_TOP_MARGIN + PANEL_BORDER,
            -(PANEL_BORDER + SPACING) * 2 - SLOT_SIZE, SLOT_SIZE);

    public static final int TEXT_COLOR = 0xFFFFAA00;
    private static final int OVERLAY_COLOR = 0x80FFAA00;

    private class ConfigLabel extends Label {
        private final List<Layout.SlotInfo> slots;

        public ConfigLabel(IMenu menu, Component line, List<Layout.SlotInfo> slots) {
            super(menu, line);
            this.slots = slots;
            this.verticalAlign = Label.Alignment.MIDDLE;
            this.color = TEXT_COLOR;
        }

        @Override
        protected boolean canHover() {
            return true;
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            super.doRender(poseStack, mouseX, mouseY, partialTick);
            if (isHovering(mouseX, mouseY)) {
                renderHoverOverlay(poseStack, slots);
            }
        }
    }

    private final int xOffset;

    public PortPanel(ProcessingScreen screen, Layout layout) {
        super(screen);
        var menu = screen.menu();
        this.xOffset = layout.getXOffset();

        var background = new StretchImage(menu, RECIPE_BOOK_BG, BACKGROUND_TEX_RECT, PANEL_BORDER);
        addWidget(RectD.FULL, Rect.ZERO, background);

        var i = 0;
        for (var port = 0; port < layout.portSlots.size(); port++) {
            var slots = layout.portSlots.get(port);
            var type = layout.ports.get(port).type();
            if (type == SlotType.NONE) {
                continue;
            }

            var label = new ConfigLabel(menu, ProcessingPlugin.portLabel(type.portType, port), slots);
            label.verticalAlign = Label.Alignment.MIDDLE;
            label.color = 0xFFFFAA00;

            var y = (SLOT_SIZE + SPACING) * i;
            addWidget(RectD.corners(0d, 0d, 1d, 0d), LABEL_RECT.offset(0, y), label);
            i++;
        }
    }

    private void renderHoverOverlay(PoseStack poseStack, List<Layout.SlotInfo> slots) {
        var bx = screen.getGuiLeft() + MARGIN_HORIZONTAL + xOffset;
        var by = screen.getGuiTop() + MARGIN_TOP;
        for (var slot : slots) {
            var x = slot.x() + 1 + bx;
            var y = slot.y() + 1 + by;
            RenderUtil.fill(poseStack, new Rect(x, y, 16, 16), OVERLAY_COLOR);
        }
    }
}
