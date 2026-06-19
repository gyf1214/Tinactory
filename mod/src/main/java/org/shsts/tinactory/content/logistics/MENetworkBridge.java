package org.shsts.tinactory.content.logistics;

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
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.logistics.CombinedPort;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.logistics.StoragePorts;
import org.shsts.tinactory.integration.network.IConnector;
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
import static org.shsts.tinactory.integration.network.MachineBlock.IO_FACING;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MENetworkBridge extends CapabilityProvider implements IEventSubscriber {
    private static final String ID = "logistics/me_network_bridge";

    private final BlockEntity blockEntity;
    private final CombinedPort<ItemStack> frontItem;
    private final CombinedPort<FluidStack> frontFluid;
    private final CombinedPort<ItemStack> backItem;
    private final CombinedPort<FluidStack> backFluid;
    private final LazyOptional<IElectricMachine> electricCap;

    private IMachine machine;
    @Nullable
    private BlockPos frontSubnet;
    @Nullable
    private BlockPos backSubnet;

    public MENetworkBridge(BlockEntity blockEntity, double power) {
        this.blockEntity = blockEntity;
        this.frontItem = StoragePorts.combinedItem();
        this.frontFluid = StoragePorts.combinedFluid();
        this.backItem = StoragePorts.combinedItem();
        this.backFluid = StoragePorts.combinedFluid();

        var voltage = getBlockVoltage(blockEntity);
        var electric = new SimpleElectricConsumer(voltage.value, power);
        this.electricCap = LazyOptional.of(() -> electric);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.capability(ID, be -> new MENetworkBridge(be, power));
    }

    private void onUpdateLogistics(LogisticComponent logistics) {
        if (frontSubnet == null || backSubnet == null) {
            return;
        }
        combinePorts(logistics.getStoragePorts(frontSubnet), frontItem, frontFluid);
        combinePorts(logistics.getStoragePorts(backSubnet), backItem, backFluid);
    }

    private boolean isConnectedEndpoint(Direction dir, BlockPos pos1) {
        var world = blockEntity.getLevel();
        if (world == null || !world.isLoaded(pos1)) {
            return false;
        }
        var pos = blockEntity.getBlockPos();
        var state = blockEntity.getBlockState();
        var state1 = world.getBlockState(pos1);
        return IConnector.isConnectedInWorld(world, pos, state, dir) &&
            IConnector.isConnectedInWorld(world, pos1, state1, dir.getOpposite());
    }

    private void onConnect(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        logistics.onUpdate(() -> onUpdateLogistics(logistics));

        frontSubnet = backSubnet = null;

        var pos = blockEntity.getBlockPos();
        var dir = blockEntity.getBlockState().getValue(IO_FACING);
        var front = pos.relative(dir);
        var back = pos.relative(dir.getOpposite());
        var allBlocks = network.allBlocks();

        if (!allBlocks.contains(front) || !allBlocks.contains(back) ||
            !isConnectedEndpoint(dir, front) || !isConnectedEndpoint(dir.getOpposite(), back)) {
            return;
        }

        frontSubnet = network.getSubnet(front, LOGISTICS_SUBNET.get());
        backSubnet = network.getSubnet(back, LOGISTICS_SUBNET.get());

        if (frontSubnet.equals(backSubnet)) {
            frontSubnet = backSubnet = null;
            return;
        }

        logistics.registerPort(machine, 0, frontItem, backSubnet);
        logistics.registerPort(machine, 1, frontFluid, backSubnet);
        logistics.registerPort(machine, 2, backItem, frontSubnet);
        logistics.registerPort(machine, 3, backFluid, frontSubnet);
    }

    @Override
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
