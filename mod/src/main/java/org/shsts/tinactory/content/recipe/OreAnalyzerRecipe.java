package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzerRecipe extends AssemblyRecipe {
    public static final MapCodec<OreAnalyzerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingRecipe.inputCodec(ProcessingHelper.INGREDIENT_CODEC).listOf().fieldOf("inputs")
            .forGetter($ -> $.inputs),
        ProcessingRecipe.outputCodec(ProcessingHelper.RESULT_CODEC).listOf().fieldOf("outputs")
            .forGetter($ -> $.outputs),
        Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
        Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
        Codec.LONG.fieldOf("power").forGetter($ -> $.power),
        ResourceLocation.CODEC.listOf().optionalFieldOf("required_tech", List.of()).forGetter($ -> $.requiredTech),
        Codec.DOUBLE.fieldOf("rate").forGetter($ -> $.rate)
    ).apply(instance, OreAnalyzerRecipe::new));

    public final double rate;

    private OreAnalyzerRecipe(Builder builder) {
        super(builder);
        this.rate = builder.rate;
    }

    public OreAnalyzerRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
        List<ResourceLocation> requiredTech, double rate) {
        super(inputs, outputs, workTicks, voltage, power, requiredTech);
        this.rate = rate;
        assert rate > 0d;
    }

    public static class Builder extends AssemblyRecipe.BuilderBase<OreAnalyzerRecipe, Builder> {
        public double rate = 0d;

        public Builder(IRecipeType<?> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder rate(double value) {
            rate = value;
            return this;
        }

        @Override
        protected OreAnalyzerRecipe createObject() {
            assert rate > 0d;
            return new OreAnalyzerRecipe(this);
        }
    }
}
