package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;

import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzerProcessor extends MachineProcessor<OreAnalyzerRecipe> {
    private boolean emptyRecipe = false;

    public OreAnalyzerProcessor(BlockEntity blockEntity) {
        super(blockEntity, AllRecipes.ORE_ANALYZER, true);
    }

    @Override
    protected Optional<OreAnalyzerRecipe> getNewRecipe(Level world, IMachine machine) {
        var matches = getMatchedRecipes(world, machine);
        var size = matches.size();
        if (size == 0) {
            return Optional.empty();
        }

        var random = world.random;

        var emptyRate = 1d;
        for (var match : matches) {
            emptyRate *= 1 - match.rate;
        }
        emptyRecipe = random.nextDouble() <= emptyRate;

        if (emptyRecipe || size == 1) {
            return Optional.of(matches.get(0));
        }

        var rates = matches.stream().mapToDouble(r -> r.rate).toArray();
        for (var i = 1; i < size; i++) {
            rates[i] += rates[i - 1];
        }
        for (var i = 0; i < size; i++) {
            rates[i] /= rates[size - 1];
        }
        var rn = random.nextDouble();
        for (var i = 0; i < size; i++) {
            if (rn <= rates[i]) {
                return Optional.of(matches.get(i));
            }
        }
        return Optional.of(matches.get(size - 1));
    }

    @Override
    protected void onWorkDone(OreAnalyzerRecipe recipe, IMachine machine, Random random) {
        if (!emptyRecipe) {
            machine.container().ifPresent(container -> recipe.doInsertOutputs(container, random));
        }
    }
}
