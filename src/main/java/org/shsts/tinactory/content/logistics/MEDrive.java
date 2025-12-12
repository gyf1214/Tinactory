package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.AllTags;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidCollection;
import org.shsts.tinactory.core.logistics.CombinedItemCollection;
import org.shsts.tinactory.core.logistics.IMenuItemHandler;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.shsts.tinactory.AllCapabilities.DIGITAL_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.FLUID_COLLECTION;
import static org.shsts.tinactory.AllCapabilities.ITEM_COLLECTION;
import static org.shsts.tinactory.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.content.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEDrive extends CapabilityProvider
    implements IEventSubscriber, ILayoutProvider, INBTSerializable<CompoundTag> {
    public static final String PRIORITY_KEY = ElectricStorage.PRIORITY_KEY;
    public static final int PRIORITY_DEFAULT = 2;
    public static final String AMOUNT_SIGNAL = ElectricStorage.AMOUNT_SIGNAL;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "logistics/me_drive";

    private final BlockEntity blockEntity;
    private final Layout layout;
    private final WrapperItemHandler storages;
    private final CombinedItemCollection combinedItems;
    private final CombinedFluidCollection combinedFluids;
    private final LazyOptional<IMenuItemHandler> menuItemHandlerCap;
    private final LazyOptional<IElectricMachine> electricCap;

    private IMachine machine;
    private IMachineConfig machineConfig;
    private int amountSignal = 0;

    public MEDrive(BlockEntity blockEntity, Layout layout, double power) {
        this.blockEntity = blockEntity;
        this.layout = layout;
        var size = layout.slots.size();
        this.storages = new WrapperItemHandler(size);
        for (var i = 0; i < size; i++) {
            storages.setFilter(i, this::allowItem);
        }
        this.menuItemHandlerCap = IMenuItemHandler.cap(storages);
        storages.onUpdate(this::onStorageChange);

        this.combinedItems = new CombinedItemCollection();
        combinedItems.onUpdate(this::onContainerChange);

        this.combinedFluids = new CombinedFluidCollection();
        combinedFluids.onUpdate(this::onContainerChange);

        var voltage = getBlockVoltage(blockEntity);
        var electric = new SimpleElectricConsumer(voltage.value, power);
        this.electricCap = LazyOptional.of(() -> electric);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Layout layout, double power) {
        return $ -> $.capability(ID, be -> new MEDrive(be, layout, power));
    }

    private boolean allowItem(ItemStack stack) {
        return stack.is(AllTags.STORAGE_CELL);
    }

    private int updateSignal() {
        var totalBytes = 0;
        var totalCapacity = 0;
        for (var i = 0; i < storages.getSlots(); i++) {
            var storage = storages.getStackInSlot(i);
            if (storage.isEmpty()) {
                continue;
            }
            var cap = storage.getCapability(DIGITAL_PROVIDER.get());
            if (cap.isPresent()) {
                var cap1 = cap.orElseThrow(NoSuchElementException::new);
                totalBytes += cap1.bytesUsed();
                totalCapacity += cap1.capacity();
            }
        }
        return totalCapacity == 0 ? 0 : MathUtil.toSignal((double) totalBytes / totalCapacity);
    }

    private void registerPort(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        var priority = machineConfig.getInt(PRIORITY_KEY, PRIORITY_DEFAULT);
        logistics.unregisterPort(machine, 0);
        logistics.unregisterPort(machine, 1);
        logistics.registerStoragePort(machine, 0, combinedItems, false, priority);
        logistics.registerStoragePort(machine, 1, combinedFluids, false, priority);
    }

    private void onConnect(INetwork network) {
        registerPort(network);

        var signal = network.getComponent(SIGNAL_COMPONENT.get());
        signal.registerRead(machine, AMOUNT_SIGNAL, () -> amountSignal);
        amountSignal = updateSignal();
    }

    private void onContainerChange() {
        amountSignal = updateSignal();
        blockEntity.setChanged();
    }

    private void onStorageChange() {
        var world = blockEntity.getLevel();
        if (world != null && world.isClientSide) {
            return;
        }
        LOGGER.debug("{} on update storage", blockEntity);
        var slots = storages.getSlots();
        var items = new ArrayList<IItemCollection>();
        var fluids = new ArrayList<IFluidCollection>();
        for (var i = 0; i < slots; i++) {
            var stack = storages.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            stack.getCapability(ITEM_COLLECTION.get()).ifPresent(items::add);
            stack.getCapability(FLUID_COLLECTION.get()).ifPresent(fluids::add);
        }
        combinedItems.setComposes(items);
        combinedFluids.setComposes(fluids);
        onContainerChange();
    }

    private void onMachineConfig() {
        machine.network().ifPresent(this::registerPort);
    }

    private void onLoad() {
        machine = MACHINE.get(blockEntity);
        machineConfig = machine.config();
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == LAYOUT_PROVIDER.get()) {
            return myself();
        } else if (cap == MENU_ITEM_HANDLER.get()) {
            return menuItemHandlerCap.cast();
        } else if (cap == ELECTRIC_MACHINE.get()) {
            return electricCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
        eventManager.subscribe(REMOVED_IN_WORLD.get(), world ->
            StackHelper.dropItemHandler(world, blockEntity.getBlockPos(), storages));
    }

    @Override
    public CompoundTag serializeNBT() {
        return StackHelper.serializeItemHandler(storages);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        StackHelper.deserializeItemHandler(storages, tag);
    }
}
