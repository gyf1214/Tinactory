package org.shsts.tinactory.content.gui.client;

import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.gui.MenuBase;

import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;

class MachineConfigButton extends SwitchButton {
    private final IMachineConfig config;
    private final String configKey;
    private final boolean defaultValue;

    public MachineConfigButton(MenuBase menu,
        IMachineConfig config, String configKey, boolean defaultValue,
        Texture texture, int disableTexY, int enableTexY,
        String disableLang, String enableLang) {
        super(menu, texture, disableTexY, enableTexY, disableLang, enableLang);
        this.config = config;
        this.configKey = configKey;
        this.defaultValue = defaultValue;
    }

    @Override
    protected boolean getValue() {
        return config.getBoolean(configKey, defaultValue);
    }

    @Override
    protected void setValue(boolean val) {
        menu.triggerEvent(SET_MACHINE_CONFIG,
            SetMachineConfigPacket.builder().set(configKey, val));
    }
}
