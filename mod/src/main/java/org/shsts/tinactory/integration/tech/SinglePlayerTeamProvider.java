package org.shsts.tinactory.integration.tech;

import com.mojang.authlib.GameProfile;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.tech.ITeamProvider;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.util.ServerUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SinglePlayerTeamProvider implements ITeamProvider {
    private static final String PREFIX = "single_player:";

    @Override
    public void unregister() {}

    @Override
    public Optional<String> teamIdByPlayer(Player player) {
        return Optional.of(teamId(player.getUUID()));
    }

    public static Optional<UUID> teamIdToUuid(String prefix, String teamId) {
        if (!teamId.startsWith(prefix)) {
            return Optional.empty();
        }
        try {
            var playerId = UUID.fromString(teamId.substring(prefix.length()));
            return Optional.of(playerId);
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<GameProfile> teamIdToGameProfile(String teamId) {
        return teamIdToUuid(PREFIX, teamId).flatMap(ServerUtil::getPlayerProfile);
    }

    @Override
    public Optional<String> teamIdById(String teamId) {
        return teamIdToGameProfile(teamId).map(profile -> teamId(profile.getId()));
    }

    @Override
    public Collection<ServerPlayer> onlineMembers(String teamId) {
        return teamIdById(teamId).stream()
            .flatMap(id -> ServerUtil.getPlayerList().getPlayers().stream()
                .filter(player -> teamId(player.getUUID()).equals(id)))
            .toList();
    }

    @Override
    public Optional<Component> teamDisplayName(String teamId) {
        return teamIdToGameProfile(teamId).map(profile -> I18n.raw(profile.getName()));
    }

    private static String teamId(UUID uuid) {
        return PREFIX + uuid;
    }
}
