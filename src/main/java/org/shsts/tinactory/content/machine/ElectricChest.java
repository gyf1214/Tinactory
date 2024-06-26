package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.logistics.ItemSlotHandler;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChest extends CapabilityProvider
        implements IEventSubscriber, IProcessor, INBTSerializable<CompoundTag> {
    public final int capacity;
    private final BlockEntity blockEntity;
    private Machine machine;
    private MachineConfig machineConfig;
    private final int size;
    private final WrapperItemHandler itemHandler;
    private final WrapperItemHandler view;
    private final IItemCollection port;
    private final ItemStack[] filters;

    public ElectricChest(BlockEntity blockEntity, Layout layout) {
        this.blockEntity = blockEntity;
        this.size = layout.slots.size() / 2;
        this.capacity = 1024;

        var inner = new ItemSlotHandler(size, capacity);
        this.itemHandler = new WrapperItemHandler(inner);
        itemHandler.onUpdate(this::onSlotChange);
        for (var i = 0; i < size; i++) {
            var slot = i;
            itemHandler.setFilter(i, stack -> allowStackInSlot(slot, stack));
        }

        this.view = new WrapperItemHandler(itemHandler);
        this.port = new ItemHandlerCollection(view);
        this.filters = new ItemStack[size];
    }

    public ItemStack getStackInSlot(int slot) {
        return itemHandler.getStackInSlot(slot).copy();
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack.copy());
    }

    public void insertItem(int slot, ItemStack stack) {
        itemHandler.insertItem(slot, stack, false);
    }

    public void extractItem(int slot, int amount) {
        itemHandler.extractItem(slot, amount, false);
    }

    public Optional<ItemStack> getFilter(int slot) {
        return Optional.ofNullable(filters[slot]);
    }

    public void setFilter(int slot, ItemStack stack) {
        filters[slot] = ItemHelper.copyWithCount(stack, 1);
        blockEntity.setChanged();
    }

    public void resetFilter(int slot) {
        filters[slot] = null;
        blockEntity.setChanged();
    }

    private void onSlotChange() {
        blockEntity.setChanged();
    }

    public boolean allowStackInSlot(int slot, ItemStack stack) {
        if (filters[slot] != null) {
            return ItemHelper.canItemsStack(stack, filters[slot]);
        }
        var stack1 = itemHandler.getStackInSlot(slot);
        return (stack1.isEmpty() && machineConfig.getBoolean("unlockChest")) ||
                ItemHelper.canItemsStack(stack, stack1);
    }

    private void onLoad() {
        machine = AllCapabilities.MACHINE.get(blockEntity);
        machineConfig = machine.config;
    }

    private void onConnect(Network network) {
        var logistics = network.getComponent(AllNetworks.LOGISTICS_COMPONENT);
        logistics.addStorage(port);
    }

    private void onMachineConfig() {
        view.allowInput = machineConfig.getPortConfig("chestInput") != MachineConfig.PortConfig.NONE;
        view.allowOutput = machineConfig.getPortConfig("chestOutput") != MachineConfig.PortConfig.NONE;
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.CLIENT_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.CONNECT, this::onConnect);
        eventManager.subscribe(AllEvents.SET_MACHINE_CONFIG, this::onMachineConfig);
    }

    @Override
    public void onPreWork() {
        if (machineConfig.getPortConfig("chestOutput") == MachineConfig.PortConfig.ACTIVE) {
            machine.getNetwork()
                    .map(network -> network.getComponent(AllNetworks.LOGISTICS_COMPONENT))
                    .ifPresent(logistics -> logistics.addActiveItem(PortDirection.OUTPUT, port));
        }
    }

    @Override
    public void onWorkTick(double partial) {}

    @Override
    public double getProgress() {
        return 0d;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.ELECTRIC_CHEST.get() ||
                cap == AllCapabilities.PROCESSOR.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("items", ItemHelper.serializeItemHandler(itemHandler));
        var tag1 = new ListTag();
        for (var i = 0; i < size; i++) {
            if (filters[i] != null) {
                var tag2 = new CompoundTag();
                filters[i].save(tag2);
                tag2.putInt("Slot", i);
                tag1.add(tag2);
            }
        }
        tag.put("filters", tag1);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ItemHelper.deserializeItemHandler(itemHandler, tag.getCompound("items"));
        var tag1 = tag.getList("filters", Tag.TAG_COMPOUND);
        Arrays.fill(filters, null);
        for (var tag2 : tag1) {
            var tag3 = (CompoundTag) tag2;
            var slot = tag3.getInt("Slot");
            var item = ItemStack.of(tag3);
            filters[slot] = item;
        }
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>>
    builder(Layout layout) {
        return CapabilityProviderBuilder.fromFactory("machine/chest", be -> new ElectricChest(be, layout));
    }
}
