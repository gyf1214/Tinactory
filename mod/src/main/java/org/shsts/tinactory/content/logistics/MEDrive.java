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
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.IBytesProvider;
import org.shsts.tinactory.core.logistics.CombinedFluidPort;
import org.shsts.tinactory.core.logistics.CombinedItemPort;
import org.shsts.tinactory.core.logistics.IMenuItemHandler;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.autocraft.integration.NetworkPatternCell;
import org.shsts.tinactory.core.autocraft.integration.IPatternCellPort;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;

import static org.shsts.tinactory.AllCapabilities.BYTES_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.DIGITAL_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.FLUID_PORT;
import static org.shsts.tinactory.AllCapabilities.ITEM_PORT;
import static org.shsts.tinactory.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllCapabilities.PATTERN_CELL;
import static org.shsts.tinactory.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.core.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEDrive extends CapabilityProvider implements IEventSubscriber,
    ILayoutProvider, IBytesProvider, INBTSerializable<CompoundTag> {
    public static final String PRIORITY_KEY = ElectricStorage.PRIORITY_KEY;
    public static final int PRIORITY_DEFAULT = 2;
    public static final String AMOUNT_SIGNAL = ElectricStorage.AMOUNT_SIGNAL;

    public record ByteStats(int bytesUsed, int bytesCapacity) {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "logistics/me_drive";

    private final BlockEntity blockEntity;
    private final Layout layout;
    private final WrapperItemHandler storages;
    private final CombinedItemPort combinedItems;
    private final CombinedFluidPort combinedFluids;
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

        this.combinedItems = new CombinedItemPort();
        combinedItems.onUpdate(this::onContainerChange);

        this.combinedFluids = new CombinedFluidPort();
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

    @Override
    public int bytesCapacity() {
        return collectByteStats().bytesCapacity();
    }

    @Override
    public int bytesUsed() {
        return collectByteStats().bytesUsed();
    }

    private ByteStats collectByteStats() {
        var digital = new ArrayList<IBytesProvider>();
        var patterns = new ArrayList<IPatternCellPort>();
        for (var i = 0; i < storages.getSlots(); i++) {
            var storage = storages.getStackInSlot(i);
            if (storage.isEmpty()) {
                continue;
            }
            storage.getCapability(DIGITAL_PROVIDER.get()).ifPresent(digital::add);
            storage.getCapability(PATTERN_CELL.get()).ifPresent(patterns::add);
        }
        return aggregateByteStats(digital, patterns);
    }

    public static ByteStats aggregateByteStats(
        Iterable<? extends IBytesProvider> digitalProviders,
        Iterable<? extends IPatternCellPort> patternPorts) {
        var bytesUsed = 0;
        var bytesCapacity = 0;
        for (var provider : digitalProviders) {
            bytesUsed += provider.bytesUsed();
            bytesCapacity += provider.bytesCapacity();
        }
        for (var patternPort : patternPorts) {
            bytesUsed += patternPort.bytesUsed();
            bytesCapacity += patternPort.bytesCapacity();
        }
        return new ByteStats(bytesUsed, bytesCapacity);
    }

    private int updateSignal() {
        var totalBytes = bytesUsed();
        var totalCapacity = bytesCapacity();
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
        var items = new ArrayList<IItemPort>();
        var fluids = new ArrayList<IFluidPort>();
        for (var i = 0; i < slots; i++) {
            var stack = storages.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            stack.getCapability(ITEM_PORT.get()).ifPresent(items::add);
            stack.getCapability(FLUID_PORT.get()).ifPresent(fluids::add);
        }
        combinedItems.setComposes(items);
        combinedFluids.setComposes(fluids);
        if (machine != null) {
            machine.network().ifPresent(network -> {
                var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
                logistics.unregisterPatternCells(machine.uuid());
                var subnet = network.getSubnet(blockEntity.getBlockPos());
                var priority = machineConfig.getInt(PRIORITY_KEY, PRIORITY_DEFAULT);
                for (var i = 0; i < slots; i++) {
                    var stack = storages.getStackInSlot(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    var slotIndex = i;
                    stack.getCapability(PATTERN_CELL.get()).ifPresent(port ->
                        logistics.registerPatternCell(new NetworkPatternCell(
                            machine.uuid(), subnet, priority, slotIndex, port)));
                }
            });
        }
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
        if (cap == LAYOUT_PROVIDER.get() || cap == BYTES_PROVIDER.get()) {
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
