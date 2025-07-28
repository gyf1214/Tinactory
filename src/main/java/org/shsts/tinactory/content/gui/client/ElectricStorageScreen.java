package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.ElectricStorageMenu;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.MenuScreen;

import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.ALLOW_ARROW_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.GLOBAL_PORT_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.LOCK_BUTTON;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricStorageScreen<M extends ElectricStorageMenu> extends MenuScreen<M> {
    private final IMachineConfig machineConfig;

    private class ConfigButton extends SwitchButton {
        private final String configKey;
        private final boolean defaultValue;

        public ConfigButton(String configKey, boolean defaultValue,
            Texture texture, int disableTexY, int enableTexY,
            String disableLang, String enableLang) {
            super(menu(), texture, enableTexY, disableTexY, enableLang, disableLang);
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

    public ElectricStorageScreen(M menu, Component title) {
        super(menu, title);

        this.machineConfig = menu.machineConfig();
        var buttonY = menu.layout().rect.endY() + SPACING;
        var offset = new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE);
        var anchor = RectD.corners(1d, 0d, 1d, 0d);
        addWidget(anchor, offset, new ConfigButton("unlockChest", false,
            LOCK_BUTTON, 18, 0, "chestLock", "chestUnlock"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        addWidget(anchor, offset.enlarge(2, 2).offset(-1, -1),
            new ConfigButton("storage", true, ALLOW_ARROW_BUTTON, 0, 20, "chestNotStorage", "chestStorage"));
        offset = offset.offset(-SLOT_SIZE - SPACING, 0);
        addWidget(anchor, offset, new ConfigButton("global", false,
            GLOBAL_PORT_BUTTON, 0, 18, "chestLocal", "chestGlobal"));
    }
}
