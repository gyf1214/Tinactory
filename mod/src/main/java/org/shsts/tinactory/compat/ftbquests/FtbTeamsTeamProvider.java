package org.shsts.tinactory.compat.ftbquests;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.TinactoryConfig.FtbUnaffiliatedPlayerPolicy;
import org.shsts.tinactory.api.tech.ITeamProvider;
import org.shsts.tinactory.integration.tech.ServerTechManager;
import org.shsts.tinactory.integration.tech.SinglePlayerTeamProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class FtbTeamsTeamProvider implements ITeamProvider {
    private static final String PREFIX = "ftb_teams:";
    private final FtbUnaffiliatedPlayerPolicy unaffiliatedPlayerPolicy;
    private final Consumer<PlayerChangedTeamEvent> playerChangedListener;

    public FtbTeamsTeamProvider(ServerTechManager techManager,
        FtbUnaffiliatedPlayerPolicy unaffiliatedPlayerPolicy) {
        this.unaffiliatedPlayerPolicy = unaffiliatedPlayerPolicy;
        playerChangedListener = event -> {
            if (event.getPlayer() != null) {
                techManager.syncTeam(event.getPlayer());
            }
        };
        TeamEvent.PLAYER_CHANGED.register(playerChangedListener);
    }

    @Override
    public void unregister() {
        TeamEvent.PLAYER_CHANGED.unregister(playerChangedListener);
    }

    @Override
    public Optional<String> teamIdByPlayer(Player player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID())
            .filter(this::isAcceptedTeam)
            .map(Team::getId)
            .map(FtbTeamsTeamProvider::profileId);
    }

    @Override
    public Optional<String> teamIdById(String teamId) {
        return teamByProfileId(teamId).map(team -> profileId(team.getId()));
    }

    @Override
    public Collection<ServerPlayer> onlineMembers(String teamId) {
        return teamByProfileId(teamId).map(Team::getOnlineMembers).orElseGet(List::of);
    }

    @Override
    public Optional<Component> teamDisplayName(String teamId) {
        return teamByProfileId(teamId).map(Team::getName);
    }

    public static Optional<UUID> profileIdToTeamId(String profileId) {
        return SinglePlayerTeamProvider.teamIdToUuid(PREFIX, profileId);
    }

    public static boolean isFtbProfileId(String profileId) {
        return profileId.startsWith(PREFIX);
    }

    private Optional<Team> teamByProfileId(String profileId) {
        return profileIdToTeamId(profileId)
            .flatMap(teamId -> FTBTeamsAPI.api().getManager().getTeamByID(teamId))
            .filter(this::isAcceptedTeam);
    }

    private boolean isAcceptedTeam(Team team) {
        return team.isPartyTeam() || (unaffiliatedPlayerPolicy == FtbUnaffiliatedPlayerPolicy.PERSONAL &&
            team.isPlayerTeam());
    }

    private static String profileId(UUID teamId) {
        return PREFIX + teamId;
    }
}
