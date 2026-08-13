package org.shsts.tinactory.integration.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.tech.ITeamProvider;
import org.shsts.tinactory.integration.util.ServerUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SinglePlayerTeamProvider implements ITeamProvider {
    private static final String PREFIX = "single_player:";

    @Override
    public Optional<String> teamIdByPlayer(Player player) {
        return Optional.of(teamId(player.getUUID()));
    }

    @Override
    public Optional<String> teamIdById(String teamId) {
        if (!teamId.startsWith(PREFIX)) {
            return Optional.empty();
        }
        try {
            var uuid = UUID.fromString(teamId.substring(PREFIX.length()));
            var canonicalId = teamId(uuid);
            return teamId.equals(canonicalId) ? Optional.of(canonicalId) : Optional.empty();
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<ServerPlayer> onlineMembers(String teamId) {
        return teamIdById(teamId).stream()
            .flatMap(id -> ServerUtil.getPlayerList().getPlayers().stream()
                .filter(player -> teamId(player.getUUID()).equals(id)))
            .toList();
    }

    private static String teamId(UUID uuid) {
        return PREFIX + uuid;
    }
}
