package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.core.common.CapabilityItem;
import org.shsts.tinactory.core.common.ItemCapabilityProvider;
import org.shsts.tinactory.core.logistics.DigitalFluidStorage;
import org.shsts.tinactory.core.logistics.DigitalItemStorage;
import org.shsts.tinactory.core.logistics.DigitalProvider;
import org.shsts.tinactory.core.logistics.IDigitalProvider;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.AllCapabilities.DIGITAL_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.FLUID_PORT;
import static org.shsts.tinactory.AllCapabilities.ITEM_PORT;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageCell extends CapabilityItem {
    private static final ResourceLocation ID = modLoc("logistics/me_storage_cell");

    private final boolean isFluid;
    private final int bytesLimit;

    public MEStorageCell(Properties properties, boolean isFluid, int bytesLimit) {
        super(properties.stacksTo(1));
        this.isFluid = isFluid;
        this.bytesLimit = bytesLimit;
    }

    public static Function<Properties, MEStorageCell> itemCell(int bytesLimit) {
        return properties -> new MEStorageCell(properties, false, bytesLimit);
    }

    public static Function<Properties, MEStorageCell> fluidCell(int bytesLimit) {
        return properties -> new MEStorageCell(properties, true, bytesLimit);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world,
        List<Component> tooltip, TooltipFlag isAdvanced) {
        stack.getCapability(DIGITAL_PROVIDER.get())
            .ifPresent(provider -> addTooltip(tooltip, "meStorageCell",
                NUMBER_FORMAT.format(provider.bytesUsed()), NUMBER_FORMAT.format(bytesLimit)));
    }

    private static class ItemCapability extends ItemCapabilityProvider {
        private final IDigitalProvider provider;
        private final DigitalItemStorage storage;
        private final LazyOptional<IItemPort> itemCap;
        private final LazyOptional<IDigitalProvider> providerCap;

        public ItemCapability(ItemStack stack, int bytesLimit) {
            super(stack, ID);
            this.provider = new DigitalProvider(bytesLimit);
            this.storage = new DigitalItemStorage(provider);
            this.itemCap = LazyOptional.of(() -> capabilityItemPort(storage));
            this.providerCap = LazyOptional.of(() -> provider);

            storage.onUpdate(this::syncTag);
        }

        @Override
        protected CompoundTag serializeNBT() {
            return storage.serializeNBT();
        }

        @Override
        protected void deserializeNBT(CompoundTag tag) {
            storage.deserializeNBT(tag);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            if (cap == ITEM_PORT.get()) {
                return itemCap.cast();
            } else if (cap == DIGITAL_PROVIDER.get()) {
                return providerCap.cast();
            }
            return LazyOptional.empty();
        }
    }

    private static class FluidCapability extends ItemCapabilityProvider {
        private final IDigitalProvider provider;
        private final DigitalFluidStorage storage;
        private final LazyOptional<IFluidPort> fluidCap;
        private final LazyOptional<IDigitalProvider> providerCap;

        protected FluidCapability(ItemStack stack, int bytesLimit) {
            super(stack, ID);
            this.provider = new DigitalProvider(bytesLimit);
            this.storage = new DigitalFluidStorage(provider);
            this.fluidCap = LazyOptional.of(() -> capabilityFluidPort(storage));
            this.providerCap = LazyOptional.of(() -> provider);

            storage.onUpdate(this::syncTag);
        }

        @Override
        protected CompoundTag serializeNBT() {
            return storage.serializeNBT();
        }

        @Override
        protected void deserializeNBT(CompoundTag tag) {
            storage.deserializeNBT(tag);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            if (cap == FLUID_PORT.get()) {
                return fluidCap.cast();
            } else if (cap == DIGITAL_PROVIDER.get()) {
                return providerCap.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        var stack = event.getObject();
        if (isFluid) {
            event.addCapability(ID, new FluidCapability(stack, bytesLimit));
        } else {
            event.addCapability(ID, new ItemCapability(stack, bytesLimit));
        }
    }

    private static IItemPort capabilityItemPort(IPort<ItemStack> port) {
        return new IItemPort() {
            @Override
            public boolean acceptInput(ItemStack stack) {
                return port.acceptInput(stack);
            }

            @Override
            public ItemStack insert(ItemStack stack, boolean simulate) {
                return port.insert(stack, simulate);
            }

            @Override
            public ItemStack extract(ItemStack stack, boolean simulate) {
                return port.extract(stack, simulate);
            }

            @Override
            public ItemStack extract(int limit, boolean simulate) {
                return port.extract(limit, simulate);
            }

            @Override
            public int getStorageAmount(ItemStack stack) {
                return port.getStorageAmount(stack);
            }

            @Override
            public List<ItemStack> getAllStorages() {
                return List.copyOf(port.getAllStorages());
            }

            @Override
            public boolean acceptOutput() {
                return port.acceptOutput();
            }
        };
    }

    private static IFluidPort capabilityFluidPort(IPort<FluidStack> port) {
        return new IFluidPort() {
            @Override
            public boolean acceptInput(FluidStack stack) {
                return port.acceptInput(stack);
            }

            @Override
            public FluidStack insert(FluidStack stack, boolean simulate) {
                return port.insert(stack, simulate);
            }

            @Override
            public FluidStack extract(FluidStack stack, boolean simulate) {
                return port.extract(stack, simulate);
            }

            @Override
            public FluidStack extract(int limit, boolean simulate) {
                return port.extract(limit, simulate);
            }

            @Override
            public int getStorageAmount(FluidStack stack) {
                return port.getStorageAmount(stack);
            }

            @Override
            public List<FluidStack> getAllStorages() {
                return List.copyOf(port.getAllStorages());
            }

            @Override
            public boolean acceptOutput() {
                return port.acceptOutput();
            }
        };
    }
}
