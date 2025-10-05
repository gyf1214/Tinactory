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
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.Optional;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.TinactoryConfig.listConfig;
import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.content.AllEvents.CONNECT;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.LOGISTICS_SCHEDULING;
import static org.shsts.tinactory.content.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.content.logistics.LogisticWorkerConfig.PREFIX;
import static org.shsts.tinactory.content.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorker extends CapabilityProvider implements IEventSubscriber {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "logistics/logistic_worker";

    private final BlockEntity blockEntity;
    public final int workerSlots;
    private final int workerInterval;
    private final int workerStack;
    private final int workerFluidStack;
    private final int[] nextValidSlot;
    private int tick = 0;
    private int currentSlot;
    private boolean noValidSlot = true;

    private final LazyOptional<IElectricMachine> electricCap;

    public LogisticWorker(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        var voltage = getBlockVoltage(blockEntity);
        var idx = voltage.rank - 1;
        this.workerSlots = listConfig(CONFIG.logisticWorkerSize, idx);
        this.workerInterval = listConfig(CONFIG.logisticWorkerDelay, idx);
        this.workerStack = listConfig(CONFIG.logisticWorkerStack, idx);
        this.workerFluidStack = listConfig(CONFIG.logisticWorkerFluidStack, idx);
        this.nextValidSlot = new int[workerSlots];
        // initialize current slot to the last slot so in the first tick it will select the first valid slot
        this.currentSlot = workerSlots - 1;

        var electric = new SimpleElectricConsumer(voltage, CONFIG.logisticWorkerAmperage.get());
        this.electricCap = LazyOptional.of(() -> electric);
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, LogisticWorker::new);
    }

    private Optional<LogisticWorkerConfig> getConfig(int index) {
        var machine = MACHINE.get(blockEntity);
        return machine.config().getCompound(PREFIX + index)
            .map(LogisticWorkerConfig::fromTag);
    }

    private static Optional<IPort> getPort(LogisticComponent logistic, BlockPos subnet,
        LogisticComponent.PortKey key) {
        return logistic.getPort(key, subnet)
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
        noValidSlot = true;

        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }
        var machine = MACHINE.get(blockEntity);
        var network = machine.network();
        if (network.isEmpty()) {
            return;
        }

        var logistic = network.get().getComponent(LOGISTIC_COMPONENT.get());
        var subnet = network.get().getSubnet(blockEntity.getBlockPos());

        var firstValidSlot = -1;
        var lastValidSlot = 0;
        var packet = SetMachineConfigPacket.builder();
        var needUpdate = false;

        for (var i = 0; i < workerSlots; i++) {
            var key = PREFIX + i;
            var entry = getConfig(i).filter(LogisticWorkerConfig::isValid);
            var valid = false;
            if (entry.isPresent()) {
                var entry1 = entry.get();
                if (!validateConfig(logistic, subnet, entry1)) {
                    entry1.setValid(false);
                    packet.set(key, entry1.serializeNBT());
                    needUpdate = true;
                } else {
                    valid = true;
                }
            }

            if (valid) {
                for (var j = lastValidSlot; j < i; j++) {
                    nextValidSlot[j] = i;
                }
                if (firstValidSlot < 0) {
                    firstValidSlot = i;
                }
                lastValidSlot = i;
            }
        }

        if (firstValidSlot >= 0) {
            for (var j = lastValidSlot; j < workerSlots; j++) {
                nextValidSlot[j] = firstValidSlot;
            }
            noValidSlot = false;
        }

        if (needUpdate) {
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

    private void onTick(Level world, INetwork network) {
        if (noValidSlot) {
            return;
        }
        if (tick < workerInterval) {
            tick++;
            return;
        }

        currentSlot = nextValidSlot[currentSlot];

        var entry = getConfig(currentSlot);
        if (entry.isEmpty() || !entry.get().isValid()) {
            // this should not happen, we should revalidate
            LOGGER.warn("{}: unexpected invalid entry slot {}", this, currentSlot);
            validateConfigs();
            return;
        }
        var entry1 = entry.get();

        var machine = MACHINE.get(blockEntity);
        var logistic = network.getComponent(LOGISTIC_COMPONENT.get());
        var subnet = network.getSubnet(blockEntity.getBlockPos());

        if (validateConfig(logistic, subnet, entry1)) {
            LOGGER.trace("{}: transmit entry slot {}", blockEntity, currentSlot);
            var from = entry1.from().flatMap(k -> getPort(logistic, subnet, k)).orElseThrow();
            var to = entry1.to().flatMap(k -> getPort(logistic, subnet, k)).orElseThrow();
            if (from.type() == PortType.ITEM) {
                transmitItem(from.asItem(), to.asItem());
            } else {
                transmitFluid(from.asFluid(), to.asFluid());
            }
            tick = 0;
        } else {
            LOGGER.trace("{}: entry slot {} becomes invalid", blockEntity, currentSlot);
            entry1.setValid(false);
            var packet = SetMachineConfigPacket.builder()
                .set(PREFIX + currentSlot, entry1.serializeNBT())
                .get();
            // try revalidate
            machine.setConfig(packet);
        }
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(CONNECT.get(), $ -> validateConfigs());
        eventManager.subscribe(BUILD_SCHEDULING.get(), this::buildScheduling);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::validateConfigs);
    }

    private void buildScheduling(INetworkComponent.SchedulingBuilder builder) {
        builder.add(LOGISTICS_SCHEDULING.get(), this::onTick);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ELECTRIC_MACHINE.get()) {
            return electricCap.cast();
        }
        return LazyOptional.empty();
    }

    public static Optional<LogisticWorker> tryGet(BlockEntity be) {
        return tryGetProvider(be, ID, LogisticWorker.class);
    }

    public static LogisticWorker get(BlockEntity be) {
        return getProvider(be, ID, LogisticWorker.class);
    }
}
