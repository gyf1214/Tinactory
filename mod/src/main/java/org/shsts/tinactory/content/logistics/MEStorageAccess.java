package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.logistics.CombinedPort;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.integration.logistics.StoragePorts;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;

import java.util.ArrayList;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MEStorageAccess extends CapabilityProvider implements IEventSubscriber {
    protected final BlockEntity blockEntity;
    protected final CombinedPort<ItemStack> combinedItem;
    protected final CombinedPort<FluidStack> combinedFluid;
    private final LazyOptional<IElectricMachine> electricCap;

    protected IMachine machine;

    public MEStorageAccess(BlockEntity blockEntity, double power) {
        this.blockEntity = blockEntity;
        this.combinedItem = StoragePorts.combinedItem();
        this.combinedFluid = StoragePorts.combinedFluid();

        var voltage = getBlockVoltage(blockEntity);
        var electric = new SimpleElectricConsumer(voltage.value, power);
        this.electricCap = LazyOptional.of(() -> electric);
    }

    private void onUpdateLogistics(LogisticComponent logistics) {
        var items = new ArrayList<IPort<ItemStack>>();
        var fluids = new ArrayList<IPort<FluidStack>>();
        var ports = logistics.getStoragePorts();
        for (var port : ports) {
            if (port.type() == PortType.ITEM) {
                items.add(port.asItem());
            } else if (port.type() == PortType.FLUID) {
                fluids.add(port.asFluid());
            }
        }
        combinedItem.setComposes(items);
        combinedFluid.setComposes(fluids);
    }

    protected void onConnect(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        logistics.onUpdate(() -> onUpdateLogistics(logistics));
    }

    public void onUpdate(Runnable listener) {
        combinedItem.onUpdate(listener);
        combinedFluid.onUpdate(listener);
    }

    public void unregisterListener(Runnable listener) {
        combinedItem.unregisterListener(listener);
        combinedFluid.unregisterListener(listener);
    }

    public IPort<ItemStack> itemPort() {
        return combinedItem;
    }

    public IPort<FluidStack> fluidPort() {
        return combinedFluid;
    }

    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> machine = MACHINE.get(blockEntity));
        eventManager.subscribe(CONNECT.get(), this::onConnect);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ELECTRIC_MACHINE.get()) {
            return electricCap.cast();
        }
        return LazyOptional.empty();
    }
}
