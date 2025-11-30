package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Predicate;

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
    private boolean stopped = false;
    private boolean noValidSlot = true;
    private boolean needRevalidate = true;

    private final LazyOptional<IElectricMachine> electricCap;

    public record Properties(int slots, int interval, int stack, int fluidStack, double power) {}

    public LogisticWorker(BlockEntity blockEntity, Properties properties) {
        this.blockEntity = blockEntity;
        this.workerSlots = properties.slots;
        this.workerInterval = properties.interval;
        this.workerStack = properties.stack;
        this.workerFluidStack = properties.fluidStack;
        this.nextValidSlot = new int[workerSlots];
        // initialize current slot to the last slot so in the first tick it will select the first valid slot
        this.currentSlot = workerSlots - 1;

        var voltage = getBlockVoltage(blockEntity);
        var electric = new SimpleElectricConsumer(voltage.value, properties.power) {
            @Override
            public double getPowerCons() {
                return stopped ? 0 : super.getPowerCons();
            }
        };
        this.electricCap = LazyOptional.of(() -> electric);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Properties properties) {
        return $ -> $.capability(ID, be -> new LogisticWorker(be, properties));
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
        return from1.isPresent() && to1.isPresent() && from1.get().type() == to1.get().type() &&
            (entry.filterType() == LogisticWorkerConfig.FilterType.NONE ||
                entry.filterType().portType == from1.get().type());
    }

    private void validateConfigs() {
        needRevalidate = false;
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

    private ItemStack testTransmitItem(IItemCollection from, IItemCollection to, ItemStack stack) {
        var stack1 = StackHelper.copyWithCount(stack, workerStack);
        var stack2 = from.extractItem(stack1, true);
        var remaining = to.insertItem(stack2, true);
        var limit = stack2.getCount() - remaining.getCount();
        if (limit > 0) {
            stack2.setCount(limit);
            return stack2;
        } else {
            return ItemStack.EMPTY;
        }
    }

    private ItemStack selectTransmittedItem(IItemCollection from, IItemCollection to, LogisticWorkerConfig config) {
        var filterType = config.filterType();
        if (filterType == LogisticWorkerConfig.FilterType.ITEM) {
            return testTransmitItem(from, to, config.itemFilter());
        }

        Predicate<ItemStack> filter = filterType == LogisticWorkerConfig.FilterType.TAG ?
            stack -> stack.is(config.tagFilter()) : StackHelper.TRUE_FILTER;
        for (var stack : from.getAllItems()) {
            if (!filter.test(stack)) {
                continue;
            }
            var stack1 = testTransmitItem(from, to, stack);
            if (!stack1.isEmpty()) {
                return stack1;
            }
        }
        return ItemStack.EMPTY;
    }

    private void transmitItem(IItemCollection from, IItemCollection to, LogisticWorkerConfig config) {
        var stack = selectTransmittedItem(from, to, config);
        if (stack.isEmpty()) {
            return;
        }
        var stack1 = from.extractItem(stack, false);
        var remaining = to.insertItem(stack1, false);
        if (!remaining.isEmpty()) {
            LOGGER.warn("transmit item failed from={} to={} content={}", from, to, stack);
        }
    }

    private FluidStack testTransmitFluid(IFluidCollection from, IFluidCollection to, FluidStack stack) {
        var stack1 = StackHelper.copyWithAmount(stack, workerFluidStack);
        var stack2 = from.drain(stack1, true);
        var limit = to.fill(stack2, true);
        if (limit > 0) {
            stack2.setAmount(limit);
            return stack2;
        } else {
            return FluidStack.EMPTY;
        }
    }

    private FluidStack selectTransmittedFluid(IFluidCollection from, IFluidCollection to, FluidStack filter) {
        if (!filter.isEmpty()) {
            return testTransmitFluid(from, to, filter);
        }

        for (var stack : from.getAllFluids()) {
            var stack1 = testTransmitFluid(from, to, stack);
            if (!stack1.isEmpty()) {
                return stack1;
            }
        }
        return FluidStack.EMPTY;
    }

    private void transmitFluid(IFluidCollection from, IFluidCollection to, FluidStack filter) {
        var stack = selectTransmittedFluid(from, to, filter);
        var stack1 = from.drain(stack, false);
        var inserted = to.fill(stack1, false);
        if (inserted != stack1.getAmount()) {
            LOGGER.warn("transmit fluid failed from={} to={} content={}", from, to, stack);
        }
    }

    private void onConnect(INetwork network) {
        Machine.registerStopSignal(network, MACHINE.get(blockEntity), $ -> stopped = $);
    }

    private void onTick(Level world, INetwork network) {
        if (needRevalidate) {
            validateConfigs();
        }
        if (stopped) {
            stopped = false;
            return;
        }
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
            needRevalidate = true;
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
                transmitItem(from.asItem(), to.asItem(), entry1);
            } else {
                transmitFluid(from.asFluid(), to.asFluid(), entry1.fluidFilter());
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
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(BUILD_SCHEDULING.get(), this::buildScheduling);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), () -> needRevalidate = true);
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
