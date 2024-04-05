package org.shsts.tinactory.core.tech;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TinactorySavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NAME = "tinactory_saved_data";

    public final Map<UUID, TeamProfile> teams = new HashMap<>();
    public final Map<UUID, TeamProfile> playerTeams = new HashMap<>();

    private TinactorySavedData() {}

    @Override
    public CompoundTag save(CompoundTag tag) {
        var teamsTag = new ListTag();
        teams.values().stream()
                .map(TeamProfile::serializeNBT)
                .forEach(teamsTag::add);
        tag.put("teams", teamsTag);
        var playerTeamsTag = new ListTag();
        playerTeams.entrySet().stream()
                .map(entry -> {
                    var tag1 = new CompoundTag();
                    tag1.putUUID("player", entry.getKey());
                    tag1.putUUID("team", entry.getValue().uuid);
                    return tag1;
                }).forEach(playerTeamsTag::add);
        tag.put("playerTeams", playerTeamsTag);
        return tag;
    }

    private void load(CompoundTag tag) {
        teams.clear();
        tag.getList("teams", Tag.TAG_COMPOUND).stream()
                .map(TeamProfile::fromTag)
                .forEach(team -> teams.put(team.uuid, team));
        playerTeams.clear();
        tag.getList("playerTeams", Tag.TAG_COMPOUND)
                .forEach(tag1 -> {
                    var compoundTag = (CompoundTag) tag1;
                    var playerId = compoundTag.getUUID("player");
                    var team = teams.get(compoundTag.getUUID("team"));
                    if (team != null) {
                        playerTeams.put(playerId, team);
                        team.players.add(playerId);
                    }
                });
    }

    @Override
    public void setDirty() {
        LOGGER.debug("{} set dirty", this);
        super.setDirty();
    }

    private static TinactorySavedData fromTag(CompoundTag tag) {
        var data = new TinactorySavedData();
        data.load(tag);
        return data;
    }

    /**
     * Must be called on Server!!
     */
    public static TinactorySavedData get() {
        var overworld = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
        assert overworld != null;
        return overworld.getDataStorage()
                .computeIfAbsent(TinactorySavedData::fromTag, TinactorySavedData::new, NAME);
    }
}