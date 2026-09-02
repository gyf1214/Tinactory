package org.shsts.tinactory.core.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.shsts.tinactory.api.tech.IServerTechManager;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TinactorySavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final IServerTechManager techManager;
    private final Map<String, TeamProfile> teams = new HashMap<>();

    public TinactorySavedData(IServerTechManager techManager) {
        this.techManager = techManager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        var teamsTag = new ListTag();
        teams.values().stream()
            .map($ -> $.serializeNBT(provider))
            .forEach(teamsTag::add);
        tag.put("teams", teamsTag);
        return tag;
    }

    private void load(CompoundTag tag, HolderLookup.Provider provider) {
        teams.clear();
        for (var rawTag : tag.getList("teams", Tag.TAG_COMPOUND)) {
            var teamTag = (CompoundTag) rawTag;
            var name = teamTag.getString("name");
            var team = new TeamProfile(techManager, name);
            team.deserializeNBT(provider, teamTag);
            teams.put(team.getName(), team);
        }
    }

    public TeamProfile getTeamProfile(String name) {
        if (!teams.containsKey(name)) {
            teams.put(name, new TeamProfile(techManager, name));
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

    public static TinactorySavedData fromTag(CompoundTag tag, HolderLookup.Provider provider,
        IServerTechManager techManager) {
        var data = new TinactorySavedData(techManager);
        data.load(tag, provider);
        return data;
    }
}
