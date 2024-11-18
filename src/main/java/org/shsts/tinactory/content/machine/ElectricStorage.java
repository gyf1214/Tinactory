package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.network.Network;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ElectricStorage extends CapabilityProvider implements IEventSubscriber, IProcessor {
    protected final BlockEntity blockEntity;

    protected MachineConfig machineConfig;
    protected Machine machine;

    public ElectricStorage(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public boolean isUnlocked() {
        return machineConfig.getBoolean("unlockChest");
    }

    public boolean allowInput() {
        return machineConfig.getBoolean("allowInput");
    }

    public boolean allowOutput() {
        return machineConfig.getBoolean("allowOutput");
    }

    public boolean isGlobal() {
        return machineConfig.getBoolean("global");
    }

    protected void registerPort(Network network, IPort port) {
        var logistics = network.getComponent(AllNetworks.LOGISTICS_COMPONENT);
        logistics.unregisterPort(machine, 0);

        if (isGlobal()) {
            logistics.registerGlobalPort(machine, 0, port);
        } else {
            var subnet = network.getSubnet(blockEntity.getBlockPos());
            logistics.registerPort(subnet, machine, 0, port);
        }
    }

    protected void onSlotChange() {
        blockEntity.setChanged();
    }

    private void onLoad() {
        machine = AllCapabilities.MACHINE.get(blockEntity);
        machineConfig = machine.config;
    }

    protected void onConnect(Network network) {
        onMachineConfig();
    }

    protected abstract void onMachineConfig();

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.CLIENT_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.CONNECT, this::onConnect);
        eventManager.subscribe(AllEvents.SET_MACHINE_CONFIG, this::onMachineConfig);
    }

    @Override
    public void onWorkTick(double partial) {}

    @Override
    public double getProgress() {
        return 0d;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }
}
