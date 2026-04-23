package org.shsts.tinactory.integration.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.AddReloadListenerEvent;
import org.shsts.tinactory.api.tech.IServerTechManager;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.tech.TechInitPacket;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.TechUpdatePacket;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.util.ServerUtil;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.shsts.tinactory.Tinactory.CHANNEL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ServerTechManager extends TechManager implements IServerTechManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private class ReloadListener implements PreparableReloadListener {
        private static final String PREFIX = "technologies";
        private static final String SUFFIX = ".json";

        private static Collection<ResourceLocation> listResources(ResourceManager manager) {
            return manager.listResources(PREFIX, file -> file.endsWith(SUFFIX));
        }

        private Optional<Technology> loadResource(ResourceManager manager, ResourceLocation loc) {
            var path = loc.getPath();
            var path1 = path.substring(PREFIX.length() + 1, path.length() - SUFFIX.length());
            var loc1 = new ResourceLocation(loc.getNamespace(), path1);
            try (var resource = manager.getResource(loc)) {
                var is = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                var jo = CodecHelper.jsonFromReader(new BufferedReader(is));
                var ret = CodecHelper.parseJson(Technology.CODEC, jo);
                ret.setLoc(loc1);
                return Optional.of(ret);
            } catch (IOException | RuntimeException ex) {
                LOGGER.warn("Decode resource {} failed", loc, ex);
            }
            return Optional.empty();
        }

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
            ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
            Executor backgroundExecutor, Executor gameExecutor) {

            LOGGER.debug("tech manager reload resources");
            return stage.wait(Unit.INSTANCE)
                .thenApplyAsync(unused -> listResources(manager).stream()
                    .flatMap(loc -> loadResource(manager, loc).stream())
                    .toList(), backgroundExecutor)
                .thenAcceptAsync(techs -> {
                    technologies.clear();
                    techs.forEach(tech -> technologies.put(tech.getLoc(), tech));
                    LOGGER.debug("reload {} techs", technologies.size());
                    techs.forEach(tech -> tech.resolve(ServerTechManager.this));
                }, backgroundExecutor);
        }
    }

    private final PreparableReloadListener reloadListener = new ReloadListener();

    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(reloadListener);
    }

    private void broadcastUpdate(TeamProfile team, TechUpdatePacket packet) {
        TechManagers.savedData().setDirty();
        invokeChange(team);
        var playerList = ServerUtil.getPlayerList();
        TechHelper.scoreboardTeam(team.getName()).ifPresent(playerTeam -> {
            for (var playerName : playerTeam.getPlayers()) {
                var player = playerList.getPlayerByName(playerName);
                if (player != null) {
                    CHANNEL.sendToPlayer(player, packet);
                }
            }
        });
    }

    @Override
    public Optional<TeamProfile> teamByPlayer(Player player) {
        return TechHelper.playerTeam(player)
            .map(PlayerTeam::getName)
            .map(name -> TechManagers.savedData().getTeamProfile(name));
    }

    @Override
    public Optional<TeamProfile> teamByName(String name) {
        return TechHelper.scoreboardTeam(name)
            .map(playerTeam -> TechManagers.savedData().getTeamProfile(playerTeam.getName()));
    }

    @Override
    public int nextId() {
        return TechManagers.savedData().nextId();
    }

    private void sendFullUpdatePacket(ServerPlayer player, TeamProfile team) {
        CHANNEL.sendToPlayer(player, team.fullUpdatePacket());
    }

    @Override
    public void addPlayerToTeam(ServerPlayer player, ITeamProfile team) {
        var playerTeam = TechHelper.scoreboardTeam(team.getName()).orElseThrow();
        ServerUtil.getScoreboard().addPlayerToTeam(player.getScoreboardName(), playerTeam);
        sendFullUpdatePacket(player, TechManagers.savedData().getTeamProfile(team.getName()));
    }

    @Override
    public void newTeam(ServerPlayer player, String name) {
        var scoreboard = ServerUtil.getScoreboard();
        var playerTeam = scoreboard.addPlayerTeam(name);
        scoreboard.addPlayerToTeam(player.getScoreboardName(), playerTeam);
        var team = TechManagers.savedData().getTeamProfile(playerTeam.getName());
        sendFullUpdatePacket(player, team);
    }

    @Override
    public void leaveTeam(ServerPlayer player) {
        ServerUtil.getScoreboard().removePlayerFromTeam(player.getScoreboardName());
    }

    public void removeTeam(PlayerTeam playerTeam) {
        TechManagers.savedData().removeTeamProfile(playerTeam.getName());
        ServerUtil.getScoreboard().removePlayerTeam(playerTeam);
    }

    public void syncTeam(ServerPlayer player) {
        teamByPlayer(player).ifPresent(profile -> sendFullUpdatePacket(player, profile));
    }

    public void onPlayerJoin(ServerPlayer player) {
        CHANNEL.sendToPlayer(player, new TechInitPacket(technologies.values()));
        syncTeam(player);
    }

    public TeamProfile.IUpdateHandler updateHandler() {
        return this::broadcastUpdate;
    }
}
