package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreBlock extends Block {
    protected final IntegerProperty variant;

    public OreBlock(Properties properties, int states) {
        super(properties.requiresCorrectToolForDrops());
        this.variant = IntegerProperty.create("variant", 0, states);
        // hack to re-assign stateDefinition
        var builder = new StateDefinition.Builder<Block, BlockState>(this);
        builder.add(this.variant);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    public IntegerProperty getProperty() {
        return this.variant;
    }
}
