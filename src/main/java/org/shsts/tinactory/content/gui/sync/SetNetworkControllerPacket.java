package org.shsts.tinactory.content.gui.sync;

import org.shsts.tinactory.core.gui.sync.MenuEventPacket;

public class SetNetworkControllerPacket extends MenuEventPacket {
    public SetNetworkControllerPacket() {}

    public SetNetworkControllerPacket(int containerId, int eventId) {
        super(containerId, eventId);
    }
}
