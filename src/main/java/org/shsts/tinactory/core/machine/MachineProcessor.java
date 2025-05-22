package org.shsts.tinactory.core.machine;

import com.google.common.collect.ArrayListMultimap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.content.AllRecipes.MARKER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineProcessor<R extends ProcessingRecipe>
    extends RecipeProcessor<R> implements IElectricMachine {
    public final IRecipeType<? extends IRecipeBuilderBase<R>> recipeType;
    protected final Voltage voltage;

    protected double workFactor = 1d;
    protected double energyFactor = 1d;

    public MachineProcessor(BlockEntity blockEntity,
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType, boolean autoRecipe) {
        this(blockEntity, recipeType, getBlockVoltage(blockEntity), autoRecipe);
    }

    public MachineProcessor(BlockEntity blockEntity,
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType,
        Voltage voltage, boolean autoRecipe) {
        super(blockEntity, autoRecipe);
        this.recipeType = recipeType;
        this.voltage = voltage;
    }

    @Override
    protected boolean matches(Level world, R recipe, IMachine machine) {
        return recipe.matches(machine, world);
    }

    @Override
    protected List<R> getMatchedRecipes(Level world, IMachine machine) {
        return CORE.recipeManager(world).getRecipesFor(recipeType, machine, world);
    }

    @Override
    protected Optional<R> fromLoc(Level world, ResourceLocation loc) {
        return CORE.recipeManager(world).byLoc(recipeType, loc);
    }

    @Override
    protected ResourceLocation toLoc(R recipe) {
        return recipe.loc();
    }

    @Override
    public boolean allowTargetRecipe(Level world, ResourceLocation loc) {
        var manager = CORE.recipeManager(world);

        var recipe = manager.byLoc(recipeType, loc);
        if (recipe.isPresent()) {
            return true;
        }

        var marker = manager.byLoc(MARKER, loc);
        return marker.filter($ -> $.baseType == recipeType.get()).isPresent();
    }

    @Override
    protected void doSetTargetRecipe(Level world, ResourceLocation loc) {
        var manager = CORE.recipeManager(world);

        ProcessingRecipe marker;
        var recipe = manager.byLoc(recipeType, loc);
        if (recipe.isPresent()) {
            marker = recipe.get();
            targetRecipe = recipe.get();
        } else {
            targetRecipe = null;
            marker = manager.byLoc(MARKER, loc).orElse(null);
        }

        if (marker == null) {
            return;
        }
        getContainer().ifPresent(container -> {
            var itemFilters = ArrayListMultimap.<Integer, Predicate<ItemStack>>create();
            var fluidFilters = ArrayListMultimap.<Integer, Predicate<FluidStack>>create();

            for (var input : marker.inputs) {
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
                var port = container.getPort(idx, true);
                if (port.type() == PortType.ITEM) {
                    port.asItem().setItemFilter(itemFilters.get(idx));
                }
            }

            for (var idx : fluidFilters.keys().elementSet()) {
                var port = container.getPort(idx, true);
                if (port.type() == PortType.FLUID) {
                    port.asFluid().setFluidFilter(fluidFilters.get(idx));
                }
            }
        });
    }

    protected void calculateFactors(R recipe) {
        var baseVoltage = recipe.voltage == 0 ? Voltage.ULV.value : recipe.voltage;
        var voltage = getVoltage();
        var voltageFactor = 1L;
        var overclock = 1L;
        while (baseVoltage * voltageFactor * 4 <= voltage) {
            overclock *= 2;
            voltageFactor *= 4;
        }
        energyFactor = voltageFactor;
        workFactor = overclock;
    }

    @Override
    protected void onWorkBegin(R recipe, IMachine machine) {
        recipe.consumeInputs(machine.container().orElseThrow());
        calculateFactors(recipe);
    }

    @Override
    protected void onWorkContinue(R recipe) {
        calculateFactors(recipe);
    }

    @Override
    protected long onWorkProgress(R recipe, double partial) {
        return (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
    }

    @Override
    protected void onWorkDone(R recipe, IMachine machine, Random random) {
        recipe.insertOutputs(machine, random);
    }

    @Override
    protected long getMaxWorkProgress(R recipe) {
        return recipe.workTicks * PROGRESS_PER_TICK;
    }

    @Override
    public long getVoltage() {
        return voltage.value;
    }

    @Override
    public ElectricMachineType getMachineType() {
        return ElectricMachineType.CONSUMER;
    }

    @Override
    public double getPowerGen() {
        return 0;
    }

    @Override
    public double getPowerCons() {
        return currentRecipe == null ? 0d : currentRecipe.power * energyFactor;
    }

    @Nonnull
    @Override
    public <T1> LazyOptional<T1> getCapability(Capability<T1> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.ELECTRIC_MACHINE.get()) {
            return myself();
        }
        return super.getCapability(cap, side);
    }
}
