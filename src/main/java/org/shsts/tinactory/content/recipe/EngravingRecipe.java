package org.shsts.tinactory.content.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.content.multiblock.Lithography;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EngravingRecipe extends CleanRecipe {
    private EngravingRecipe(Builder builder) {
        super(builder);
    }

    private Optional<Lithography> getLithography(IMachine machine) {
        if (!(machine instanceof MultiblockInterface multiblockInterface)) {
            return Optional.empty();
        }
        return multiblockInterface.getMultiblock()
            .flatMap($ -> $ instanceof Lithography lithography ?
                Optional.of(lithography) : Optional.empty());
    }

    private boolean matchLens(Lithography lithography, IProcessingIngredient ingredient) {
        var lens = lithography.getLens();
        if (ingredient instanceof ProcessingIngredients.ItemsIngredientBase items) {
            return lens.stream().anyMatch($ -> items.ingredient.test(new ItemStack($)));
        } else if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
            return lens.stream().anyMatch($ -> item.stack().is($));
        } else {
            return false;
        }
    }

    @Override
    public boolean canCraft(IMachine machine) {
        return super.canCraft(machine) && getLithography(machine)
            .map($ -> inputs.stream().allMatch(input ->
                input.port() != 1 || matchLens($, input.ingredient())))
            .orElse(true);
    }

    private boolean matchInputs(IMachine machine, IContainer container) {
        return getLithography(machine)
            .map($ -> inputs.stream().allMatch(input ->
                input.port() == 1 || consumeInput(container, input, true)))
            .orElseGet(() -> matchInputs(container));
    }

    @Override
    public boolean matches(IMachine machine, Level world) {
        var container = machine.container();
        return canCraft(machine) && container
            .filter($ -> matchInputs(machine, $) && matchOutputs($, world.random))
            .isPresent();
    }

    @Override
    protected double getCleanness(IMachine machine, Level world, BlockPos pos) {
        var factor = getLithography(machine).map(Lithography::getCleannessFactor).orElse(1d);
        return factor * super.getCleanness(machine, world, pos);
    }

    public static Builder builder(IRecipeType<Builder> parent, ResourceLocation loc) {
        return new Builder(parent, loc) {
            @Override
            protected CleanRecipe createObject() {
                return new EngravingRecipe(this);
            }
        };
    }
}
