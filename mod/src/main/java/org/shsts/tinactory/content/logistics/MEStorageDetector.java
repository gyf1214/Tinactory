package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.network.SignalMachineBlock;
import org.shsts.tinactory.core.logistics.ISignalMachine;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllCapabilities.SIGNAL_MACHINE;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.STORAGE_DETECTOR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageDetector extends MEStorageAccess implements ISignalMachine {
    private static final String ID = "logistics/me_storage_detector";

    private int signal = 0;

    public MEStorageDetector(BlockEntity blockEntity, double power) {
        super(blockEntity, power);
        onUpdate(this::updateSignal);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.container(ID, be -> new MEStorageDetector(be, power));
    }

    private int toSignal(long amount, long targetAmount) {
        if (targetAmount <= 0) {
            return amount > 0 ? 15 : 0;
        }
        return MathUtil.toSignal((double) amount / targetAmount);
    }

    private int recalculateSignal() {
        var config = machine().config().get(STORAGE_DETECTOR);
        if (config.isEmpty()) {
            return 0;
        }
        var key = config.get().key();
        var targetAmount = config.get().amount();
        if (key == null) {
            return 0;
        }
        if (key.type() == PortType.ITEM) {
            var amount = combinedItem.getStorageAmount(StackHelper.ITEM_ADAPTER.stackOf(key));
            return toSignal(amount, targetAmount);
        } else if (key.type() == PortType.FLUID) {
            var amount = combinedFluid.getStorageAmount(StackHelper.FLUID_ADAPTER.stackOf(key));
            return toSignal(amount, targetAmount);
        } else {
            return 0;
        }
    }

    private void updateSignal() {
        var world = machine().world();
        if (world.isClientSide) {
            return;
        }
        var oldSignal = signal;
        signal = recalculateSignal();
        if (signal != oldSignal) {
            SignalMachineBlock.updateSignal(world, blockEntity);
        }
    }

    @Override
    protected void onConnect(INetwork network) {
        super.onConnect(network);
        updateSignal();
    }

    @Override
    public int getSignal() {
        return signal;
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::updateSignal);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        super.attachCapability(builder);
        builder.attach(SIGNAL_MACHINE, this);
    }
}
