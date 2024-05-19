package org.shsts.tinactory.api.tech;

import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public interface IServerTechManager extends ITechManager {
    Optional<? extends IServerTeamProfile> teamByPlayer(Player player);

    Optional<? extends IServerTeamProfile> teamByName(String name);

    void addPlayerToTeam(Player player, ITeamProfile team);

    void newTeam(Player player, String name);

    void leaveTeam(Player player);
}
