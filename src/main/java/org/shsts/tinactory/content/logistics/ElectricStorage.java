package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;

import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.content.AllEvents.CONNECT;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.content.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.content.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ElectricStorage extends CapabilityProvider implements ILayoutProvider, IEventSubscriber {
    public static final String UNLOCK_KEY = "unlockChest";
    public static final boolean UNLOCK_DEFAULT = false;
    public static final String PRIORITY_KEY = "priority";
    public static final int PRIORITY_DEFAULT = -1;
    public static final String GLOBAL_KEY = "global";
    public static final boolean GLOBAL_DEFAULT = false;
    public static final String VOID_KEY = "void";
    public static final boolean VOID_DEFAULT = false;
    public static final String AMOUNT_SIGNAL = "amount";

    protected final BlockEntity blockEntity;
    private final Layout layout;
    private final LazyOptional<IElectricMachine> electricCap;

    protected IMachine machine;
    protected IMachineConfig machineConfig;
    private int amountSignal = 0;

    protected ElectricStorage(BlockEntity blockEntity, Layout layout, IElectricMachine electric) {
        this.blockEntity = blockEntity;
        this.layout = layout;
        this.electricCap = LazyOptional.of(() -> electric);
    }

    protected ElectricStorage(BlockEntity blockEntity, Layout layout, double power) {
        this(blockEntity, layout, new SimpleElectricConsumer(
            getBlockVoltage(blockEntity).value, power));
    }

    public boolean isUnlocked() {
        return machineConfig.getBoolean(UNLOCK_KEY, UNLOCK_DEFAULT);
    }

    public boolean isVoid() {
        return machineConfig.getBoolean(VOID_KEY, VOID_DEFAULT);
    }

    protected void registerPort(INetwork network, IPort port) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        logistics.unregisterPort(machine, 0);
        logistics.registerStoragePort(machine, 0, port,
            machineConfig.getBoolean(GLOBAL_KEY, GLOBAL_DEFAULT),
            machineConfig.getInt(PRIORITY_KEY, PRIORITY_DEFAULT));
    }

    protected void onSlotChange() {
        blockEntity.setChanged();
        amountSignal = updateSignal();
    }

    private void onLoad() {
        machine = MACHINE.get(blockEntity);
        machineConfig = machine.config();
    }

    protected abstract void onMachineConfig();

    protected abstract int updateSignal();

    private void onConnect(INetwork network) {
        onMachineConfig();

        var signal = network.getComponent(SIGNAL_COMPONENT.get());
        signal.registerRead(machine, AMOUNT_SIGNAL, () -> amountSignal);
        amountSignal = updateSignal();
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == LAYOUT_PROVIDER.get()) {
            return myself();
        } else if (cap == ELECTRIC_MACHINE.get()) {
            return electricCap.cast();
        }
        return LazyOptional.empty();
    }
}
