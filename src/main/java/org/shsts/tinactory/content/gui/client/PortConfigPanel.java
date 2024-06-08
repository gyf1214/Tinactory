package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
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
import java.util.Optional;

import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.BACKGROUND_TEX_RECT;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.BUTTON_TOP_MARGIN;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.PANEL_BORDER;
import static org.shsts.tinactory.content.model.ModelGen.gregtech;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PortConfigPanel extends Panel {
    private static final Texture ITEM_OUT_BUTTON = new Texture(
            gregtech("gui/widget/button_item_output_overlay"), 18, 18);
    private static final Texture AUTO_OUT_BUTTON = new Texture(
            gregtech("gui/widget/button_output"), 18, 18);
    private static final Texture CLEAR_GRID_BUTTON = new Texture(
            gregtech("gui/widget/button_clear_grid"), 18, 18);
    private static final Rect LABEL_RECT =
            new Rect(PANEL_BORDER + SPACING, BUTTON_TOP_MARGIN + PANEL_BORDER,
                    -(PANEL_BORDER + SPACING) * 2 - SLOT_SIZE, SLOT_SIZE);
    private static final Rect BUTTON_RECT =
            new Rect(-PANEL_BORDER - SLOT_SIZE - SPACING, BUTTON_TOP_MARGIN + PANEL_BORDER,
                    SLOT_SIZE, SLOT_SIZE);
    private static final int TEXT_COLOR = 0xFFFFAA00;
    private static final int OVERLAY_COLOR = 0x80FFAA00;


    private class ConfigButton extends Button {

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
                case NONE -> RenderUtil.blit(poseStack, CLEAR_GRID_BUTTON, z, rect);
                case PASSIVE -> RenderUtil.blit(poseStack, AUTO_OUT_BUTTON, z, rect);
                case ACTIVE -> RenderUtil.blit(poseStack, ITEM_OUT_BUTTON, z, rect);
            }
            if (isHovering(mouseX, mouseY)) {
                renderHoverOverlay(poseStack, slots);
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

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            var subKey = I18n.tr("tinactory.tooltip.portConfig." + direction.name().toLowerCase());
            var tooltip = switch (machineConfig.getPortConfig(port)) {
                case NONE -> I18n.tr("tinactory.tooltip.portConfig.none", subKey);
                case PASSIVE -> I18n.tr("tinactory.tooltip.portConfig.passive", subKey);
                case ACTIVE -> I18n.tr("tinactory.tooltip.portConfig.active", subKey);
            };
            return Optional.of(List.of(tooltip));
        }
    }

    private class ConfigLabel extends Label {

        private final List<Layout.SlotInfo> slots;

        public ConfigLabel(Menu<?, ?> menu, Component line, List<Layout.SlotInfo> slots) {
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
        for (var port = 0; port < layout.portSlots.size(); port++) {
            var slots = layout.portSlots.get(port);
            var type = layout.ports.get(port).type();
            if (type == SlotType.NONE) {
                continue;
            }

            var key = "tinactory.gui.portConfig." + type.portType.name().toLowerCase() + "Label";
            var label = new ConfigLabel(menu, I18n.tr(key, port), slots);
            label.verticalAlign = Label.Alignment.MIDDLE;
            label.color = 0xFFFFAA00;
            var button = new ConfigButton(menu, port, type.direction, slots);

            var y = (SLOT_SIZE + SPACING) * i;
            addWidget(RectD.corners(0d, 0d, 1d, 0d), LABEL_RECT.offset(0, y), label);
            addWidget(RectD.corners(1d, 0d, 1d, 0d), BUTTON_RECT.offset(0, y), button);
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
