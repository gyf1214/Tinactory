package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.api.tech.IServerTeamProfile;
import org.shsts.tinactory.api.tech.ITeamProfile;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchRecipe extends ProcessingRecipe {
    public final ResourceLocation target;
    public final long progress;

    public ResearchRecipe(List<Input> inputs, long workTicks, long voltage, long power,
        ResourceLocation target, long progress) {
        super(inputs, List.of(), workTicks, voltage, power);
        this.target = target;
        this.progress = progress;
    }

    @Override
    protected boolean matchTeam(Optional<ITeamProfile> team) {
        return team.filter($ -> $.canResearch(target))
            .flatMap(ITeamProfile::getTargetTech)
            .filter(tech -> tech.loc().equals(target))
            .isPresent();
    }

    @Override
    protected boolean matchOutputs(IMachine machine, IContainer container, int parallel, RandomSource random) {
        var progress1 = parallel <= 1 ? 1 : progress * parallel;
        return machine.owner()
            .filter($ -> $.canResearch(target, progress1))
            .isPresent();
    }

    @Override
    public void insertOutputs(IMachine machine, int parallel, RandomSource random,
        Consumer<IProcessingResult> callback) {
        machine.owner()
            .ifPresent(team -> ((IServerTeamProfile) team).advanceTechProgress(target, progress * parallel));
    }

    public static MapCodec<ResearchRecipe> codec(Codec<Input> inputCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            inputCodec.listOf().fieldOf("inputs").forGetter($ -> $.inputs),
            Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
            Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
            Codec.LONG.fieldOf("power").forGetter($ -> $.power),
            ResourceLocation.CODEC.fieldOf("target").forGetter($ -> $.target),
            Codec.LONG.fieldOf("progress").forGetter($ -> $.progress)
        ).apply(instance, ResearchRecipe::new));
    }
}
