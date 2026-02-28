package org.shsts.tinactory.core.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.logistics.IPort;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class StackHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Predicate<ItemStack> TRUE_FILTER = $ -> true;
    public static final Predicate<ItemStack> FALSE_FILTER = $ -> false;
    public static final Predicate<FluidStack> TRUE_FLUID_FILTER = $ -> true;
    public static final Predicate<FluidStack> FALSE_FLUID_FILTER = $ -> false;

    /**
     * Use this if the itemStack can have more than 255 items.
     */
    public static CompoundTag serializeItemStack(ItemStack stack) {
        var tag = new CompoundTag();
        stack.save(tag);
        tag.putInt("CountInt", stack.getCount());
        return tag;
    }

    /**
     * Use this if the itemStack can have more than 255 items.
     */
    public static ItemStack deserializeItemStack(CompoundTag tag) {
        var stack = ItemStack.of(tag);
        if (tag.contains("CountInt", Tag.TAG_INT)) {
            stack.setCount(tag.getInt("CountInt"));
        }
        return stack;
    }

    public static CompoundTag serializeFluidStack(FluidStack stack) {
        var tag = new CompoundTag();
        stack.writeToNBT(tag);
        return tag;
    }

    public static CompoundTag serializeItemHandler(IItemHandler itemHandler) {
        var listTag = new ListTag();
        var size = itemHandler.getSlots();
        for (var i = 0; i < size; i++) {
            var stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                var itemTag = serializeItemStack(stack);
                itemTag.putInt("Slot", i);
                listTag.add(itemTag);
            }
        }
        var tag = new CompoundTag();
        tag.put("Items", listTag);
        tag.putInt("Size", size);
        return tag;
    }

    public static void deserializeItemHandler(IItemHandlerModifiable itemHandler, CompoundTag tag) {
        var size = itemHandler.getSlots();
        for (int i = 0; i < size; i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        var listTag = tag.getList("Items", Tag.TAG_COMPOUND);
        for (var tag1 : listTag) {
            var itemTag = (CompoundTag) tag1;
            int slot = itemTag.getInt("Slot");
            if (slot >= 0 && slot < size) {
                var stack = deserializeItemStack(itemTag);
                itemHandler.setStackInSlot(slot, stack);
            }
        }
    }

    public static void serializeStackToBuf(FriendlyByteBuf buf, ItemStack stack) {
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
        if (stack.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }
        return ItemHandlerHelper.copyStackWithSize(stack, count);
    }

    public static FluidStack copyWithAmount(FluidStack stack, int amount) {
        if (stack.isEmpty() || amount <= 0) {
            return FluidStack.EMPTY;
        }
        var ret = stack.copy();
        ret.setAmount(amount);
        return ret;
    }

    public static Optional<ItemStack> hasItem(IPort<ItemStack> port, Predicate<ItemStack> ingredient) {
        for (var stack : port.getAllStorages()) {
            if (ingredient.test(stack)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    /**
     * Return the itemStack that is actually consumed.
     */
    public static Optional<ItemStack> consumeItemPort(IPort<ItemStack> port,
        Predicate<ItemStack> ingredient, int count, boolean simulate) {
        for (var stack : port.getAllStorages()) {
            if (ingredient.test(stack) && stack.getCount() >= count) {
                var stack1 = copyWithCount(stack, count);
                // always simulate first, this is because we actually don't know which item to extract
                var extracted = port.extract(stack1, true);
                if (extracted.getCount() >= count) {
                    if (simulate) {
                        return Optional.of(extracted);
                    } else {
                        var extracted1 = port.extract(stack1, false);
                        if (extracted1.getCount() < count) {
                            LOGGER.warn("consume item failed port={} content={}/{}",
                                port, extracted, stack1);
                        }
                        return Optional.of(extracted1);
                    }
                }
            }
        }
        return Optional.empty();
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

    public static boolean transmitFluidFromHandler(IFluidHandler handler, IPort<FluidStack> port,
        FluidStack fluid) {
        if (fluid.isEmpty()) {
            return false;
        }
        var remaining = port.insert(fluid, true);
        var amount = fluid.getAmount() - remaining.getAmount();
        if (amount > 0) {
            var fluid1 = StackHelper.copyWithAmount(fluid, amount);
            var fluid2 = handler.drain(fluid1, IFluidHandler.FluidAction.EXECUTE);
            var remaining1 = port.insert(fluid2, false);
            if (!remaining1.isEmpty()) {
                LOGGER.warn("Failed to execute fluid fill inserted={}/{}", amount - remaining1.getAmount(), amount);
            }
            return true;
        }
        return false;
    }

    public static void returnItemHandlerToPlayer(Player player, IItemHandlerModifiable itemHandler) {
        if (!player.isAlive() || (player instanceof ServerPlayer serverPlayer &&
            serverPlayer.hasDisconnected())) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                player.drop(itemHandler.getStackInSlot(i), false);
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        } else if (player instanceof ServerPlayer) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                var inventory = player.getInventory();
                inventory.placeItemBackInInventory(itemHandler.getStackInSlot(i));
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
}
