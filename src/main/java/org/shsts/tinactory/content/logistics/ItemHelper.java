package org.shsts.tinactory.content.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.ParametersAreNonnullByDefault;

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
}
