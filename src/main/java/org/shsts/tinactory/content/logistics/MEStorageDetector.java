package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.network.SignalMachineBlock;
import org.shsts.tinactory.core.logistics.ISignalMachine;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllCapabilities.SIGNAL_MACHINE;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageDetector extends MEStorageAccess implements ISignalMachine {
    public static final String TARGET_ITEM_KEY = "targetItem";
    public static final String TARGET_FLUID_KEY = "targetFluid";
    public static final String TARGET_AMOUNT_KEY = "targetAmount";
    private static final String ID = "logistics/me_storage_detector";

    private ItemStack targetItem = ItemStack.EMPTY;
    private FluidStack targetFluid = FluidStack.EMPTY;
    private int targetAmount = 0;
    private int signal = 0;

    public MEStorageDetector(BlockEntity blockEntity, double power) {
        super(blockEntity, power);
        onUpdate(this::updateSignal);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.capability(ID, be -> new MEStorageDetector(be, power));
    }

    public static ItemStack targetItem(IMachineConfig config) {
        return config.getCompound(TARGET_ITEM_KEY)
            .map(ItemStack::of)
            .orElse(ItemStack.EMPTY);
    }

    public static FluidStack targetFluid(IMachineConfig config) {
        return config.getCompound(TARGET_FLUID_KEY)
            .map(FluidStack::loadFluidStackFromNBT)
            .orElse(FluidStack.EMPTY);
    }

    private void validateConfig() {
        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }
        var config = machine.config();
        targetItem = targetItem(config);
        targetFluid = targetFluid(config);
        targetAmount = config.getInt(TARGET_AMOUNT_KEY, 0);

        updateSignal();
    }

    private int toSignal(int amount) {
        if (targetAmount <= 0) {
            return amount > 0 ? 15 : 0;
        }
        return MathUtil.toSignal((double) amount / targetAmount);
    }

    private int recalculateSignal() {
        if (!targetItem.isEmpty()) {
            return toSignal(combinedItem.getItemCount(targetItem));
        } else if (!targetFluid.isEmpty()) {
            return toSignal(combinedFluid.getFluidAmount(targetFluid));
        } else {
            return 0;
        }
    }

    private void updateSignal() {
        var oldSignal = signal;
        signal = recalculateSignal();
        var world = machine.world();
        if (!world.isClientSide && signal != oldSignal) {
            SignalMachineBlock.updateSignal(world, blockEntity);
        }
    }

    @Override
    protected void onConnect(INetwork network) {
        super.onConnect(network);
        validateConfig();
    }

    @Override
    public int getSignal() {
        return signal;
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::validateConfig);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == SIGNAL_MACHINE.get()) {
            return myself();
        }
        return super.getCapability(cap, side);
    }
}
