package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeneratorRecipe extends DisplayInputRecipe {
    public static final MapCodec<GeneratorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingRecipe.inputCodec(ProcessingHelper.INGREDIENT_CODEC).listOf().fieldOf("inputs")
            .forGetter($ -> $.inputs),
        ProcessingRecipe.outputCodec(ProcessingHelper.RESULT_CODEC).listOf().optionalFieldOf("outputs", List.of())
            .forGetter($ -> $.outputs),
        Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
        Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
        Codec.LONG.fieldOf("power").forGetter($ -> $.power),
        Codec.BOOL.optionalFieldOf("exactVoltage", false).forGetter($ -> $.exactVoltage)
    ).apply(instance, GeneratorRecipe::new));

    // this is used to distinguish generator recipes that can be overclocked
    private final boolean exactVoltage;

    public GeneratorRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
        boolean exactVoltage) {
        super(inputs, outputs, workTicks, voltage, power);
        this.exactVoltage = exactVoltage;
    }

    public boolean exactVoltage() {
        return exactVoltage;
    }

    @Override
    public boolean matches(IMachine machine) {
        if (exactVoltage) {
            return super.matches(machine);
        }
        var machineVoltage = (long) machine.electric().map(IElectricMachine::getVoltage).orElse(0L);
        if (machineVoltage < voltage) {
            return false;
        }
        return matches(machine, (int) (machineVoltage / voltage));
    }

    @Override
    protected boolean matchElectric(Optional<IElectricMachine> electric) {
        if (exactVoltage) {
            return electric.filter($ -> $.getVoltage() == voltage).isPresent();
        }
        return super.matchElectric(electric);
    }

}
