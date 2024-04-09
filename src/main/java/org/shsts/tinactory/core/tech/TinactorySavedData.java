package org.shsts.tinactory.core.tech;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.PlayerTeam;
import org.shsts.tinactory.core.util.ServerUtil;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TinactorySavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NAME = "tinactory_saved_data";

    private final Map<PlayerTeam, TeamProfile> teams = new HashMap<>();

    private TinactorySavedData() {}

    @Override
    public CompoundTag save(CompoundTag tag) {
        var teamsTag = new ListTag();
        teams.values().stream()
                .map(TeamProfile::serializeNBT)
                .forEach(teamsTag::add);
        tag.put("teams", teamsTag);
        return tag;
    }

    private void load(CompoundTag tag) {
        teams.clear();
        tag.getList("teams", Tag.TAG_COMPOUND).stream()
                .flatMap(tag1 -> TeamProfile.fromTag(tag1).stream())
                .forEach(team -> teams.put(team.getPlayerTeam(), team));
    }

    public TeamProfile getTeamProfile(PlayerTeam playerTeam) {
        if (!teams.containsKey(playerTeam)) {
            teams.put(playerTeam, TeamProfile.create(playerTeam));
            setDirty();
        }
        return teams.get(playerTeam);
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
        var overworld = ServerUtil.getServer().getLevel(Level.OVERWORLD);
        assert overworld != null;
        return overworld.getDataStorage()
                .computeIfAbsent(TinactorySavedData::fromTag, TinactorySavedData::new, NAME);
    }
}