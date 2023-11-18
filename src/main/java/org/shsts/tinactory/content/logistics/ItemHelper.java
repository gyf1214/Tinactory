package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ItemHelper {
    public static CompoundTag serializeItemHandler(IItemHandler itemHandler) {
        var listTag = new ListTag();
        var size = itemHandler.getSlots();
        for (var i = 0; i < size; i++) {
            var item = itemHandler.getStackInSlot(i);
            if (!item.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                item.save(itemTag);
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
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < size) {
                itemHandler.setStackInSlot(slot, ItemStack.of(itemTags));
            }
        }
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
}
