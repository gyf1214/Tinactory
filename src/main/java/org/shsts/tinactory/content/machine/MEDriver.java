package org.shsts.tinactory.content.machine;

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
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidCollection;
import org.shsts.tinactory.core.logistics.CombinedItemCollection;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.FLUID_COLLECTION;
import static org.shsts.tinactory.content.AllCapabilities.ITEM_COLLECTION;
import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.content.AllEvents.CONNECT;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.content.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEDriver extends CapabilityProvider
    implements IEventSubscriber, ILayoutProvider, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "machine/me_driver";

    private final BlockEntity blockEntity;
    private final Layout layout;
    private final WrapperItemHandler storages;
    private final CombinedItemCollection combinedItems;
    private final CombinedFluidCollection combinedFluids;
    private final LazyOptional<IItemHandler> itemHandlerCap;
    private final LazyOptional<IElectricMachine> electricCap;

    private IMachine machine;
    private IMachineConfig machineConfig;

    public MEDriver(BlockEntity blockEntity, Layout layout) {
        this.blockEntity = blockEntity;
        this.layout = layout;
        var size = layout.slots.size();
        this.storages = new WrapperItemHandler(size);
        for (var i = 0; i < size; i++) {
            storages.setFilter(i, this::allowItem);
        }
        this.itemHandlerCap = LazyOptional.of(() -> storages);
        storages.onUpdate(this::onUpdateStorage);

        this.combinedItems = new CombinedItemCollection();
        combinedItems.onUpdate(blockEntity::setChanged);

        this.combinedFluids = new CombinedFluidCollection();
        combinedFluids.onUpdate(blockEntity::setChanged);

        var electric = new SimpleElectricConsumer(getBlockVoltage(blockEntity),
            CONFIG.meDriverAmperage.get());
        this.electricCap = LazyOptional.of(() -> electric);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Layout layout) {
        return $ -> $.capability(ID, be -> new MEDriver(be, layout));
    }

    private boolean allowItem(ItemStack stack) {
        return stack.is(AllTags.STORAGE_CELL);
    }

    private void registerPort(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        var isStorage = machineConfig.getBoolean("storage", true);
        logistics.unregisterPort(machine, 0);
        logistics.unregisterPort(machine, 1);
        logistics.registerPort(machine, 0, combinedItems, false, isStorage);
        logistics.registerPort(machine, 1, combinedFluids, false, isStorage);
    }

    private void onUpdateStorage() {
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
        blockEntity.setChanged();
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
            return itemHandlerCap.cast();
        } else if (cap == ELECTRIC_MACHINE.get()) {
            return electricCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CONNECT.get(), this::registerPort);
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
