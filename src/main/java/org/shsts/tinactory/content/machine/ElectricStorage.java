package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;

import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.content.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.content.AllEvents.CONNECT;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.LOGISTIC_COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ElectricStorage extends CapabilityProvider
    implements ILayoutProvider, IProcessor, IEventSubscriber {
    protected final BlockEntity blockEntity;
    public final Layout layout;

    protected IMachineConfig machineConfig;
    protected IMachine machine;

    public ElectricStorage(BlockEntity blockEntity, Layout layout) {
        this.blockEntity = blockEntity;
        this.layout = layout;
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

    protected void registerPort(INetwork network, IPort port) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
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
        machine = MACHINE.get(blockEntity);
        machineConfig = machine.config();
    }

    protected void onConnect(INetwork network) {
        onMachineConfig();
    }

    protected abstract void onMachineConfig();

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {}

    @Override
    public double getProgress() {
        return 0d;
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == LAYOUT_PROVIDER.get() || cap == PROCESSOR.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }
}
