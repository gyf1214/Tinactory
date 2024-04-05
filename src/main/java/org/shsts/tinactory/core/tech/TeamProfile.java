package org.shsts.tinactory.core.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TeamProfile implements INBTSerializable<CompoundTag> {
    public final UUID uuid;
    public final String name;
    /**
     * Player team association is managed by SavedData directly.
     */
    public final Set<UUID> players = new HashSet<>();
    private final Map<Technology, Long> technologies = new HashMap<>();


    private TeamProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public void advanceTechProgress(Technology tech, long progress) {
        technologies.merge(tech, progress, ($, v) -> v + progress);
        TinactorySavedData.get().setDirty();
    }

    public long getTechProgress(Technology tech) {
        return technologies.getOrDefault(tech, 0L);
    }

    public boolean isTechFinished(Technology tech) {
        return getTechProgress(tech) >= tech.maxProgress;
    }

    public boolean isTechAvailable(Technology tech) {
        return getTechProgress(tech) > 0 || tech.depends.stream().allMatch(this::isTechFinished);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putUUID("id", uuid);
        tag.putString("name", name);
        var listTag = new ListTag();
        for (var tech : technologies.entrySet()) {
            var loc = tech.getKey().getRegistryName();
            var tag1 = new CompoundTag();
            assert loc != null;
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
            var loc = tag2.getString("id");
            var progress = tag2.getLong("progress");
            TechManager.techByKey(new ResourceLocation(loc))
                    .ifPresent(tech -> technologies.put(tech, progress));
        }
    }

    public static TeamProfile create(String name) {
        return new TeamProfile(UUID.randomUUID(), name);
    }

    public static TeamProfile fromTag(Tag tag) {
        var compoundTag = (CompoundTag) tag;
        var uuid = compoundTag.getUUID("id");
        var name = compoundTag.getString("name");
        var profile = new TeamProfile(uuid, name);
        profile.deserializeNBT(compoundTag);
        return profile;
    }

    @Override
    public String toString() {
        return "TeamProfile{%s, uuid=%s}".formatted(name, uuid);
    }
}
