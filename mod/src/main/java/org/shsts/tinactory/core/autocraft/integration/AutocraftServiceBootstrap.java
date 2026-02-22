package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.AllRecipes;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AutocraftServiceBootstrap {
    private AutocraftServiceBootstrap() {}

    @SuppressWarnings("unchecked")
    public static AutocraftJobService create(
        BlockEntity blockEntity,
        INetwork network,
        LogisticComponent logistics,
        IItemPort itemPort,
        IFluidPort fluidPort) {

        var level = blockEntity.getLevel();
        if (level == null || level.isClientSide) {
            throw new IllegalStateException("autocraft service must be created on server level");
        }
        var patterns = new ArrayList<CraftPattern>();
        var recipeManager = CORE.recipeManager(level);
        for (var info : AllRecipes.PROCESSING_TYPES.values()) {
            var recipeType = (IRecipeType<?>) info.recipeType();
            var recipes = (List<ProcessingRecipe>) (List<?>) recipeManager.getAllRecipesFor((IRecipeType) recipeType);
            patterns.addAll(new ProcessingRecipePatternSource(
                LocHelper.modLoc(recipeType.id()),
                recipes).loadPatterns());
        }
        var smelting = level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING);
        patterns.addAll(new SmeltingRecipePatternSource(smelting).loadPatterns());

        var inventory = new LogisticsInventoryView(itemPort, fluidPort);
        var subnet = network.getSubnet(blockEntity.getBlockPos());
        var allocator = new LogisticsMachineAllocator(() -> logistics.getVisiblePorts(subnet));
        var planner = new GoalReductionPlanner(new LogisticsPatternRepository(patterns));
        return new AutocraftJobService(
            planner,
            () -> new SequentialCraftExecutor(inventory, allocator, new SilentJobEvents()),
            inventory::snapshotAvailable);
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    private static final class SilentJobEvents implements IJobEvents {
        @Override
        public void onStepStarted(org.shsts.tinactory.core.autocraft.plan.CraftStep step) {}

        @Override
        public void onStepCompleted(org.shsts.tinactory.core.autocraft.plan.CraftStep step) {}

        @Override
        public void onStepBlocked(org.shsts.tinactory.core.autocraft.plan.CraftStep step, String reason) {}
    }
}
