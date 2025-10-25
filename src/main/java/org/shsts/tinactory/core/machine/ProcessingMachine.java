package org.shsts.tinactory.core.machine;

import com.google.common.collect.ArrayListMultimap;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.client.ProcessingRecipeBookItem;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinycorelib.api.core.DistLazy;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.content.AllRecipes.MARKER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingMachine<R extends ProcessingRecipe> implements IRecipeProcessor<R> {
    public static final long PROGRESS_PER_TICK = 256;

    protected final IRecipeType<? extends IRecipeBuilderBase<R>> recipeType;

    protected int parallel = 1;
    protected double workFactor = 1d;
    protected double energyFactor = 1d;

    public ProcessingMachine(IRecipeType<? extends IRecipeBuilderBase<R>> recipeType) {
        this.recipeType = recipeType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<R> baseClass() {
        return (Class<R>) recipeType.recipeClass();
    }

    @Override
    public Optional<R> byLoc(Level world, ResourceLocation loc) {
        return CORE.recipeManager(world).byLoc(recipeType, loc);
    }

    @Override
    public ResourceLocation toLoc(R recipe) {
        return recipe.loc();
    }

    protected Stream<MarkerRecipe> markers(IRecipeManager recipeManager, IMachine machine) {
        return recipeManager.getAllRecipesFor(MARKER).stream()
            .filter($ -> $.matchesType(recipeType) && $.canCraft(machine));
    }

    protected List<ProcessingRecipe> targetRecipes(Level world, IMachine machine) {
        var recipeManager = CORE.recipeManager(world);
        var ret = new ArrayList<ProcessingRecipe>();
        markers(recipeManager, machine).forEach(ret::add);
        recipeManager.getAllRecipesFor(recipeType).stream()
            .filter($ -> $.canCraft(machine))
            .forEach(ret::add);
        return ret;
    }

    @Override
    public DistLazy<List<IRecipeBookItem>> recipeBookItems(Level world, IMachine machine) {
        var loc = targetRecipes(world, machine);
        var comparator = Comparator.<ProcessingRecipe>comparingLong($ -> $.voltage)
            .thenComparing(ProcessingRecipe::loc, ResourceLocation::compareNamespaced);
        loc.sort(comparator);

        return () -> () -> loc.stream()
            .<IRecipeBookItem>map(ProcessingRecipeBookItem::new)
            .toList();
    }

    @Override
    public boolean allowTargetRecipe(Level world, ResourceLocation loc, IMachine machine) {
        var recipeManager = CORE.recipeManager(world);

        var marker = recipeManager.byLoc(MARKER, loc);
        if (marker.isPresent()) {
            var recipe = marker.get();
            return recipe.matchesType(recipeType) && recipe.canCraft(machine);
        }

        var processing = recipeManager.byLoc(recipeType, loc);
        if (processing.isPresent()) {
            var recipe = processing.get();
            return recipe.canCraft(machine);
        }

        return false;
    }

    @Override
    public void setTargetRecipe(Level world, ResourceLocation loc, IMachine machine) {
        var recipeManager = CORE.recipeManager(world);
        var recipe = recipeManager.byLoc(MARKER, loc)
            .map($ -> (ProcessingRecipe) $)
            .or(() -> recipeManager.byLoc(recipeType, loc));
        if (recipe.isEmpty()) {
            return;
        }
        var recipe1 = recipe.get();
        // skip filter if targetRecipe is a marker without any input
        if (recipe1 instanceof MarkerRecipe && recipe1.inputs.isEmpty()) {
            return;
        }

        machine.container().ifPresent(container -> {
            var itemFilters = ArrayListMultimap.<Integer, Predicate<ItemStack>>create();
            var fluidFilters = ArrayListMultimap.<Integer, Predicate<FluidStack>>create();

            for (var input : recipe1.inputs) {
                var idx = input.port();
                var ingredient = input.ingredient();
                if (!container.hasPort(idx)) {
                    continue;
                }
                if (ingredient instanceof ProcessingIngredients.ItemsIngredientBase item) {
                    itemFilters.put(idx, item.ingredient);
                } else if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
                    var stack1 = item.stack();
                    itemFilters.put(idx, stack -> StackHelper.canItemsStack(stack, stack1));
                } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
                    var stack1 = fluid.fluid();
                    fluidFilters.put(idx, stack -> stack.isFluidEqual(stack1));
                }
            }

            for (var idx : itemFilters.keys().elementSet()) {
                var port = container.getPort(idx, ContainerAccess.INTERNAL);
                if (port.type() == PortType.ITEM) {
                    port.asItemFilter().setFilters(itemFilters.get(idx));
                }
            }

            for (var idx : fluidFilters.keys().elementSet()) {
                var port = container.getPort(idx, ContainerAccess.INTERNAL);
                if (port.type() == PortType.FLUID) {
                    port.asFluidFilter().setFilters(fluidFilters.get(idx));
                }
            }
        });
    }

    @Override
    public Optional<R> newRecipe(Level world, IMachine machine) {
        var recipeManager = CORE.recipeManager(world);
        return recipeManager.getRecipeFor(recipeType, machine, world);
    }

    @Override
    public Optional<R> newRecipe(Level world, IMachine machine, ResourceLocation target) {
        var recipeManager = CORE.recipeManager(world);

        var processing = recipeManager.byLoc(recipeType, target);
        if (processing.isPresent()) {
            return processing.filter($ -> $.matches(machine, world));
        }

        var marker = recipeManager.byLoc(MARKER, target);
        if (marker.isPresent()) {
            var recipe = marker.get();
            if (recipe.matchesType(recipeType) && recipe.canCraft(machine)) {
                return recipeManager.getRecipesFor(recipeType, machine, world)
                    .stream().filter(marker.get()::matches)
                    .findAny();
            }
        }

        return Optional.empty();
    }

    protected int calculateParallel(R recipe, Level world, IMachine machine, int maxParallel) {
        var l = 1;
        var r = maxParallel + 1;
        while (r - l > 1) {
            var m = (l + r) / 2;
            if (recipe.matches(machine, world, m)) {
                l = m;
            } else {
                r = m;
            }
        }
        return l;
    }

    protected void addOutputInfo(R recipe, int parallel, Consumer<ProcessingInfo> info) {
        for (var output : recipe.outputs) {
            var result = output.result();
            if (result instanceof ProcessingResults.ItemResult item) {
                var stack1 = StackHelper.copyWithCount(item.stack, parallel * item.stack.getCount());
                info.accept(new ProcessingInfo(output.port(), new ProcessingResults.ItemResult(1, stack1)));
            } else if (result instanceof ProcessingResults.FluidResult fluid) {
                var stack1 = StackHelper.copyWithAmount(fluid.stack, parallel * fluid.stack.getAmount());
                info.accept(new ProcessingInfo(output.port(), new ProcessingResults.FluidResult(1, stack1)));
            }
        }
    }

    public static long machineVoltage(IMachine machine) {
        return machine.electric().map(IElectricMachine::getVoltage).orElse(0L);
    }

    protected void calculateFactors(R recipe, IMachine machine, int parallel) {
        // parallel will limit overclock
        var baseVoltage = parallel * Math.max(recipe.voltage, Voltage.ULV.value);
        var voltage = machineVoltage(machine);
        var voltageFactor = 1L;
        var overclock = 1L;
        while (baseVoltage * voltageFactor * 4 <= voltage) {
            overclock *= 2;
            voltageFactor *= 4;
        }
        workFactor = overclock;
        energyFactor = parallel * voltageFactor;
    }

    @Override
    public void onWorkBegin(R recipe, IMachine machine, int maxParallel, Consumer<ProcessingInfo> info) {
        parallel = calculateParallel(recipe, machine.world(), machine, maxParallel);
        recipe.consumeInputs(machine.container().orElseThrow(), parallel, info);
        addOutputInfo(recipe, parallel, info);
        calculateFactors(recipe, machine, parallel);
    }

    @Override
    public void onWorkContinue(R recipe, IMachine machine) {}

    @Override
    public long onWorkProgress(R recipe, double partial) {
        return (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
    }

    @Override
    public void onWorkDone(R recipe, IMachine machine, Random random) {
        recipe.insertOutputs(machine, parallel, random);
    }

    @Override
    public long getMaxWorkProgress(R recipe) {
        return recipe.workTicks * PROGRESS_PER_TICK;
    }

    @Override
    public ElectricMachineType electricMachineType(R recipe) {
        return ElectricMachineType.CONSUMER;
    }

    @Override
    public double powerGen(R recipe) {
        return 0;
    }

    @Override
    public double powerCons(R recipe) {
        return energyFactor * recipe.power;
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
