package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.machine.MachineConfig;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.BACKGROUND_TEX_RECT;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.BUTTON_TOP_MARGIN;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.PANEL_BORDER;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.RECIPE_BOOK_BG;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PortConfigPanel extends Panel {
    private static final Rect LABEL_RECT =
            new Rect(PANEL_BORDER, BUTTON_TOP_MARGIN + PANEL_BORDER, 0, SLOT_SIZE);
    private static final Rect BUTTON_RECT =
            new Rect(-PANEL_BORDER - SLOT_SIZE, BUTTON_TOP_MARGIN + PANEL_BORDER, SLOT_SIZE, SLOT_SIZE);


    private class ConfigButton extends Button {
        public static final int COLOR = 0x80FFAA00;

        private final int port;
        private final PortDirection direction;
        private final List<Layout.SlotInfo> slots;

        public ConfigButton(Menu<?, ?> menu, int port, PortDirection direction,
                            List<Layout.SlotInfo> slots) {
            super(menu);
            this.port = port;
            this.direction = direction;
            this.slots = slots;
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var config = machineConfig.getPortConfig(port);
            var z = getBlitOffset();
            if (config != MachineConfig.PortConfig.NONE) {
                RenderUtil.blit(poseStack, Texture.SWITCH_BUTTON, z, rect);
            }
            switch (config) {
                case NONE -> RenderUtil.blit(poseStack, Texture.CLEAR_GRID_BUTTON, z, rect);
                case PASSIVE -> RenderUtil.blit(poseStack, Texture.AUTO_OUT_BUTTON, z, rect);
                case ACTIVE -> RenderUtil.blit(poseStack, Texture.ITEM_OUT_BUTTON, z, rect);
            }
            if (isHovering(mouseX, mouseY)) {
                var bx = screen.getGuiLeft() + MARGIN_HORIZONTAL + xOffset;
                var by = screen.getGuiTop() + MARGIN_TOP;
                for (var slot : slots) {
                    var x = slot.x() + 1 + bx;
                    var y = slot.y() + 1 + by;
                    RenderUtil.fill(poseStack, new Rect(x, y, 16, 16), COLOR);
                }
            }
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            var config = machineConfig.getPortConfig(port);

            var nextConfig = MachineConfig.PortConfig.fromIndex((config.index + 1) % 3);
            // Disallow active input request
            if (nextConfig == MachineConfig.PortConfig.ACTIVE && direction != PortDirection.OUTPUT) {
                nextConfig = MachineConfig.PortConfig.NONE;
            }

            var packet = SetMachinePacket.builder().setPort(port, nextConfig);
            menu.triggerEvent(MenuEventHandler.SET_MACHINE, packet);
        }
    }

    private final MachineConfig machineConfig;
    private final int xOffset;

    public PortConfigPanel(MenuScreen<?> screen, Layout layout) {
        super(screen);
        var menu = screen.getMenu();
        this.machineConfig = AllCapabilities.MACHINE.get(menu.blockEntity).config;
        this.xOffset = layout.getXOffset();

        var background = new StretchImage(menu, RECIPE_BOOK_BG, BACKGROUND_TEX_RECT, PANEL_BORDER);
        addWidget(RectD.FULL, Rect.ZERO, background);

        var i = 0;
        for (var port : layout.portSlots.keys().elementSet()) {
            var slots = layout.portSlots.get(port);
            var type = slots.get(0).type();
            if (type == SlotType.NONE) {
                continue;
            }

            var label = new Label(menu, Label.Alignment.BEGIN, I18n.raw("%s", type));
            label.verticalAlign = Label.Alignment.MIDDLE;
            label.color = 0xFFFFAA00;
            var button = new ConfigButton(menu, port, type.direction, slots);

            var y = SLOT_SIZE * i;
            addWidget(RectD.ZERO, LABEL_RECT.offset(0, y), label);
            addWidget(RectD.corners(1d, 0d, 1d, 0d), BUTTON_RECT.offset(0, y), button);
            i++;
        }
    }
}
