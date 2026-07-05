package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalReactorRecipe extends AssemblyRecipe {
    public static final MapCodec<ChemicalReactorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingHelper.INPUT_CODEC.listOf().fieldOf("inputs").forGetter($ -> $.inputs),
        ProcessingHelper.OUTPUT_CODEC.listOf().fieldOf("outputs").forGetter($ -> $.outputs),
        Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
        Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
        Codec.LONG.fieldOf("power").forGetter($ -> $.power),
        ResourceLocation.CODEC.listOf().fieldOf("required_tech").forGetter($ -> $.requiredTech),
        Codec.BOOL.fieldOf("require_multiblock").forGetter($ -> $.requireMultiblock)
    ).apply(instance, ChemicalReactorRecipe::new));

    public final boolean requireMultiblock;

    public ChemicalReactorRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
        List<ResourceLocation> requiredTech, boolean requireMultiblock) {
        super(inputs, outputs, workTicks, voltage, power, requiredTech);
        this.requireMultiblock = requireMultiblock;
    }

    @Override
    public boolean canCraft(IMachine machine) {
        return super.canCraft(machine) &&
            (!requireMultiblock || machine.isMultiblock());
    }
}
