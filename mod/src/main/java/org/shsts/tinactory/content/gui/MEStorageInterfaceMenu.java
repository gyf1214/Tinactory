package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.logistics.MEStorageInterface;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.gui.InventoryMenu;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEStorageInterfaceMenu extends StorageMenu {
    public MEStorageInterfaceMenu(Properties properties) {
        this(properties, CapabilityProvider.getContainer(properties.blockEntity(), MEStorageInterface.ID,
            MEStorageInterface.class));
    }

    private MEStorageInterfaceMenu(Properties properties, MEStorageInterface storage) {
        super(properties, storage.itemPort(), 0, storage.fluidPort(), 0);
    }

    public static InventoryMenu factory(Properties properties) {
        return new MEStorageInterfaceMenu(properties);
    }
}
