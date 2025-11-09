package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.shsts.tinactory.content.network.MachineBlock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FixedBlock extends Block {
    public static final BooleanProperty WORKING = MachineBlock.WORKING;

    public FixedBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WORKING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WORKING);
    }
}
