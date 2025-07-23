package org.shsts.tinactory.content.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.LayoutPlugin;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.I18n.tr;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricStoragePlugin extends LayoutPlugin<MenuScreen> {
    protected final IMachine machine;
    private final IMachineConfig machineConfig;

    protected ElectricStoragePlugin(IMenu menu) {
        super(menu, SLOT_SIZE + SPACING);
        this.machine = MACHINE.get(menu.blockEntity());
        this.machineConfig = machine.config();

        menu.setValidPredicate(() -> machine.canPlayerInteract(menu.player()));
        menu.onEventPacket(SET_MACHINE_CONFIG, machine::setConfig);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<MenuScreen> menuScreenClass() {
        return MenuScreen.class;
    }

    @OnlyIn(Dist.CLIENT)
    private class SwitchButton extends Button {
        private final String configKey;
        private final boolean defaultValue;
        private final Texture texture;
        private final int enableTexY;
        private final int disableTexY;
        private final String enableLang;
        private final String disableLang;

        public SwitchButton(String configKey, boolean defaultValue, Texture texture,
            int disableTexY, int enableTexY, String disableLang, String enableLang) {
            super(ElectricStoragePlugin.this.menu);
            this.configKey = configKey;
            this.defaultValue = defaultValue;
            this.texture = texture;
            this.disableTexY = disableTexY;
            this.enableTexY = enableTexY;
            this.disableLang = disableLang;
            this.enableLang = enableLang;
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var texRect = machineConfig.getBoolean(configKey, defaultValue) ? enableTexY : disableTexY;
            RenderUtil.blit(poseStack, texture, getBlitOffset(), rect, 0, texRect);
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            var langKey = machineConfig.getBoolean(configKey, defaultValue) ? enableLang : disableLang;
            return Optional.of(List.of(tr("tinactory.tooltip." + langKey)));
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            var val = !machineConfig.getBoolean(configKey, defaultValue);
            menu.triggerEvent(SET_MACHINE_CONFIG,
                SetMachineConfigPacket.builder().set(configKey, val));
        }
    }

    private static final Texture LOCK_TEX = new Texture(
        gregtech("gui/widget/button_public_private"), 18, 36);
    private static final Texture STORAGE_TEX = new Texture(
        gregtech("gui/widget/button_allow_import_export"), 20, 40);
    private static final Texture GLOBAL_TEX = new Texture(
        gregtech("gui/widget/button_distinct_buses"), 18, 36);

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(MenuScreen screen) {
        super.applyMenuScreen(screen);

        var buttonY = layout.rect.endY() + SPACING;
        var offset = new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE);
        var anchor = RectD.corners(1d, 0d, 1d, 0d);
        screen.addWidget(anchor, offset, new SwitchButton("unlockChest", false,
            LOCK_TEX, 18, 0, "chestLock", "chestUnlock"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        screen.addWidget(anchor, offset.enlarge(2, 2).offset(-1, -1), new SwitchButton("storage", true,
            STORAGE_TEX, 0, 20, "chestNotStorage", "chestStorage"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        screen.addWidget(anchor, offset, new SwitchButton("global", false,
            GLOBAL_TEX, 0, 18, "chestLocal", "chestGlobal"));
    }
}
