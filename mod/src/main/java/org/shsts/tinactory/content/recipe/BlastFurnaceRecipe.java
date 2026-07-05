package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.multiblock.CoilMultiblock;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceRecipe extends ProcessingRecipe {
    public static final MapCodec<BlastFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingHelper.INPUT_CODEC.listOf().fieldOf("inputs").forGetter($ -> $.inputs),
        ProcessingHelper.OUTPUT_CODEC.listOf().fieldOf("outputs").forGetter($ -> $.outputs),
        Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
        Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
        Codec.LONG.fieldOf("power").forGetter($ -> $.power),
        Codec.INT.fieldOf("temperature").forGetter($ -> $.temperature)
    ).apply(instance, BlastFurnaceRecipe::new));

    public final int temperature;

    public BlastFurnaceRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
        int temperature) {
        super(inputs, outputs, workTicks, voltage, power);
        this.temperature = temperature;
    }

    @Override
    public boolean canCraft(IMachine machine) {
        var machineTemp = CoilMultiblock.getTemperature(machine);
        return super.canCraft(machine) && machineTemp.isPresent() &&
            temperature <= machineTemp.getAsInt();
    }
}
