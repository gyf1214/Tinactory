package org.shsts.tinactory.api.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITeamProfile {
    PlayerTeam getPlayerTeam();

    default String getName() {
        return getPlayerTeam().getName();
    }

    default boolean hasPlayer(Player player) {
        return player.getTeam() != null && player.getTeam().getName().equals(getName());
    }

    default long getTechProgress(ITechnology tech) {
        return getTechProgress(tech.getLoc());
    }

    long getTechProgress(ResourceLocation tech);

    default boolean isTechFinished(ITechnology tech) {
        return getTechProgress(tech) >= tech.getMaxProgress();
    }

    boolean isTechFinished(ResourceLocation tech);

    default boolean isTechAvailable(ITechnology tech) {
        return getTechProgress(tech) > 0 || tech.getDepends().stream().allMatch(this::isTechFinished);
    }

    boolean isTechAvailable(ResourceLocation tech);
}
