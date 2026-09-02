package org.shsts.tinactory.integration.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.shsts.tinactory.api.tech.IServerTechManager;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITeamProvider;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.TechInitPacket;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.TechUpdatePacket;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.network.IPacket;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.integration.tech.TechManagers.TECH_INIT;
import static org.shsts.tinactory.integration.tech.TechManagers.TECH_UPDATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ServerTechManager extends TechManager implements IServerTechManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private class ReloadListener extends ContextAwareReloadListener {
        private static final String PREFIX = "technologies";
        private static final String SUFFIX = ".json";

        private static Map<ResourceLocation, Resource> listResources(ResourceManager manager) {
            return manager.listResources(PREFIX, file -> file.getPath().endsWith(SUFFIX));
        }

        private Optional<Map.Entry<ResourceLocation, Technology>> loadResource(ResourceLocation loc,
            Resource resource) {
            var path = loc.getPath();
            var path1 = path.substring(PREFIX.length() + 1, path.length() - SUFFIX.length());
            var loc1 = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), path1);
            try {
                try (var ir = resource.openAsReader()) {
                    var jo = CodecHelper.jsonFromReader(ir);
                    var ret = CodecHelper.parseJson(getRegistryLookup(), Technology.CODEC, jo);
                    return Optional.of(Map.entry(loc1, ret));
                }
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
                .thenApplyAsync(unused -> listResources(manager).entrySet().stream()
                    .flatMap(entry -> loadResource(entry.getKey(), entry.getValue()).stream())
                    .toList(), backgroundExecutor)
                .thenAcceptAsync(techs -> {
                    unload();
                    techs.forEach(entry -> putTech(entry.getKey(), entry.getValue()));
                    LOGGER.debug("reload {} techs", technologies.size());
                    techs.forEach(entry -> entry.getValue().resolve(ServerTechManager.this));
                }, backgroundExecutor);
        }
    }

    private final PreparableReloadListener reloadListener = new ReloadListener();
    @Nullable
    private ITeamProvider teamProvider;

    public void installTeamProvider(ITeamProvider provider) {
        if (teamProvider != null) {
            throw new IllegalStateException("Team provider is already initialized");
        }
        teamProvider = provider;
    }

    public void uninstallTeamProvider() {
        if (teamProvider != null) {
            teamProvider.unregister();
            teamProvider = null;
        }
    }

    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(reloadListener);
    }

    @Override
    public void broadcastUpdate(ITeamProfile team, IPacket packet) {
        if (!(packet instanceof TechUpdatePacket techUpdatePacket)) {
            return;
        }
        TechManagers.savedData().setDirty();
        invokeChange(team);
        provider().onlineMembers(team.getName())
            .forEach(player -> CORE.sendToPlayer(player, TECH_UPDATE, techUpdatePacket));
    }

    @Override
    public Optional<TeamProfile> teamByPlayer(Player player) {
        return provider().teamIdByPlayer(player)
            .map(name -> TechManagers.savedData().getTeamProfile(name));
    }

    @Override
    public Optional<TeamProfile> teamByName(String name) {
        return provider().teamIdById(name)
            .map(id -> TechManagers.savedData().getTeamProfile(id));
    }

    @Override
    public Collection<ServerPlayer> onlineMembers(String profileId) {
        return provider().teamIdById(profileId)
            .map(provider()::onlineMembers)
            .orElseGet(List::of);
    }

    private void sendFullUpdatePacket(ServerPlayer player, TeamProfile team) {
        CORE.sendToPlayer(player, TECH_UPDATE, team.fullUpdatePacket());
    }

    @Override
    public void syncTeam(ServerPlayer player) {
        teamByPlayer(player).ifPresentOrElse(profile -> sendFullUpdatePacket(player, profile),
            () -> CORE.sendToPlayer(player, TECH_UPDATE, TechUpdatePacket.clear()));
    }

    @Override
    public Component teamDisplayName(String name) {
        return provider().teamDisplayName(name).orElseThrow();
    }

    public void onPlayerJoin(ServerPlayer player) {
        CORE.sendToPlayer(player, TECH_INIT, TechInitPacket.fromMap(technologies));
        syncTeam(player);
    }

    private ITeamProvider provider() {
        if (teamProvider == null) {
            throw new IllegalStateException("Team provider has not been initialized");
        }
        return teamProvider;
    }
}
