package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.function.Predicate;

import static org.shsts.tinactory.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChest extends ElectricStorage<ItemStack> implements INBTSerializable<CompoundTag> {
    public static final String ID = "machine/chest";

    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return storageSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return getStackInVirtualSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return insertIntoVirtualSlot(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return extractFromVirtualSlot(slot, amount, simulate, ItemStack::getMaxStackSize);
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

    public ElectricChest(BlockEntity blockEntity, Properties properties) {
        super(blockEntity, PortType.ITEM, StackHelper.ITEM_ADAPTER, properties);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Properties properties) {
        return $ -> $.container(ID, be -> new ElectricChest(be, properties));
    }

    @Override
    protected Predicate<ItemStack> asPredicate(HolderLookup.Provider provider, FilterEntry entry) {
        return stack -> entry.testItem(stack, provider);
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
    protected CompoundTag serializeStack(HolderLookup.Provider provider, ItemStack stack) {
        return StackHelper.serializeItemStack(provider, stack);
    }

    @Override
    protected ItemStack deserializeStack(HolderLookup.Provider provider, CompoundTag tag) {
        return StackHelper.deserializeItemStack(provider, tag);
    }
}
