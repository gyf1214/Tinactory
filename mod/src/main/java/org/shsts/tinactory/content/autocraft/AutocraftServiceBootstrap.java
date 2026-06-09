package org.shsts.tinactory.content.autocraft;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.plan.AutocraftMemoryConfig;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinactory.integration.autocraft.LogisticsInventoryView;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AutocraftServiceBootstrap {
    private AutocraftServiceBootstrap() {}

    public static AutocraftJobService create(
        LogisticComponent logistics,
        IPort<ItemStack> itemPort,
        IPort<FluidStack> fluidPort,
        long itemBandwidth,
        long fluidBandwidth,
        int executionIntervalTicks,
        long memoryLimit) {

        var inventory = new LogisticsInventoryView(itemPort, fluidPort);
        var allocator = new LogisticsMachineAllocator(logistics);
        var executor = new SequentialCraftExecutor(inventory, allocator, IJobEvents.NO_OP);
        return new AutocraftJobService(
            executor,
            itemBandwidth,
            fluidBandwidth,
            executionIntervalTicks,
            memoryLimit);
    }

    public static AutocraftTerminalService createTerminalService(
        AutocraftComponent autocraft,
        IPort<ItemStack> itemPort,
        IPort<FluidStack> fluidPort) {

        var inventory = new LogisticsInventoryView(itemPort, fluidPort);
        var repository = autocraft.patternRepository();
        var memoryConfig = new AutocraftMemoryConfig(
            TinactoryConfig.CONFIG.bytesPerCraftStep.get(),
            TinactoryConfig.CONFIG.bytesPerItem.get(),
            TinactoryConfig.CONFIG.bytesPerItemType.get(),
            TinactoryConfig.CONFIG.bytesPerFluid.get(),
            TinactoryConfig.CONFIG.bytesPerFluidType.get());
        var planner = new GoalReductionPlanner(repository, inventory, memoryConfig);
        return new AutocraftTerminalService(
            planner,
            repository,
            autocraft);
    }
}
