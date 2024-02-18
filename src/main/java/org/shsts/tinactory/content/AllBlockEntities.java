package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<SmartBlockEntityType<NetworkController>> NETWORK_CONTROLLER;

    public static final RegistryEntry<SmartBlockEntityType<SmartBlockEntity>> WORKBENCH;

    static {
        NETWORK_CONTROLLER = REGISTRATE.blockEntity("network/controller", NetworkController::new)
                .entityClass(NetworkController.class)
                .ticking()
                .validBlock(AllBlocks.NETWORK_CONTROLLER)
                .register();

        WORKBENCH = REGISTRATE.blockEntity("primitive/workbench", SmartBlockEntity::new)
                .entityClass(SmartBlockEntity.class)
                .ticking()
                .validBlock(AllBlocks.WORKBENCH)
                .capability(AllCapabilities.WORKBENCH_CONTAINER)
                .menu(WorkbenchMenu::new).layout(AllLayouts.WORKBENCH).build()
                .register();
    }

    public static void init() {}
}
