package org.shsts.tinactory.content.multiblock;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnace extends MultiBlock {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private CoilBlock coilBlock = null;

    public BlastFurnace(SmartBlockEntity blockEntity) {
        super(blockEntity, AllLayouts.BLAST_FURNACE);
    }

    @Override
    public BlockState getAppearanceBlock() {
        return AllItems.HEAT_PROOF_BLOCK.get().defaultBlockState();
    }

    @Override
    protected boolean checkMultiBlock(Level world, int dx, int dy, int dz,
                                      BlockPos pos, BlockState blockState) {
        if (dy == 0 && checkInterface(world, pos)) {
            return true;
        } else if (dy > 0 && dy < 3 && dx == 1 && dz == 1) {
            return blockState.isAir();
        } else if (dy > 0 && dy < 3) {
            if (!blockState.is(AllTags.COIL) || !(blockState.getBlock() instanceof CoilBlock coil)) {
                return false;
            }
            if (coilBlock == null) {
                coilBlock = coil;
                return true;
            } else {
                return blockState.is(coilBlock);
            }
        } else {
            return blockState.is(AllItems.HEAT_PROOF_BLOCK.get());
        }
    }

    @Override
    protected Optional<Collection<BlockPos>> checkMultiBlock() {
        LOGGER.debug("{}: check multiblock", this.blockEntity);

        var facing = blockEntity.getBlockState().getValue(PrimitiveBlock.FACING);
        var start = blockEntity.getBlockPos().relative(facing, -1).offset(-1, 0, -1);
        coilBlock = null;

        return checkMultiBlock(start, 3, 4, 3);
    }
}
