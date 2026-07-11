package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.AllTags;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.gui.ILayoutProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedPort;
import org.shsts.tinactory.core.logistics.IBytesProvider;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.logistics.StoragePorts;
import org.shsts.tinactory.integration.logistics.WrapperItemHandler;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;

import static org.shsts.tinactory.AllCapabilities.BYTES_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.BYTES_PROVIDER_ITEM;
import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.FLUID_PORT_ITEM;
import static org.shsts.tinactory.AllCapabilities.ITEM_PORT_ITEM;
import static org.shsts.tinactory.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllCapabilities.PATTERN_CELL_ITEM;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.AUTOCRAFT_COMPONENT;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEDrive extends CapabilityProvider implements IEventSubscriber,
    ILayoutProvider, IBytesProvider, INBTSerializable<CompoundTag> {
    public static final String PRIORITY_KEY = ElectricStorage.PRIORITY_KEY;
    public static final int PRIORITY_DEFAULT = ElectricStorage.PRIORITY_DEFAULT;
    public static final String AMOUNT_SIGNAL = ElectricStorage.AMOUNT_SIGNAL;

    public record ByteStats(long bytesUsed, long bytesCapacity) {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "logistics/me_drive";

    private final BlockEntity blockEntity;
    private final Layout layout;
    private final WrapperItemHandler storages;
    private final CombinedPort<ItemStack> combinedItems;
    private final CombinedPort<FluidStack> combinedFluids;
    private final IElectricMachine electric;

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
        storages.onUpdate(this::onStorageChange);

        this.combinedItems = StoragePorts.combinedItem();
        combinedItems.onUpdate(this::onContainerChange);

        this.combinedFluids = StoragePorts.combinedFluid();
        combinedFluids.onUpdate(this::onContainerChange);

        var voltage = getBlockVoltage(blockEntity);
        this.electric = new SimpleElectricConsumer(voltage.value, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Layout layout, double power) {
        return $ -> $.container(ID, be -> new MEDrive(be, layout, power));
    }

    private boolean allowItem(ItemStack stack) {
        return stack.is(AllTags.STORAGE_CELL);
    }

    @Override
    public long bytesCapacity() {
        return collectByteStats().bytesCapacity();
    }

    @Override
    public long bytesUsed() {
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
            BYTES_PROVIDER_ITEM.tryGet(storage).ifPresent(digital::add);
            PATTERN_CELL_ITEM.tryGet(storage).ifPresent(patterns::add);
        }
        return aggregateByteStats(digital, patterns);
    }

    public static ByteStats aggregateByteStats(
        Iterable<? extends IBytesProvider> digitalProviders,
        Iterable<? extends IPatternCellPort> patternPorts) {
        var bytesUsed = 0L;
        var bytesCapacity = 0L;
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

    private IMachine machine() {
        if (machine == null) {
            machine = MACHINE.get(blockEntity);
        }
        return machine;
    }

    private IMachineConfig machineConfig() {
        if (machineConfig == null) {
            machineConfig = machine().config();
        }
        return machineConfig;
    }

    private int updateSignal() {
        var totalBytes = bytesUsed();
        var totalCapacity = bytesCapacity();
        return totalCapacity == 0 ? 0 : MathUtil.toSignal((double) totalBytes / totalCapacity);
    }

    private void registerPort(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        var priority = machineConfig().getInt(PRIORITY_KEY, PRIORITY_DEFAULT);
        logistics.unregisterPort(machine(), 0);
        logistics.unregisterPort(machine(), 1);
        logistics.registerStoragePort(machine(), 0, combinedItems, priority);
        logistics.registerStoragePort(machine(), 1, combinedFluids, priority);
    }

    private void onConnect(INetwork network) {
        registerPort(network);
        registerPatternCells(network);

        var signal = network.getComponent(SIGNAL_COMPONENT.get());
        signal.registerRead(machine(), AMOUNT_SIGNAL, () -> amountSignal);
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
        var items = new ArrayList<IPort<ItemStack>>();
        var fluids = new ArrayList<IPort<FluidStack>>();
        for (var i = 0; i < slots; i++) {
            var stack = storages.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ITEM_PORT_ITEM.tryGet(stack).ifPresent(items::add);
            FLUID_PORT_ITEM.tryGet(stack).ifPresent(fluids::add);
        }
        combinedItems.setComposes(items);
        combinedFluids.setComposes(fluids);
        if (machine != null) {
            machine().network().ifPresent(this::registerPatternCells);
        }
        onContainerChange();
    }

    private void registerPatternCells(INetwork network) {
        var patternRepository = network.getComponent(AUTOCRAFT_COMPONENT.get()).patternRepository();
        patternRepository.removeCellPorts(machine().uuid());
        var priority = machineConfig().getInt(PRIORITY_KEY, PRIORITY_DEFAULT);
        for (var i = 0; i < storages.getSlots(); i++) {
            var stack = storages.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            var slotIndex = i;
            PATTERN_CELL_ITEM.tryGet(stack).ifPresent(port ->
                patternRepository.addCellPort(machine().uuid(), priority, slotIndex, port));
        }
    }

    private void onMachineConfig() {
        machine().network().ifPresent(network -> {
            registerPort(network);
            registerPatternCells(network);
        });
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(LAYOUT_PROVIDER, this);
        builder.attach(BYTES_PROVIDER, this);
        builder.attach(MENU_ITEM_HANDLER, storages);
        builder.attach(ELECTRIC_MACHINE, electric);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
        eventManager.subscribe(REMOVED_IN_WORLD.get(), world ->
            StackHelper.dropItemHandler(world, blockEntity.getBlockPos(), storages));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return StackHelper.serializeItemHandler(provider, storages);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        StackHelper.deserializeItemHandler(provider, storages, tag);
    }
}
