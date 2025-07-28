package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinycorelib.api.gui.MenuBase;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerMenu extends MenuBase {
    public LogisticWorkerMenu(Properties properties) {
        super(properties);
        addSyncSlot("info", LogisticWorkerSyncPacket::new);
        onEventPacket(SET_MACHINE_CONFIG, p -> MACHINE.get(blockEntity).setConfig(p));
    }
}
