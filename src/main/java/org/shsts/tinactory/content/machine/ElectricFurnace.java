package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.client.IRecipeBookItem;
import org.shsts.tinactory.content.gui.client.SmeltingRecipeBookItem;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.shsts.tinactory.content.network.MachineBlock.getBlockVoltage;
import static org.shsts.tinactory.core.machine.RecipeProcessors.PROGRESS_PER_TICK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricFurnace implements IRecipeProcessor<SmeltingRecipe> {
    private long voltage = 0L;
    private double workFactor;

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
        var recipes = world.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING);
        return () -> () -> recipes.stream()
            .<IRecipeBookItem>map(SmeltingRecipeBookItem::new)
            .toList();
    }

    @Override
    public boolean allowTargetRecipe(Level world, ResourceLocation loc, IMachine machine) {
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
        machine.container().ifPresent(container -> {
            if (container.hasPort(0) && container.getPort(0, true) instanceof IItemCollection itemPort) {
                itemPort.asItemFilter().setFilters(smeltingRecipe.getIngredients());
            }
        });
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
        var input = getInputWrapper(container.get());
        return world.getRecipeManager().byKey(target)
            .filter(recipe -> recipe instanceof SmeltingRecipe smeltingRecipe &&
                smeltingRecipe.matches(input, world) &&
                canOutput(smeltingRecipe, container.get()))
            .map($ -> (SmeltingRecipe) $);
    }

    private void calculateFactors(IMachine machine) {
        var baseVoltage = Voltage.ULV.value;
        voltage = getBlockVoltage(machine.blockEntity()).value;
        var voltageFactor = 1L;
        var overclock = 1L;
        while (baseVoltage * voltageFactor * 4 <= voltage) {
            overclock *= 2;
            voltageFactor *= 4;
        }
        workFactor = overclock;
    }

    @Override
    public void onWorkBegin(SmeltingRecipe recipe, IMachine machine) {
        var container = machine.container().orElseThrow();
        var ingredient = recipe.getIngredients().get(0);
        StackHelper.consumeItemCollection(getInputPort(container), ingredient, 1, false);
        calculateFactors(machine);
    }

    @Override
    public void onWorkContinue(SmeltingRecipe recipe, IMachine machine) {
        calculateFactors(machine);
    }

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
        return voltage * 0.625d;
    }
}
