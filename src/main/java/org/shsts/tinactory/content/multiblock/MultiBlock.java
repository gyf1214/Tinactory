package org.shsts.tinactory.content.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.core.multiblock.MultiBlockBase;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiBlock extends MultiBlockBase implements ICapabilityProvider {
    public MultiBlock(BlockEntity blockEntity) {
        super(blockEntity);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>> blastFurnace() {
        return CapabilityProviderBuilder.fromFactory("multi_block", BlastFurnace::new);
    }
}
