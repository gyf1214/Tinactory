package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.RandomSource;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.multiblock.DistillationTower;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;

import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationRecipe extends DisplayInputRecipe {
    public static final MapCodec<DistillationRecipe> CODEC =
        ProcessingHelper.PROCESSING_CODEC.xmap(DistillationRecipe::new, $ -> $);

    public DistillationRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power) {
        super(inputs, outputs, workTicks, voltage, power);
    }

    private DistillationRecipe(ProcessingRecipe recipe) {
        this(recipe.inputs, recipe.outputs, recipe.workTicks, recipe.voltage, recipe.power);
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

    @Override
    protected boolean matchOutputs(IMachine machine, IContainer container,
        int parallel, RandomSource random) {
        var slots = getSlots(machine);
        return outputs.stream().limit(slots)
            .allMatch(output -> canInsertOutput(container, output, parallel, random));
    }

    @Override
    public void insertOutputs(IMachine machine, int parallel, RandomSource random,
        Consumer<IProcessingResult> callback) {
        var container = machine.container().orElseThrow();
        var slots = Math.min(outputs.size(), getSlots(machine));
        for (var i = 0; i < slots; i++) {
            insertOutput(container, outputs.get(i), parallel, random, false).ifPresent(callback);
        }
    }
}
