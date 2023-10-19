package org.shsts.tinactory.test;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.network.AllSchedulings;
import org.shsts.tinactory.network.Component;
import org.shsts.tinactory.network.Network;
import org.shsts.tinactory.network.Scheduling;

import javax.annotation.Nonnull;
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
    private final IItemHandler outputBuffer = new ItemStackHandler(1);

    private void onWorkTick(Level world, Network network) {
        if (this.workProgress >= 5 * 20) {
            this.outputBuffer.insertItem(0, new ItemStack(Items.COBBLESTONE, 1), false);
            this.workProgress = 0;
        } else {
            this.workProgress++;
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> outputBuffer).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<Scheduling>, Component.Ticker> cons) {
        cons.accept(AllSchedulings.WORK, this::onWorkTick);
    }
}
