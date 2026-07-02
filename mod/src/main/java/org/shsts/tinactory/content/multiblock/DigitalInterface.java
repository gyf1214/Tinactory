package org.shsts.tinactory.content.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.logistics.ContainerPort;
import org.shsts.tinactory.core.gui.ILayoutProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedPort;
import org.shsts.tinactory.core.logistics.IBytesProvider;
import org.shsts.tinactory.core.logistics.IDigitalProvider;
import org.shsts.tinactory.core.logistics.IFlexibleContainer;
import org.shsts.tinactory.integration.logistics.StoragePorts;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.AllCapabilities.BYTES_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.AllEvents.CONTAINER_CHANGE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalInterface extends MultiblockInterface implements ILayoutProvider,
    IFlexibleContainer, IBytesProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final int maxParallel;
    private final long bytesLimit;
    private final long inputTypeReserveBytes;
    private final int inputTypeReserveSlots;
    private final long outputReserveBytes;

    private long sharedBytes;
    private long outputReserveUsed;
    private long outputBytesUsed;
    private final Map<IStackKey, Long> inputReserveUsed = new HashMap<>();
    private final Map<IStackKey, Long> inputBytesUsed = new HashMap<>();

    private class Storage implements IDigitalProvider, INBTSerializable<CompoundTag> {
        private final StoragePorts.ItemStorage internalItem;
        private final CombinedPort<ItemStack> menuItem;
        private final CombinedPort<ItemStack> externalItem;
        private final ContainerPort itemPort;
        private final StoragePorts.FluidStorage internalFluid;
        private final CombinedPort<FluidStack> menuFluid;
        private final CombinedPort<FluidStack> externalFluid;
        private final ContainerPort fluidPort;
        public SlotType type;
        private long bytesUsed;

        private Storage() {
            this.internalItem = StoragePorts.itemStorage(this);
            this.menuItem = StoragePorts.combinedItem(internalItem);
            this.externalItem = StoragePorts.combinedItem(internalItem);
            this.itemPort = new ContainerPort(SlotType.NONE, internalItem, menuItem, externalItem);
            this.internalFluid = StoragePorts.fluidStorage(this);
            this.menuFluid = StoragePorts.combinedFluid(internalFluid);
            this.externalFluid = StoragePorts.combinedFluid(internalFluid);
            this.fluidPort = new ContainerPort(SlotType.NONE, internalFluid, menuFluid, externalFluid);
            this.type = SlotType.NONE;
            this.bytesUsed = 0;

            internalItem.onUpdate(this::onUpdate);
            internalFluid.onUpdate(this::onUpdate);
        }

        private boolean isInput() {
            return type.direction == PortDirection.INPUT;
        }

        public void setType(SlotType val) {
            type = val;
            internalItem.resetFilters();
            internalFluid.resetFilters();
            if (type.direction == PortDirection.INPUT) {
                internalItem.maxAmount = Integer.MAX_VALUE;
                menuItem.allowInput = true;
                externalItem.allowInput = true;
                externalItem.allowOutput = false;
                internalFluid.maxAmount = Integer.MAX_VALUE;
                menuFluid.allowInput = true;
                externalFluid.allowInput = true;
                externalFluid.allowOutput = false;
            } else {
                internalItem.maxAmount = Integer.MAX_VALUE;
                menuItem.allowInput = false;
                externalItem.allowInput = false;
                externalItem.allowOutput = true;
                internalFluid.maxAmount = Integer.MAX_VALUE;
                menuFluid.allowInput = false;
                externalFluid.allowInput = false;
                externalFluid.allowOutput = true;
            }
        }

        public IPort<?> port(ContainerAccess access) {
            return switch (type.portType) {
                case ITEM -> itemPort.get(access);
                case FLUID -> fluidPort.get(access);
                case NONE -> IPort.empty();
            };
        }

        private void onUpdate() {
            invoke(blockEntity, CONTAINER_CHANGE);
            blockEntity.setChanged();
        }

        /**
         * This is not used.
         */
        @Override
        public long bytesCapacity() {
            return bytesLimit;
        }

        @Override
        public long bytesUsed() {
            return bytesUsed;
        }

        @Override
        public int consumeLimit(int offset, int bytes) {
            return consumeLimitFor(availableBytesForNoKey(isInput()), offset, bytes);
        }

        @Override
        public int consumeLimit(IStackKey key, int offset, int bytes) {
            return consumeLimitFor(availableBytesForKey(isInput(), key), offset, bytes);
        }

        @Override
        public void consume(long bytes) {
            assert bytes > 0;
            consumeBytesWithoutKey(isInput(), bytes);
            bytesUsed += bytes;
            LOGGER.trace("consume {}, bytesUsed={}", bytes, bytesUsed);
        }

        @Override
        public void consume(IStackKey key, long bytes) {
            assert bytes > 0;
            consumeBytesForKey(isInput(), key, bytes);
            bytesUsed += bytes;
            LOGGER.trace("consume {}, key={}, bytesUsed={}", bytes, key, bytesUsed);
        }

        @Override
        public void restore(long bytes) {
            assert bytes > 0;
            restoreBytesWithoutKey(isInput(), bytes);
            bytesUsed -= bytes;
            assert bytesUsed >= 0;
            LOGGER.trace("restore {}, bytesUsed={}", bytes, bytesUsed);
        }

        @Override
        public void restore(IStackKey key, long bytes) {
            assert bytes > 0;
            restoreBytesForKey(isInput(), key, bytes);
            bytesUsed -= bytes;
            assert bytesUsed >= 0;
            LOGGER.trace("restore {}, key={}, bytesUsed={}", bytes, key, bytesUsed);
        }

        /**
         * Resetting bytesUsed will be done before deserializing digital storage.
         */
        @Override
        public void reset() {}

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            var tag = new CompoundTag();
            tag.put("items", internalItem.serializeNBT(provider));
            tag.put("fluids", internalFluid.serializeNBT(provider));
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
            bytesUsed = 0;
            internalItem.deserializeNBT(provider, tag.getCompound("items"));
            internalFluid.deserializeNBT(provider, tag.getCompound("fluids"));
        }

        private void recomputeBytesUsed() {
            var tag = serializeNBT(world().registryAccess());
            deserializeNBT(world().registryAccess(), tag);
        }
    }

    private final List<Storage> storages = new ArrayList<>();
    private Layout layout = Layout.EMPTY;

    public record Properties(int maxParallel, long bytesLimit, long inputTypeReserveBytes,
        int inputTypeReserveSlots, long outputReserveBytes) {}

    public DigitalInterface(BlockEntity be, Properties properties) {
        super(be);
        this.maxParallel = properties.maxParallel;
        this.bytesLimit = properties.bytesLimit;
        this.inputTypeReserveBytes = properties.inputTypeReserveBytes;
        this.inputTypeReserveSlots = properties.inputTypeReserveSlots;
        this.outputReserveBytes = properties.outputReserveBytes;
        var reservedBytes = saturatedAdd(saturatedMultiply(inputTypeReserveBytes, inputTypeReserveSlots),
            outputReserveBytes);
        if (reservedBytes > bytesLimit) {
            throw new IllegalArgumentException("Digital Interface reserve bytes exceed total capacity: " +
                reservedBytes + " > " + bytesLimit);
        }
        this.sharedBytes = sharedCapacity();
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Properties properties) {
        return $ -> $.container(ID, be -> new DigitalInterface(be, properties));
    }

    @Override
    public boolean isDigital() {
        return true;
    }

    @Override
    public long bytesCapacity() {
        return bytesLimit;
    }

    @Override
    public long bytesUsed() {
        var inputUsed = inputBytesUsed.values().stream().mapToLong(Long::longValue).sum();
        return Math.min(bytesLimit, inputUsed + outputBytesUsed);
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public int maxParallel() {
        return maxParallel;
    }

    @Override
    public int portSize() {
        return layout.ports.size();
    }

    @Override
    public boolean hasPort(int port) {
        return port >= 0 && port < layout.ports.size() && storages.get(port).type != SlotType.NONE;
    }

    @Override
    public PortDirection portDirection(int port) {
        return storages.get(port).type.direction;
    }

    @Override
    public IPort<?> getPort(int port, ContainerAccess access) {
        return storages.get(port).port(access);
    }

    @Override
    public void setLayout(Layout layout) {
        for (var i = storages.size(); i < layout.ports.size(); i++) {
            storages.add(new Storage());
        }
        for (var i = 0; i < storages.size(); i++) {
            var storage = storages.get(i);
            var type = i < layout.ports.size() ? layout.ports.get(i).type() : SlotType.NONE;
            storage.setType(type);
        }
        resetAccounting();
        for (var storage : storages) {
            storage.recomputeBytesUsed();
        }
        this.layout = layout;
    }

    @Override
    public void resetLayout() {
        for (var storage : storages) {
            storage.setType(SlotType.NONE);
        }
        layout = Layout.EMPTY;
    }

    @Override
    protected void onLoad() {
        container = this;
    }

    private long sharedCapacity() {
        return bytesLimit - inputTypeReserveBytes * inputTypeReserveSlots - outputReserveBytes;
    }

    private void resetAccounting() {
        sharedBytes = sharedCapacity();
        outputReserveUsed = 0;
        outputBytesUsed = 0;
        inputReserveUsed.clear();
        inputBytesUsed.clear();
    }

    private long availableBytesForNoKey(boolean input) {
        if (input) {
            return sharedBytes;
        }
        return sharedBytes + outputReserveBytes - outputReserveUsed;
    }

    private long availableBytesForKey(boolean input, IStackKey key) {
        if (!input) {
            return availableBytesForNoKey(false);
        }
        var reserveUsed = inputReserveUsed.get(key);
        if (reserveUsed != null) {
            return sharedBytes + inputTypeReserveBytes - reserveUsed;
        }
        if (inputReserveUsed.size() < inputTypeReserveSlots) {
            return sharedBytes + inputTypeReserveBytes;
        }
        return sharedBytes;
    }

    private void consumeBytesWithoutKey(boolean input, long bytes) {
        if (input) {
            sharedBytes -= bytes;
            assert sharedBytes >= 0;
            return;
        }
        var reserveBytes = Math.min(bytes, outputReserveBytes - outputReserveUsed);
        outputReserveUsed += reserveBytes;
        var sharedDelta = bytes - reserveBytes;
        sharedBytes -= sharedDelta;
        outputBytesUsed += bytes;
        assert sharedBytes >= 0;
    }

    private void consumeBytesForKey(boolean input, IStackKey key, long bytes) {
        if (!input) {
            consumeBytesWithoutKey(false, bytes);
            return;
        }
        var reserveUsed = inputReserveUsed.getOrDefault(key, 0L);
        var canUseReserve = reserveUsed > 0 || inputReserveUsed.containsKey(key) ||
            inputReserveUsed.size() < inputTypeReserveSlots;
        var reserveBytes = canUseReserve ? Math.min(bytes, inputTypeReserveBytes - reserveUsed) : 0L;
        if (reserveBytes > 0 || canUseReserve) {
            inputReserveUsed.put(key, reserveUsed + reserveBytes);
        }
        var sharedDelta = bytes - reserveBytes;
        sharedBytes -= sharedDelta;
        inputBytesUsed.put(key, inputBytesUsed.getOrDefault(key, 0L) + bytes);
        assert sharedBytes >= 0;
    }

    private void restoreBytesWithoutKey(boolean input, long bytes) {
        if (input) {
            sharedBytes += bytes;
            return;
        }
        var sharedUsed = outputBytesUsed - outputReserveUsed;
        var sharedDelta = Math.min(bytes, sharedUsed);
        sharedBytes += sharedDelta;
        outputReserveUsed -= bytes - sharedDelta;
        outputBytesUsed -= bytes;
        assert outputReserveUsed >= 0 && outputBytesUsed >= 0;
    }

    private void restoreBytesForKey(boolean input, IStackKey key, long bytes) {
        if (!input) {
            restoreBytesWithoutKey(false, bytes);
            return;
        }
        if (!inputBytesUsed.containsKey(key)) {
            restoreBytesWithoutKey(true, bytes);
            return;
        }
        var totalUsed = inputBytesUsed.get(key);
        var reserveUsed = inputReserveUsed.getOrDefault(key, 0L);
        var sharedUsed = totalUsed - reserveUsed;
        var sharedDelta = Math.min(bytes, sharedUsed);
        sharedBytes += sharedDelta;
        var reserveDelta = bytes - sharedDelta;
        var newReserveUsed = reserveUsed - reserveDelta;
        var newTotalUsed = totalUsed - bytes;
        if (newTotalUsed <= 0) {
            inputBytesUsed.remove(key);
            inputReserveUsed.remove(key);
        } else {
            inputBytesUsed.put(key, newTotalUsed);
            inputReserveUsed.put(key, newReserveUsed);
        }
        assert newReserveUsed >= 0 && newTotalUsed >= 0;
    }

    private static int consumeLimitFor(long availableBytes, int offset, int bytes) {
        var limit = Math.max(0L, (availableBytes - offset) / bytes);
        return limit > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) limit;
    }

    private static long saturatedMultiply(long left, long right) {
        if (left == 0L || right == 0L) {
            return 0L;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    private static long saturatedAdd(long left, long right) {
        if (Long.MAX_VALUE - left < right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        super.attachCapability(builder);
        builder.attach(CONTAINER, this);
        builder.attach(LAYOUT_PROVIDER, this);
        builder.attach(BYTES_PROVIDER, this);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = super.serializeNBT(provider);

        var tag1 = new ListTag();
        for (var storage : storages) {
            tag1.add(storage.serializeNBT(provider));
        }
        tag.put("storage", tag1);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);

        resetAccounting();
        storages.clear();
        for (var tag1 : tag.getList("storage", Tag.TAG_COMPOUND)) {
            var storage = new Storage();
            storage.deserializeNBT(provider, (CompoundTag) tag1);
            storages.add(storage);
        }
    }
}
