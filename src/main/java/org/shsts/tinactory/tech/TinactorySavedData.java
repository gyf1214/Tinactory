package org.shsts.tinactory.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TinactorySavedData extends SavedData {
    private static final String NAME = "tinactory_saved_data";

    private final Map<UUID, TeamProfile> teams = new HashMap<>();
    private final Map<UUID, TeamProfile> playerTeams = new HashMap<>();

    private TinactorySavedData() {}

    @Override
    public CompoundTag save(CompoundTag tag) {
        var teams = new ListTag();
        this.teams.values().stream()
                .map(TeamProfile::serializeNBT)
                .forEach(teams::add);
        tag.put("teams", teams);
        var playerTeams = new ListTag();
        this.playerTeams.entrySet().stream()
                .map(entry -> {
                    var tag1 = new CompoundTag();
                    tag1.putUUID("player", entry.getKey());
                    tag1.putUUID("team", entry.getValue().uuid);
                    return tag1;
                }).forEach(playerTeams::add);
        tag.put("playerTeams", playerTeams);
        return tag;
    }

    private void load(CompoundTag tag) {
        this.teams.clear();
        tag.getList("teams", Tag.TAG_COMPOUND).stream()
                .map(TeamProfile::fromTag)
                .forEach(team -> this.teams.put(team.uuid, team));
        this.playerTeams.clear();
        tag.getList("playerTeams", Tag.TAG_COMPOUND)
                .forEach(tag1 -> {
                    var compoundTag = (CompoundTag) tag1;
                    var playerId = compoundTag.getUUID("player");
                    var team = this.teams.get(compoundTag.getUUID("team"));
                    if (team != null) {
                        this.playerTeams.put(playerId, team);
                        team.addPlayer(playerId);
                    }
                });
    }

    public void invalidatePlayer(UUID playerId) {
        var team = this.playerTeams.get(playerId);
        if (team != null) {
            team.removePlayer(playerId);
        }
        this.playerTeams.remove(playerId);
    }

    private static TinactorySavedData create() {
        return new TinactorySavedData();
    }

    private static TinactorySavedData fromTag(CompoundTag tag) {
        var data = create();
        data.load(tag);
        return data;
    }

    public static TinactorySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TinactorySavedData::fromTag, TinactorySavedData::create, NAME);
    }
}
