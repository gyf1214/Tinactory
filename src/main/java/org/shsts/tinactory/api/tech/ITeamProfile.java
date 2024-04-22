package org.shsts.tinactory.api.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
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
        return player.getTeam() == getPlayerTeam();
    }
}
