package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.client.SwitchButton;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.LayoutPlugin;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.ALLOW_ARROW_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.GLOBAL_PORT_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.LOCK_BUTTON;

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
    private class ConfigButton extends SwitchButton {
        private final String configKey;
        private final boolean defaultValue;

        public ConfigButton(String configKey, boolean defaultValue,
            Texture texture, int disableTexY, int enableTexY,
            String disableLang, String enableLang) {
            super(ElectricStoragePlugin.this.menu, texture, enableTexY, disableTexY, enableLang, disableLang);
            this.configKey = configKey;
            this.defaultValue = defaultValue;
        }

        @Override
        protected boolean getValue() {
            return machineConfig.getBoolean(configKey, defaultValue);
        }

        @Override
        protected void setValue(boolean val) {
            menu.triggerEvent(SET_MACHINE_CONFIG,
                SetMachineConfigPacket.builder().set(configKey, val));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(MenuScreen screen) {
        super.applyMenuScreen(screen);

        var buttonY = layout.rect.endY() + SPACING;
        var offset = new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE);
        var anchor = RectD.corners(1d, 0d, 1d, 0d);
        screen.addWidget(anchor, offset, new ConfigButton("unlockChest", false,
            LOCK_BUTTON, 18, 0, "chestLock", "chestUnlock"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        screen.addWidget(anchor, offset.enlarge(2, 2).offset(-1, -1),
            new ConfigButton("storage", true, ALLOW_ARROW_BUTTON, 0, 20, "chestNotStorage", "chestStorage"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        screen.addWidget(anchor, offset, new ConfigButton("global", false,
            GLOBAL_PORT_BUTTON, 0, 18, "chestLocal", "chestGlobal"));
    }
}
