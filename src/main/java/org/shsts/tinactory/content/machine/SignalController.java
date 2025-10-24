package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;

import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.SIGNAL_MACHINE;
import static org.shsts.tinactory.content.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.content.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.content.AllNetworks.SIGNAL_READ_SCHEDULING;
import static org.shsts.tinactory.content.AllNetworks.SIGNAL_WRITE_SCHEDULING;
import static org.shsts.tinactory.content.network.MachineBlock.FACING;
import static org.shsts.tinactory.content.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SignalController extends CapabilityProvider implements IEventSubscriber, ISignalMachine {
    private static final String CONFIG_KEY = "signal";

    private final BlockEntity blockEntity;
    private int signal = 0;
    @Nullable
    private SignalConfig config = null;
    private boolean isWrite;
    private boolean needRevalidate = true;

    private final LazyOptional<IElectricMachine> electricCap;

    public SignalController(BlockEntity blockEntity, double power) {
        this.blockEntity = blockEntity;

        var voltage = getBlockVoltage(blockEntity);
        var electric = new SimpleElectricConsumer(voltage.value, power);
        this.electricCap = LazyOptional.of(() -> electric);
    }

    @Override
    public int getSignal() {
        return signal;
    }

    private void validateConfig(SignalComponent component) {
        needRevalidate = false;
        var config1 = MACHINE.get(blockEntity).config()
            .getCompound(CONFIG_KEY)
            .map(SignalConfig::fromTag);

        if (config1.isEmpty()) {
            config = null;
            return;
        }

        var config2 = config1.get();
        if (component.has(config2.machine(), config2.key(), false)) {
            config = config2;
            isWrite = false;
        } else if (component.has(config2.machine(), config2.key(), true)) {
            config = config2;
            isWrite = true;
        } else {
            config = null;
        }
    }

    private void readSignal(Level world, INetwork network) {
        var component = network.getComponent(SIGNAL_COMPONENT.get());
        if (needRevalidate) {
            validateConfig(component);
        }
        if (config == null || isWrite) {
            return;
        }
        signal = component.read(config.machine(), config.key());
    }

    private void writeSignal(Level world, INetwork network) {
        var component = network.getComponent(SIGNAL_COMPONENT.get());
        if (needRevalidate) {
            validateConfig(component);
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
        component.write(config.machine(), config.key(), signal);
    }

    private void buildScheduling(INetworkComponent.SchedulingBuilder builder) {
        builder.add(SIGNAL_READ_SCHEDULING.get(), this::readSignal);
        builder.add(SIGNAL_WRITE_SCHEDULING.get(), this::writeSignal);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(BUILD_SCHEDULING.get(), this::buildScheduling);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ELECTRIC_MACHINE.get()) {
            return electricCap.cast();
        } else if (cap == SIGNAL_MACHINE.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }
}
