package org.shsts.tinactory.core.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.tech.IServerTeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.api.tech.ITechnology;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TeamProfile implements INBTSerializable<CompoundTag>, IServerTeamProfile {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final ITechManager techManager;
    protected final String name;
    protected final Map<ResourceLocation, Long> technologies = new HashMap<>();
    protected final Map<String, Integer> modifiers = new HashMap<>();
    @Nullable
    protected ITechnology targetTech = null;

    public TeamProfile(ITechManager techManager, String name) {
        this.techManager = techManager;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void advanceTechProgress(ITechnology tech, long progress) {
        var loc = techKey(tech);
        var v = technologies.getOrDefault(loc, 0L) + progress;
        setTechProgress(tech, v);
    }

    @Override
    public void advanceTechProgress(ResourceLocation loc, long progress) {
        techManager.techByKey(loc).ifPresent(tech -> advanceTechProgress(tech, progress));
    }

    private void broadcastUpdate(TechUpdatePacket packet) {
        techManager.broadcastUpdate(this, packet);
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
        var loc = techKey(tech);
        var oldProgress = technologies.getOrDefault(loc, 0L);
        var maxProgress = tech.getMaxProgress();
        progress = Math.min(progress, maxProgress);

        technologies.put(loc, progress);
        if (oldProgress < maxProgress && progress >= maxProgress) {
            onTechComplete(tech);
        }

        broadcastUpdate(TechUpdatePacket.progress(loc, progress));
    }

    public void applyProgressUpdate(ResourceLocation tech, long progress) {
        technologies.put(tech, progress);
    }

    @Override
    public long getTechProgress(ResourceLocation tech) {
        return technologies.getOrDefault(tech, 0L);
    }

    @Override
    public long getTechProgress(ITechnology tech) {
        return getTechProgress(techKey(tech));
    }

    @Override
    public boolean isTechFinished(ResourceLocation tech) {
        return techManager.techByKey(tech).map(this::isTechFinished).orElse(false);
    }

    @Override
    public boolean isTechFinished(ITechnology tech) {
        return getTechProgress(tech) >= tech.getMaxProgress();
    }

    @Override
    public boolean isTechAvailable(ResourceLocation tech) {
        return techManager.techByKey(tech).map(this::isTechAvailable).orElse(false);
    }

    @Override
    public boolean isTechAvailable(ITechnology tech) {
        return getTechProgress(tech) > 0 || tech.getDepends().stream().allMatch(this::isTechFinished);
    }

    @Override
    public boolean canResearch(ResourceLocation tech, long progress) {
        return techManager.techByKey(tech).map($ -> canResearch($, progress)).orElse(false);
    }

    @Override
    public boolean canResearch(ITechnology tech) {
        return isTechAvailable(tech) && !isTechFinished(tech);
    }

    @Override
    public boolean canResearch(ITechnology tech, long progress) {
        return isTechAvailable(tech) && getTechProgress(tech) + progress <= tech.getMaxProgress();
    }

    @Override
    public Optional<ITechnology> getTargetTech() {
        return Optional.ofNullable(targetTech);
    }

    @Override
    public Optional<ResourceLocation> getTargetTechKey() {
        return targetTech == null ? Optional.empty() : Optional.of(techKey(targetTech));
    }

    /**
     * Can only be called on server.
     */
    @Override
    public void setTargetTech(ITechnology tech) {
        targetTech = tech;
        broadcastUpdate(TechUpdatePacket.target(techKey(tech)));
    }

    @Override
    public void resetTargetTech() {
        targetTech = null;
        broadcastUpdate(TechUpdatePacket.target((ResourceLocation) null));
    }

    public void applyTargetTechUpdate(@Nullable ITechnology tech) {
        targetTech = tech;
    }

    @Override
    public int getModifier(String key) {
        return modifiers.getOrDefault(key, 0);
    }

    public TechUpdatePacket fullUpdatePacket() {
        return TechUpdatePacket.full(technologies, targetTech == null ? null : techKey(targetTech));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putString("name", name);
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
            tag.putString("target", techKey(targetTech).toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        technologies.clear();
        modifiers.clear();
        targetTech = null;

        var listTag = tag.getList("tech", Tag.TAG_COMPOUND);
        for (var tag1 : listTag) {
            var tag2 = (CompoundTag) tag1;
            var loc = ResourceLocation.parse(tag2.getString("id"));
            var progress = tag2.getLong("progress");
            techManager.techByKey(loc).ifPresent(tech -> {
                technologies.put(loc, progress);
                if (progress >= tech.getMaxProgress()) {
                    onTechComplete(tech);
                }
            });
        }
        if (tag.contains("target", Tag.TAG_STRING)) {
            var loc = ResourceLocation.parse(tag.getString("target"));
            targetTech = techManager.techByKey(loc).orElse(null);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }

    private ResourceLocation techKey(ITechnology tech) {
        return techManager.key(tech)
            .orElseThrow(() -> new IllegalArgumentException("Unknown technology " + tech));
    }
}
