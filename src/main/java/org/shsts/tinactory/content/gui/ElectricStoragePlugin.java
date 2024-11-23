package org.shsts.tinactory.content.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.machine.MachineConfig;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.util.I18n.tr;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricStoragePlugin<M extends Menu<?, M>> implements IMenuPlugin<M> {
    private final M menu;
    private final int buttonY;
    private final MachineConfig machineConfig;

    public ElectricStoragePlugin(M menu) {
        this.menu = menu;

        this.buttonY = menu.getHeight() + MARGIN_VERTICAL;
        menu.setHeight(buttonY + SLOT_SIZE);

        var machine = AllCapabilities.MACHINE.get(menu.blockEntity);
        this.machineConfig = machine.config;
        menu.onEventPacket(MenuEventHandler.SET_MACHINE_CONFIG, machine::setConfig);
    }

    @OnlyIn(Dist.CLIENT)
    private class LockButton extends Button {
        private static final Texture TEX = new Texture(
            gregtech("gui/widget/button_public_private"), 18, 36);

        public LockButton() {
            super(ElectricStoragePlugin.this.menu);
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var uy = machineConfig.getBoolean("unlockChest") ? 0 : TEX.height() / 2;
            RenderUtil.blit(poseStack, TEX, getBlitOffset(), rect, 0, uy);
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            var component = machineConfig.getBoolean("unlockChest") ?
                tr("tinactory.tooltip.chestUnlock") : tr("tinactory.tooltip.chestLock");
            return Optional.of(List.of(component));
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            var val = !machineConfig.getBoolean("unlockChest");
            menu.triggerEvent(MenuEventHandler.SET_MACHINE_CONFIG,
                SetMachineConfigPacket.builder().set("unlockChest", val));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMenuScreen(MenuScreen<M> screen) {
        var offset = new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE);
        var anchor = RectD.corners(1d, 0d, 1d, 0d);
        screen.addWidget(anchor, offset, new LockButton());
    }
}
