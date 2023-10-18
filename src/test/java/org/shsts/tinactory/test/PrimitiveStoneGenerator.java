package org.shsts.tinactory.test;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.network.AllSchedulings;
import org.shsts.tinactory.network.Component;
import org.shsts.tinactory.network.Network;
import org.shsts.tinactory.network.Scheduling;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveStoneGenerator extends Machine {
    public PrimitiveStoneGenerator(BlockEntityType<PrimitiveStoneGenerator> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private int workProgress = 0;
    private ItemStack outputBuffer = ItemStack.EMPTY;

    private void onWorkTick(Level world, Network network) {
        if (this.workProgress >= 5 * 20) {
            if (this.outputBuffer.isEmpty()) {
                this.outputBuffer = new ItemStack(Items.COBBLESTONE, 1);
            } else {
                this.outputBuffer.setCount(this.outputBuffer.getCount() + 1);
            }
            this.workProgress = 0;
        } else {
            this.workProgress++;
        }
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<Scheduling>, Component.Ticker> cons) {
        cons.accept(AllSchedulings.WORK, this::onWorkTick);
    }
}
