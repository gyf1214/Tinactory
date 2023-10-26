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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.logistics.OutputItemHandler;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.network.AllNetworks;
import org.shsts.tinactory.network.Component;
import org.shsts.tinactory.network.Network;
import org.shsts.tinactory.network.Scheduling;
import org.shsts.tinactory.util.MathUtil;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveStoneGenerator extends Machine {
    private static final int WORK_TICKS = 5 * 20;

    private int workProgress = 0;
    private final IItemHandlerModifiable outputBuffer;
    private final IItemHandler outputView;

    public PrimitiveStoneGenerator(BlockEntityType<PrimitiveStoneGenerator> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.outputBuffer = new ItemStackHandler(1);
        this.outputView = new OutputItemHandler(this.outputBuffer);
    }

    private void onWorkTick(Level world, Network network) {
        if (this.workProgress >= WORK_TICKS) {
            this.outputBuffer.insertItem(0, new ItemStack(Items.COBBLESTONE, 1), false);
            this.workProgress = 0;
        } else {
            this.workProgress++;
        }
    }

    public double getProgress() {
        return MathUtil.clamp((double) this.workProgress / (double) WORK_TICKS, 0, 1);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> this.outputView).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<Scheduling>, Component.Ticker> cons) {
        cons.accept(AllNetworks.WORK, this::onWorkTick);
    }
}
