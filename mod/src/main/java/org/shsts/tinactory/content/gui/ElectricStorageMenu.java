package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.logistics.ElectricChest;
import org.shsts.tinactory.content.logistics.ElectricStorage;
import org.shsts.tinactory.content.logistics.ElectricTank;

import java.util.Collection;

import static org.shsts.tinactory.integration.common.CapabilityProvider.getContainer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ElectricStorageMenu extends StorageMenu {
    private final ElectricStorage<?> storage;

    private ElectricStorageMenu(Properties properties, ElectricChest storage) {
        super(properties, storage, storage.stackLimit(), IPort.empty(), 0);
        this.storage = storage;
    }

    private ElectricStorageMenu(Properties properties, ElectricTank storage) {
        super(properties, IPort.empty(), 0, storage, storage.stackLimit());
        this.storage = storage;
    }

    public static ElectricStorageMenu chest(Properties properties) {
        return new ElectricStorageMenu(properties,
            getContainer(properties.blockEntity(), ElectricChest.ID, ElectricChest.class));
    }

    public static ElectricStorageMenu tank(Properties properties) {
        return new ElectricStorageMenu(properties,
            getContainer(properties.blockEntity(), ElectricTank.ID, ElectricTank.class));
    }

    @Override
    protected Collection<IStackKey> filters() {
        return storage.filters();
    }

    @Override
    protected boolean isUnlocked() {
        return storage.isUnlocked();
    }

    @Override
    protected boolean setFilter(IStackKey key) {
        return storage.setFilter(key);
    }

    @Override
    protected boolean resetFilter(IStackKey key) {
        return storage.resetFilter(key);
    }

    public IMachineConfig machineConfig() {
        return storage.machineConfig();
    }
}
