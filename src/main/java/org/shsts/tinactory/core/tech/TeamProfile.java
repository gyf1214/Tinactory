package org.shsts.tinactory.core.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.tech.IServerTeamProfile;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.util.ServerUtil;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.shsts.tinactory.Tinactory.CHANNEL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TeamProfile implements INBTSerializable<CompoundTag>, IServerTeamProfile {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final TechManager techManager;
    protected final PlayerTeam playerTeam;
    protected final Map<ResourceLocation, Long> technologies = new HashMap<>();
    protected final Map<String, Integer> modifiers = new HashMap<>();
    @Nullable
    protected ITechnology targetTech = null;

    protected TeamProfile(TechManager techManager, PlayerTeam playerTeam) {
        this.techManager = techManager;
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

    @Override
    public void advanceTechProgress(ResourceLocation loc, long progress) {
        techManager.techByKey(loc).ifPresent(tech -> advanceTechProgress(tech, progress));
    }

    private void broadcastUpdate(TechUpdatePacket packet) {
        TinactorySavedData.get().setDirty();
        techManager.invokeChange(this);
        var playerList = ServerUtil.getPlayerList();
        for (var name : playerTeam.getPlayers()) {
            var player = playerList.getPlayerByName(name);
            if (player == null) {
                continue;
            }
            CHANNEL.sendToPlayer(player, packet);
        }
    }

    public void onTechComplete(ITechnology tech) {
        for (var modifier : tech.getModifiers().entrySet()) {
            var key = modifier.getKey();
            var val = modifier.getValue();
            modifiers.merge(key, val, Integer::sum);
            LOGGER.debug("{} set modifier {} = {}", this, key, val);
        }
    }

    /**
     * Can only be called on server
     */
    public void setTechProgress(ITechnology tech, long progress) {
        var oldProgress = technologies.getOrDefault(tech.getLoc(), 0L);
        var maxProgress = tech.getMaxProgress();
        progress = Math.min(progress, maxProgress);

        technologies.put(tech.getLoc(), progress);
        if (oldProgress < maxProgress && progress >= maxProgress) {
            onTechComplete(tech);
        }

        broadcastUpdate(TechUpdatePacket.progress(tech, progress));
    }

    @Override
    public long getTechProgress(ResourceLocation tech) {
        return technologies.getOrDefault(tech, 0L);
    }

    @Override
    public boolean isTechFinished(ResourceLocation tech) {
        return techManager.techByKey(tech).map(this::isTechFinished).orElse(false);
    }

    @Override
    public boolean isTechAvailable(ResourceLocation tech) {
        return techManager.techByKey(tech).map(this::isTechAvailable).orElse(false);
    }

    @Override
    public Optional<ITechnology> getTargetTech() {
        return Optional.ofNullable(targetTech);
    }

    /**
     * Can only be called on server.
     */
    @Override
    public void setTargetTech(ITechnology tech) {
        targetTech = tech;
        broadcastUpdate(TechUpdatePacket.target(tech));
    }

    @Override
    public void resetTargetTech() {
        targetTech = null;
        broadcastUpdate(TechUpdatePacket.target(null));
    }

    @Override
    public int getModifier(String key) {
        return modifiers.getOrDefault(key, 0);
    }

    public TechUpdatePacket fullUpdatePacket() {
        return TechUpdatePacket.full(technologies, targetTech);
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
        if (targetTech != null) {
            tag.putString("target", targetTech.getLoc().toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        var listTag = tag.getList("tech", Tag.TAG_COMPOUND);
        for (var tag1 : listTag) {
            var tag2 = (CompoundTag) tag1;
            var loc = new ResourceLocation(tag2.getString("id"));
            var progress = tag2.getLong("progress");
            techManager.techByKey(loc).ifPresent(tech -> {
                technologies.put(loc, progress);
                if (progress >= tech.maxProgress) {
                    onTechComplete(tech);
                }
            });
        }
        if (tag.contains("target", Tag.TAG_STRING)) {
            var loc = new ResourceLocation(tag.getString("target"));
            targetTech = techManager.techByKey(loc).orElse(null);
        }
    }

    public static TeamProfile create(PlayerTeam team) {
        return new TeamProfile(TechManager.server(), team);
    }

    public static Optional<TeamProfile> fromTag(Tag tag) {
        var compoundTag = (CompoundTag) tag;

        var name = compoundTag.getString("name");
        var playerTeam = ServerUtil.getScoreboard().getPlayerTeam(name);
        if (playerTeam == null) {
            return Optional.empty();
        }
        var ret = new TeamProfile(TechManager.server(), playerTeam);
        ret.deserializeNBT(compoundTag);
        return Optional.of(ret);
    }

    @Override
    public String toString() {
        return "%s{%s}".formatted(getClass().getSimpleName(), playerTeam.getName());
    }
}
