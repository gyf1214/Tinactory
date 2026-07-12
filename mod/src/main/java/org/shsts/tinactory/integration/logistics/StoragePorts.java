package org.shsts.tinactory.integration.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.core.logistics.CombinedPort;
import org.shsts.tinactory.core.logistics.DigitalStorage;
import org.shsts.tinactory.core.logistics.IDigitalProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class StoragePorts {
    private StoragePorts() {}

    public static ItemStorage itemStorage(IDigitalProvider provider) {
        return new ItemStorage(provider);
    }

    public static FluidStorage fluidStorage(IDigitalProvider provider) {
        return new FluidStorage(provider);
    }

    public static CombinedPort<ItemStack> combinedItem() {
        return combinedItem(true);
    }

    public static CombinedPort<ItemStack> combinedItem(boolean delegateChildUpdates) {
        return new ItemCombinedPort(delegateChildUpdates);
    }

    @SafeVarargs
    public static CombinedPort<ItemStack> combinedItem(IPort<ItemStack>... composes) {
        return combinedItem(Arrays.asList(composes));
    }

    public static CombinedPort<ItemStack> combinedItem(Collection<IPort<ItemStack>> composes) {
        return combinedItem(composes, true);
    }

    public static CombinedPort<ItemStack> combinedItem(Collection<IPort<ItemStack>> composes,
        boolean delegateChildUpdates) {
        return new ItemCombinedPort(composes, delegateChildUpdates);
    }

    public static CombinedPort<FluidStack> combinedFluid() {
        return combinedFluid(true);
    }

    public static CombinedPort<FluidStack> combinedFluid(boolean delegateChildUpdates) {
        return new FluidCombinedPort(delegateChildUpdates);
    }

    @SafeVarargs
    public static CombinedPort<FluidStack> combinedFluid(IPort<FluidStack>... composes) {
        return combinedFluid(Arrays.asList(composes));
    }

    public static CombinedPort<FluidStack> combinedFluid(Collection<IPort<FluidStack>> composes) {
        return combinedFluid(composes, true);
    }

    public static CombinedPort<FluidStack> combinedFluid(Collection<IPort<FluidStack>> composes,
        boolean delegateChildUpdates) {
        return new FluidCombinedPort(composes, delegateChildUpdates);
    }

    public static class ItemStorage extends DigitalStorage<ItemStack>
        implements IItemPort, INBTSerializable<CompoundTag> {
        private ItemStorage(IDigitalProvider provider) {
            super(provider, StackHelper.ITEM_ADAPTER, CONFIG.bytesPerItemType.get(), CONFIG.bytesPerItem.get());
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            var tag = new CompoundTag();
            var listTag = new ListTag();
            for (var stack : getAllStorages()) {
                listTag.add(StackHelper.serializeItemStack(provider, stack));
            }
            tag.put("Items", listTag);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
            clear();
            var listTag = tag.getList("Items", Tag.TAG_COMPOUND);
            for (var itemTag : listTag) {
                var stack = StackHelper.deserializeItemStack(provider, (CompoundTag) itemTag);
                insert(stack, false);
            }
        }
    }

    public static class FluidStorage extends DigitalStorage<FluidStack>
        implements IFluidPort, INBTSerializable<CompoundTag> {
        private FluidStorage(IDigitalProvider provider) {
            super(provider, StackHelper.FLUID_ADAPTER, CONFIG.bytesPerFluidType.get(), CONFIG.bytesPerFluid.get());
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            var tag = new CompoundTag();
            var listTag = new ListTag();
            for (var stack : getAllStorages()) {
                listTag.add(stack.save(provider));
            }
            tag.put("Fluids", listTag);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
            clear();
            var listTag = tag.getList("Fluids", Tag.TAG_COMPOUND);
            for (var fluidTag : listTag) {
                var stack = FluidStack.parseOptional(provider, (CompoundTag) fluidTag);
                insert(stack, false);
            }
        }
    }

    private static class ItemCombinedPort extends CombinedPort<ItemStack> implements IItemPort {
        private ItemCombinedPort(boolean delegateChildUpdates) {
            super(StackHelper.ITEM_ADAPTER, List.of(), delegateChildUpdates);
        }

        private ItemCombinedPort(Collection<IPort<ItemStack>> composes, boolean delegateChildUpdates) {
            super(StackHelper.ITEM_ADAPTER, composes, delegateChildUpdates);
        }
    }

    private static class FluidCombinedPort extends CombinedPort<FluidStack> implements IFluidPort {
        private FluidCombinedPort(boolean delegateChildUpdates) {
            super(StackHelper.FLUID_ADAPTER, List.of(), delegateChildUpdates);
        }

        private FluidCombinedPort(Collection<IPort<FluidStack>> composes, boolean delegateChildUpdates) {
            super(StackHelper.FLUID_ADAPTER, composes, delegateChildUpdates);
        }
    }
}
