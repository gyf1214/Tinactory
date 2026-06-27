package org.shsts.tinactory.content.recipe;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleanRecipe extends ProcessingRecipe {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<CleanRecipe> CODEC = codec(CleanRecipe::new);

    public final double minCleanness;
    public final double maxCleanness;

    public CleanRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
        double minCleanness, double maxCleanness) {
        super(inputs, outputs, workTicks, voltage, power);
        this.minCleanness = minCleanness;
        this.maxCleanness = maxCleanness;
    }

    protected double getCleanness(IMachine machine, Level world, BlockPos pos) {
        return Cleanroom.getCleanness(world, pos);
    }

    private double getCleannessRate(IMachine machine) {
        var blockEntity = machine.blockEntity();
        var world = blockEntity.getLevel();
        assert world != null;
        var pos = blockEntity.getBlockPos();

        var cleanness = getCleanness(machine, world, pos);

        LOGGER.debug("check cleanness pos={}:{}, cleanness={}",
            world.dimension().location(), pos, cleanness);

        if (cleanness >= maxCleanness) {
            return 1d;
        }
        if (cleanness <= minCleanness) {
            return 0d;
        }
        return (cleanness - minCleanness) / (maxCleanness - minCleanness);
    }

    @Override
    public void insertOutputs(IMachine machine, int parallel, Random random,
        Consumer<IProcessingResult> callback) {
        var rate = getCleannessRate(machine);
        if (rate <= 0d) {
            return;
        }
        var parallel1 = rate < 1d ? MathUtil.sampleBinomial(parallel, rate, random) : parallel;
        super.insertOutputs(machine, parallel1, random, callback);
    }

    @FunctionalInterface
    protected interface Factory<R extends CleanRecipe> {
        R create(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
            double minCleanness, double maxCleanness);
    }

    protected static <R extends CleanRecipe> MapCodec<R> codec(Factory<R> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProcessingRecipe.inputCodec(ProcessingHelper.INGREDIENT_CODEC).listOf().fieldOf("inputs")
                .forGetter($ -> $.inputs),
            ProcessingRecipe.outputCodec(ProcessingHelper.RESULT_CODEC).listOf().fieldOf("outputs")
                .forGetter($ -> $.outputs),
            Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
            Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
            Codec.LONG.fieldOf("power").forGetter($ -> $.power),
            Codec.DOUBLE.optionalFieldOf("min_cleanness", 0d).forGetter($ -> $.minCleanness),
            Codec.DOUBLE.optionalFieldOf("max_cleanness", 0d).forGetter($ -> $.maxCleanness)
        ).apply(instance, factory::create));
    }
}
