package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.gui.ILayoutProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ElectricStorage extends CapabilityProvider implements ILayoutProvider, IEventSubscriber {
    public static final String UNLOCK_KEY = "unlockChest";
    public static final boolean UNLOCK_DEFAULT = false;
    public static final String PRIORITY_KEY = "priority";
    public static final int PRIORITY_DEFAULT = 2;
    public static final String VOID_KEY = "void";
    public static final boolean VOID_DEFAULT = false;
    public static final String AMOUNT_SIGNAL = "amount";

    protected final BlockEntity blockEntity;
    private final Layout layout;
    private final IElectricMachine electric;

    protected IMachine machine;
    protected IMachineConfig machineConfig;
    private int amountSignal = 0;

    protected ElectricStorage(BlockEntity blockEntity, Layout layout, IElectricMachine electric) {
        this.blockEntity = blockEntity;
        this.layout = layout;
        this.electric = electric;
    }

    protected ElectricStorage(BlockEntity blockEntity, Layout layout, double power) {
        this(blockEntity, layout, new SimpleElectricConsumer(
            getBlockVoltage(blockEntity).value, power));
    }

    protected IMachine machine() {
        if (machine == null) {
            machine = MACHINE.get(blockEntity);
        }
        return machine;
    }

    protected IMachineConfig machineConfig() {
        if (machineConfig == null) {
            machineConfig = machine().config();
        }
        return machineConfig;
    }

    public boolean isUnlocked() {
        return machineConfig().getBoolean(UNLOCK_KEY, UNLOCK_DEFAULT);
    }

    public boolean isVoid() {
        return machineConfig().getBoolean(VOID_KEY, VOID_DEFAULT);
    }

    protected void registerPort(INetwork network, IPort<?> port) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        logistics.unregisterPort(machine(), 0);
        logistics.registerStoragePort(machine(), 0, port,
            machineConfig().getInt(PRIORITY_KEY, PRIORITY_DEFAULT));
    }

    protected void onSlotChange() {
        blockEntity.setChanged();
        amountSignal = updateSignal();
    }

    protected abstract void onMachineConfig();

    protected abstract int updateSignal();

    private void onConnect(INetwork network) {
        onMachineConfig();

        var signal = network.getComponent(SIGNAL_COMPONENT.get());
        signal.registerRead(machine(), AMOUNT_SIGNAL, () -> amountSignal);
        amountSignal = updateSignal();
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(LAYOUT_PROVIDER, this);
        builder.attach(ELECTRIC_MACHINE, electric);
    }
}
