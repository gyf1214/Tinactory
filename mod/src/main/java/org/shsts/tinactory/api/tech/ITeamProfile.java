package org.shsts.tinactory.api.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITeamProfile {
    String getName();

    long getTechProgress(ITechnology tech);

    long getTechProgress(ResourceLocation tech);

    boolean isTechFinished(ITechnology tech);

    boolean isTechFinished(ResourceLocation tech);

    boolean isTechAvailable(ITechnology tech);

    boolean isTechAvailable(ResourceLocation tech);

    default boolean canResearch(ResourceLocation tech) {
        return isTechAvailable(tech) && !isTechFinished(tech);
    }

    boolean canResearch(ResourceLocation tech, long progress);

    boolean canResearch(ITechnology tech);

    boolean canResearch(ITechnology tech, long progress);

    Optional<ITechnology> getTargetTech();

    Optional<ResourceLocation> getTargetTechKey();

    int getModifier(String key);
}
