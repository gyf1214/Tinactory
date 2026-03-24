package org.shsts.tinactory.integration.autocraft;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.content.autocraft.AutocraftComponent;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AutocraftServiceBootstrap {
    private AutocraftServiceBootstrap() {}

    public static AutocraftJobService create(
        BlockEntity blockEntity,
        LogisticComponent logistics,
        IPort<ItemStack> itemPort,
        IPort<FluidStack> fluidPort,
        long transmissionBandwidth,
        int executionIntervalTicks) {

        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            throw new IllegalStateException("autocraft service must be created on server level");
        }
        var inventory = new LogisticsInventoryView(itemPort, fluidPort);
        var allocator = new LogisticsMachineAllocator(logistics::getAllPorts);
        var executor = new SequentialCraftExecutor(inventory, allocator, new SilentJobEvents());
        return new AutocraftJobService(
            executor,
            transmissionBandwidth,
            executionIntervalTicks);
    }

    public static AutocraftTerminalService createTerminalService(
        BlockEntity blockEntity,
        AutocraftComponent autocraft,
        IPort<ItemStack> itemPort,
        IPort<FluidStack> fluidPort) {

        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            throw new IllegalStateException("autocraft terminal service must be created on server level");
        }
        var inventory = new LogisticsInventoryView(itemPort, fluidPort);
        var repository = autocraft.patternRepository();
        var planner = new GoalReductionPlanner(repository, inventory);
        return new AutocraftTerminalService(
            planner,
            repository,
            autocraft);
    }

    private static final class SilentJobEvents implements IJobEvents {
    }
}
