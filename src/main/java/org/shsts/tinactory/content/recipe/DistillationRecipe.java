package org.shsts.tinactory.content.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.multiblock.DistillationTower;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Random;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationRecipe extends DisplayInputRecipe {
    private DistillationRecipe(BuilderBase<?, ?> builder) {
        super(builder);
    }

    private int getSlots(IMachine machine) {
        if (!(machine instanceof MultiblockInterface multiblockInterface)) {
            return 0;
        }
        return multiblockInterface.getMultiblock()
            .filter($ -> $ instanceof DistillationTower)
            .map($ -> ((DistillationTower) $).getSlots())
            .orElse(0);
    }

    private boolean matchOutputs(IMachine machine, IContainer container, int parallel, Random random) {
        var slots = getSlots(machine);
        return outputs.stream().limit(slots)
            .allMatch(output -> canInsertOutput(container, output, parallel, random));
    }

    @Override
    public boolean matches(IMachine machine, Level world, int parallel) {
        var container = machine.container();
        return canCraft(machine) && container
            .filter($ -> matchInputs($, parallel) && matchOutputs(machine, $, parallel, world.random))
            .isPresent();
    }

    @Override
    public void insertOutputs(IMachine machine, int parallel, Random random,
        Consumer<IProcessingResult> callback) {
        var container = machine.container().orElseThrow();
        var slots = Math.min(outputs.size(), getSlots(machine));
        for (var i = 0; i < slots; i++) {
            insertOutput(container, outputs.get(i), parallel, random, false).ifPresent(callback);
        }
    }

    public static Builder builder(IRecipeType<Builder> parent, ResourceLocation loc) {
        return new Builder(parent, loc) {
            @Override
            protected ProcessingRecipe createObject() {
                return new DistillationRecipe(this);
            }
        };
    }
}
