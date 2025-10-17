package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.client.ProcessingRecipeBookItem;
import org.shsts.tinactory.content.gui.client.SmeltingRecipeBookItem;
import org.shsts.tinactory.content.multiblock.CoilMultiblock;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.ProcessingInfo;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllRecipes.MARKER;
import static org.shsts.tinactory.core.machine.ProcessingMachine.PROGRESS_PER_TICK;
import static org.shsts.tinactory.core.machine.ProcessingMachine.machineVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricFurnace implements IRecipeProcessor<SmeltingRecipe> {
    private static final Voltage BASE_VOLTAGE = Voltage.ULV;

    private final int inputPort;
    private final int outputPort;
    private final double basePower;
    private final int baseTemperature;
    private int parallel = 1;
    private double workFactor = 1d;
    private double energyFactor = 1d;

    public ElectricFurnace(int inputPort, int outputPort, double amperage, int baseTemperature) {
        this.inputPort = inputPort;
        this.outputPort = outputPort;
        this.basePower = BASE_VOLTAGE.value * amperage;
        this.baseTemperature = baseTemperature;
    }

    private IItemCollection getInputPort(IContainer container) {
        return container.getPort(inputPort, true).asItem();
    }

    private IItemCollection getOutputPort(IContainer container) {
        return container.getPort(outputPort, true).asItem();
    }

    private RecipeWrapper getInputWrapper(IContainer container) {
        return new RecipeWrapper((IItemHandlerModifiable)
            ((ItemHandlerCollection) getInputPort(container)).itemHandler);
    }

    private boolean canOutput(SmeltingRecipe recipe, IContainer container) {
        var result = recipe.assemble(getInputWrapper(container));
        var outputPort = getOutputPort(container);
        return outputPort.insertItem(result, true).isEmpty();
    }

    @Override
    public Class<SmeltingRecipe> baseClass() {
        return SmeltingRecipe.class;
    }

    @Override
    public Optional<SmeltingRecipe> byLoc(Level world, ResourceLocation loc) {
        return world.getRecipeManager().byKey(loc)
            .flatMap(r -> r instanceof SmeltingRecipe smelting ?
                Optional.of(smelting) : Optional.empty());
    }

    @Override
    public ResourceLocation toLoc(SmeltingRecipe recipe) {
        return recipe.getId();
    }

    @Override
    public DistLazy<List<IRecipeBookItem>> recipeBookItems(Level world, IMachine machine) {
        var recipeManager = CORE.recipeManager(world);
        var markers = recipeManager.getAllRecipesFor(MARKER).stream()
            .filter($ -> $.matchesType(RecipeType.SMELTING) && $.canCraft(machine))
            .toList();
        var recipes = world.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING);

        return () -> () -> {
            var ret = new ArrayList<IRecipeBookItem>();
            for (var marker : markers) {
                ret.add(new ProcessingRecipeBookItem(marker));
            }
            for (var recipe : recipes) {
                ret.add(new SmeltingRecipeBookItem(recipe, inputPort, outputPort));
            }
            return ret;
        };
    }

    @Override
    public boolean allowTargetRecipe(Level world, ResourceLocation loc, IMachine machine) {
        var marker = CORE.recipeManager(world).byLoc(MARKER, loc);
        if (marker.isPresent()) {
            var recipe = marker.get();
            return recipe.matchesType(RecipeType.SMELTING) && recipe.canCraft(machine);
        }

        return world.getRecipeManager().byKey(loc)
            .filter(r -> r.getType() == RecipeType.SMELTING)
            .isPresent();
    }

    @Override
    public void setTargetRecipe(Level world, ResourceLocation loc, IMachine machine) {
        var recipe = world.getRecipeManager().byKey(loc);
        if (recipe.isEmpty() || !(recipe.get() instanceof SmeltingRecipe smeltingRecipe)) {
            return;
        }
        machine.container().ifPresent(container -> getInputPort(container)
            .asItemFilter().setFilters(smeltingRecipe.getIngredients()));
    }

    @Override
    public Optional<SmeltingRecipe> newRecipe(Level world, IMachine machine) {
        return machine.container().flatMap(container -> world.getRecipeManager()
            .getRecipeFor(RecipeType.SMELTING, getInputWrapper(container), world)
            .filter(recipe -> canOutput(recipe, container)));
    }

    @Override
    public Optional<SmeltingRecipe> newRecipe(Level world, IMachine machine, ResourceLocation target) {
        var container = machine.container();
        if (container.isEmpty()) {
            return Optional.empty();
        }
        var container1 = container.get();
        var input = getInputWrapper(container1);
        var recipeManager = world.getRecipeManager();

        var vanilla = recipeManager.byKey(target);
        if (vanilla.isPresent() && vanilla.get() instanceof SmeltingRecipe smelting) {
            return smelting.matches(input, world) && canOutput(smelting, container1) ?
                Optional.of(smelting) : Optional.empty();
        }

        var marker = CORE.recipeManager(world).byLoc(MARKER, target);
        if (marker.isPresent()) {
            var recipe = marker.get();
            if (recipe.matchesType(RecipeType.SMELTING) && recipe.canCraft(machine)) {
                return recipeManager.getRecipesFor(RecipeType.SMELTING, input, world)
                    .stream().filter($ -> recipe.matches($::getId) && canOutput($, container1))
                    .findAny();
            }
        }

        return Optional.empty();
    }

    private int getTemperature(IMachine machine) {
        return CoilMultiblock.getTemperature(machine).orElse(0);
    }

    private int calculateParallel(IItemCollection port, Ingredient ingredient, int maxParallel) {
        var l = 1;
        var r = maxParallel + 1;
        while (r - l > 1) {
            var m = (l + r) / 2;
            if (StackHelper.consumeItemCollection(port, ingredient, m, true).isPresent()) {
                l = m;
            } else {
                r = m;
            }
        }
        return l;
    }

    private void calculateFactors(IMachine machine, int parallel) {
        var baseVoltage = parallel * BASE_VOLTAGE.value;
        var voltage = machineVoltage(machine);
        var voltageFactor = 1L;
        var overclock = 1L;
        while (baseVoltage * voltageFactor * 4 <= voltage) {
            overclock *= 2;
            voltageFactor *= 4;
        }
        workFactor = overclock;
        energyFactor = parallel * voltageFactor;

        var temp = getTemperature(machine);
        if (temp > 0 && baseTemperature > 0) {
            var factor = Math.max(1d, (temp - baseTemperature) / CONFIG.blastFurnaceTempFactor.get());
            energyFactor /= factor;
        }
    }

    @Override
    public void onWorkBegin(SmeltingRecipe recipe, IMachine machine,
        int maxParallel, Consumer<ProcessingInfo> info) {
        var port = getInputPort(machine.container().orElseThrow());
        var ingredient = recipe.getIngredients().get(0);

        parallel = calculateParallel(port, ingredient, maxParallel);
        StackHelper.consumeItemCollection(port, ingredient, parallel, false)
            .ifPresent($ -> info.accept(new ProcessingInfo(inputPort, new ProcessingIngredients.ItemIngredient($))));

        var result = recipe.getResultItem();
        var result1 = StackHelper.copyWithCount(result, parallel * result.getCount());
        info.accept(new ProcessingInfo(outputPort, new ProcessingResults.ItemResult(1, result1)));

        calculateFactors(machine, parallel);
    }

    @Override
    public void onWorkContinue(SmeltingRecipe recipe, IMachine machine) {}

    @Override
    public long onWorkProgress(SmeltingRecipe recipe, double partial) {
        return (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
    }

    @Override
    public void onWorkDone(SmeltingRecipe recipe, IMachine machine, Random random) {
        var container = machine.container().orElseThrow();
        getOutputPort(container).insertItem(recipe.assemble(getInputWrapper(container)), false);
    }

    @Override
    public long getMaxWorkProgress(SmeltingRecipe recipe) {
        return recipe.getCookingTime() * PROGRESS_PER_TICK;
    }

    @Override
    public ElectricMachineType electricMachineType(SmeltingRecipe recipe) {
        return ElectricMachineType.CONSUMER;
    }

    @Override
    public double powerGen(SmeltingRecipe recipe) {
        return 0;
    }

    @Override
    public double powerCons(SmeltingRecipe recipe) {
        return basePower * energyFactor;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putInt("parallel", parallel);
        tag.putDouble("workFactor", workFactor);
        tag.putDouble("energyFactor", energyFactor);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        parallel = tag.getInt("parallel");
        workFactor = tag.getDouble("workFactor");
        energyFactor = tag.getDouble("energyFactor");
    }
}
