package org.shsts.tinactory.api.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITeamProvider {
    default void unregister() {}

    Optional<String> teamIdByPlayer(Player player);

    Optional<String> teamIdById(String teamId);

    Collection<ServerPlayer> onlineMembers(String teamId);
}
