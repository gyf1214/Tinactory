package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.content.logistics.ItemSlotHandler;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Arrays;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChest extends ElectricStorage implements INBTSerializable<CompoundTag> {
    private static final String ID = "machine/chest";

    public final int capacity;
    private final int size;
    private final WrapperItemHandler internalItems;
    private final WrapperItemHandler externalItems;
    private final IItemCollection externalPort;
    private final ItemStack[] filters;
    private final LazyOptional<?> itemHandlerCap;

    private class ExternalItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return size;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            var item = internalItems.getStackInSlot(slot);
            if (item.isEmpty() || item.getCount() <= item.getMaxStackSize()) {
                return item;
            } else {
                return StackHelper.copyWithCount(item, item.getMaxStackSize());
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return internalItems.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            var item = internalItems.getStackInSlot(slot);
            if (item.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (amount > item.getMaxStackSize()) {
                amount = item.getMaxStackSize();
            }
            return internalItems.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return internalItems.isItemValid(slot, stack);
        }
    }

    public ElectricChest(BlockEntity blockEntity, Layout layout) {
        super(blockEntity, layout);
        this.size = layout.slots.size() / 2;
        this.capacity = TinactoryConfig.INSTANCE.chestSize.get();

        var inner = new ItemSlotHandler(size, capacity);
        this.internalItems = new WrapperItemHandler(inner);
        internalItems.onUpdate(this::onSlotChange);
        for (var i = 0; i < size; i++) {
            var slot = i;
            internalItems.setFilter(i, stack -> allowStackInSlot(slot, stack));
        }
        var externalHandler = new ExternalItemHandler();

        this.externalItems = new WrapperItemHandler(internalItems);
        this.externalPort = new ItemHandlerCollection(this.externalItems) {
            @Override
            public boolean acceptOutput() {
                return allowOutput();
            }
        };
        this.filters = new ItemStack[size];
        this.itemHandlerCap = LazyOptional.of(() -> externalHandler);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Layout layout) {
        return $ -> $.capability(ID, be -> new ElectricChest(be, layout));
    }

    public ItemStack getStackInSlot(int slot) {
        return internalItems.getStackInSlot(slot).copy();
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        internalItems.setStackInSlot(slot, stack.copy());
    }

    public void insertItem(int slot, ItemStack stack) {
        internalItems.insertItem(slot, stack, false);
    }

    public void extractItem(int slot, int amount) {
        internalItems.extractItem(slot, amount, false);
    }

    public Optional<ItemStack> getFilter(int slot) {
        return Optional.ofNullable(filters[slot]);
    }

    public void setFilter(int slot, ItemStack stack) {
        filters[slot] = StackHelper.copyWithCount(stack, 1);
        blockEntity.setChanged();
    }

    public void resetFilter(int slot) {
        filters[slot] = null;
        blockEntity.setChanged();
    }

    public boolean allowStackInSlot(int slot, ItemStack stack) {
        if (filters[slot] != null) {
            return StackHelper.canItemsStack(stack, filters[slot]);
        }
        var stack1 = internalItems.getStackInSlot(slot);
        return (stack1.isEmpty() && isUnlocked()) || StackHelper.canItemsStack(stack, stack1);
    }

    @Override
    protected void onMachineConfig() {
        var allowInput = allowInput();
        var allowOutput = allowOutput();
        for (var i = 0; i < size; i++) {
            externalItems.setFilter(i, $ -> allowInput);
            externalItems.setAllowOutput(i, allowOutput);
        }
        machine.network().ifPresent(network -> registerPort(network, externalPort));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("items", StackHelper.serializeItemHandler(internalItems));
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
        StackHelper.deserializeItemHandler(internalItems, tag.getCompound("items"));
        var tag1 = tag.getList("filters", Tag.TAG_COMPOUND);
        Arrays.fill(filters, null);
        for (var tag2 : tag1) {
            var tag3 = (CompoundTag) tag2;
            var slot = tag3.getInt("Slot");
            var item = ItemStack.of(tag3);
            filters[slot] = item;
        }
    }
}
