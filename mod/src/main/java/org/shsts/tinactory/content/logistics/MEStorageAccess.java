package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.AllRecipes;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.exec.SequentialCraftExecutor;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.integration.LogisticsInventoryView;
import org.shsts.tinactory.core.autocraft.integration.LogisticsMachineAllocator;
import org.shsts.tinactory.core.autocraft.integration.LogisticsPatternRepository;
import org.shsts.tinactory.core.autocraft.integration.ProcessingRecipePatternSource;
import org.shsts.tinactory.core.autocraft.integration.SmeltingRecipePatternSource;
import org.shsts.tinactory.core.autocraft.plan.GoalReductionPlanner;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.logistics.CombinedFluidPort;
import org.shsts.tinactory.core.logistics.CombinedItemPort;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.core.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MEStorageAccess extends CapabilityProvider implements IEventSubscriber {
    protected final BlockEntity blockEntity;
    protected final CombinedItemPort combinedItem;
    protected final CombinedFluidPort combinedFluid;
    private final LazyOptional<IElectricMachine> electricCap;

    protected IMachine machine;

    public MEStorageAccess(BlockEntity blockEntity, double power) {
        this.blockEntity = blockEntity;
        this.combinedItem = new CombinedItemPort();
        this.combinedFluid = new CombinedFluidPort();

        var voltage = getBlockVoltage(blockEntity);
        var electric = new SimpleElectricConsumer(voltage.value, power);
        this.electricCap = LazyOptional.of(() -> electric);
    }

    private void onUpdateLogistics(LogisticComponent logistics) {
        var items = new ArrayList<IItemPort>();
        var fluids = new ArrayList<IFluidPort>();
        var ports = logistics.getStoragePorts();
        for (var port : ports) {
            if (port.type() == PortType.ITEM) {
                items.add(port.asItem());
            } else if (port.type() == PortType.FLUID) {
                fluids.add(port.asFluid());
            }
        }
        combinedItem.setComposes(items);
        combinedFluid.setComposes(fluids);
    }

    protected void onConnect(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        logistics.onUpdate(() -> onUpdateLogistics(logistics));
        initAutocraftService(network, logistics);
    }

    @SuppressWarnings("unchecked")
    private void initAutocraftService(INetwork network, LogisticComponent logistics) {
        var level = blockEntity.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        var patterns = new ArrayList<org.shsts.tinactory.core.autocraft.model.CraftPattern>();
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
        var inventory = new LogisticsInventoryView(combinedItem, combinedFluid);
        var allocator = new LogisticsMachineAllocator(
            () -> logistics.getVisiblePorts(network.getSubnet(blockEntity.getBlockPos())));
        var planner = new GoalReductionPlanner(new LogisticsPatternRepository(patterns));
        var service = new AutocraftJobService(
            planner,
            () -> new SequentialCraftExecutor(inventory, allocator, new SilentJobEvents()),
            inventory::snapshotAvailable);
        logistics.setAutocraftJobService(service);
    }

    public void onUpdate(Runnable listener) {
        combinedItem.onUpdate(listener);
        combinedFluid.onUpdate(listener);
    }

    public void unregisterListener(Runnable listener) {
        combinedItem.unregisterListener(listener);
        combinedFluid.unregisterListener(listener);
    }

    public IItemPort itemPort() {
        return combinedItem;
    }

    public IFluidPort fluidPort() {
        return combinedFluid;
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

    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> machine = MACHINE.get(blockEntity));
        eventManager.subscribe(CONNECT.get(), this::onConnect);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ELECTRIC_MACHINE.get()) {
            return electricCap.cast();
        }
        return LazyOptional.empty();
    }
}
