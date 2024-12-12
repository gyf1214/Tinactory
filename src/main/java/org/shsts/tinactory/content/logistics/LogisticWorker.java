package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.network.NetworkComponent;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.logistics.LogisticWorkerConfig.PREFIX;
import static org.shsts.tinactory.core.gui.ProcessingPlugin.portLabel;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorker extends CapabilityProvider
    implements IEventSubscriber {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "network/logistic_worker";

    private final BlockEntity blockEntity;
    private final Voltage voltage;
    public final int workerSlots;
    private final int workerInterval;
    private final int workerStack;
    private final int workerFluidStack;
    private int tick = 0;
    private int currentSlot = 0;

    private final LazyOptional<IElectricMachine> electricCap;

    public LogisticWorker(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.voltage = RecipeProcessor.getBlockVoltage(blockEntity);
        var idx = voltage.rank - 1;
        this.workerSlots = TinactoryConfig.INSTANCE.workerSize.get().get(idx);
        this.workerInterval = TinactoryConfig.INSTANCE.workerDelay.get().get(idx);
        this.workerStack = TinactoryConfig.INSTANCE.workerStack.get().get(idx);
        this.workerFluidStack = TinactoryConfig.INSTANCE.workerFluidStack.get().get(idx);

        var electric = new Electric();
        this.electricCap = LazyOptional.of(() -> electric);
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, LogisticWorker::new);
    }

    private class Electric implements IElectricMachine {
        @Override
        public long getVoltage() {
            return voltage.value;
        }

        @Override
        public ElectricMachineType getMachineType() {
            return ElectricMachineType.CONSUMER;
        }

        @Override
        public double getPowerGen() {
            return 0;
        }

        @Override
        public double getPowerCons() {
            return voltage.value * 0.125d;
        }
    }

    public Optional<Network> getNetwork() {
        return AllCapabilities.MACHINE.tryGet(blockEntity)
            .flatMap(Machine::getNetwork);
    }

    public List<LogisticWorkerSyncPacket.PortInfo> getVisiblePorts() {
        var ret = new ArrayList<LogisticWorkerSyncPacket.PortInfo>();
        var machine = AllCapabilities.MACHINE.get(blockEntity);
        machine.getNetwork().ifPresent(network -> {
            var logistics = network.getComponent(AllNetworks.LOGISTIC_COMPONENT);
            var subnet = network.getSubnet(blockEntity.getBlockPos());
            for (var info : logistics.getVisiblePorts(subnet)) {
                var machine1 = info.machine();
                var index = info.portIndex();
                var portName = portLabel(info.port().type(), index);

                ret.add(new LogisticWorkerSyncPacket.PortInfo(machine1.getUuid(),
                    index, machine1.getTitle(), machine1.getIcon(), portName));
            }
        });
        return ret;
    }

    private Optional<LogisticWorkerConfig> getConfig(int index) {
        var machine = AllCapabilities.MACHINE.get(blockEntity);
        return machine.config.getCompound(PREFIX + index)
            .map(LogisticWorkerConfig::fromTag);
    }

    private static Optional<IPort> getPort(LogisticComponent logistic, BlockPos subnet,
        LogisticComponent.PortKey key) {
        return logistic.getPort(key)
            .filter(p -> p.subnet() == null || p.subnet().equals(subnet))
            .map(LogisticComponent.PortInfo::port);
    }

    private static boolean validateConfig(LogisticComponent logistic, BlockPos subnet,
        LogisticWorkerConfig entry) {
        if (!entry.isValid()) {
            return true;
        }
        var from = entry.from();
        var to = entry.to();
        if (from.isEmpty() || to.isEmpty() || from.get().equals(to.get())) {
            return false;
        }
        var from1 = getPort(logistic, subnet, from.get());
        var to1 = getPort(logistic, subnet, to.get());
        return from1.isPresent() && to1.isPresent() && from1.get().type() == to1.get().type();
    }

    private void validateConfigs() {
        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }
        var machine = AllCapabilities.MACHINE.get(blockEntity);
        var network = machine.getNetwork();
        if (network.isEmpty()) {
            return;
        }
        var logistic = network.get().getComponent(AllNetworks.LOGISTIC_COMPONENT);
        var subnet = network.get().getSubnet(blockEntity.getBlockPos());

        var packet = SetMachineConfigPacket.builder();
        var empty = true;
        for (var i = 0; i < workerSlots; i++) {
            var key = PREFIX + i;
            var entry = getConfig(i);
            if (entry.isEmpty()) {
                continue;
            }
            if (!validateConfig(logistic, subnet, entry.get())) {
                entry.get().setValid(false);
                packet.set(key, entry.get().serializeNBT());
                empty = false;
            }
        }
        if (!empty) {
            // skip event to skip validation
            machine.setConfig(packet.get(), false);
        }
    }

    private void transmitItem(IItemCollection from, IItemCollection to) {
        var stack = from.extractItem(workerStack, true);
        if (stack.isEmpty()) {
            return;
        }
        var remaining = to.insertItem(stack, true);
        var limit = stack.getCount() - remaining.getCount();
        if (limit > 0) {
            stack.setCount(limit);
            var stack2 = from.extractItem(stack, false);
            var remaining1 = to.insertItem(stack2, false);
            if (!remaining1.isEmpty()) {
                LOGGER.warn("transmit item failed from={} to={} content={}", from, to, stack);
            }
        }
    }

    private void transmitFluid(IFluidCollection from, IFluidCollection to) {
        var stack = from.drain(workerFluidStack, true);
        if (stack.isEmpty()) {
            return;
        }
        var limit = to.fill(stack, true);
        if (limit > 0) {
            stack.setAmount(limit);
            var stack2 = from.drain(stack, false);
            var remaining1 = to.fill(stack2, false);
            if (remaining1 != stack2.getAmount()) {
                LOGGER.warn("transmit fluid failed from={} to={} content={}", from, to, stack);
            }
        }
    }

    private void onTick(Level world, Network network) {
        if (tick < workerInterval) {
            tick++;
            return;
        }
        getConfig(currentSlot).filter(LogisticWorkerConfig::isValid).ifPresent(entry -> {
            var machine = AllCapabilities.MACHINE.get(blockEntity);
            var logistic = network.getComponent(AllNetworks.LOGISTIC_COMPONENT);
            var subnet = network.getSubnet(blockEntity.getBlockPos());

            if (validateConfig(logistic, subnet, entry)) {
                var from = entry.from().flatMap(k -> getPort(logistic, subnet, k)).orElseThrow();
                var to = entry.to().flatMap(k -> getPort(logistic, subnet, k)).orElseThrow();
                if (from.type() == PortType.ITEM) {
                    transmitItem(from.asItem(), to.asItem());
                } else {
                    transmitFluid(from.asFluid(), to.asFluid());
                }
                tick = 0;
            } else {
                entry.setValid(false);
                var packet = SetMachineConfigPacket.builder()
                    .set(PREFIX + currentSlot, entry.serializeNBT())
                    .get();
                // try revalidate
                machine.setConfig(packet);
            }
        });
        currentSlot = (currentSlot + 1) % workerSlots;
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(BUILD_SCHEDULING.get(), this::buildScheduling);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::validateConfigs);
    }

    private void buildScheduling(NetworkComponent.SchedulingBuilder builder) {
        builder.add(AllNetworks.LOGISTICS_SCHEDULING, this::onTick);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ELECTRIC_MACHINE.get()) {
            return electricCap.cast();
        }
        return LazyOptional.empty();
    }

    public static Optional<LogisticWorker> tryGet(BlockEntity be) {
        return tryGet(be, ID, LogisticWorker.class);
    }

    public static LogisticWorker get(BlockEntity be) {
        return get(be, ID, LogisticWorker.class);
    }
}
