package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.core.logistics.PortTransmitter;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.machine.Machine;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Predicate;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.LOGISTICS_SCHEDULING;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.PRE_SIGNAL_SCHEDULING;
import static org.shsts.tinactory.content.logistics.LogisticWorkerConfig.PREFIX;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorker extends CapabilityProvider implements IEventSubscriber {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "logistics/logistic_worker";
    private static final PortTransmitter<ItemStack> ITEM_TRANSMITTER =
        new PortTransmitter<>(StackHelper.ITEM_ADAPTER);
    private static final PortTransmitter<FluidStack> FLUID_TRANSMITTER =
        new PortTransmitter<>(StackHelper.FLUID_ADAPTER);

    private final BlockEntity blockEntity;
    public final int workerSlots;
    private final int workerInterval;
    private final int itemBandwidth;
    private final int fluidBandwidth;
    private final int[] nextValidSlot;
    private int tick = 0;
    private int currentSlot;
    private boolean stopped = false;
    private boolean noValidSlot = true;
    private boolean needRevalidate = true;

    private final IElectricMachine electric;

    public record Properties(int slots, int interval, int itemBandwidth, int fluidBandwidth, double power) {}

    public LogisticWorker(BlockEntity blockEntity, Properties properties) {
        this.blockEntity = blockEntity;
        this.workerSlots = properties.slots;
        this.workerInterval = properties.interval;
        this.itemBandwidth = properties.itemBandwidth;
        this.fluidBandwidth = properties.fluidBandwidth;
        this.nextValidSlot = new int[workerSlots];
        // initialize current slot to the last slot so in the first tick it will select the first valid slot
        this.currentSlot = workerSlots - 1;

        var voltage = getBlockVoltage(blockEntity);
        this.electric = new SimpleElectricConsumer(voltage.value, properties.power) {
            @Override
            public ElectricMachineType getMachineType() {
                return stopped ? ElectricMachineType.NONE : super.getMachineType();
            }

            @Override
            public double getPowerCons() {
                return stopped ? 0 : super.getPowerCons();
            }
        };
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Properties properties) {
        return $ -> $.container(ID, be -> new LogisticWorker(be, properties));
    }

    private Optional<LogisticWorkerConfig> getConfig(int index) {
        var machine = MACHINE.get(blockEntity);
        var provider = machine.registryAccess();
        return machine.config().getCompound(PREFIX + index)
            .map($ -> LogisticWorkerConfig.fromTag(provider, $));
    }

    private static Optional<IPort<?>> getPort(IMachine machine, LogisticComponent logistic,
        LogisticComponent.PortKey key) {
        return logistic.getPort(machine, key)
            .map(LogisticComponent.PortInfo::port);
    }

    private static boolean validateConfig(IMachine machine, LogisticComponent logistic,
        LogisticWorkerConfig entry) {
        if (!entry.isValid()) {
            return true;
        }
        var from = entry.from();
        var to = entry.to();
        if (from.isEmpty() || to.isEmpty() || from.get().equals(to.get())) {
            return false;
        }
        var from1 = getPort(machine, logistic, from.get());
        var to1 = getPort(machine, logistic, to.get());
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

        var firstValidSlot = -1;
        var lastValidSlot = 0;

        for (var i = 0; i < workerSlots; i++) {
            var valid = getConfig(i)
                .filter(LogisticWorkerConfig::isValid)
                .filter(entry -> validateConfig(machine, logistic, entry))
                .isPresent();

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
    }

    private ItemStack selectTransmittedItem(IPort<ItemStack> from, IPort<ItemStack> to,
        LogisticWorkerConfig config) {
        var filterType = config.filterType();
        if (filterType == LogisticWorkerConfig.FilterType.ITEM) {
            return ITEM_TRANSMITTER.probeIdentity(from, to, config.itemFilter(), itemBandwidth);
        }

        Predicate<ItemStack> filter = filterType == LogisticWorkerConfig.FilterType.TAG ?
            stack -> stack.is(config.tagFilter()) : StackHelper.TRUE_FILTER;
        return ITEM_TRANSMITTER.select(from, to,
            from.getAllStorages().stream().filter(filter).toList(), itemBandwidth);
    }

    private void transmitItem(IPort<ItemStack> from, IPort<ItemStack> to, LogisticWorkerConfig config) {
        var stack = selectTransmittedItem(from, to, config);
        if (stack.isEmpty()) {
            return;
        }
        var remaining = ITEM_TRANSMITTER.transmit(from, to, stack);
        if (!remaining.isEmpty()) {
            LOGGER.warn("transmit item failed from={} to={} content={}", from, to, stack);
        }
    }

    private FluidStack selectTransmittedFluid(IPort<FluidStack> from, IPort<FluidStack> to,
        FluidStack filter) {
        if (!filter.isEmpty()) {
            return FLUID_TRANSMITTER.probeIdentity(from, to, filter, fluidBandwidth);
        }
        return FLUID_TRANSMITTER.select(from, to, from.getAllStorages(), fluidBandwidth);
    }

    private void transmitFluid(IPort<FluidStack> from, IPort<FluidStack> to, FluidStack filter) {
        var stack = selectTransmittedFluid(from, to, filter);
        var remaining = FLUID_TRANSMITTER.transmit(from, to, stack);
        if (!remaining.isEmpty()) {
            LOGGER.warn("transmit fluid failed from={} to={} content={}", from, to, stack);
        }
    }

    private void onConnect(INetwork network) {
        Machine.registerStopSignal(network, MACHINE.get(blockEntity), $ -> stopped = $);
        needRevalidate = true;
    }

    private void onTick(Level world, INetwork network) {
        if (needRevalidate) {
            validateConfigs();
        }
        if (stopped || noValidSlot) {
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

        var logistic = network.getComponent(LOGISTIC_COMPONENT.get());

        var machine = MACHINE.get(blockEntity);
        if (validateConfig(machine, logistic, entry1)) {
            LOGGER.trace("{}: transmit entry slot {}", blockEntity, currentSlot);
            var from = entry1.from().flatMap(k -> getPort(machine, logistic, k)).orElseThrow();
            var to = entry1.to().flatMap(k -> getPort(machine, logistic, k)).orElseThrow();
            if (from.type() == PortType.ITEM) {
                transmitItem(from.asItem(), to.asItem(), entry1);
            } else {
                transmitFluid(from.asFluid(), to.asFluid(), entry1.fluidFilter());
            }
            tick = 0;
        } else {
            LOGGER.trace("{}: entry slot {} becomes invalid", blockEntity, currentSlot);
            needRevalidate = true;
        }
    }

    private void buildScheduling(ISchedulingRegister builder) {
        builder.add(PRE_SIGNAL_SCHEDULING.get(), (world, network) -> stopped = false);
        builder.add(LOGISTICS_SCHEDULING.get(), this::onTick);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(BUILD_SCHEDULING.get(), this::buildScheduling);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), () -> needRevalidate = true);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(ELECTRIC_MACHINE, electric);
    }

    public static Optional<LogisticWorker> tryGet(BlockEntity be) {
        return tryGetContainer(be, ID, LogisticWorker.class);
    }

    public static LogisticWorker get(BlockEntity be) {
        return getContainer(be, ID, LogisticWorker.class);
    }
}
