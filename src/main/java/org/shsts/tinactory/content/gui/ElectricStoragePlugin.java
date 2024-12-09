package org.shsts.tinactory.content.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineConfig;
import org.shsts.tinactory.core.gui.InventoryPlugin;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.I18n.tr;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricStoragePlugin extends InventoryPlugin<MenuScreen> {
    protected final Layout layout;
    protected final Machine machine;
    private final MachineConfig machineConfig;

    protected ElectricStoragePlugin(IMenu menu, Layout layout) {
        super(menu, layout.rect.endY() + SPACING * 2 + SLOT_SIZE);
        this.layout = layout;
        this.machine = AllCapabilities.MACHINE.get(menu.blockEntity());
        this.machineConfig = machine.config;

        menu.onEventPacket(SET_MACHINE_CONFIG, machine::setConfig);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<MenuScreen> menuScreenClass() {
        return MenuScreen.class;
    }

    @OnlyIn(Dist.CLIENT)
    private class PortButton extends Button {
        private static final Texture PORT_TEX = new Texture(
            modLoc("gui/import_export"), 18, 18);
        private static final Texture DISABLE_TEX = new Texture(
            gregtech("gui/widget/button_clear_grid"), 18, 18);
        private static final Rect OUTPUT_RECT = new Rect(0, 0, 18, 18);
        private static final Rect INPUT_RECT = new Rect(0, 18, 18, -18);

        private final Rect portRect;
        private final String configKey;
        private final String langKey;

        public PortButton(boolean input) {
            super(ElectricStoragePlugin.this.menu);
            this.portRect = input ? INPUT_RECT : OUTPUT_RECT;
            var name = input ? "Input" : "Output";
            this.configKey = "allow" + name;
            this.langKey = "tinactory.tooltip.chest" + name;
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var z = getBlitOffset();
            if (machineConfig.getBoolean(configKey)) {
                RenderUtil.blit(poseStack, Texture.SWITCH_BUTTON, z, rect);
                RenderUtil.blit(poseStack, PORT_TEX, z, 0xFF5555FF, rect, portRect);
            } else {
                RenderUtil.blit(poseStack, DISABLE_TEX, z, rect);
            }
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            var suffix = machineConfig.getBoolean(configKey) ? ".allow" : ".disallow";
            return Optional.of(List.of(tr(langKey + suffix)));
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            var val = !machineConfig.getBoolean(configKey);
            iMenu.triggerEvent(SET_MACHINE_CONFIG,
                SetMachineConfigPacket.builder().set(configKey, val));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private class SwitchButton extends Button {
        private final String configKey;
        private final Texture texture;
        private final int enableTexY;
        private final int disableTexY;
        private final String enableLang;
        private final String disableLang;

        public SwitchButton(String configKey, Texture texture, int disableTexY, int enableTexY,
            String disableLang, String enableLang) {
            super(ElectricStoragePlugin.this.menu);
            this.configKey = configKey;
            this.texture = texture;
            this.disableTexY = disableTexY;
            this.enableTexY = enableTexY;
            this.disableLang = disableLang;
            this.enableLang = enableLang;
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var texRect = machineConfig.getBoolean(configKey) ? enableTexY : disableTexY;
            RenderUtil.blit(poseStack, texture, getBlitOffset(), rect, 0, texRect);
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            var langKey = machineConfig.getBoolean(configKey) ? enableLang : disableLang;
            return Optional.of(List.of(tr("tinactory.tooltip." + langKey)));
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            var val = !machineConfig.getBoolean(configKey);
            iMenu.triggerEvent(SET_MACHINE_CONFIG,
                SetMachineConfigPacket.builder().set(configKey, val));
        }
    }

    private static final Texture LOCK_TEX = new Texture(
        gregtech("gui/widget/button_public_private"), 18, 36);
    private static final Texture GLOBAL_TEX = new Texture(
        gregtech("gui/widget/button_distinct_buses"), 18, 36);

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(MenuScreen screen) {
        super.applyMenuScreen(screen);

        var buttonY = layout.rect.endY() + SPACING;
        var offset = new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE);
        var anchor = RectD.corners(1d, 0d, 1d, 0d);
        screen.addWidget(anchor, offset, new SwitchButton("unlockChest", LOCK_TEX,
            18, 0, "chestLock", "chestUnlock"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        screen.addWidget(anchor, offset, new PortButton(false));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        screen.addWidget(anchor, offset, new PortButton(true));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        screen.addWidget(anchor, offset, new SwitchButton("global", GLOBAL_TEX,
            0, 18, "chestLocal", "chestGlobal"));
    }
}
