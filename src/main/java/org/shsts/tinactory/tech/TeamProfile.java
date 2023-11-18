package org.shsts.tinactory.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TeamProfile implements INBTSerializable<CompoundTag> {
    public final UUID uuid;
    public final String name;
    public final Set<UUID> players = new HashSet<>();

    public TeamProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public void addPlayer(UUID uuid) {
        this.players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        this.players.remove(uuid);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putUUID("id", this.uuid);
        tag.putString("name", this.name);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        // TODO
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
}
