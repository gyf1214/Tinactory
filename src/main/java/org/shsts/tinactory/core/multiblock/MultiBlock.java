package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.multiblock.BlastFurnace;
import org.shsts.tinactory.content.multiblock.MultiBlockSpec;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiBlock extends MultiBlockBase {
    private static final Logger LOGGER = LogUtils.getLogger();

    public final Layout layout;
    private final Consumer<MultiBlockCheckCtx> checker;
    private final Supplier<BlockState> appearance;

    /**
     * must set this during checkMultiBlock, or fail
     */
    @Nullable
    protected MultiBlockInterface multiBlockInterface = null;

    public MultiBlock(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity);
        this.layout = Objects.requireNonNull(builder.layout);
        this.checker = Objects.requireNonNull(builder.checker);
        this.appearance = Objects.requireNonNull(builder.appearance);
    }

    public BlockState getAppearanceBlock() {
        return appearance.get();
    }

    protected static class CheckContext implements MultiBlockCheckCtx {
        protected boolean failed = false;
        protected final Level world;
        protected final BlockPos center;
        protected final List<BlockPos> blocks = new ArrayList<>();
        protected final Map<String, Object> properties = new HashMap<>();

        protected CheckContext(Level world, BlockPos center) {
            this.world = world;
            this.center = center;
        }

        @Override
        public boolean isFailed() {
            return failed;
        }

        @Override
        public void setFailed() {
            failed = true;
        }

        @Override
        public BlockPos getCenter() {
            return center;
        }

        @Override
        public Optional<BlockState> getBlock(BlockPos pos) {
            if (!world.isLoaded(pos)) {
                return Optional.empty();
            }
            return Optional.of(world.getBlockState(pos));
        }

        @Override
        public Optional<BlockEntity> getBlockEntity(BlockPos pos) {
            if (!world.isLoaded(pos)) {
                return Optional.empty();
            }
            return Optional.ofNullable(world.getBlockEntity(pos));
        }

        @Override
        public void addBlock(BlockPos pos) {
            blocks.add(pos);
        }

        @Override
        public Object getProperty(String key) {
            var val = properties.get(key);
            assert val != null;
            return val;
        }

        @Override
        public void setProperty(String key, Object val) {
            properties.put(key, val);
        }

        @Override
        public boolean hasProperty(String key) {
            return properties.containsKey(key);
        }
    }

    protected void doCheckMultiBlock(CheckContext ctx) {
        checker.accept(ctx);
    }

    @Override
    protected Optional<Collection<BlockPos>> checkMultiBlock() {
        LOGGER.debug("{}: check multiblock", this.blockEntity);

        var world = blockEntity.getLevel();
        assert world != null;
        var context = new CheckContext(world, blockEntity.getBlockPos());
        doCheckMultiBlock(context);
        var ok = !context.failed && context.hasProperty("interface") &&
            (multiBlockInterface == null || context.getProperty("interface") == multiBlockInterface);
        if (ok) {
            multiBlockInterface = (MultiBlockInterface) context.getProperty("interface");
            return Optional.of(context.blocks);
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected void onRegister() {
        assert multiBlockInterface != null;
        multiBlockInterface.setMultiBlock(this);
        sendUpdate(blockEntity);
        invoke(blockEntity, SET_MACHINE_CONFIG);
    }

    @Override
    protected void onInvalidate() {
        if (multiBlockInterface != null) {
            multiBlockInterface.resetMultiBlock();
        }
        multiBlockInterface = null;
        sendUpdate(blockEntity);
        invoke(blockEntity, SET_MACHINE_CONFIG);
    }

    public Optional<MultiBlockInterface> getInterface() {
        return Optional.ofNullable(multiBlockInterface);
    }

    public Optional<IContainer> getContainer() {
        return getInterface().flatMap(MultiBlockInterface::getContainer);
    }

    public IProcessor getProcessor() {
        return AllCapabilities.PROCESSOR.get(blockEntity);
    }

    public IElectricMachine getElectric() {
        return AllCapabilities.ELECTRIC_MACHINE.get(blockEntity);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.MULTI_BLOCK.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = new CompoundTag();
        if (multiBlockInterface != null) {
            var pos = multiBlockInterface.blockEntity.getBlockPos();
            tag.put("interfacePos", CodecHelper.encodeBlockPos(pos));
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        var world = blockEntity.getLevel();
        assert world != null;

        if (tag.contains("interfacePos", Tag.TAG_COMPOUND)) {
            var pos = CodecHelper.parseBlockPos(tag.getCompound("interfacePos"));

            var be1 = world.getBlockEntity(pos);
            if (be1 == null) {
                return;
            }
            AllCapabilities.MACHINE.tryGet(be1).ifPresent(machine ->
                multiBlockInterface = (MultiBlockInterface) machine);
        } else {
            multiBlockInterface = null;
        }

        invoke(blockEntity, SET_MACHINE_CONFIG);
    }

    public static class Builder<P> extends SimpleBuilder<Function<BlockEntity, MultiBlock>,
        IBlockEntityTypeBuilder<P>, Builder<P>> {
        private static final String ID = "multi_block";

        private final BiFunction<BlockEntity, Builder<P>, MultiBlock> factory;
        @Nullable
        private Supplier<BlockState> appearance = null;
        @Nullable
        private Layout layout = null;
        @Nullable
        private Consumer<MultiBlockCheckCtx> checker = null;

        public Builder(IBlockEntityTypeBuilder<P> parent,
            BiFunction<BlockEntity, Builder<P>, MultiBlock> factory) {
            super(parent);
            this.factory = factory;

            onCreateObject($ -> parent.capability(ID, $::apply));
        }

        public Builder<P> layout(Layout val) {
            this.layout = val;
            return self();
        }

        public Builder<P> appearance(Supplier<BlockState> val) {
            this.appearance = val;
            return self();
        }

        public Builder<P> appearanceBlock(Supplier<Block> val) {
            return appearance(() -> val.get().defaultBlockState());
        }

        public MultiBlockSpec.Builder<Builder<P>> spec() {
            return child(MultiBlockSpec::builder)
                .onCreateObject(spec -> this.checker = spec);
        }

        @Override
        protected Function<BlockEntity, MultiBlock> createObject() {
            return be -> factory.apply(be, this);
        }
    }

    public static <P> Builder<P> simple(IBlockEntityTypeBuilder<P> parent) {
        return new Builder<>(parent, MultiBlock::new);
    }

    public static <P> Builder<P> blastFurnace(IBlockEntityTypeBuilder<P> parent) {
        return new Builder<>(parent, BlastFurnace::new);
    }
}
