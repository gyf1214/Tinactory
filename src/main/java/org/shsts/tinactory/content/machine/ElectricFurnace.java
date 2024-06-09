package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
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
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricFurnace extends RecipeProcessor<SmeltingRecipe> implements IElectricMachine {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Voltage voltage;
    private final double workFactor;

    private IItemCollection inputPort;
    private RecipeWrapper inputWrapper;
    private IItemCollection outputPort;

    public ElectricFurnace(BlockEntity blockEntity, Voltage voltage) {
        super(blockEntity, RecipeType.SMELTING);
        this.voltage = voltage;
        this.workFactor = 1 << (voltage.rank - 1);
    }

    @Override
    protected boolean matches(Level world, SmeltingRecipe recipe, IContainer container) {
        var result = recipe.assemble(inputWrapper);
        return outputPort.acceptInput(result) && outputPort.insertItem(result, true).isEmpty();
    }

    @Override
    protected List<? extends SmeltingRecipe> getMatchedRecipes(Level world, IContainer container) {
        return world.getRecipeManager().getRecipeFor(recipeType, inputWrapper, world)
                .filter($ -> matches(world, $, container))
                .map(List::of).orElse(List.of());
    }

    @Override
    protected boolean allowTargetRecipe(Recipe<?> recipe) {
        return recipe.getType() == RecipeType.SMELTING;
    }

    @Override
    protected void doSetTargetRecipe(Recipe<?> recipe) {
        targetRecipe = (SmeltingRecipe) recipe;
        inputPort.setItemFilter(targetRecipe.getIngredients());
    }

    @Override
    public void resetTargetRecipe() {
        LOGGER.debug("update target recipe = <null>");
        targetRecipe = null;
        inputPort.resetItemFilter();
    }

    @Override
    protected void onWorkBegin(SmeltingRecipe recipe, IContainer container) {
        var ingredient = recipe.getIngredients().get(0);
        ItemHelper.consumeItemCollection(inputPort, ingredient, 1, false);
    }

    @Override
    protected long onWorkProgress(SmeltingRecipe recipe, double partial) {
        return (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
    }

    @Override
    protected void onWorkDone(SmeltingRecipe recipe, IContainer container, Random random) {
        outputPort.insertItem(recipe.assemble(inputWrapper), false);
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
        return currentRecipe == null ? 0d : voltage.value * 0.625d;
    }

    @Override
    public void onLoad() {
        container = AllCapabilities.CONTAINER.get(blockEntity);
        inputPort = container.getPort(0, false).asItem();
        outputPort = container.getPort(1, true).asItem();
        inputWrapper = new RecipeWrapper((IItemHandlerModifiable)
                ((ItemHandlerCollection) inputPort).itemHandler);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.ELECTRIC_MACHINE.get()) {
            return myself();
        }
        return super.getCapability(cap, side);
    }
}
