package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.multiblock.CoilMultiblock;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceRecipe extends ProcessingRecipe {
    public static final MapCodec<BlastFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingRecipe.inputCodec(ProcessingHelper.INGREDIENT_CODEC).listOf().fieldOf("inputs")
            .forGetter($ -> $.inputs),
        ProcessingRecipe.outputCodec(ProcessingHelper.RESULT_CODEC).listOf().fieldOf("outputs")
            .forGetter($ -> $.outputs),
        Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
        Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
        Codec.LONG.fieldOf("power").forGetter($ -> $.power),
        Codec.INT.fieldOf("temperature").forGetter($ -> $.temperature)
    ).apply(instance, BlastFurnaceRecipe::new));

    public final int temperature;

    private BlastFurnaceRecipe(Builder builder) {
        super(builder);
        this.temperature = builder.temperature;
    }

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

    public static class Builder extends BuilderBase<BlastFurnaceRecipe, Builder> {
        private int temperature = 0;

        public Builder(IRecipeType<?> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder temperature(int value) {
            this.temperature = value;
            return this;
        }

        @Override
        protected BlastFurnaceRecipe createObject() {
            return new BlastFurnaceRecipe(this);
        }
    }
}
