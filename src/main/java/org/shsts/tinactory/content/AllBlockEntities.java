package org.shsts.tinactory.content;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.network.NetworkController;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public class AllBlockEntities {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<SmartBlockEntityType<NetworkController>> NETWORK_CONTROLLER;

    static {
        NETWORK_CONTROLLER = REGISTRATE.blockEntity("network/controller", NetworkController::new)
                .entityClass(NetworkController.class)
                .ticking()
                .validBlock(AllBlocks.NETWORK_CONTROLLER)
                .register();
    }

    public static void init() {}
}
