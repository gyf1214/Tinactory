package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;

import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AutocraftServiceBootstrap {
    private AutocraftServiceBootstrap() {}

    public static AutocraftJobService create(
        BlockEntity blockEntity,
        INetwork network,
        LogisticComponent logistics,
        IItemPort itemPort,
        IFluidPort fluidPort,
        UUID cpuId,
        long transmissionBandwidth,
        int executionIntervalTicks) {

        var level = blockEntity.getLevel();
        if (level == null || level.isClientSide) {
            throw new IllegalStateException("autocraft service must be created on server level");
        }
        var inventory = new LogisticsInventoryView(itemPort, fluidPort);
        BlockPos subnet = network.getSubnet(blockEntity.getBlockPos());
        var allocator = new LogisticsMachineAllocator(() -> logistics.getVisiblePorts(subnet));
        var planner = new GoalReductionPlanner(new LogisticsPatternRepository(logistics.listVisiblePatterns()));
        return new AutocraftJobService(
            cpuId,
            planner,
            () -> new SequentialCraftExecutor(inventory, allocator, new SilentJobEvents()),
            inventory::snapshotAvailable,
            transmissionBandwidth,
            executionIntervalTicks);
    }

    private static final class SilentJobEvents implements IJobEvents {
    }
}
