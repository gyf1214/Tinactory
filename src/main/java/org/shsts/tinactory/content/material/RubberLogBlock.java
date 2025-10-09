package org.shsts.tinactory.content.material;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RubberLogBlock extends RotatedPillarBlock {
    public static final BooleanProperty HAS_RUBBER = BooleanProperty.create("has_rubber");

    public RubberLogBlock(Properties prop) {
        super(prop);
        registerDefaultState(defaultBlockState().setValue(HAS_RUBBER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_RUBBER);
    }
}
