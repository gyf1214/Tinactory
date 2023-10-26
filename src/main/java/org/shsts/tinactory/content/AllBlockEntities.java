package org.shsts.tinactory.content;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.network.NetworkController;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

import static org.shsts.tinactory.gui.ContainerMenu.SLOT_SIZE;
import static org.shsts.tinactory.gui.ContainerMenu.SPACING_VERTICAL;

public class AllBlockEntities {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<SmartBlockEntityType<NetworkController>> NETWORK_CONTROLLER;

    public static final RegistryEntry<SmartBlockEntityType<SmartBlockEntity>> WORKBENCH;

    static {
        NETWORK_CONTROLLER = REGISTRATE.blockEntity("network/controller", NetworkController::new)
                .entityClass(NetworkController.class)
                .ticking()
                .validBlock(AllBlocks.NETWORK_CONTROLLER)
                .register();

        var workbenchMenu = REGISTRATE.blockEntity("primitive/workbench", SmartBlockEntity::new)
                .entityClass(SmartBlockEntity.class)
                .ticking()
                .validBlock(AllBlocks.WORKBENCH)
                .capability(AllCapabilities.WORKBENCH_CONTAINER::get)
                .menu();

        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 3; j++) {
                workbenchMenu.slot(i * 3 + j, (2 + j) * SLOT_SIZE, i * SLOT_SIZE);
            }
        }
        workbenchMenu.slot(9, 6 * SLOT_SIZE, SLOT_SIZE);
        for (var j = 0; j < 9; j++) {
            workbenchMenu.slot(10 + j, j * SLOT_SIZE, 3 * SLOT_SIZE + SPACING_VERTICAL);
        }

        WORKBENCH = workbenchMenu.build().register();
    }

    public static void init() {}
}
