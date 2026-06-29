package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.content.network.SignalMachineBlock;
import org.shsts.tinactory.core.logistics.ISignalMachine;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.SIGNAL_MACHINE;
import static org.shsts.tinactory.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.AllNetworks.SIGNAL_READ_SCHEDULING;
import static org.shsts.tinactory.AllNetworks.SIGNAL_WRITE_SCHEDULING;
import static org.shsts.tinactory.integration.network.MachineBlock.FACING;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MESignalController extends CapabilityProvider implements IEventSubscriber, ISignalMachine {
    public static final String SIGNAL_CONFIG_KEY = "signal";
    private static final String ID = "logistics/me_signal_controller";

    private final BlockEntity blockEntity;
    private int signal = 0;
    @Nullable
    private SignalConfig config = null;
    private boolean isWrite;
    private boolean needRevalidate = true;

    private final IElectricMachine electric;

    public MESignalController(BlockEntity blockEntity, double power) {
        this.blockEntity = blockEntity;

        var voltage = getBlockVoltage(blockEntity);
        this.electric = new SimpleElectricConsumer(voltage.value, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.capability(ID, be -> new MESignalController(be, power));
    }

    @Override
    public int getSignal() {
        return signal;
    }

    private void validateConfig(IMachine machine, SignalComponent component) {
        needRevalidate = false;
        var config1 = machine.config()
            .getCompound(SIGNAL_CONFIG_KEY)
            .map(SignalConfig::fromTag);

        if (config1.isEmpty()) {
            config = null;
            return;
        }

        var config2 = config1.get();
        if (component.has(machine, config2.machine(), config2.key(), false)) {
            config = config2;
            isWrite = false;
        } else if (component.has(machine, config2.machine(), config2.key(), true)) {
            config = config2;
            isWrite = true;
        } else {
            config = null;
        }
    }

    private void readSignal(Level world, INetwork network) {
        var component = network.getComponent(SIGNAL_COMPONENT.get());
        var machine = MACHINE.get(blockEntity);
        if (needRevalidate) {
            validateConfig(machine, component);
        }
        var oldSignal = signal;
        signal = config != null && !isWrite ?
            component.read(machine, config.machine(), config.key()) : 0;
        if (signal != oldSignal) {
            SignalMachineBlock.updateSignal(world, blockEntity);
        }
    }

    private void writeSignal(Level world, INetwork network) {
        var component = network.getComponent(SIGNAL_COMPONENT.get());
        var machine = MACHINE.get(blockEntity);
        if (needRevalidate) {
            validateConfig(machine, component);
        }
        if (config == null || !isWrite) {
            return;
        }

        var pos = blockEntity.getBlockPos();
        var state = blockEntity.getBlockState();
        if (!state.hasProperty(FACING)) {
            return;
        }
        var dir = state.getValue(FACING);
        var pos1 = pos.relative(dir);
        var signal = world.getDirectSignal(pos1, dir);
        component.write(machine, config.machine(), config.key(), signal);
    }

    private void buildScheduling(ISchedulingRegister builder) {
        builder.add(SIGNAL_READ_SCHEDULING.get(), this::readSignal);
        builder.add(SIGNAL_WRITE_SCHEDULING.get(), this::writeSignal);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(CONNECT.get(), $ -> needRevalidate = true);
        eventManager.subscribe(BUILD_SCHEDULING.get(), this::buildScheduling);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), () -> needRevalidate = true);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(ELECTRIC_MACHINE, electric);
        builder.attach(SIGNAL_MACHINE, this);
    }
}
