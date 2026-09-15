package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.content.logistics.ElectricChest;
import org.shsts.tinactory.content.logistics.ElectricTank;
import org.shsts.tinactory.content.logistics.MEStorageInterface;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinycorelib.api.blockentity.ICapabilityContainer;
import org.shsts.tinycorelib.api.gui.MenuBase;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class StorageMenus {
    private StorageMenus() {}

    private static <T extends ICapabilityContainer> T getContainer(MenuBase.Properties properties,
        String id, Class<T> clazz) {
        var blockEntity = properties.blockEntity();
        assert blockEntity != null;
        return CapabilityProvider.getContainer(blockEntity, id, clazz);
    }

    public static StorageMenu meStorageInterface(MenuBase.Properties properties) {
        var storage = getContainer(properties, MEStorageInterface.ID, MEStorageInterface.class);
        return new StorageMenu(properties, storage.itemPort(), 0, storage.fluidPort(), 0);
    }

    public static StorageMenu electricChest(MenuBase.Properties properties) {
        var storage = getContainer(properties, ElectricChest.ID, ElectricChest.class);
        return new StorageMenu(properties, storage.port(), storage.stackLimit(), IPort.empty(), 0,
            storage.storageSlots(), storage.filterSlots());
    }

    public static StorageMenu electricTank(MenuBase.Properties properties) {
        var storage = getContainer(properties, ElectricTank.ID, ElectricTank.class);
        return new StorageMenu(properties, IPort.empty(), 0, storage.port(), storage.stackLimit(),
            storage.storageSlots(), storage.filterSlots());
    }
}
