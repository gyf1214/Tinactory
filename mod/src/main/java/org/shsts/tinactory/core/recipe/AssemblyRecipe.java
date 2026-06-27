package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblyRecipe extends ProcessingRecipe {
    public final List<ResourceLocation> requiredTech;

    protected AssemblyRecipe(BuilderBase<?, ?> builder) {
        super(builder);
        this.requiredTech = List.copyOf(builder.requiredTech);
    }

    public AssemblyRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
        List<ResourceLocation> requiredTech) {
        super(inputs, outputs, workTicks, voltage, power);
        this.requiredTech = List.copyOf(requiredTech);
    }

    @Override
    protected boolean matchTeam(Optional<ITeamProfile> team) {
        return team.map($ -> requiredTech.stream().allMatch($::isTechFinished))
            .orElse(requiredTech.isEmpty());
    }

    protected abstract static class BuilderBase<R extends AssemblyRecipe, S extends BuilderBase<R, S>> extends
        ProcessingRecipe.BuilderBase<R, S> {
        protected final List<ResourceLocation> requiredTech = new ArrayList<>();

        protected BuilderBase(IRecipeType<?> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public S requireTech(ResourceLocation... loc) {
            requiredTech.addAll(Arrays.asList(loc));
            return self();
        }
    }

    public static class Builder extends BuilderBase<AssemblyRecipe, Builder> {
        public Builder(IRecipeType<?> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        @Override
        protected AssemblyRecipe createObject() {
            return new AssemblyRecipe(this);
        }
    }

    @FunctionalInterface
    public interface Factory<R extends AssemblyRecipe> {
        R create(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
            List<ResourceLocation> requiredTech);
    }

    public static <R extends AssemblyRecipe> MapCodec<R> codec(Codec<IProcessingIngredient> ingredientCodec,
        Codec<IProcessingResult> resultCodec, Factory<R> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProcessingRecipe.inputCodec(ingredientCodec).listOf().fieldOf("inputs").forGetter($ -> $.inputs),
            ProcessingRecipe.outputCodec(resultCodec).listOf().fieldOf("outputs").forGetter($ -> $.outputs),
            Codec.LONG.fieldOf("work_ticks").forGetter($ -> $.workTicks),
            Codec.LONG.fieldOf("voltage").forGetter($ -> $.voltage),
            Codec.LONG.fieldOf("power").forGetter($ -> $.power),
            ResourceLocation.CODEC.listOf().optionalFieldOf("required_tech", List.of())
                .forGetter($ -> $.requiredTech)
        ).apply(instance, factory::create));
    }

    public static MapCodec<AssemblyRecipe> codec(Codec<IProcessingIngredient> ingredientCodec,
        Codec<IProcessingResult> resultCodec) {
        return codec(ingredientCodec, resultCodec, AssemblyRecipe::new);
    }
}
