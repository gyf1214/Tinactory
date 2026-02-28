package org.shsts.tinactory.integration.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.core.logistics.CombinedPort;
import org.shsts.tinactory.core.logistics.DigitalStorage;
import org.shsts.tinactory.core.logistics.IDigitalProvider;
import org.shsts.tinactory.core.logistics.StackHelper;

import java.util.Arrays;
import java.util.Collection;

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

    public static ItemCombinedPort combinedItem() {
        return new ItemCombinedPort();
    }

    @SafeVarargs
    public static ItemCombinedPort combinedItem(IPort<ItemStack>... composes) {
        return combinedItem(Arrays.asList(composes));
    }

    public static ItemCombinedPort combinedItem(Collection<IPort<ItemStack>> composes) {
        return new ItemCombinedPort(composes);
    }

    public static FluidCombinedPort combinedFluid() {
        return new FluidCombinedPort();
    }

    @SafeVarargs
    public static FluidCombinedPort combinedFluid(IPort<FluidStack>... composes) {
        return combinedFluid(Arrays.asList(composes));
    }

    public static FluidCombinedPort combinedFluid(Collection<IPort<FluidStack>> composes) {
        return new FluidCombinedPort(composes);
    }

    public static class ItemStorage extends DigitalStorage<ItemStack>
        implements IItemPort, INBTSerializable<CompoundTag> {
        private ItemStorage(IDigitalProvider provider) {
            super(provider, ItemPortAdapter.INSTANCE, CONFIG.bytesPerItemType.get(), CONFIG.bytesPerItem.get());
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            var listTag = new ListTag();
            for (var stack : getAllStorages()) {
                listTag.add(StackHelper.serializeItemStack(stack));
            }
            tag.put("Items", listTag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            clear();
            var listTag = tag.getList("Items", Tag.TAG_COMPOUND);
            for (var itemTag : listTag) {
                var stack = StackHelper.deserializeItemStack((CompoundTag) itemTag);
                insert(stack, false);
            }
        }
    }

    public static class FluidStorage extends DigitalStorage<FluidStack>
        implements IFluidPort, INBTSerializable<CompoundTag> {
        private FluidStorage(IDigitalProvider provider) {
            super(provider, FluidPortAdapter.INSTANCE, CONFIG.bytesPerFluidType.get(), CONFIG.bytesPerFluid.get());
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            var listTag = new ListTag();
            for (var stack : getAllStorages()) {
                listTag.add(StackHelper.serializeFluidStack(stack));
            }
            tag.put("Fluids", listTag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            clear();
            var listTag = tag.getList("Fluids", Tag.TAG_COMPOUND);
            for (var fluidTag : listTag) {
                var stack = FluidStack.loadFluidStackFromNBT((CompoundTag) fluidTag);
                insert(stack, false);
            }
        }
    }

    public static class ItemCombinedPort extends CombinedPort<ItemStack> implements IItemPort {
        private ItemCombinedPort() {
            super(ItemPortAdapter.INSTANCE);
        }

        private ItemCombinedPort(Collection<IPort<ItemStack>> composes) {
            super(ItemPortAdapter.INSTANCE, composes);
        }
    }

    public static class FluidCombinedPort extends CombinedPort<FluidStack> implements IFluidPort {
        private FluidCombinedPort() {
            super(FluidPortAdapter.INSTANCE);
        }

        private FluidCombinedPort(Collection<IPort<FluidStack>> composes) {
            super(FluidPortAdapter.INSTANCE, composes);
        }
    }
}
