package org.shsts.tinactory.core.tech;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
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
import net.minecraftforge.event.AddReloadListenerEvent;
import org.shsts.tinactory.api.tech.IServerTechManager;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.core.util.ServerUtil;
import org.slf4j.Logger;

import javax.annotation.Nullable;
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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TechManager implements ITechManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Map<ResourceLocation, Technology> technologies = new HashMap<>();

    protected TechManager() {}

    @Override
    public Optional<Technology> techByKey(ResourceLocation loc) {
        return Optional.of(technologies.get(loc));
    }

    @Override
    public Collection<Technology> allTechs() {
        return technologies.values();
    }

    public void unload() {
        technologies.clear();
        LOGGER.debug("unload tech manager {}", this);
    }

    public static class Server extends TechManager implements IServerTechManager {
        private class ReloadListener implements PreparableReloadListener {
            private static final String PREFIX = "technologies";
            private static final String SUFFIX = ".json";

            private final Gson gson = new Gson();

            private static Collection<ResourceLocation> listResources(ResourceManager manager) {
                return manager.listResources(PREFIX, f -> f.endsWith(SUFFIX));
            }

            private Optional<Technology> loadResource(ResourceManager manager, ResourceLocation loc) {
                var codec = Technology.CODEC;
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

                LOGGER.debug("tech manager reload resources");
                return stage.wait(Unit.INSTANCE)
                        .thenApplyAsync($ -> listResources(manager).stream()
                                .flatMap(loc -> loadResource(manager, loc).stream())
                                .toList(), backgroundExecutor)
                        .thenAcceptAsync(techs -> {
                            technologies.clear();
                            techs.forEach(tech -> {
                                assert tech.getRegistryName() != null;
                                technologies.put(tech.getRegistryName(), tech);
                            });
                            techs.forEach(Technology::resolve);
                        }, backgroundExecutor);
            }
        }

        private final PreparableReloadListener reloadListener = new ReloadListener();

        public void addReloadListener(AddReloadListenerEvent event) {
            event.addListener(reloadListener);
        }

        @Override
        public Optional<TeamProfile> teamByPlayer(Player player) {
            var playerTeam = (PlayerTeam) player.getTeam();
            if (playerTeam == null) {
                return Optional.empty();
            }
            return Optional.of(TinactorySavedData.get().getTeamProfile(playerTeam));
        }

        @Override
        public Optional<TeamProfile> teamByName(String name) {
            var playerTeam = ServerUtil.getScoreboard().getPlayerTeam(name);
            if (playerTeam == null) {
                return Optional.empty();
            }
            return Optional.of(TinactorySavedData.get().getTeamProfile(playerTeam));
        }

        @Override
        public void addPlayerToTeam(Player player, ITeamProfile team) {
            ServerUtil.getScoreboard().addPlayerToTeam(player.getScoreboardName(), team.getPlayerTeam());
        }

        @Override
        public void newTeam(Player player, String name) {
            var scoreboard = ServerUtil.getScoreboard();
            var playerTeam = scoreboard.addPlayerTeam(name);
            scoreboard.addPlayerToTeam(player.getScoreboardName(), playerTeam);
            TinactorySavedData.get().getTeamProfile(playerTeam);
        }

        @Override
        public void leaveTeam(Player player) {
            var scoreboard = ServerUtil.getScoreboard();
            scoreboard.removePlayerFromTeam(player.getScoreboardName());
        }
    }

    @Nullable
    private static Server server = null;

    public static Server server() {
        assert server != null;
        return server;
    }

    public static void init() {
        server = new Server();
        LOGGER.debug("create server tech manager {}", server);
    }
}
