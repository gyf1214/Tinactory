package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRecipeProcessor<T> extends INBTSerializable<CompoundTag> {
    Class<T> baseClass();

    Optional<T> byLoc(Level world, ResourceLocation loc);

    ResourceLocation toLoc(T recipe);

    DistLazy<List<IRecipeBookItem>> recipeBookItems(Level world, IMachine machine);

    boolean allowTargetRecipe(Level world, ResourceLocation loc, IMachine machine);

    void setTargetRecipe(Level world, ResourceLocation loc, IMachine machine);

    Optional<T> newRecipe(Level world, IMachine machine);

    /**
     * Call this when there's a target recipe.
     */
    Optional<T> newRecipe(Level world, IMachine machine, ResourceLocation target);

    default Optional<T> newRecipe(Level world, IMachine machine, Optional<ResourceLocation> target) {
        return target.map($ -> newRecipe(world, machine, $))
            .orElseGet(() -> newRecipe(world, machine));
    }

    /**
     * Info is for returning actual ingredients.
     */
    void onWorkBegin(T recipe, IMachine machine, int maxParallel, Consumer<ProcessingInfo> info);

    void onWorkContinue(T recipe, IMachine machine);

    long onWorkProgress(T recipe, double partial);

    void onWorkDone(T recipe, IMachine machine, Random random);

    long getMaxWorkProgress(T recipe);

    ElectricMachineType electricMachineType(T recipe);

    double powerGen(T recipe);

    double powerCons(T recipe);
}
