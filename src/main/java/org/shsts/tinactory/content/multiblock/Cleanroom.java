package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinactory.core.multiblock.IMultiblockCheckCtx;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.multiblock.MultiblockManager;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.content.AllEvents.CONNECT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Cleanroom extends Multiblock implements IProcessor, IElectricMachine,
    INBTSerializable<CompoundTag> {
    public record Properties(double amperage, double baseClean, double baseDecay, double openDecay) {}

    private final Properties properties;

    private record DoorState(BlockPos pos, Direction facing) {}

    private double cleanness = 0d;
    private int w, d, h;
    private int size;
    private List<DoorState> doors;
    private boolean stopped = false;

    public Cleanroom(BlockEntity blockEntity, Builder<?> builder, Properties properties) {
        super(blockEntity, builder);
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doCheckMultiblock(CheckContext ctx) {
        super.doCheckMultiblock(ctx);
        if (!ctx.isFailed()) {
            w = (int) ctx.getProperty("w");
            d = (int) ctx.getProperty("d");
            h = (int) ctx.getProperty("h");
            size = (2 * w - 1) * (2 * d - 1);
            doors = (List<DoorState>) ctx.getProperty("doors");
        }
    }

    @Override
    protected void onRegister() {
        super.onRegister();
        manager.registerCleanroom(this, blockEntity.getBlockPos(), w, d, h);
    }

    @Override
    public IMenuType menu(IMachine machine) {
        return AllMenus.PRIMITIVE_MACHINE;
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
        return stopped ? 0 : properties.amperage * getVoltage() * Math.sqrt(size);
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {
        if (stopped) {
            stopped = false;
            return;
        }
        var voltage = getVoltage();
        if (voltage <= 0) {
            return;
        }
        var workFactor = partial * Math.sqrt((double) voltage / (double) Voltage.ULV.value);
        var clean = workFactor * properties.baseClean;

        cleanness = Math.min(1d, cleanness + clean - cleanness * clean);
    }

    @Override
    public double getProgress() {
        return cleanness;
    }

    private boolean isOpen() {
        if (multiblockInterface == null) {
            return true;
        }
        var world = blockEntity.getLevel();
        assert world != null;
        for (var doorState : doors) {
            if (!world.isLoaded(doorState.pos)) {
                return true;
            }
            var state = world.getBlockState(doorState.pos);
            var axis = state.getOptionalValue(DoorBlock.FACING).map(Direction::getAxis);
            var open = state.getOptionalValue(DoorBlock.OPEN);
            if (axis.isEmpty() || open.isEmpty() ||
                ((axis.get() == doorState.facing.getAxis()) == open.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onServerTick() {
        super.onServerTick();
        var decay = isOpen() ? properties.openDecay : properties.baseDecay;
        cleanness = cleanness * (1d - decay);

        blockEntity.setChanged();
    }

    private void onConnect(INetwork network) {
        if (multiblockInterface != null) {
            Machine.registerStopSignal(network, multiblockInterface, $ -> stopped = $);
        }
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(CONNECT.get(), this::onConnect);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == PROCESSOR.get() || cap == ELECTRIC_MACHINE.get()) {
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

    private static class Spec implements Consumer<IMultiblockCheckCtx> {
        private final Supplier<Block> baseBlock;
        private final Supplier<Block> ceilingBlock;
        private final TagKey<Block> wallTag;
        @Nullable
        private final TagKey<Block> doorTag;
        @Nullable
        private final TagKey<Block> connectorTag;
        private final int maxSize;
        private final int maxDoors;
        private final int maxConnectors;

        public Spec(SpecBuilder<?> builder) {
            this.baseBlock = Objects.requireNonNull(builder.baseBlock);
            this.ceilingBlock = Objects.requireNonNull(builder.ceilingBlock);
            this.wallTag = Objects.requireNonNull(builder.wallTag);
            this.doorTag = builder.doorTag;
            this.connectorTag = builder.connectorTag;
            this.maxSize = builder.maxSize;
            this.maxDoors = builder.maxDoors;
            this.maxConnectors = builder.maxConnectors;

            assert maxSize > 0;
            assert maxDoors <= 0 || doorTag != null;
            assert maxConnectors <= 0 || connectorTag != null;
        }

        private boolean getSizes(IMultiblockCheckCtx ctx) {
            var center = ctx.getCenter();
            var w = 1;
            var d = 1;
            for (; w < maxSize; w++) {
                if (ctx.getBlock(center.east(w)).filter($ -> $.is(ceilingBlock.get())).isEmpty()) {
                    break;
                }
            }
            for (; d < maxSize; d++) {
                if (ctx.getBlock(center.south(d)).filter($ -> $.is(ceilingBlock.get())).isEmpty()) {
                    break;
                }
            }
            for (var x = -w; x <= w; x++) {
                for (var z = -d; z <= d; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }
                    var edge = x == -w || x == w || z == -d || z == d;
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
            ctx.setProperty("d", d);
            ctx.setProperty("doors", new ArrayList<DoorState>());
            ctx.setProperty("connectors", 0);
            return true;
        }

        private boolean checkGroundBlock(IMultiblockCheckCtx ctx, BlockPos pos, BlockState block) {
            if (MultiblockSpec.checkInterface(ctx, pos)) {
                ctx.setProperty("interfaceSetByGround", true);
                return !ctx.isFailed();
            }
            return block.is(baseBlock.get());
        }

        private boolean checkGround(IMultiblockCheckCtx ctx, int y) {
            if (y <= 1) {
                return false;
            }
            var center = ctx.getCenter().below(y);
            var w = (int) ctx.getProperty("w");
            var d = (int) ctx.getProperty("d");
            var blocks = new ArrayList<BlockPos>();
            for (var x = -w; x <= w; x++) {
                for (var z = -d; z <= d; z++) {
                    var pos = center.offset(x, 0, z);
                    var block = ctx.getBlock(pos);
                    if (block.isEmpty() || !checkGroundBlock(ctx, pos, block.get())) {
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
        private boolean checkWallBlock(IMultiblockCheckCtx ctx, BlockPos pos, BlockState block, Direction face) {
            if (MultiblockSpec.checkInterface(ctx, pos)) {
                return !ctx.isFailed();
            }
            if (doorTag != null && block.is(doorTag)) {
                var doors = (List<DoorState>) ctx.getProperty("doors");
                if (doors.size() < maxDoors) {
                    doors.add(new DoorState(pos, face));
                    return true;
                } else {
                    return false;
                }
            }
            if (connectorTag != null && block.is(connectorTag)) {
                var connectors = (int) ctx.getProperty("connectors");
                if (connectors < maxConnectors) {
                    ctx.setProperty("connectors", connectors + 1);
                    return true;
                } else {
                    return false;
                }
            }
            return block.is(wallTag);
        }

        private boolean checkPillarBlock(IMultiblockCheckCtx ctx, BlockPos pos, BlockState block) {
            if (MultiblockSpec.checkInterface(ctx, pos)) {
                return !ctx.isFailed();
            }
            return block.is(baseBlock.get());
        }

        private boolean checkWall(IMultiblockCheckCtx ctx, int y) {
            var center = ctx.getCenter().below(y);
            var w = (int) ctx.getProperty("w");
            var d = (int) ctx.getProperty("d");
            for (var x = -w; x <= w; x++) {
                var corner = x == -w || x == w;
                var pos1 = center.offset(x, 0, -d);
                var block1 = ctx.getBlock(pos1);
                var pos2 = center.offset(x, 0, d);
                var block2 = ctx.getBlock(pos2);
                if (block1.isEmpty() || block2.isEmpty()) {
                    return false;
                }
                if (corner) {
                    if (!checkPillarBlock(ctx, pos1, block1.get()) ||
                        !checkPillarBlock(ctx, pos2, block2.get())) {
                        return false;
                    }
                } else {
                    if (!checkWallBlock(ctx, pos1, block1.get(), Direction.NORTH) ||
                        !checkWallBlock(ctx, pos2, block2.get(), Direction.SOUTH)) {
                        return false;
                    }
                }
                ctx.addBlock(pos1);
                ctx.addBlock(pos2);
            }

            for (var z = -d + 1; z <= d - 1; z++) {
                var pos1 = center.offset(w, 0, z);
                var block1 = ctx.getBlock(pos1);
                var pos2 = center.offset(-w, 0, z);
                var block2 = ctx.getBlock(pos2);
                if (block1.isEmpty() || block2.isEmpty() ||
                    !checkWallBlock(ctx, pos1, block1.get(), Direction.EAST) ||
                    !checkWallBlock(ctx, pos2, block2.get(), Direction.WEST)) {
                    return false;
                }
                ctx.addBlock(pos1);
                ctx.addBlock(pos2);
            }

            return true;
        }

        private boolean checkLayer(IMultiblockCheckCtx ctx, int y) {
            if (checkGround(ctx, y)) {
                return false;
            } else {
                if (ctx.isFailed()) {
                    return false;
                }
                // TODO: deal with the problem that the "try" test will modify property
                if (ctx.hasProperty("interfaceSetByGround")) {
                    ctx.deleteProperty("interfaceSetByGround");
                    ctx.deleteProperty("interface");
                }
            }
            if (!checkWall(ctx, y)) {
                ctx.setFailed();
                return false;
            }
            return true;
        }

        private void checkLayers(IMultiblockCheckCtx ctx) {
            for (var y = 1; y < maxSize; y++) {
                if (!checkLayer(ctx, y)) {
                    ctx.setProperty("h", y);
                    return;
                }
            }
            ctx.setFailed();
        }

        @Override
        public void accept(IMultiblockCheckCtx ctx) {
            if (!getSizes(ctx)) {
                ctx.setFailed();
                return;
            }
            checkLayers(ctx);
        }
    }

    public static class SpecBuilder<P> extends SimpleBuilder<Consumer<IMultiblockCheckCtx>, P, SpecBuilder<P>> {
        private Supplier<Block> baseBlock = null;
        private Supplier<Block> ceilingBlock = null;
        private TagKey<Block> wallTag = null;
        private TagKey<Block> doorTag = null;
        private TagKey<Block> connectorTag = null;
        private int maxSize = 0;
        private int maxDoors = 0;
        private int maxConnectors = 0;

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

        public SpecBuilder<P> maxSize(int val) {
            this.maxSize = val;
            return this;
        }

        public SpecBuilder<P> maxDoors(int val) {
            this.maxDoors = val;
            return this;
        }

        public SpecBuilder<P> maxConnectors(int val) {
            this.maxConnectors = val;
            return this;
        }

        @Override
        protected Consumer<IMultiblockCheckCtx> createObject() {
            return new Spec(this);
        }
    }

    public static <P> SpecBuilder<P> spec(P parent) {
        return new SpecBuilder<>(parent);
    }

    public static double getCleanness(Level world, BlockPos pos) {
        assert !world.isClientSide;
        return MultiblockManager.get(world).getCleanroom(pos)
            .map($ -> ((Cleanroom) $).cleanness).orElse(0d);
    }
}
