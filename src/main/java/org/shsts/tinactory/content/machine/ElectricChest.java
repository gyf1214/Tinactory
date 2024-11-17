package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.logistics.ItemSlotHandler;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChest extends ElectricStorage implements INBTSerializable<CompoundTag> {
    public final int capacity;
    private final int size;
    private final WrapperItemHandler items;
    private final WrapperItemHandler externalItems;
    private final IItemCollection port;
    private final ItemStack[] filters;

    public ElectricChest(BlockEntity blockEntity, Layout layout) {
        super(blockEntity);
        this.size = layout.slots.size() / 2;
        this.capacity = TinactoryConfig.INSTANCE.chestSize.get();

        var inner = new ItemSlotHandler(size, capacity);
        this.items = new WrapperItemHandler(inner);
        items.onUpdate(this::onSlotChange);
        for (var i = 0; i < size; i++) {
            var slot = i;
            items.setFilter(i, stack -> allowStackInSlot(slot, stack));
        }

        this.externalItems = new WrapperItemHandler(items);
        this.port = new ItemHandlerCollection(externalItems);
        this.filters = new ItemStack[size];
    }

    public ItemStack getStackInSlot(int slot) {
        return items.getStackInSlot(slot).copy();
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        items.setStackInSlot(slot, stack.copy());
    }

    public void insertItem(int slot, ItemStack stack) {
        items.insertItem(slot, stack, false);
    }

    public void extractItem(int slot, int amount) {
        items.extractItem(slot, amount, false);
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

    public boolean allowStackInSlot(int slot, ItemStack stack) {
        if (filters[slot] != null) {
            return ItemHelper.canItemsStack(stack, filters[slot]);
        }
        var stack1 = items.getStackInSlot(slot);
        return (stack1.isEmpty() && isUnlocked()) || ItemHelper.canItemsStack(stack, stack1);
    }

    @Override
    protected void onConnect(Network network) {
        super.onConnect(network);
        var logistics = network.getComponent(AllNetworks.LOGISTICS_COMPONENT);
        logistics.addStorage(port);
    }

    @Override
    protected void onMachineConfig() {
        var allowInput = machineConfig.getPortConfig("chestInput") != MachineConfig.PortConfig.NONE;
        var allowOutput = machineConfig.getPortConfig("chestOutput") != MachineConfig.PortConfig.NONE;
        for (var i = 0; i < size; i++) {
            externalItems.setFilter(i, $ -> allowInput);
            externalItems.setAllowOutput(i, allowOutput);
        }
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
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("items", ItemHelper.serializeItemHandler(items));
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
        ItemHelper.deserializeItemHandler(items, tag.getCompound("items"));
        var tag1 = tag.getList("filters", Tag.TAG_COMPOUND);
        Arrays.fill(filters, null);
        for (var tag2 : tag1) {
            var tag3 = (CompoundTag) tag2;
            var slot = tag3.getInt("Slot");
            var item = ItemStack.of(tag3);
            filters[slot] = item;
        }
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>> builder(Layout layout) {
        return CapabilityProviderBuilder.fromFactory("machine/chest", be -> new ElectricChest(be, layout));
    }
}
