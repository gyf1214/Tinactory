package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.logistics.CombinedPort;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.logistics.StoragePorts;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllNetworks.LOGISTICS_SUBNET;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.content.logistics.MEStorageAccess.combinePorts;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MENetworkBridge extends CapabilityProvider implements IEventSubscriber {
    private static final String ID = "logistics/me_network_bridge";

    private final BlockEntity blockEntity;
    private final CombinedPort<ItemStack> parentItem;
    private final CombinedPort<FluidStack> parentFluid;
    private final CombinedPort<ItemStack> childItem;
    private final CombinedPort<FluidStack> childFluid;
    private final IElectricMachine electric;

    private IMachine machine;

    public MENetworkBridge(BlockEntity blockEntity, double power) {
        this.blockEntity = blockEntity;
        this.parentItem = StoragePorts.combinedItem();
        this.parentFluid = StoragePorts.combinedFluid();
        this.childItem = StoragePorts.combinedItem();
        this.childFluid = StoragePorts.combinedFluid();

        var voltage = getBlockVoltage(blockEntity);
        this.electric = new SimpleElectricConsumer(voltage.value, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.capability(ID, be -> new MENetworkBridge(be, power));
    }

    private void onUpdateLogistics(LogisticComponent logistics, BlockPos parentSubnet, BlockPos childSubnet) {
        combinePorts(logistics.getStoragePorts(parentSubnet), parentItem, parentFluid);
        combinePorts(logistics.getStoragePorts(childSubnet), childItem, childFluid);
    }

    private void onConnect(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());

        var pos = blockEntity.getBlockPos();
        var parentSubnet = network.getSubnet(pos, LOGISTICS_SUBNET.get());
        var childSubnet = pos;
        if (parentSubnet.equals(childSubnet)) {
            return;
        }

        logistics.registerPort(machine, 0, parentItem, childSubnet);
        logistics.registerPort(machine, 1, parentFluid, childSubnet);
        logistics.registerPort(machine, 2, childItem, parentSubnet);
        logistics.registerPort(machine, 3, childFluid, parentSubnet);
        logistics.onUpdate(() -> onUpdateLogistics(logistics, parentSubnet, childSubnet));
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> machine = MACHINE.get(blockEntity));
        eventManager.subscribe(CONNECT.get(), this::onConnect);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(ELECTRIC_MACHINE, electric);
    }
}
