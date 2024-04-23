package org.shsts.tinactory.core.tech;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.core.util.ServerUtil;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Must be called on server
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TechManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static class ReloadListener implements PreparableReloadListener {
        private static final String PREFIX = "technologies";
        private static final String SUFFIX = ".json";

        private final Gson gson = new Gson();

        private static Collection<ResourceLocation> listResources(ResourceManager manager, String prefix) {
            return manager.listResources(prefix, f -> f.endsWith(SUFFIX));
        }

        private <T extends IForgeRegistryEntry<T>, U extends T> Optional<U>
        loadResource(ResourceManager manager, ResourceLocation loc, Decoder<U> codec) {
            var path = loc.getPath();
            var path1 = path.substring(PREFIX.length() + 1, path.length() - SUFFIX.length());
            var loc1 = new ResourceLocation(loc.getNamespace(), path1);
            try (var resource = manager.getResource(loc)) {
                var is = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                var reader = new BufferedReader(is);
                var jo = GsonHelper.fromJson(gson, reader, JsonObject.class);
                var ret = codec.parse(JsonOps.INSTANCE, jo).getOrThrow(false, $ -> {});
                ret.setRegistryName(loc1);
                return Optional.of(ret);
            } catch (IOException | RuntimeException ex) {
                LOGGER.warn("Decode resource {} failed: {}", loc, ex);
            }
            return Optional.empty();
        }

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
                                              ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                              Executor backgroundExecutor, Executor gameExecutor) {

            return stage.wait(Unit.INSTANCE)
                    .thenApplyAsync($ -> listResources(manager, PREFIX).stream()
                            .flatMap(loc -> loadResource(manager, loc, Technology.CODEC).stream())
                            .toList(), backgroundExecutor)
                    .thenAcceptAsync(techs -> {
                        TECHNOLOGIES.clear();
                        techs.forEach(tech -> {
                            assert tech.getRegistryName() != null;
                            TECHNOLOGIES.put(tech.getRegistryName(), tech);
                        });
                        techs.forEach(Technology::resolve);
                    }, backgroundExecutor);
        }
    }

    public static final PreparableReloadListener RELOAD_LISTENER = new ReloadListener();

    private static final Map<ResourceLocation, Technology> TECHNOLOGIES = new HashMap<>();

    public static Optional<Technology> techByKey(ResourceLocation loc) {
        return Optional.of(TECHNOLOGIES.get(loc));
    }

    public static Collection<Technology> allTechs() {
        return TECHNOLOGIES.values();
    }

    public static Optional<TeamProfile> teamByPlayer(Player player) {
        var playerTeam = (PlayerTeam) player.getTeam();
        if (playerTeam == null) {
            return Optional.empty();
        }
        return Optional.of(TinactorySavedData.get().getTeamProfile(playerTeam));
    }

    public static Optional<TeamProfile> teamByName(String name) {
        var playerTeam = ServerUtil.getScoreboard().getPlayerTeam(name);
        if (playerTeam == null) {
            return Optional.empty();
        }
        return Optional.of(TinactorySavedData.get().getTeamProfile(playerTeam));
    }

    public static void addPlayerToTeam(Player player, TeamProfile team) {
        ServerUtil.getScoreboard().addPlayerToTeam(player.getScoreboardName(), team.getPlayerTeam());
    }

    public static TeamProfile newTeam(Player player, String name) {
        var scoreboard = ServerUtil.getScoreboard();
        var playerTeam = scoreboard.addPlayerTeam(name);
        scoreboard.addPlayerToTeam(player.getScoreboardName(), playerTeam);
        return TinactorySavedData.get().getTeamProfile(playerTeam);
    }

    public static void leaveTeam(Player player) {
        var scoreboard = ServerUtil.getScoreboard();
        scoreboard.removePlayerFromTeam(player.getScoreboardName());
    }
}
