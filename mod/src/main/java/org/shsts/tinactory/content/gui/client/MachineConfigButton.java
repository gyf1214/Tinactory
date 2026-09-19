package org.shsts.tinactory.content.gui.client;

import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;

class MachineConfigButton extends SwitchButton {
    private final IMachineConfig config;
    private final IEntry<IMachineConfigType<Boolean>> type;
    private final boolean defaultValue;

    public MachineConfigButton(MenuBase menu, IMachineConfig config,
        IEntry<IMachineConfigType<Boolean>> type, boolean defaultValue,
        Texture texture, int disableTexY, int enableTexY,
        String disableLang, String enableLang) {
        super(menu, texture, disableTexY, enableTexY, disableLang, enableLang);
        this.config = config;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    protected boolean getValue() {
        return config.get(type).orElse(defaultValue);
    }

    @Override
    protected void setValue(boolean val) {
        menu.triggerEvent(SET_MACHINE_CONFIG,
            SetMachineConfigPacket.builder().set(type, val));
    }
}
