package org.shsts.tinactory.core.util;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.server.ServerLifecycleHooks;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ServerUtil {
    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static PlayerList getPlayerList() {
        return getServer().getPlayerList();
    }

    public static Scoreboard getScoreboard() {
        return getServer().getScoreboard();
    }

    public static <T> Registry<T> getRegistry(ResourceKey<? extends Registry<? extends T>> key) {
        return getServer().registryAccess().registryOrThrow(key);
    }
}
