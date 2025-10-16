package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidCollection;
import org.shsts.tinactory.core.logistics.CombinedItemCollection;
import org.shsts.tinactory.core.logistics.DigitalFluidStorage;
import org.shsts.tinactory.core.logistics.DigitalItemStorage;
import org.shsts.tinactory.core.logistics.IDigitalProvider;
import org.shsts.tinactory.core.logistics.IFlexibleContainer;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllEvents.CONTAINER_CHANGE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalInterface extends MultiblockInterface implements ILayoutProvider, IFlexibleContainer {
    private final int maxParallel;
    private final int bytesLimit;
    private final int dedicatedLimit;
    private final int amountByteLimit;

    private int sharedBytes;

    private class Storage implements IDigitalProvider, INBTSerializable<CompoundTag> {
        public final DigitalItemStorage internalItem;
        public final DigitalFluidStorage internalFluid;
        public final CombinedItemCollection externalItem;
        public final CombinedFluidCollection externalFluid;
        public SlotType type;
        private int bytesUsed;

        private Storage() {
            this.internalItem = new DigitalItemStorage(this);
            this.internalFluid = new DigitalFluidStorage(this);
            this.externalItem = new CombinedItemCollection(List.of(internalItem));
            this.externalFluid = new CombinedFluidCollection(List.of(internalFluid));
            this.type = SlotType.NONE;
            this.bytesUsed = 0;

            internalItem.onUpdate(this::onUpdate);
            internalFluid.onUpdate(this::onUpdate);
        }

        public void setType(SlotType val) {
            type = val;
            var isInput = type.direction == PortDirection.INPUT;
            // always allow output as we don't have a menu to extract items.
            externalItem.allowInput = isInput;
            externalFluid.allowInput = isInput;
            internalItem.maxCount = isInput ? amountByteLimit / CONFIG.bytesPerItem.get() : Integer.MAX_VALUE;
            internalFluid.maxAmount = isInput ? amountByteLimit / CONFIG.bytesPerFluid.get() : Integer.MAX_VALUE;
        }

        public IPort port(boolean internal) {
            return switch (type.portType) {
                case ITEM -> internal ? internalItem : externalItem;
                case FLUID -> internal ? internalFluid : externalFluid;
                case NONE -> IPort.EMPTY;
            };
        }

        private void onUpdate() {
            invoke(blockEntity, CONTAINER_CHANGE);
            blockEntity.setChanged();
        }

        @Override
        public int bytesUsed() {
            return bytesUsed;
        }

        @Override
        public int consumeLimit(int bytes) {
            if (bytesUsed < dedicatedLimit) {
                return (sharedBytes + dedicatedLimit - bytesUsed) / bytes;
            } else {
                return sharedBytes / bytes;
            }
        }

        @Override
        public void consume(int bytes) {
            assert bytes > 0;
            if (bytesUsed >= dedicatedLimit) {
                // consume only shared
                sharedBytes -= bytes;
            } else if (bytesUsed + bytes > dedicatedLimit) {
                // shared is consumed by the exceeding part
                sharedBytes -= bytesUsed + bytes - dedicatedLimit;
            }
            assert sharedBytes >= 0;
            bytesUsed += bytes;
        }

        @Override
        public void restore(int bytes) {
            assert bytes > 0;
            if (bytes < bytesUsed - dedicatedLimit) {
                // restore only shared
                sharedBytes += bytes;
            } else if (bytesUsed > dedicatedLimit) {
                // restore all shared consumed
                sharedBytes += bytesUsed - dedicatedLimit;
            }
            bytesUsed -= bytes;
            assert bytesUsed >= 0;
        }

        /**
         * Resetting bytesUsed will be done before deserializing digital storage.
         */
        @Override
        public void reset() {}

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.put("items", internalItem.serializeNBT());
            tag.put("fluids", internalFluid.serializeNBT());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            bytesUsed = 0;
            internalItem.deserializeNBT(tag.getCompound("items"));
            internalFluid.deserializeNBT(tag.getCompound("fluids"));
        }
    }

    private final List<Storage> storages = new ArrayList<>();
    private Layout layout = Layout.EMPTY;

    public record Properties(int maxParallel, int bytesLimit, int dedicatedBytes, int amountByteLimit) {}

    public DigitalInterface(BlockEntity be, Properties properties) {
        super(be);
        this.maxParallel = properties.maxParallel();
        this.bytesLimit = properties.bytesLimit();
        this.dedicatedLimit = properties.dedicatedBytes();
        this.sharedBytes = bytesLimit;
        this.amountByteLimit = properties.amountByteLimit();
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Properties properties) {
        return $ -> $.capability(ID, be -> new DigitalInterface(be, properties));
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
    public IPort getPort(int port, boolean internal) {
        return storages.get(port).port(internal);
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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == LAYOUT_PROVIDER.get()) {
            return myself();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();

        var tag1 = new ListTag();
        for (var storage : storages) {
            tag1.add(storage.serializeNBT());
        }
        tag.put("storage", tag1);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);

        sharedBytes = bytesLimit;
        storages.clear();
        for (var tag1 : tag.getList("storage", Tag.TAG_COMPOUND)) {
            var storage = new Storage();
            storage.deserializeNBT((CompoundTag) tag1);
            storages.add(storage);
        }
    }
}
