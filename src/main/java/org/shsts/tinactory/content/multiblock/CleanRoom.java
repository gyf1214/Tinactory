package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.core.multiblock.MultiBlockCheckCtx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleanRoom extends MultiBlock implements IProcessor, IElectricMachine,
    INBTSerializable<CompoundTag> {
    private double cleanness = 0d;

    public CleanRoom(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder.layout(AllLayouts.CLEANROOM));
    }

    @Override
    public long getVoltage() {
        return getInterface().map($ -> $.voltage.value).orElse(0L);
    }

    @Override
    public ElectricMachineType getMachineType() {
        return ElectricMachineType.CONSUMER;
    }

    @Override
    public double getPowerGen() {
        return 0;
    }

    @Override
    public double getPowerCons() {
        return TinactoryConfig.INSTANCE.cleanroomAmperage.get() * getVoltage();
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {
        var voltage = getVoltage();
        if (voltage <= 0) {
            return;
        }
        var workFactor = partial * Math.sqrt((double) voltage / (double) Voltage.ULV.value);
        cleanness = Math.min(1d, cleanness + workFactor * TinactoryConfig.INSTANCE.cleanroomBaseClean.get());
    }

    @Override
    public double getProgress() {
        return cleanness;
    }

    private boolean isOpen() {
        return multiBlockInterface == null;
    }

    @Override
    protected void onServerTick() {
        super.onServerTick();
        var decay = isOpen() ? TinactoryConfig.INSTANCE.cleanroomOpenDecay.get() :
            TinactoryConfig.INSTANCE.cleanroomBaseDecay.get();
        cleanness = cleanness * (1d - decay);

        blockEntity.setChanged();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == PROCESSOR.get()) {
            return myself();
        } else if (cap == ELECTRIC_MACHINE.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putDouble("cleanness", cleanness);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        cleanness = tag.getDouble("cleanness");
    }

    private static class Spec implements Consumer<MultiBlockCheckCtx> {
        private final Supplier<Block> baseBlock;
        private final Supplier<Block> ceilingBlock;
        private final TagKey<Block> wallTag;
        @Nullable
        private final TagKey<Block> doorTag;
        @Nullable
        private final TagKey<Block> connectorTag;
        private final int maxDoor;
        private final int maxConnector;
        private final int maxSize;

        public Spec(SpecBuilder<?> builder) {
            this.baseBlock = Objects.requireNonNull(builder.baseBlock);
            this.ceilingBlock = Objects.requireNonNull(builder.ceilingBlock);
            this.wallTag = Objects.requireNonNull(builder.wallTag);
            this.doorTag = builder.doorTag;
            this.connectorTag = builder.connectorTag;
            this.maxDoor = builder.maxDoor;
            this.maxConnector = builder.maxConnector;
            this.maxSize = builder.maxSize;

            assert maxDoor <= 0 || doorTag != null;
            assert maxConnector <= 0 || connectorTag != null;
            assert maxSize > 0;
        }

        private boolean getSizes(MultiBlockCheckCtx ctx) {
            var center = ctx.getCenter();
            var w = 1;
            var h = 1;
            for (; w < maxSize; w++) {
                if (ctx.getBlock(center.east(w)).filter($ -> $.is(ceilingBlock.get())).isEmpty()) {
                    break;
                }
            }
            for (; h < maxSize; h++) {
                if (ctx.getBlock(center.south(h)).filter($ -> $.is(ceilingBlock.get())).isEmpty()) {
                    break;
                }
            }
            for (var x = -w; x <= w; x++) {
                for (var z = -h; z <= h; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }
                    var edge = x == -w || x == w || z == -h || z == h;
                    var block = ctx.getBlock(center.offset(x, 0, z));
                    if (block.isEmpty()) {
                        ctx.setFailed();
                        return false;
                    }
                    if (edge) {
                        if (!block.get().is(baseBlock.get())) {
                            return false;
                        }
                    } else {
                        if (!block.get().is(ceilingBlock.get())) {
                            return false;
                        }
                    }
                }
            }
            ctx.setProperty("w", w);
            ctx.setProperty("h", h);
            ctx.setProperty("doors", new ArrayList<BlockPos>());
            ctx.setProperty("connectors", 0);
            return true;
        }

        private boolean checkGround(MultiBlockCheckCtx ctx, int y) {
            if (y <= 1) {
                return false;
            }
            var center = ctx.getCenter().below(y);
            var w = (int) ctx.getProperty("w");
            var h = (int) ctx.getProperty("h");
            var blocks = new ArrayList<BlockPos>();
            for (var x = -w; x <= w; x++) {
                for (var z = -h; z <= h; z++) {
                    var pos = center.offset(x, 0, z);
                    var block = ctx.getBlock(pos);
                    if (block.isEmpty() || !block.get().is(baseBlock.get())) {
                        return false;
                    }
                    blocks.add(pos);
                }
            }
            for (var pos : blocks) {
                ctx.addBlock(pos);
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        private boolean checkWallBlock(MultiBlockCheckCtx ctx, BlockPos pos, BlockState block) {
            if (doorTag != null && block.is(doorTag)) {
                var doors = (List<BlockPos>) ctx.getProperty("doors");
                if (doors.size() < maxDoor) {
                    doors.add(pos);
                    return true;
                } else {
                    return false;
                }
            }
            if (connectorTag != null && block.is(connectorTag)) {
                var connectors = (int) ctx.getProperty("connectors");
                if (connectors < maxConnector) {
                    ctx.setProperty("connectors", connectors + 1);
                    return true;
                } else {
                    return false;
                }
            }
            if (MultiBlockSpec.checkInterface(ctx, pos)) {
                return !ctx.isFailed();
            }
            return block.is(wallTag);
        }

        private boolean checkWall(MultiBlockCheckCtx ctx, int y) {
            var center = ctx.getCenter().below(y);
            var w = (int) ctx.getProperty("w");
            var h = (int) ctx.getProperty("h");
            for (var x = -w; x <= w; x++) {
                var corner = x == -w || x == w;
                var pos1 = center.offset(x, 0, -h);
                var block1 = ctx.getBlock(pos1);
                var pos2 = center.offset(x, 0, h);
                var block2 = ctx.getBlock(pos2);
                if (block1.isEmpty() || block2.isEmpty()) {
                    return false;
                }
                if (corner) {
                    if (!block1.get().is(baseBlock.get()) || !block2.get().is(baseBlock.get())) {
                        return false;
                    }
                } else {
                    if (!checkWallBlock(ctx, pos1, block1.get()) || !checkWallBlock(ctx, pos2, block2.get())) {
                        return false;
                    }
                }
                ctx.addBlock(pos1);
                ctx.addBlock(pos2);
            }

            for (var z = -h + 1; z <= h - 1; z++) {
                var pos1 = center.offset(w, 0, z);
                var block1 = ctx.getBlock(pos1);
                var pos2 = center.offset(-w, 0, z);
                var block2 = ctx.getBlock(pos2);
                if (block1.isEmpty() || block2.isEmpty() ||
                    !checkWallBlock(ctx, pos1, block1.get()) || !checkWallBlock(ctx, pos2, block2.get())) {
                    return false;
                }
                ctx.addBlock(pos1);
                ctx.addBlock(pos2);
            }

            return true;
        }

        private boolean checkLayer(MultiBlockCheckCtx ctx, int y) {
            if (checkGround(ctx, y)) {
                return false;
            }
            if (!checkWall(ctx, y)) {
                ctx.setFailed();
                return false;
            }
            return true;
        }

        private void checkLayers(MultiBlockCheckCtx ctx) {
            for (var y = 1; y < maxSize; y++) {
                if (!checkLayer(ctx, y)) {
                    ctx.isFailed();
                    return;
                }
            }
            ctx.setFailed();
        }

        @Override
        public void accept(MultiBlockCheckCtx ctx) {
            if (!getSizes(ctx)) {
                ctx.setFailed();
                return;
            }
            checkLayers(ctx);
        }
    }

    public static class SpecBuilder<P> extends SimpleBuilder<Consumer<MultiBlockCheckCtx>, P, SpecBuilder<P>> {
        private Supplier<Block> baseBlock = null;
        private Supplier<Block> ceilingBlock = null;
        private TagKey<Block> wallTag = null;
        private TagKey<Block> doorTag = null;
        private TagKey<Block> connectorTag = null;
        private int maxDoor = 0;
        private int maxConnector = 0;
        private int maxSize = 0;

        private SpecBuilder(P parent) {
            super(parent);
        }

        public SpecBuilder<P> baseBlock(Supplier<Block> val) {
            this.baseBlock = val;
            return this;
        }

        public SpecBuilder<P> ceilingBlock(Supplier<Block> val) {
            this.ceilingBlock = val;
            return this;
        }

        public SpecBuilder<P> wallTag(TagKey<Block> val) {
            this.wallTag = val;
            return this;
        }

        public SpecBuilder<P> doorTag(TagKey<Block> val) {
            this.doorTag = val;
            return this;
        }

        public SpecBuilder<P> connectorTag(TagKey<Block> val) {
            this.connectorTag = val;
            return this;
        }

        public SpecBuilder<P> maxDoor(int val) {
            this.maxDoor = val;
            return this;
        }

        public SpecBuilder<P> maxConnector(int val) {
            this.maxConnector = val;
            return this;
        }

        public SpecBuilder<P> maxSize(int val) {
            this.maxSize = val;
            return this;
        }

        @Override
        protected Consumer<MultiBlockCheckCtx> createObject() {
            return new Spec(this);
        }
    }

    public static <P> SpecBuilder<P> spec(P parent) {
        return new SpecBuilder<>(parent);
    }
}
