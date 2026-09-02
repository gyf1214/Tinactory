package org.shsts.tinactory.api.tech;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Optional;

public interface IServerTechManager extends ITechManager {
    Optional<? extends IServerTeamProfile> teamByPlayer(Player player);

    Optional<? extends IServerTeamProfile> teamByName(String name);

    Collection<ServerPlayer> onlineMembers(String profileId);

    void syncTeam(ServerPlayer player);

    Optional<Component> teamDisplayName(String name);
}
