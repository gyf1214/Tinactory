package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
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
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.multiblock.BlastFurnace;
import org.shsts.tinactory.content.multiblock.CleanRoom;
import org.shsts.tinactory.content.multiblock.DistillationTower;
import org.shsts.tinactory.content.multiblock.MultiblockSpec;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.IBuilder;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;
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

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllEvents.CLIENT_TICK;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Multiblock extends MultiblockBase {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "multiblock";

    private Layout layout;
    private final Consumer<MultiblockCheckCtx> checker;
    private final Supplier<BlockState> appearance;

    private boolean firstTick = false;
    @Nullable
    protected BlockPos multiblockInterfacePos = null;
    /**
     * must set this during checkMultiblock, or fail
     */
    @Nullable
    protected MultiblockInterface multiblockInterface = null;

    public Multiblock(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity);
        this.layout = Objects.requireNonNull(builder.layout);
        this.checker = Objects.requireNonNull(builder.checker);
        this.appearance = Objects.requireNonNull(builder.appearance);
    }

    public BlockState getAppearanceBlock() {
        return appearance.get();
    }

    public Layout getLayout() {
        return layout;
    }

    protected void setLayout(Layout val) {
        layout = val;
        if (multiblockInterface != null) {
            multiblockInterface.setLayout(val);
        }
    }

    protected static class CheckContext implements MultiblockCheckCtx {
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
        public void setFailed(boolean val) {
            failed = val;
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

    protected void doCheckMultiblock(CheckContext ctx) {
        checker.accept(ctx);
    }

    @Override
    protected Optional<Collection<BlockPos>> checkMultiblock() {
        LOGGER.debug("{}: check multiblock", this.blockEntity);

        var world = blockEntity.getLevel();
        assert world != null;
        var context = new CheckContext(world, blockEntity.getBlockPos());
        doCheckMultiblock(context);
        var ok = !context.failed && context.hasProperty("interface") &&
            (multiblockInterface == null || context.getProperty("interface") == multiblockInterface);
        if (ok) {
            multiblockInterface = (MultiblockInterface) context.getProperty("interface");
            return Optional.of(context.blocks);
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected void onRegister() {
        assert multiblockInterface != null;
        multiblockInterface.setMultiblock(this);
        sendUpdate(blockEntity);
        invoke(blockEntity, SET_MACHINE_CONFIG);
    }

    @Override
    protected void onInvalidate() {
        if (multiblockInterface != null) {
            multiblockInterface.resetMultiblock();
        }
        multiblockInterface = null;
        sendUpdate(blockEntity);
        invoke(blockEntity, SET_MACHINE_CONFIG);
    }

    public Optional<MultiblockInterface> getInterface() {
        return Optional.ofNullable(multiblockInterface);
    }

    public Optional<IContainer> getContainer() {
        return getInterface().flatMap(MultiblockInterface::container);
    }

    public IProcessor getProcessor() {
        return AllCapabilities.PROCESSOR.get(blockEntity);
    }

    public IElectricMachine getElectric() {
        return AllCapabilities.ELECTRIC_MACHINE.get(blockEntity);
    }

    /**
     * Should only be called on Client
     */
    protected void updateMultiblockInterface() {
        var world = blockEntity.getLevel();
        assert world != null && world.isClientSide;

        LOGGER.debug("update multiblockInterface current={}, pos={}, firstTick={}",
            multiblockInterface, multiblockInterfacePos, firstTick);

        if (multiblockInterfacePos != null) {
            var be1 = world.getBlockEntity(multiblockInterfacePos);
            if (be1 == null) {
                LOGGER.debug("cannot get blockEntity {}:{}",
                    world.dimension().location(), multiblockInterfacePos);
                return;
            }
            MACHINE.tryGet(be1).ifPresent(machine ->
                multiblockInterface = (MultiblockInterface) machine);
        } else {
            multiblockInterface = null;
        }
        invoke(blockEntity, SET_MACHINE_CONFIG);
    }

    private void onClientTick() {
        if (!firstTick) {
            updateMultiblockInterface();
            firstTick = true;
        }
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(CLIENT_TICK.get(), $ -> onClientTick());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = new CompoundTag();
        if (multiblockInterface != null) {
            var pos = multiblockInterface.blockEntity.getBlockPos();
            tag.put("interfacePos", CodecHelper.encodeBlockPos(pos));
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        multiblockInterfacePos = tag.contains("interfacePos", Tag.TAG_COMPOUND) ?
            CodecHelper.parseBlockPos(tag.getCompound("interfacePos")) : null;
        if (firstTick) {
            updateMultiblockInterface();
        }
    }

    public static class Builder<P> extends SimpleBuilder<Function<BlockEntity, Multiblock>,
        IBlockEntityTypeBuilder<P>, Builder<P>> {

        private final BiFunction<BlockEntity, Builder<P>, Multiblock> factory;
        @Nullable
        private Supplier<BlockState> appearance = null;
        @Nullable
        private Layout layout = null;
        @Nullable
        private Consumer<MultiblockCheckCtx> checker = null;

        public Builder(IBlockEntityTypeBuilder<P> parent,
            BiFunction<BlockEntity, Builder<P>, Multiblock> factory) {
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

        public <S extends IBuilder<? extends Consumer<MultiblockCheckCtx>, Builder<P>, S>> S spec(
            Function<Builder<P>, S> child) {
            return child(child).onCreateObject($ -> this.checker = $);
        }

        public MultiblockSpec.Builder<Builder<P>> spec() {
            return spec(MultiblockSpec::builder);
        }

        @Override
        protected Function<BlockEntity, Multiblock> createObject() {
            return be -> factory.apply(be, this);
        }
    }

    public static <P, R extends ProcessingRecipe,
        B extends IRecipeBuilderBase<R>> Function<IBlockEntityTypeBuilder<P>, Builder<P>> simple(
        IRecipeType<B> recipeType, boolean autoRecipe) {

        return p -> new Builder<>(p.transform(RecipeProcessor.multiblock(recipeType, autoRecipe)),
            Multiblock::new);
    }

    public static <P> Builder<P> blastFurnace(IBlockEntityTypeBuilder<P> parent) {
        return new Builder<>(parent.transform(RecipeProcessor::blastFurnace), BlastFurnace::new);
    }

    public static <P> Builder<P> distillationTower(IBlockEntityTypeBuilder<P> parent) {
        return new Builder<>(parent.transform(RecipeProcessor.multiblock(AllRecipes.DISTILLATION, true)),
            DistillationTower::new);
    }

    public static <P> Builder<P> cleanRoom(IBlockEntityTypeBuilder<P> parent) {
        return new Builder<>(parent, CleanRoom::new);
    }

    public static Optional<Multiblock> tryGet(BlockEntity be) {
        return tryGet(be, ID, Multiblock.class);
    }

    public static Multiblock get(BlockEntity be) {
        return get(be, ID, Multiblock.class);
    }
}
