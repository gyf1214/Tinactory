package org.shsts.tinactory.integration.tech;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.TinactoryConfig.FtbUnaffiliatedPlayerPolicy;
import org.shsts.tinactory.api.tech.ITeamProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class FtbTeamsTeamProvider implements ITeamProvider {
    private static final String PREFIX = "ftb_teams:";
    private final FtbUnaffiliatedPlayerPolicy unaffiliatedPlayerPolicy;

    public FtbTeamsTeamProvider(ServerTechManager techManager,
        FtbUnaffiliatedPlayerPolicy unaffiliatedPlayerPolicy) {
        this.unaffiliatedPlayerPolicy = unaffiliatedPlayerPolicy;
        TeamEvent.PLAYER_CHANGED.register(event -> techManager.syncTeam(event.getPlayer()));
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

    public static Optional<UUID> profileIdToTeamId(String profileId) {
        if (!profileId.startsWith(PREFIX)) {
            return Optional.empty();
        }
        try {
            var teamId = UUID.fromString(profileId.substring(PREFIX.length()));
            return profileId.equals(profileId(teamId)) ? Optional.of(teamId) : Optional.empty();
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Team> teamByProfileId(String profileId) {
        return profileIdToTeamId(profileId)
            .flatMap(teamId -> FTBTeamsAPI.api().getManager().getTeamByID(teamId))
            .filter(this::isAcceptedTeam);
    }

    private boolean isAcceptedTeam(Team team) {
        return team.isPartyTeam() || unaffiliatedPlayerPolicy == FtbUnaffiliatedPlayerPolicy.PERSONAL &&
            team.isPlayerTeam();
    }

    private static String profileId(UUID teamId) {
        return PREFIX + teamId;
    }
}
