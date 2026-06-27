package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalReactorRecipe extends AssemblyRecipe {
    public static final MapCodec<ChemicalReactorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingRecipe.inputCodec(ProcessingHelper.INGREDIENT_CODEC).listOf().fieldOf("inputs")
            .forGetter($ -> $.inputs),
        ProcessingRecipe.outputCodec(ProcessingHelper.RESULT_CODEC).listOf().fieldOf("outputs")
            .forGetter($ -> $.outputs),
        Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
        Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
        Codec.LONG.fieldOf("power").forGetter($ -> $.power),
        ResourceLocation.CODEC.listOf().optionalFieldOf("required_tech", List.of()).forGetter($ -> $.requiredTech),
        Codec.BOOL.optionalFieldOf("require_multiblock", false).forGetter($ -> $.requireMultiblock)
    ).apply(instance, ChemicalReactorRecipe::new));

    public final boolean requireMultiblock;

    private ChemicalReactorRecipe(Builder builder) {
        super(builder);
        this.requireMultiblock = builder.requireMultiblock;
    }

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

    public static class Builder extends BuilderBase<ChemicalReactorRecipe, Builder> {
        private boolean requireMultiblock = false;

        public Builder(IRecipeType<?> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder requireMultiblock(boolean val) {
            requireMultiblock = val;
            return this;
        }

        @Override
        protected ChemicalReactorRecipe createObject() {
            return new ChemicalReactorRecipe(this);
        }
    }
}
