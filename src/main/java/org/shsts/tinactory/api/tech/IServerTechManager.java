package org.shsts.tinactory.api.tech;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public interface IServerTechManager extends ITechManager {
    Optional<? extends IServerTeamProfile> teamByPlayer(Player player);

    Optional<? extends IServerTeamProfile> teamByName(String name);

    void addPlayerToTeam(ServerPlayer player, ITeamProfile team);

    void newTeam(ServerPlayer player, String name);

    void leaveTeam(ServerPlayer player);
}
