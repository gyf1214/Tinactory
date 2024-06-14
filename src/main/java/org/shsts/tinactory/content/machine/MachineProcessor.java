package org.shsts.tinactory.content.machine;

import com.google.common.collect.ArrayListMultimap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static org.shsts.tinactory.content.AllRecipes.MARKER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineProcessor<T extends ProcessingRecipe>
        extends RecipeProcessor<T> implements IElectricMachine {

    protected final Voltage voltage;

    protected double workFactor = 1d;
    protected double energyFactor = 1d;

    public MachineProcessor(BlockEntity blockEntity, RecipeType<? extends T> recipeType, Voltage voltage) {
        super(blockEntity, recipeType);
        this.voltage = voltage;
    }

    @Override
    protected boolean matches(Level world, T recipe, IContainer container) {
        return recipe.matches(container, world) && recipe.canCraftInVoltage(getVoltage());
    }

    @Override
    protected List<? extends T> getMatchedRecipes(Level world, IContainer container) {
        return SmartRecipe.getRecipesFor(recipeType, container, world)
                .stream().filter(r -> r.canCraftInVoltage(getVoltage()))
                .toList();
    }

    @Override
    protected boolean allowTargetRecipe(Recipe<?> recipe) {
        var type = recipe.getType();
        if (type == MARKER.get()) {
            return ((MarkerRecipe) recipe).baseType == recipeType;
        }
        return type == recipeType;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doSetTargetRecipe(Recipe<?> recipe) {
        var recipe1 = (ProcessingRecipe) recipe;

        if (recipe.getType() == MARKER.get()) {
            targetRecipe = null;
        } else {
            targetRecipe = (T) recipe1;
        }

        getContainer().ifPresent(container -> {
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
                    itemFilters.put(idx, stack -> ItemHelper.canItemsStack(stack, stack1));
                } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
                    var stack1 = fluid.fluid();
                    fluidFilters.put(idx, stack -> stack.isFluidEqual(stack1));
                }
            }

            for (var idx : itemFilters.keys().elementSet()) {
                var port = container.getPort(idx, false);
                if (port.type() == PortType.ITEM) {
                    port.asItem().setItemFilter(itemFilters.get(idx));
                }
            }

            for (var idx : fluidFilters.keys().elementSet()) {
                var port = container.getPort(idx, false);
                if (port.type() == PortType.FLUID) {
                    port.asFluid().setFluidFilter(fluidFilters.get(idx));
                }
            }
        });
    }

    protected void calculateFactors(ProcessingRecipe recipe) {
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
    protected void onWorkBegin(T recipe, IContainer container) {
        recipe.consumeInputs(container);
        calculateFactors(recipe);
    }

    @Override
    protected void onWorkContinue(T recipe) {
        calculateFactors(recipe);
    }

    @Override
    protected long onWorkProgress(T recipe, double partial) {
        return (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
    }

    @Override
    protected void onWorkDone(T recipe, IContainer container, Random random) {
        recipe.insertOutputs(container, random);
    }

    @Override
    protected long getMaxWorkProgress(T recipe) {
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
