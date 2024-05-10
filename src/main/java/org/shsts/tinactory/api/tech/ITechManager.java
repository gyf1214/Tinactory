package org.shsts.tinactory.api.tech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Optional;

public interface ITechManager {
    Optional<? extends ITechnology> techByKey(ResourceLocation loc);

    Collection<? extends ITechnology> allTechs();

    Optional<? extends ITeamProfile> teamByPlayer(Player player);

    Optional<? extends ITeamProfile> teamByName(String name);

    void addPlayerToTeam(Player player, ITeamProfile team);

    void newTeam(Player player, String name);

    void leaveTeam(Player player);
}
