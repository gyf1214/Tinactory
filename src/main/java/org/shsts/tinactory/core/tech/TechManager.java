package org.shsts.tinactory.core.tech;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.tech.IClientTechManager;
import org.shsts.tinactory.api.tech.IServerTechManager;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.core.util.ClientUtil;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechManager implements ITechManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Map<ResourceLocation, Technology> technologies = new HashMap<>();
    private final Set<Consumer<ITeamProfile>> changeCallbacks = new HashSet<>();

    protected TechManager() {}

    @Override
    public Optional<Technology> techByKey(ResourceLocation loc) {
        return Optional.ofNullable(technologies.get(loc));
    }

    @Override
    public Collection<Technology> allTechs() {
        return technologies.values();
    }

    public void unload() {
        LOGGER.debug("unload tech manager {}", this);
        technologies.clear();
    }

    @Override
    public void onProgressChange(Consumer<ITeamProfile> callback) {
        changeCallbacks.add(callback);
    }

    @Override
    public void removeProgressChangeListener(Consumer<ITeamProfile> callback) {
        changeCallbacks.remove(callback);
    }

    protected void invokeChange(ITeamProfile teamProfile) {
        for (var cb : changeCallbacks) {
            cb.accept(teamProfile);
        }
    }

    public static class Server extends TechManager implements IServerTechManager {
        private static final Logger LOGGER = LogUtils.getLogger();

        private class ReloadListener implements PreparableReloadListener {
            private static final String PREFIX = "technologies";
            private static final String SUFFIX = ".json";

            private final Gson gson = new Gson();

            private static Collection<ResourceLocation> listResources(ResourceManager manager) {
                return manager.listResources(PREFIX, f -> f.endsWith(SUFFIX));
            }

            private Optional<Technology> loadResource(ResourceManager manager, ResourceLocation loc) {
                var path = loc.getPath();
                var path1 = path.substring(PREFIX.length() + 1, path.length() - SUFFIX.length());
                var loc1 = new ResourceLocation(loc.getNamespace(), path1);
                try (var resource = manager.getResource(loc)) {
                    var is = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                    var reader = new BufferedReader(is);
                    var jo = GsonHelper.fromJson(gson, reader, JsonObject.class);
                    var ret = Technology.CODEC.parse(JsonOps.INSTANCE, jo).getOrThrow(false, $ -> {});
                    ret.setLoc(loc1);
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
                            techs.forEach(tech -> technologies.put(tech.getLoc(), tech));
                            LOGGER.debug("reload {} techs", technologies.size());
                            techs.forEach(tech -> tech.resolve(Server.this));
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

        private void sendFullUpdatePacket(ServerPlayer player, TeamProfile team) {
            var p = team.fullUpdatePacket();
            Tinactory.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), p);
        }

        @Override
        public void addPlayerToTeam(ServerPlayer player, ITeamProfile team) {
            ServerUtil.getScoreboard().addPlayerToTeam(player.getScoreboardName(), team.getPlayerTeam());
            sendFullUpdatePacket(player, (TeamProfile) team);
        }

        @Override
        public void newTeam(ServerPlayer player, String name) {
            var scoreboard = ServerUtil.getScoreboard();
            var playerTeam = scoreboard.addPlayerTeam(name);
            scoreboard.addPlayerToTeam(player.getScoreboardName(), playerTeam);
            var team = TinactorySavedData.get().getTeamProfile(playerTeam);
            sendFullUpdatePacket(player, team);
        }

        @Override
        public void leaveTeam(ServerPlayer player) {
            var scoreboard = ServerUtil.getScoreboard();
            scoreboard.removePlayerFromTeam(player.getScoreboardName());
        }

        public void removeTeam(PlayerTeam playerTeam) {
            TinactorySavedData.get().removeTeamProfile(playerTeam.getName());
            var scoreboard = ServerUtil.getScoreboard();
            scoreboard.removePlayerTeam(playerTeam);
        }

        public void onPlayerJoin(ServerPlayer player) {
            var p = new TechInitPacket(technologies.values());
            Tinactory.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), p);
            teamByPlayer(player).ifPresent(profile -> sendFullUpdatePacket(player, profile));
        }
    }

    public static class Client extends TechManager implements IClientTechManager {
        private static final Logger LOGGER = LogUtils.getLogger();

        private class ClientTeamProfile extends TeamProfile {
            public ClientTeamProfile(PlayerTeam playerTeam) {
                super(Client.this, playerTeam);
            }
        }

        @Nullable
        private ClientTeamProfile localTeam = null;

        @Nullable
        private ClientTeamProfile getLocalTeam() {
            var team = (PlayerTeam) ClientUtil.getPlayer().getTeam();
            var curTeam = localTeam == null ? null : localTeam.playerTeam;
            if (team != curTeam) {
                localTeam = team == null ? null : new ClientTeamProfile(team);
                LOGGER.debug("reset local client team to {}", localTeam);
            }
            return localTeam;
        }

        @Override
        public Optional<ITeamProfile> localTeamProfile() {
            return Optional.ofNullable(getLocalTeam());
        }

        @Override
        public void unload() {
            super.unload();
            localTeam = null;
        }

        private void handleTechInit(TechInitPacket p, NetworkEvent.Context ctx) {
            technologies.clear();
            var techs = p.getTechs();
            for (var tech : techs) {
                technologies.put(tech.getLoc(), tech);
            }
            techs.forEach(tech -> tech.resolve(this));
            LOGGER.debug("reload {} techs", technologies.size());
        }

        private void handleTechUpdate(TechUpdatePacket p, NetworkEvent.Context ctx) {
            var team = getLocalTeam();
            if (team == null) {
                return;
            }
            team.technologies.putAll(p.getProgress());
            LOGGER.debug("update {} techs for team {}", p.getProgress().size(), team.getName());
            if (p.isUpdateTarget()) {
                team.targetTech = p.getTargetTech().flatMap(this::techByKey).orElse(null);
                LOGGER.debug("update targetTech = {} for team {}", team.targetTech, team.getName());
            }
            invokeChange(team);
        }
    }

    @Nullable
    private static Server server = null;
    @Nullable
    private static Client client = null;

    public static Server server() {
        assert server != null;
        return server;
    }

    public static Client client() {
        assert client != null;
        return client;
    }

    public static Optional<ITeamProfile> localTeam() {
        return client().localTeamProfile();
    }

    public static void init() {
        server = new Server();
        LOGGER.debug("create server tech manager {}", server);
    }

    public static void initClient() {
        client = new Client();
        LOGGER.debug("create client tech manager {}", client);
        Tinactory.registryClientPacket(TechInitPacket.class, TechInitPacket::new, client::handleTechInit);
        Tinactory.registryClientPacket(TechUpdatePacket.class, TechUpdatePacket::new, client::handleTechUpdate);
    }
}
