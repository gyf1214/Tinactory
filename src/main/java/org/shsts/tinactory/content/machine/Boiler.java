package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.content.recipe.BoilerRecipe;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.content.AllRecipes.BOILER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Boiler implements INBTSerializable<CompoundTag> {
    private final double baseHeat;
    private final double baseDecay;
    private IFluidCollection input;
    private IFluidCollection output;

    private double heat;
    // we don't serialize these two, so on reload, hiddenProgress is lost, but it's negligible.
    @Nullable
    private BoilerRecipe lastRecipe = null;
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

    public double getHeat() {
        return heat;
    }

    public IFluidCollection getInput() {
        return input;
    }

    public double absorbHeat(BoilerRecipe recipe, double parallel) {
        if (lastRecipe != recipe) {
            lastRecipe = recipe;
            hiddenProgress = 0;
        }

        var reaction = recipe.getReaction(heat, parallel) + hiddenProgress;
        var reaction1 = (int) Math.floor(reaction);
        hiddenProgress = reaction - reaction1;
        return reaction1 > 0 ? recipe.absorbHeat(input, output, reaction1, heat) : 0;
    }

    public void tick(Level world, double heatInput, double parallel) {
        var decay = Math.max(0, heat - baseHeat) * baseDecay;

        var recipeManager = CORE.recipeManager(world);
        var recipe = recipeManager.getRecipeFor(BOILER, this, world);
        var absorb = (double) recipe.map($ -> absorbHeat($, parallel)).orElse(0d);

        heat += heatInput - decay - absorb;
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
