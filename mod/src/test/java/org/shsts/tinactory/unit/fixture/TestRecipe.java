package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import java.util.List;

import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.INPUT_CODEC;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.OUTPUT_CODEC;

public class TestRecipe extends ProcessingRecipe {
    public static final MapCodec<TestRecipe> CODEC = ProcessingRecipe.codec(
        INPUT_CODEC, OUTPUT_CODEC, TestRecipe::new);

    public TestRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage,
        long power) {
        super(inputs, outputs, workTicks, voltage, power);
    }

    public boolean matchInputsForTest(TestMachine machine, IContainer container, int parallel) {
        return matchInputs(machine, container, parallel);
    }

    public boolean matchOutputsForTest(TestMachine machine, IContainer container, int parallel,
        RandomSource random) {
        return matchOutputs(machine, container, parallel, random);
    }

    public boolean matchesForTest(TestMachine machine, int parallel, RandomSource random) {
        return canCraft(machine) && machine.container()
            .filter(container -> matchInputs(machine, container, parallel) ||
                machine.config().getBoolean("void", false))
            .filter(container -> machine.config().getBoolean("void", false) ||
                matchOutputs(machine, container, parallel, random))
            .isPresent();
    }
}
