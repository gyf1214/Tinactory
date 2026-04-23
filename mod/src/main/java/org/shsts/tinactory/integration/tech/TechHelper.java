package org.shsts.tinactory.integration.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.util.ServerUtil;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TechHelper {
    private TechHelper() {}

    public static Optional<PlayerTeam> playerTeam(Player player) {
        return Optional.ofNullable((PlayerTeam) player.getTeam());
    }

    public static Optional<PlayerTeam> scoreboardTeam(String name) {
        return Optional.ofNullable(ServerUtil.getScoreboard().getPlayerTeam(name));
    }

    public static boolean isPlayerOnTeam(Player player, ITeamProfile team) {
        return playerTeam(player)
            .map(playerTeam -> playerTeam.getName().equals(team.getName()))
            .orElse(false);
    }
}
