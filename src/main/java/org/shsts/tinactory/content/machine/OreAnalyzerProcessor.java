package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzerProcessor extends MachineProcessor<OreAnalyzerRecipe> {
    private boolean emptyRecipe = false;

    public OreAnalyzerProcessor(BlockEntity blockEntity, Voltage voltage) {
        super(blockEntity, AllRecipes.ORE_ANALYZER.get(), voltage);
    }

    @Override
    protected Optional<OreAnalyzerRecipe> getNewRecipe(Level world, IContainer container) {
        var matches = getMatchedRecipes(world, container);
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

        if (size == 1) {
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
    protected void onWorkDone(OreAnalyzerRecipe recipe, IContainer container, Random random) {
        if (!emptyRecipe) {
            recipe.doInsertOutputs(container, random);
        }
    }
}
