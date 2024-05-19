package org.shsts.tinactory.core.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.tech.IServerTeamProfile;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.util.ServerUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TeamProfile implements INBTSerializable<CompoundTag>, IServerTeamProfile {
    protected final PlayerTeam playerTeam;
    protected final Map<ResourceLocation, Long> technologies = new HashMap<>();

    protected TeamProfile(PlayerTeam playerTeam) {
        this.playerTeam = playerTeam;
    }

    @Override
    public PlayerTeam getPlayerTeam() {
        return playerTeam;
    }

    @Override
    public void advanceTechProgress(ITechnology tech, long progress) {
        var v = technologies.getOrDefault(tech.getLoc(), 0L) + progress;
        setTechProgress(tech, v);
    }

    public void setTechProgress(ITechnology tech, long progress) {
        technologies.put(tech.getLoc(), progress);
        TinactorySavedData.get().setDirty();

        var playerList = ServerUtil.getPlayerList();
        var packet = new TechUpdatePacket(Map.of(tech.getLoc(), progress));
        for (var name : playerTeam.getPlayers()) {
            var player = playerList.getPlayerByName(name);
            if (player == null) {
                continue;
            }
            Tinactory.sendToPlayer(player, packet);
        }
    }

    @Override
    public long getTechProgress(ITechnology tech) {
        return technologies.getOrDefault(tech.getLoc(), 0L);
    }

    public TechUpdatePacket updatePacket() {
        return new TechUpdatePacket(technologies);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString("name", playerTeam.getName());
        var listTag = new ListTag();
        for (var tech : technologies.entrySet()) {
            var loc = tech.getKey();
            assert loc != null;
            var tag1 = new CompoundTag();
            tag1.putString("id", loc.toString());
            tag1.putLong("progress", tech.getValue());
            listTag.add(tag1);
        }
        tag.put("tech", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        var listTag = tag.getList("tech", Tag.TAG_COMPOUND);
        for (var tag1 : listTag) {
            var tag2 = (CompoundTag) tag1;
            var loc = new ResourceLocation(tag2.getString("id"));
            var progress = tag2.getLong("progress");
            if (TechManager.server().techByKey(loc).isPresent()) {
                technologies.put(loc, progress);
            }
        }
    }

    public static TeamProfile create(PlayerTeam team) {
        return new TeamProfile(team);
    }

    public static Optional<TeamProfile> fromTag(Tag tag) {
        var compoundTag = (CompoundTag) tag;

        var name = compoundTag.getString("name");
        var playerTeam = ServerUtil.getScoreboard().getPlayerTeam(name);
        if (playerTeam == null) {
            return Optional.empty();
        }
        var ret = new TeamProfile(playerTeam);
        ret.deserializeNBT(compoundTag);
        return Optional.of(ret);
    }

    @Override
    public String toString() {
        return "TeamProfile{%s}".formatted(playerTeam.getName());
    }
}
