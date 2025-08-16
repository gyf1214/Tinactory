package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.logistics.CombinedFluidCollection;
import org.shsts.tinactory.core.logistics.CombinedItemCollection;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.ArrayList;
import java.util.Collection;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllEvents.CONNECT;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.content.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageInterface extends CapabilityProvider implements IEventSubscriber {
    public static final String ID = "machine/me_storage_interface";
    public static final String CONNECT_PARENT_KEY = "connectParent";

    private final BlockEntity blockEntity;
    private final CombinedItemCollection combinedItem;
    private final CombinedFluidCollection combinedFluid;
    private final LazyOptional<IElectricMachine> electricCap;

    private IMachine machine;
    private IMachineConfig machineConfig;

    public MEStorageInterface(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.combinedItem = new CombinedItemCollection();
        this.combinedFluid = new CombinedFluidCollection();

        var electric = new SimpleElectricConsumer(getBlockVoltage(blockEntity),
            CONFIG.meStorageInterfaceAmperage.get());
        this.electricCap = LazyOptional.of(() -> electric);
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, MEStorageInterface::new);
    }

    private void onUpdateLogistics(LogisticComponent logistics, BlockPos subnet) {
        var items = new ArrayList<IItemCollection>();
        var fluids = new ArrayList<IFluidCollection>();
        var ports = logistics.getStoragePorts(subnet);
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

    private void onLoad() {
        machine = MACHINE.get(blockEntity);
        machineConfig = machine.config();
    }

    private void registerPorts(LogisticComponent logistics, INetwork network, BlockPos subnet) {
        logistics.unregisterPort(machine, 0);
        logistics.unregisterPort(machine, 1);
        var connectParent = machineConfig.getBoolean(CONNECT_PARENT_KEY, false);
        var parent = network.getSubnet(subnet);
        if (parent.equals(subnet) || !connectParent) {
            logistics.registerPortInSubnets(machine, 0, combinedItem, false,
                subnet, false);
            logistics.registerPortInSubnets(machine, 1, combinedFluid, false,
                subnet, false);
        } else {
            logistics.registerPortInSubnets(machine, 0, combinedItem, false,
                subnet, false, parent, true);
            logistics.registerPortInSubnets(machine, 1, combinedFluid, false,
                subnet, false, parent, true);
        }
    }

    private void registerPorts() {
        var world = blockEntity.getLevel();
        if (world != null && !world.isClientSide) {
            machine.network().ifPresent(network -> {
                var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
                var subnet = network.getSubnet(blockEntity.getBlockPos());
                registerPorts(logistics, network, subnet);
            });
        }
    }

    private void onConnect(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        var subnet = network.getSubnet(blockEntity.getBlockPos());
        logistics.onUpdatePorts(() -> onUpdateLogistics(logistics, subnet));
        registerPorts(logistics, network, subnet);
    }

    public void onUpdate(Runnable listener) {
        combinedItem.onUpdate(listener);
        combinedFluid.onUpdate(listener);
    }

    public void unregisterListener(Runnable listener) {
        combinedItem.unregisterListener(listener);
        combinedFluid.unregisterListener(listener);
    }

    public IItemCollection itemPort() {
        return combinedItem;
    }

    public IFluidCollection fluidPort() {
        return combinedFluid;
    }

    public Collection<ItemStack> getAllItems() {
        return combinedItem.getAllItems();
    }

    public Collection<FluidStack> getAllFluids() {
        return combinedFluid.getAllFluids();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ELECTRIC_MACHINE.get()) {
            return electricCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::registerPorts);
    }
}
