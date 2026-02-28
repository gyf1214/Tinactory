package org.shsts.tinactory.content.logistics;

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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.ItemHandlerPort;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Arrays;
import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChest extends ElectricStorage implements INBTSerializable<CompoundTag> {
    public static final String ID = "machine/chest";

    public final int capacity;
    private final int size;
    private final WrapperItemHandler internalItems;
    private final IPort<ItemStack> externalPort;
    private final ItemStack[] filters;
    private final LazyOptional<IItemHandler> itemHandlerCap;

    private class VoidableItemHandler extends WrapperItemHandler {
        public VoidableItemHandler(IItemHandlerModifiable compose) {
            super(compose);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            var ret = super.insertItem(slot, stack, simulate);
            return isVoid() ? ItemStack.EMPTY : ret;
        }
    }

    private class ExternalItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return size;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return internalItems.getStackInSlot(slot);
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

    public ElectricChest(BlockEntity blockEntity, Layout layout, int capacity, double power) {
        super(blockEntity, layout, power);
        this.size = layout.slots.size() / 2;
        this.capacity = capacity;
        this.filters = new ItemStack[size];

        var inner = new ItemSlotHandler(size, capacity);
        this.internalItems = new VoidableItemHandler(inner);
        internalItems.onUpdate(this::onSlotChange);
        for (var i = 0; i < size; i++) {
            var slot = i;
            internalItems.setFilter(i, stack -> allowStackInSlot(slot, stack));
        }

        var externalHandler = new ExternalItemHandler();
        this.externalPort = new ItemHandlerPort(internalItems);
        this.itemHandlerCap = LazyOptional.of(() -> externalHandler);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(
        Layout layout, int slotSize, double power) {
        return $ -> $.capability(ID, be -> new ElectricChest(be, layout, slotSize, power));
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
        onSlotChange();
    }

    public void resetFilter(int slot) {
        filters[slot] = null;
        onSlotChange();
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
        machine.network().ifPresent(network -> registerPort(network, externalPort));
    }

    @Override
    protected int updateSignal() {
        var totalCapacity = 0;
        var totalCount = 0;
        for (var i = 0; i < size; i++) {
            if (filters[i] != null) {
                totalCapacity += capacity;
                totalCount += internalItems.getStackInSlot(i).getCount();
            }
        }
        return totalCapacity == 0 ? 0 : MathUtil.toSignal((double) totalCount / totalCapacity);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(REMOVED_IN_WORLD.get(), world ->
            StackHelper.dropItemHandler(world, blockEntity.getBlockPos(), internalItems));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ITEM_HANDLER.get()) {
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
