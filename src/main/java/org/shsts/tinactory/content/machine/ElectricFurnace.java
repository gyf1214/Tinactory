package org.shsts.tinactory.content.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.machine.RecipeProcessor;

import java.util.Random;
import java.util.stream.Stream;

import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricFurnace extends RecipeProcessor<SmeltingRecipe> implements IElectricMachine {
    private final Voltage voltage;
    private double workFactor;

    public ElectricFurnace(BlockEntity blockEntity, Voltage voltage) {
        super(blockEntity, RecipeType.SMELTING, true);
        this.voltage = voltage;
    }

    private IItemCollection getInputPort(IContainer container) {
        return container.getPort(0, true).asItem();
    }

    private IItemCollection getOutputPort(IContainer container) {
        return container.getPort(1, true).asItem();
    }

    private RecipeWrapper getInputWrapper(IContainer container) {
        return new RecipeWrapper((IItemHandlerModifiable)
            ((ItemHandlerCollection) getInputPort(container)).itemHandler);
    }

    private boolean canOutput(SmeltingRecipe recipe, IContainer container) {
        var result = recipe.assemble(getInputWrapper(container));
        var outputPort = getOutputPort(container);
        return outputPort.acceptInput(result) && outputPort.insertItem(result, true).isEmpty();
    }

    @Override
    protected boolean matches(Level world, SmeltingRecipe recipe, IContainer container) {
        return recipe.matches(getInputWrapper(container), world) &&
            canOutput(recipe, container);
    }

    @Override
    protected Stream<? extends SmeltingRecipe> getMatchedRecipes(Level world, IContainer container) {
        return world.getRecipeManager().getRecipeFor(recipeType, getInputWrapper(container), world)
            .filter(recipe -> canOutput(recipe, container))
            .stream();
    }

    @Override
    public boolean allowTargetRecipe(Recipe<?> recipe) {
        return recipe.getType() == RecipeType.SMELTING;
    }

    @Override
    protected void doSetTargetRecipe(Recipe<?> recipe) {
        targetRecipe = (SmeltingRecipe) recipe;
        getContainer().ifPresent(container -> {
            if (container.hasPort(0) && container.getPort(0, false) instanceof IItemCollection itemPort) {
                itemPort.setItemFilter(targetRecipe.getIngredients());
            }
        });
    }

    private void calculateFactors() {
        var baseVoltage = Voltage.ULV.value;
        var voltage = getVoltage();
        var voltageFactor = 1L;
        var overclock = 1L;
        while (baseVoltage * voltageFactor * 4 <= voltage) {
            overclock *= 2;
            voltageFactor *= 4;
        }
        workFactor = overclock;
    }

    @Override
    protected void onWorkBegin(SmeltingRecipe recipe, IContainer container) {
        var ingredient = recipe.getIngredients().get(0);
        StackHelper.consumeItemCollection(getInputPort(container), ingredient, 1, false);
        calculateFactors();
    }

    @Override
    protected void onWorkContinue(SmeltingRecipe recipe) {
        calculateFactors();
    }

    @Override
    protected long onWorkProgress(SmeltingRecipe recipe, double partial) {
        return (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
    }

    @Override
    protected void onWorkDone(SmeltingRecipe recipe, IContainer container, Random random) {
        getOutputPort(container).insertItem(recipe.assemble(getInputWrapper(container)), false);
    }

    @Override
    protected long getMaxWorkProgress(SmeltingRecipe recipe) {
        return recipe.getCookingTime() * PROGRESS_PER_TICK;
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
        return currentRecipe == null ? 0d : getVoltage() * 0.625d;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ELECTRIC_MACHINE.get()) {
            return myself();
        }
        return super.getCapability(cap, side);
    }
}
