package org.shsts.tinactory.integration.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.multiblock.MultiblockCheckCtx;
import org.shsts.tinactory.integration.network.PrimitiveBlock;

import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LevelMultiblockCheckCtx extends MultiblockCheckCtx<BlockState> {
    private final Level world;

    public LevelMultiblockCheckCtx(Level world, BlockPos center) {
        super(center);
        this.world = world;
    }

    @Override
    public Optional<BlockState> getBlock(BlockPos pos) {
        if (!world.isLoaded(pos)) {
            return Optional.empty();
        }
        return Optional.of(world.getBlockState(pos));
    }

    @Override
    public Optional<IMachine> getMachine(BlockPos pos) {
        if (!world.isLoaded(pos)) {
            return Optional.empty();
        }
        return Optional.ofNullable(world.getBlockEntity(pos)).flatMap(MACHINE::tryGet);
    }

    @Override
    public Optional<Direction> getFacing() {
        return getBlock(getCenter()).flatMap(block -> block.getOptionalValue(PrimitiveBlock.FACING));
    }
}
