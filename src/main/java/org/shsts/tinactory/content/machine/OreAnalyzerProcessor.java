package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzerProcessor extends RecipeProcessor<OreAnalyzerRecipe> {
    private boolean emptyRecipe = false;

    public OreAnalyzerProcessor(BlockEntity blockEntity, Voltage voltage) {
        super(blockEntity, AllRecipes.ORE_ANALYZER.get(), voltage);
    }

    @Override
    protected void setTargetRecipe(ResourceLocation loc, boolean updateFilter) {
        var variant = Arrays.stream(OreVariant.values())
                .filter(v -> loc.equals(v.baseItem.getRegistryName()))
                .findAny();
        if (variant.isEmpty()) {
            resetTargetRecipe(updateFilter);
            return;
        }
        var item = variant.get().baseItem;

        var port = container.getPort(0, false);
        container.setItemFilter(0, stack -> stack.is(item));
        getLogistics().ifPresent($ -> $.addPassiveStorage(PortDirection.INPUT, port));
    }

    @Override
    protected Optional<OreAnalyzerRecipe> getNewRecipe(Level world, IContainer container) {
        var matches = getMatchedRecipes(world);
        var size = matches.size();
        if (size == 0) {
            return Optional.empty();
        }
        if (size == 1) {
            return Optional.of(matches.get(0));
        }

        var random = world.random;

        var emptyRate = 1d;
        for (var match : matches) {
            emptyRate *= 1 - match.rate;
        }
        emptyRecipe = random.nextDouble() <= emptyRate;

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
    protected void onWorkDone(OreAnalyzerRecipe recipe, Random random) {
        if (!emptyRecipe) {
            recipe.doInsertOutputs(container, random);
        }
    }
}
