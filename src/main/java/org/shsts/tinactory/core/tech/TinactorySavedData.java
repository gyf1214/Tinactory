package org.shsts.tinactory.core.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TinactorySavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NAME = "tinactory_saved_data";
    private int nextId = 0;
    private final Map<String, TeamProfile> teams = new HashMap<>();

    private TinactorySavedData() {}

    public int nextId() {
        return nextId;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        var teamsTag = new ListTag();
        teams.values().stream()
            .map(TeamProfile::serializeNBT)
            .forEach(teamsTag::add);
        tag.put("teams", teamsTag);
        tag.putInt("nextId", nextId);
        return tag;
    }

    private void load(CompoundTag tag) {
        teams.clear();
        tag.getList("teams", Tag.TAG_COMPOUND).stream()
            .flatMap(tag1 -> TeamProfile.fromTag(tag1).stream())
            .forEach(team -> teams.put(team.getName(), team));
        nextId = tag.getInt("nextId");
    }

    public TeamProfile getTeamProfile(PlayerTeam playerTeam) {
        if (!teams.containsKey(playerTeam.getName())) {
            teams.put(playerTeam.getName(), TeamProfile.create(playerTeam));
            nextId++;
            setDirty();
        }
        return teams.get(playerTeam.getName());
    }

    public void removeTeamProfile(String name) {
        teams.remove(name);
        setDirty();
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

    @Nullable
    private static TinactorySavedData data = null;

    public static void load(ServerLevel overworld) {
        data = overworld.getDataStorage()
            .computeIfAbsent(TinactorySavedData::fromTag, TinactorySavedData::new, NAME);
        LOGGER.debug("load server saved data {}", data);
    }

    public static void unload() {
        data = null;
        LOGGER.debug("unload server saved data");
    }

    /**
     * Must be called on Server!!
     */
    public static TinactorySavedData get() {
        assert data != null;
        return data;
    }
}
