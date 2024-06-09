package org.shsts.tinactory.core.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.multiblock.BlastFurnace;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiBlock extends MultiBlockBase {
    public final Layout layout;

    /**
     * must set this during checkMultiBlock, or fail
     */
    @Nullable
    protected MultiBlockInterface multiBlockInterface = null;

    public MultiBlock(BlockEntity blockEntity, Layout layout) {
        super(blockEntity);
        this.layout = layout;
    }

    protected boolean checkInterface(Level world, BlockPos pos) {
        if (multiBlockInterface != null) {
            return false;
        }
        var be = world.getBlockEntity(pos);
        if (be == null) {
            return false;
        }
        var machine = AllCapabilities.MACHINE.tryGet(be);
        if (machine.isEmpty() || !(machine.get() instanceof MultiBlockInterface inter)) {
            return false;
        }
        multiBlockInterface = inter;
        return true;
    }

    @Override
    protected Optional<Collection<BlockPos>> checkMultiBlock(BlockPos start, int tx, int ty, int tz) {
        multiBlockInterface = null;
        var ret = super.checkMultiBlock(start, tx, ty, tz);
        return multiBlockInterface != null ? ret : Optional.empty();
    }

    @Override
    protected void onRegister() {
        assert multiBlockInterface != null;
        multiBlockInterface.setMultiBlock(this);
    }

    @Override
    protected void onInvalidate() {
        if (multiBlockInterface != null) {
            multiBlockInterface.resetMultiBlock();
        }
        multiBlockInterface = null;
    }

    public Optional<MultiBlockInterface> getInterface() {
        if (ref == null) {
            return Optional.empty();
        }
        assert multiBlockInterface != null;
        return Optional.of(multiBlockInterface);
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

    public static <P> CapabilityProviderBuilder<BlockEntity, P> blastFurnace(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, "multi_block", BlastFurnace::new);
    }
}
