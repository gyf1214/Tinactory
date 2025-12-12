package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.network.MachineBlock;
import org.shsts.tinactory.core.network.PrimitiveBlock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeTurbine extends Multiblock {
    public LargeTurbine(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder);
    }

    private void setBladeBlocks(Level world, Direction dir, boolean working) {
        var dirW = dir.getCounterClockWise();
        var pos = blockEntity.getBlockPos();
        for (var k = 0; k < TurbineBlock.BLADES; k++) {
            if (k == TurbineBlock.CENTER_BLADE) {
                continue;
            }
            var pos1 = pos.relative(dirW, (k % 3) - 1).below((k / 3) - 1);
            if (!world.isLoaded(pos1)) {
                continue;
            }
            var state1 = world.getBlockState(pos1);
            if (!(state1.getBlock() instanceof TurbineBlock)) {
                continue;
            }
            var state2 = state1.setValue(TurbineBlock.BLADE, k)
                .setValue(TurbineBlock.FACING, dir)
                .setValue(TurbineBlock.WORKING, working);
            world.setBlock(pos1, state2, 19);
        }
    }

    @Override
    protected void onRegister() {
        super.onRegister();
        var world = blockEntity.getLevel();
        assert world != null;
        var dir = blockEntity.getBlockState().getValue(PrimitiveBlock.FACING);
        setBladeBlocks(world, dir, false);
    }

    @Override
    public void setWorkBlock(Level world, BlockState state) {
        super.setWorkBlock(world, state);
        var dir = state.getValue(PrimitiveBlock.FACING);
        var working = state.getValue(MachineBlock.WORKING);
        setBladeBlocks(world, dir, working);
    }
}
