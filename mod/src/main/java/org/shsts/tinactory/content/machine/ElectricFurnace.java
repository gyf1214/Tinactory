package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.gui.client.SmeltingRecipeBookItem;
import org.shsts.tinactory.content.multiblock.CoilMultiblock;
import org.shsts.tinactory.core.autocraft.integration.SmeltingRecipePatternSource;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.gui.client.ProcessingRecipeBookItem;
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

import static org.shsts.tinactory.AllRecipes.MARKER;
import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.core.machine.MachineProcessor.VOID_DEFAULT;
import static org.shsts.tinactory.core.machine.MachineProcessor.VOID_KEY;
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

    private IPort<ItemStack> getInputPort(IContainer container) {
        return container.getPort(inputPort, ContainerAccess.INTERNAL).asItem();
    }

    private IPort<ItemStack> getOutputPort(IContainer container) {
        return container.getPort(outputPort, ContainerAccess.INTERNAL).asItem();
    }

    /**
     * TODO: need to use {@link SmeltingRecipe#assemble} to cope with subclass.
     */
    private ItemStack getResult(SmeltingRecipe recipe) {
        return recipe.getResultItem();
    }

    private boolean matchesInput(SmeltingRecipe recipe, IPort<ItemStack> port) {
        var ingredient = recipe.getIngredients().get(0);
        return StackHelper.consumeItemPort(port, ingredient, 1, true).isPresent();
    }

    private boolean matchesOutput(SmeltingRecipe recipe, IMachine machine, IPort<ItemStack> port) {
        if (machine.config().getBoolean(VOID_KEY, VOID_DEFAULT)) {
            return true;
        }
        var result = getResult(recipe);
        return port.insert(result, true).isEmpty();
    }

    private boolean matches(SmeltingRecipe recipe, IMachine machine, IContainer container) {
        return matchesInput(recipe, getInputPort(container)) &&
            matchesOutput(recipe, machine, getOutputPort(container));
    }

    @Override
    public Class<SmeltingRecipe> baseClass() {
        return SmeltingRecipe.class;
    }

    @Override
    public ResourceLocation recipeTypeId() {
        return SmeltingRecipePatternSource.SMELTING_RECIPE_TYPE_ID;
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
            .setFilters(smeltingRecipe.getIngredients()));
    }

    /**
     * Unfortunately we cannot use {@link net.minecraft.world.item.crafting.RecipeManager#getRecipeFor}.
     */
    @Override
    public Optional<SmeltingRecipe> newRecipe(Level world, IMachine machine) {
        return machine.container().flatMap(container -> world.getRecipeManager()
            .getAllRecipesFor(RecipeType.SMELTING).stream()
            .filter($ -> matches($, machine, container))
            .findFirst());
    }

    @Override
    public Optional<SmeltingRecipe> newRecipe(Level world, IMachine machine, ResourceLocation target) {
        var container = machine.container();
        if (container.isEmpty()) {
            return Optional.empty();
        }
        var container1 = container.get();
        var recipeManager = world.getRecipeManager();

        var vanilla = recipeManager.byKey(target);
        if (vanilla.isPresent() && vanilla.get() instanceof SmeltingRecipe smelting) {
            return matches(smelting, machine, container1) ? Optional.of(smelting) : Optional.empty();
        }

        var marker = CORE.recipeManager(world).byLoc(MARKER, target);
        if (marker.isPresent()) {
            var recipe = marker.get();
            if (recipe.matchesType(RecipeType.SMELTING) && recipe.canCraft(machine)) {
                return recipeManager.getAllRecipesFor(RecipeType.SMELTING).stream()
                    .filter($ -> matches($, machine, container1))
                    .findFirst();
            }
        }

        return Optional.empty();
    }

    private int getTemperature(IMachine machine) {
        return CoilMultiblock.getTemperature(machine).orElse(0);
    }

    private int calculateParallel(IPort<ItemStack> port, Ingredient ingredient, int maxParallel) {
        var l = 1;
        var r = maxParallel + 1;
        while (r - l > 1) {
            var m = (l + r) / 2;
            if (StackHelper.consumeItemPort(port, ingredient, m, true).isPresent()) {
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
            var factor = Math.max(1d, (temp - baseTemperature) / CONFIG.coilTemperatureFactor.get());
            energyFactor /= factor;
        }
    }

    @Override
    public void onWorkBegin(SmeltingRecipe recipe, IMachine machine,
        int maxParallel, Consumer<ProcessingInfo> callback) {
        var port = getInputPort(machine.container().orElseThrow());
        var ingredient = recipe.getIngredients().get(0);

        parallel = calculateParallel(port, ingredient, maxParallel);
        StackHelper.consumeItemPort(port, ingredient, parallel, false)
            .ifPresent($ -> callback.accept(new ProcessingInfo(inputPort,
                new ProcessingIngredients.ItemIngredient($))));

        var result = getResult(recipe);
        var result1 = StackHelper.copyWithCount(result, parallel * result.getCount());
        callback.accept(new ProcessingInfo(outputPort,
            new ProcessingResults.ItemResult(1d, result1)));

        calculateFactors(machine, parallel);
    }

    @Override
    public void onWorkContinue(SmeltingRecipe recipe, IMachine machine) {}

    @Override
    public long onWorkProgress(SmeltingRecipe recipe, double partial) {
        return (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
    }

    @Override
    public void onWorkDone(SmeltingRecipe recipe, IMachine machine, Random random,
        Consumer<IProcessingResult> callback) {
        var port = getOutputPort(machine.container().orElseThrow());
        var result = getResult(recipe);
        var result1 = StackHelper.copyWithCount(result, parallel * result.getCount());
        callback.accept(new ProcessingResults.ItemResult(1d, result1));
        port.insert(result1, false);
    }

    @Override
    public long maxWorkProgress(SmeltingRecipe recipe) {
        return recipe.getCookingTime() * PROGRESS_PER_TICK;
    }

    @Override
    public long workTicksFromProgress(long progress) {
        return (long) Math.floor((double) progress / (double) PROGRESS_PER_TICK / workFactor);
    }

    @Override
    public double workSpeed(double partial) {
        return partial;
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
