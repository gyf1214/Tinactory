package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.logistics.IItemCollection;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ItemHelper {
    public static CompoundTag serializeItemHandler(IItemHandler itemHandler) {
        var listTag = new ListTag();
        var size = itemHandler.getSlots();
        for (var i = 0; i < size; i++) {
            var stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(itemTag);
                itemTag.putInt("CountInt", stack.getCount());
                listTag.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", listTag);
        nbt.putInt("Size", size);
        return nbt;
    }

    public static void deserializeItemHandler(IItemHandlerModifiable itemHandler, CompoundTag tag) {
        var size = itemHandler.getSlots();
        for (int i = 0; i < size; i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        ListTag tagList = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTag = tagList.getCompound(i);
            int slot = itemTag.getInt("Slot");
            if (slot >= 0 && slot < size) {
                var stack = ItemStack.of(itemTag);
                if (itemTag.contains("CountInt", Tag.TAG_INT)) {
                    stack.setCount(itemTag.getInt("CountInt"));
                }
                itemHandler.setStackInSlot(slot, stack);
            }
        }
    }

    public static void serializeStackToBuf(ItemStack stack, FriendlyByteBuf buf) {
        if (stack.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            var item = stack.getItem();
            buf.writeVarInt(Item.getId(item));
            buf.writeVarInt(stack.getCount());
            var tag = item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt() ?
                stack.getShareTag() : null;
            buf.writeNbt(tag);
        }
    }

    public static ItemStack deserializeStackFromBuf(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        }
        int id = buf.readVarInt();
        int count = buf.readVarInt();
        var stack = new ItemStack(Item.byId(id), count);
        stack.readShareTag(buf.readNbt());
        return stack;
    }

    /**
     * This also ignores the stack limit. This means ItemStack with exact same NBT can also stack.
     */
    public static boolean canItemsStack(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && a.sameItem(b) && a.hasTag() == b.hasTag() &&
            (!a.hasTag() || Objects.equals(a.getTag(), b.getTag())) &&
            a.areCapsCompatible(b);
    }

    public static boolean itemStackEqual(ItemStack a, ItemStack b) {
        return (a.isEmpty() && b.isEmpty()) || (canItemsStack(a, b) && a.getCount() == b.getCount());
    }

    public static ItemStack copyWithCount(ItemStack stack, int count) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemHandlerHelper.copyStackWithSize(stack, count);
    }

    public static boolean hasItem(IItemCollection collection, Predicate<ItemStack> ingredient) {
        if (!collection.acceptOutput()) {
            return false;
        }
        for (var itemStack : collection.getAllItems()) {
            if (ingredient.test(itemStack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return whether the requirements are met.
     */
    public static boolean consumeItemCollection(IItemCollection collection, Predicate<ItemStack> ingredient,
        int amount, boolean simulate) {
        if (!collection.acceptOutput()) {
            return false;
        }
        for (var itemStack : collection.getAllItems()) {
            if (amount <= 0) {
                return true;
            }
            if (ingredient.test(itemStack)) {
                var itemStack1 = ItemHandlerHelper.copyStackWithSize(itemStack, amount);
                var extracted = collection.extractItem(itemStack1, simulate);
                amount -= extracted.getCount();
            }
        }
        return amount <= 0;
    }

    public static void dropItemHandler(Level world, BlockPos pos, IItemHandler itemHandler) {
        var slots = itemHandler.getSlots();
        for (var i = 0; i < slots; i++) {
            var stack = itemHandler.getStackInSlot(i);
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    public static Optional<ItemStack> combineStack(ItemStack item1, ItemStack item2) {
        if (item1.isEmpty()) {
            return Optional.of(item2);
        }
        if (item2.isEmpty()) {
            return Optional.of(item1);
        }
        if (ItemHandlerHelper.canItemStacksStack(item1, item2) &&
            item1.getCount() + item2.getCount() <= item1.getMaxStackSize()) {
            return Optional.of(ItemHandlerHelper.copyStackWithSize(item1, item1.getCount() + item2.getCount()));
        }
        return Optional.empty();
    }

    public static Optional<IFluidHandlerItem> getFluidHandlerFromItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return FluidUtil.getFluidHandler(stack).resolve();
    }
}
