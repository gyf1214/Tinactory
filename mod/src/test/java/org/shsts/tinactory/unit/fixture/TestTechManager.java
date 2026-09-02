package org.shsts.tinactory.unit.fixture;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.tech.IServerTeamProfile;
import org.shsts.tinactory.api.tech.IServerTechManager;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class TestTechManager implements IServerTechManager {
    private final Map<ResourceLocation, Technology> technologies = new LinkedHashMap<>();
    private final Map<ITechnology, ResourceLocation> keys = new LinkedHashMap<>();

    public TestTechManager(Technology... technologies) {
        for (var i = 0; i < technologies.length; i++) {
            var technology = technologies[i];
            var loc = TestTechnologyHelper.loc("tinactory:test_" + i);
            this.technologies.put(loc, technology);
            this.keys.put(technology, loc);
        }
        this.technologies.values().forEach(technology -> technology.resolve(this));
    }

    public TestTechManager(Map<ResourceLocation, Technology> technologies) {
        this.technologies.putAll(technologies);
        for (var entry : technologies.entrySet()) {
            keys.put(entry.getValue(), entry.getKey());
        }
        this.technologies.values().forEach(technology -> technology.resolve(this));
    }

    @Override
    public Optional<Technology> techByKey(ResourceLocation loc) {
        return Optional.ofNullable(technologies.get(loc));
    }

    @Override
    public Optional<ResourceLocation> key(ITechnology technology) {
        return Optional.ofNullable(keys.get(technology));
    }

    @Override
    public Collection<Technology> allTechs() {
        return technologies.values();
    }

    @Override
    public void onProgressChange(Consumer<ITeamProfile> callback) {}

    @Override
    public void removeProgressChangeListener(Consumer<ITeamProfile> callback) {}

    @Override
    public void broadcastUpdate(ITeamProfile team, IPacket packet) {}

    @Override
    public Optional<? extends IServerTeamProfile> teamByPlayer(Player player) {
        return Optional.empty();
    }

    @Override
    public Optional<? extends IServerTeamProfile> teamByName(String name) {
        return Optional.empty();
    }

    @Override
    public Collection<ServerPlayer> onlineMembers(String profileId) {
        return List.of();
    }

    @Override
    public void syncTeam(ServerPlayer player) {}

    @Override
    public Optional<Component> teamDisplayName(String name) {
        return Optional.of(I18n.raw(name));
    }
}
