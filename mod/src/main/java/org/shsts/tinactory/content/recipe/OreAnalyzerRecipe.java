package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzerRecipe extends AssemblyRecipe {
    public static final MapCodec<OreAnalyzerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingHelper.INPUT_CODEC.listOf().fieldOf("inputs").forGetter($ -> $.inputs),
        ProcessingHelper.OUTPUT_CODEC.listOf().fieldOf("outputs").forGetter($ -> $.outputs),
        Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
        Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
        Codec.LONG.fieldOf("power").forGetter($ -> $.power),
        ResourceLocation.CODEC.listOf().fieldOf("required_tech").forGetter($ -> $.requiredTech),
        Codec.DOUBLE.fieldOf("rate").forGetter($ -> $.rate)
    ).apply(instance, OreAnalyzerRecipe::new));

    public final double rate;

    public OreAnalyzerRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
        List<ResourceLocation> requiredTech, double rate) {
        super(inputs, outputs, workTicks, voltage, power, requiredTech);
        this.rate = rate;
        assert rate > 0d;
    }
}
