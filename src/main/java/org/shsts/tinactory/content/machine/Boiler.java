package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.content.recipe.BoilerRecipe;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.shsts.tinactory.AllRecipes.BOILER;
import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Boiler implements INBTSerializable<CompoundTag> {
    private final double baseHeat;
    private final double baseDecay;

    @Nullable
    private IFluidCollection input;
    @Nullable
    private IFluidCollection output;
    private double heat;
    // we don't serialize these two, so on reload, hiddenProgress is lost, but it's negligible.
    @Nullable
    protected BoilerRecipe lastRecipe = null;
    private int lastReaction;
    private double hiddenProgress = 0;

    public Boiler(double baseHeat, double baseDecay) {
        this.baseHeat = baseHeat;
        this.baseDecay = baseDecay;
        this.heat = baseHeat;
    }

    public void setContainer(IFluidCollection input, IFluidCollection output) {
        this.input = input;
        this.output = output;
    }

    public void resetContainer() {
        input = null;
        output = null;
    }

    public double heat() {
        return heat;
    }

    public Optional<IFluidCollection> getInput() {
        return Optional.ofNullable(input);
    }

    private double absorbHeat(BoilerRecipe recipe, double parallel,
        BiConsumer<FluidStack, FluidStack> callback) {
        if (input == null || output == null) {
            return 0;
        }
        if (lastRecipe != recipe) {
            lastRecipe = recipe;
            hiddenProgress = 0;
        }

        var reaction = recipe.getReaction(heat, parallel) + hiddenProgress;
        var reaction1 = (int) Math.floor(reaction);
        hiddenProgress = reaction - reaction1;
        lastReaction = reaction1;
        return reaction1 > 0 ? recipe.absorbHeat(input, output, reaction1, heat, callback) : 0;
    }

    public void tick(Level world, double heatInput, double parallel,
        BiConsumer<FluidStack, FluidStack> callback) {
        var decay = Math.max(0, heat - baseHeat) * baseDecay;

        var recipeManager = CORE.recipeManager(world);
        var recipe = recipeManager.getRecipeFor(BOILER, this, world);
        // hidden progress is lost if the recipe is interrupted
        if (recipe.isEmpty()) {
            lastRecipe = null;
            hiddenProgress = 0;
            lastReaction = 0;
        }
        var absorb = (double) recipe.map($ -> absorbHeat($, parallel, callback)).orElse(0d);

        heat += heatInput - decay - absorb;
    }

    private Optional<FluidStack> getInfo(Function<BoilerRecipe, FluidStack> mapper) {
        if (lastRecipe == null) {
            return Optional.empty();
        }
        var stack = mapper.apply(lastRecipe);
        if (lastReaction > 1) {
            return Optional.of(StackHelper.copyWithAmount(stack, stack.getAmount() * lastReaction));
        } else {
            return Optional.of(stack);
        }
    }

    public Optional<IProcessingObject> inputInfo() {
        return getInfo($ -> $.input).map(ProcessingIngredients.FluidIngredient::new);
    }

    public Optional<IProcessingObject> outputInfo() {
        return getInfo($ -> $.output).map(ProcessingResults.FluidResult::new);
    }

    public void addAllInfo(Consumer<IProcessingObject> cons) {
        inputInfo().ifPresent(cons);
        outputInfo().ifPresent(cons);
    }

    @Override
    public CompoundTag serializeNBT() {
        var ret = new CompoundTag();
        ret.putDouble("heat", heat);
        return ret;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        heat = tag.getDouble("heat");
    }
}
