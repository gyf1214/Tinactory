package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.StorageEntry;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import static org.shsts.tinactory.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChest extends ElectricStorage<ItemStack> implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ID = "machine/chest";

    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return storageSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stack(virtualEntries().get(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            var accepted = insertIntoVirtualSlot(slot, stack, simulate);
            return StackHelper.copyWithCount(stack, stack.getCount() - accepted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            var stack = getStackInSlot(slot);
            var limit = stack.isEmpty() ? 0 : Math.min(amount, stack.getMaxStackSize());
            return extractFromVirtualSlot(slot, limit, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return stackLimit();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return validForVirtualSlot(slot, stack);
        }
    };

    public ElectricChest(BlockEntity blockEntity, int storageSlots, int stackLimit, double power) {
        super(blockEntity, StackHelper.ITEM_ADAPTER, storageSlots, stackLimit, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(
        int storageSlots, int stackLimit, double power) {
        return $ -> $.container(ID, be -> new ElectricChest(be, storageSlots, stackLimit, power));
    }

    @Override
    public PortType type() {
        return PortType.ITEM;
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(REMOVED_IN_WORLD.get(), world ->
            StackHelper.dropItemHandler(world, blockEntity.getBlockPos(), itemHandler));
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        super.attachCapability(builder);
        builder.attach(ITEM_HANDLER, itemHandler);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return serializeEntries(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (deserializeEntries(provider, tag)) {
            return;
        }
        var items = tag.getCompound("items").getList("Items", Tag.TAG_COMPOUND);
        for (var value : items) {
            var item = StackHelper.deserializeItemStack(provider, (CompoundTag) value);
            if (!item.isEmpty() && !loadEntry(new StorageEntry(StackHelper.ITEM_ADAPTER.keyOf(item),
                item.getCount(), false))) {
                LOGGER.warn("Discarding overflowing legacy Electric Chest item {}", item);
            }
        }
        var filters = tag.getList("filters", Tag.TAG_COMPOUND);
        for (var value : filters) {
            var item = ItemStack.parseOptional(provider, (CompoundTag) value);
            if (!item.isEmpty() && !loadEntry(new StorageEntry(StackHelper.ITEM_ADAPTER.keyOf(item), 0, true))) {
                LOGGER.warn("Discarding overflowing legacy Electric Chest filter {}", item);
            }
        }
    }
}
