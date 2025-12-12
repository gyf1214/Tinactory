package org.shsts.tinactory.content.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.function.Supplier;

import static org.shsts.tinactory.core.network.MachineBlock.WORKING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FixedMachineBlock extends SmartEntityBlock {
    public FixedMachineBlock(Properties properties, Supplier<IBlockEntityType> entityType,
        @Nullable IMenuType menu) {
        super(properties.requiresCorrectToolForDrops().isValidSpawn(AllItems::never), entityType, menu);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WORKING);
    }

    @Override
    protected BlockState createDefaultBlockState() {
        return super.createDefaultBlockState()
            .setValue(WORKING, false);
    }
}
