package org.shsts.tinactory.content.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.multiblock.MultiBlockBase;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiBlock extends MultiBlockBase {
    /**
     * must set this during checkMultiBlock, or fail
     */
    @Nullable
    protected MultiBlockInterface multiBlockInterface = null;

    public MultiBlock(BlockEntity blockEntity) {
        super(blockEntity);
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.MULTI_BLOCK.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>> blastFurnace() {
        return CapabilityProviderBuilder.fromFactory("multi_block", BlastFurnace::new);
    }
}
