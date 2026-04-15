package org.shsts.tinactory.core.machine;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.gui.client.ProcessingRecipeBookItem;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingMachine<R extends ProcessingRecipe> implements IRecipeProcessor<R> {
    public static final long PROGRESS_PER_TICK = 256;

    protected final IRecipeType<? extends IRecipeBuilderBase<R>> recipeType;
    protected final IRecipeManager recipeManager;
    protected final IRecipeType<MarkerRecipe.Builder> markerType;

    protected int parallel = 1;
    protected double workFactor = 1d;
    protected double energyFactor = 1d;
    @Nullable
    private ResourceLocation filterRecipeLoc = null;
    @Nullable
    private ProcessingRecipe filterRecipe = null;

    public ProcessingMachine(IRecipeType<? extends IRecipeBuilderBase<R>> recipeType,
        IRecipeManager recipeManager, IRecipeType<MarkerRecipe.Builder> markerType) {
        this.recipeType = recipeType;
        this.recipeManager = recipeManager;
        this.markerType = markerType;
    }

    @Override
    public ResourceLocation recipeTypeId() {
        return recipeType.loc();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<R> baseClass() {
        return (Class<R>) recipeType.recipeClass();
    }

    @Override
    public Optional<R> byLoc(ResourceLocation loc) {
        return recipeManager.byLoc(recipeType, loc);
    }

    @Override
    public ResourceLocation toLoc(R recipe) {
        return recipe.loc();
    }

    protected Stream<MarkerRecipe> markers(IMachine machine) {
        return recipeManager.getAllRecipesFor(markerType).stream()
            .filter($ -> $.matchesType(recipeType) && $.canCraft(machine));
    }

    protected List<ProcessingRecipe> targetRecipes(IMachine machine) {
        var ret = new ArrayList<ProcessingRecipe>();
        markers(machine).forEach(ret::add);
        recipeManager.getAllRecipesFor(recipeType).stream()
            .filter($ -> $.canCraft(machine))
            .forEach(ret::add);
        return ret;
    }

    @Override
    public DistLazy<List<IRecipeBookItem>> recipeBookItems(IMachine machine) {
        var loc = targetRecipes(machine);
        var comparator = Comparator.<ProcessingRecipe>comparingLong($ -> $.voltage)
            .thenComparing(ProcessingRecipe::loc, ResourceLocation::compareNamespaced);
        loc.sort(comparator);

        return () -> () -> loc.stream()
            .<IRecipeBookItem>map(ProcessingRecipeBookItem::new)
            .toList();
    }

    @Override
    public boolean allowTargetRecipe(boolean isClientSide, ResourceLocation loc, IMachine machine) {
        var marker = recipeManager.byLoc(markerType, loc);
        if (marker.isPresent()) {
            var recipe = marker.get();
            return recipe.matchesType(recipeType) && recipe.canCraft(machine);
        }

        var processing = recipeManager.byLoc(recipeType, loc);
        if (processing.isPresent()) {
            var recipe = processing.get();
            return isClientSide || recipe.canCraft(machine);
        }

        return false;
    }

    private void setFilters(IContainer container, PortDirection direction,
        ListMultimap<Integer, Predicate<?>> filters) {
        for (var i = 0; i < container.portSize(); i++) {
            if (!container.hasPort(i)) {
                continue;
            }
            var dir = container.portDirection(i);
            if (dir != direction) {
                continue;
            }
            var port = container.getPort(i, ContainerAccess.INTERNAL);
            port.asFilter().setRawFilters(filters.get(i));
        }
    }

    private void addFiltersFromInput(List<ProcessingRecipe.Input> inputs,
        ListMultimap<Integer, Predicate<?>> filters) {
        for (var input : inputs) {
            var idx = input.port();
            var ingredient = input.ingredient();
            filters.put(idx, ingredient.filter());
        }
    }

    private void addFiltersFromOutput(List<ProcessingRecipe.Output> outputs,
        ListMultimap<Integer, Predicate<?>> filters) {
        for (var output : outputs) {
            var idx = output.port();
            var result = output.result();
            filters.put(idx, result.filter());
        }
    }

    private void setInputFilters(ProcessingRecipe recipe, IContainer container) {
        // if marker does not have inputs, don't set input filter
        if (recipe instanceof MarkerRecipe && recipe.inputs.isEmpty()) {
            return;
        }

        var filters = ArrayListMultimap.<Integer, Predicate<?>>create();

        addFiltersFromInput(recipe.inputs, filters);
        setFilters(container, PortDirection.INPUT, filters);
    }

    private void setOutputFilters(ProcessingRecipe recipe, IContainer container) {
        var filters = ArrayListMultimap.<Integer, Predicate<?>>create();

        if (recipe instanceof MarkerRecipe marker) {
            // if marker does not have markerOutputs, don't set output filter
            if (marker.markerOutputs.isEmpty()) {
                return;
            }
            addFiltersFromInput(marker.markerOutputs, filters);
        } else {
            addFiltersFromOutput(recipe.outputs, filters);
        }
        setFilters(container, PortDirection.OUTPUT, filters);
    }

    private Optional<ProcessingRecipe> getTargetRecipe(ResourceLocation loc) {
        return recipeManager.byLoc(markerType, loc)
            .map($ -> (ProcessingRecipe) $)
            .or(() -> recipeManager.byLoc(recipeType, loc));
    }

    @Override
    public void setTargetRecipe(ResourceLocation loc, IMachine machine) {
        var recipe = getTargetRecipe(loc);
        if (recipe.isEmpty()) {
            return;
        }
        machine.container().ifPresent(container -> setInputFilters(recipe.get(), container));
    }

    protected void setFilterRecipe(IMachine machine, @Nullable ProcessingRecipe recipe) {
        filterRecipe = recipe;
        if (recipe != null) {
            machine.container().ifPresent(container -> setOutputFilters(filterRecipe, container));
        }
    }

    @Override
    public Optional<R> newRecipe(IMachine machine) {
        setFilterRecipe(machine, null);
        return recipeManager.getRecipeFor(recipeType, machine);
    }

    @Override
    public Optional<R> newRecipe(IMachine machine, ResourceLocation target) {
        var processing = recipeManager.byLoc(recipeType, target);
        if (processing.isPresent()) {
            setFilterRecipe(machine, processing.get());
            return processing.filter($ -> $.matches(machine));
        }

        var marker = recipeManager.byLoc(markerType, target);
        if (marker.isPresent()) {
            var recipe = marker.get();
            setFilterRecipe(machine, recipe);
            if (recipe.matchesType(recipeType) && recipe.canCraft(machine)) {
                return recipeManager.getAllRecipesFor(recipeType).stream()
                    .filter($ -> $.matches(machine))
                    .filter(marker.get()::matches)
                    .findAny();
            }
        }

        return Optional.empty();
    }

    protected int calculateParallel(R recipe, IMachine machine, int maxParallel) {
        var l = 1;
        var r = maxParallel + 1;
        while (r - l > 1) {
            var m = (l + r) / 2;
            if (recipe.matches(machine, m)) {
                l = m;
            } else {
                r = m;
            }
        }
        return l;
    }

    protected void addOutputInfo(R recipe, int parallel, Consumer<ProcessingInfo> info) {
        for (var output : recipe.outputs) {
            info.accept(new ProcessingInfo(output.port(), output.result().scaledPreview(parallel)));
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
    public void onWorkBegin(R recipe, IMachine machine, int maxParallel, Consumer<ProcessingInfo> callback) {
        parallel = calculateParallel(recipe, machine, maxParallel);
        recipe.consumeInputs(machine.container().orElseThrow(), parallel, callback);
        addOutputInfo(recipe, parallel, callback);
        calculateFactors(recipe, machine, parallel);
        filterRecipeLoc = filterRecipe == null ? null : filterRecipe.loc();
    }

    @Override
    public void onWorkContinue(R recipe, IMachine machine) {
        if (filterRecipeLoc != null) {
            filterRecipe = getTargetRecipe(filterRecipeLoc).orElse(null);
            if (filterRecipe == null) {
                filterRecipeLoc = null;
            }
        } else {
            filterRecipe = null;
        }
    }

    @Override
    public long onWorkProgress(R recipe, double partial) {
        return (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
    }

    @Override
    public void onWorkDone(R recipe, IMachine machine, Random random, Consumer<IProcessingResult> callback) {
        machine.container().ifPresent(container -> {
            if (filterRecipe != null) {
                setOutputFilters(filterRecipe, container);
            }
        });
        recipe.insertOutputs(machine, parallel, random, callback);
        filterRecipe = null;
        filterRecipeLoc = null;
    }

    @Override
    public long maxWorkProgress(R recipe) {
        return recipe.workTicks * PROGRESS_PER_TICK;
    }

    @Override
    public double workSpeed(double partial) {
        return partial;
    }

    @Override
    public long workTicksFromProgress(long progress) {
        return (long) Math.floor((double) progress / (double) PROGRESS_PER_TICK / workFactor);
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
        if (filterRecipeLoc != null) {
            tag.putString("filterRecipe", filterRecipeLoc.toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        parallel = tag.getInt("parallel");
        workFactor = tag.getDouble("workFactor");
        energyFactor = tag.getDouble("energyFactor");
        if (tag.contains("filterRecipe", Tag.TAG_STRING)) {
            filterRecipeLoc = new ResourceLocation(tag.getString("filterRecipe"));
        } else {
            filterRecipeLoc = null;
        }
    }
}
