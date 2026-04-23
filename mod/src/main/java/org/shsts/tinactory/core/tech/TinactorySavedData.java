package org.shsts.tinactory.core.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.shsts.tinactory.api.tech.ITechManager;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TinactorySavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int nextId = 0;
    private final ITechManager techManager;
    private final TeamProfile.IUpdateHandler updateHandler;
    private final Map<String, TeamProfile> teams = new HashMap<>();

    public TinactorySavedData(ITechManager techManager) {
        this(techManager, (profile, packet) -> {});
    }

    public TinactorySavedData(ITechManager techManager, TeamProfile.IUpdateHandler updateHandler) {
        this.techManager = techManager;
        this.updateHandler = updateHandler;
    }

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
        for (var rawTag : tag.getList("teams", Tag.TAG_COMPOUND)) {
            var teamTag = (CompoundTag) rawTag;
            var team = new TeamProfile(techManager, teamTag.getString("name"), updateHandler);
            team.deserializeNBT(teamTag);
            teams.put(team.getName(), team);
        }
        nextId = tag.getInt("nextId");
    }

    public TeamProfile getTeamProfile(String name) {
        if (!teams.containsKey(name)) {
            teams.put(name, new TeamProfile(techManager, name, updateHandler));
            nextId++;
            setDirty();
        }
        return teams.get(name);
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

    public static TinactorySavedData fromTag(CompoundTag tag, ITechManager techManager) {
        return fromTag(tag, techManager, (profile, packet) -> {});
    }

    public static TinactorySavedData fromTag(CompoundTag tag, ITechManager techManager,
        TeamProfile.IUpdateHandler updateHandler) {

        var data = new TinactorySavedData(techManager, updateHandler);
        data.load(tag);
        return data;
    }
}
