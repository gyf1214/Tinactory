package org.shsts.tinactory.core.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.shsts.tinactory.content.AllCapabilities.UPDATE_HELPER;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class UpdateHelper extends CapabilityProvider {
    public static final ResourceLocation LOC = modLoc("update_helper");

    private final Map<ResourceLocation, INBTUpdatable<?>> updatableCapability = new HashMap<>();
    private final Set<ResourceLocation> dirtyCapabilities = new HashSet<>();

    public Tag getUpdateTag(boolean forceUpdate) {
        var listTag = new ListTag();

        for (var entry : updatableCapability.entrySet()) {
            var loc = entry.getKey();
            if (!forceUpdate && !dirtyCapabilities.contains(loc)) {
                continue;
            }
            var cap = entry.getValue();
            var tag1 = new CompoundTag();
            tag1.putString("id", loc.toString());
            tag1.put("data", cap.serializeOnUpdate());
            listTag.add(tag1);
        }
        return listTag;
    }

    public void handleUpdateTag(ListTag tag) {
        for (var subTag : tag) {
            var tag1 = (CompoundTag) subTag;
            var loc = new ResourceLocation(tag1.getString("id"));
            var cap = updatableCapability.get(loc);
            cap.deserializeTagOnUpdate(tag1.get("data"));
        }
    }

    public boolean shouldSendUpdate() {
        for (var entry : updatableCapability.entrySet()) {
            if (entry.getValue().shouldSendUpdate()) {
                dirtyCapabilities.add(entry.getKey());
            }
        }
        return !dirtyCapabilities.isEmpty();
    }

    public void resetShouldSendUpdate() {
        dirtyCapabilities.clear();
    }

    public void attachCapability(ResourceLocation loc, ICapabilityProvider provider) {
        if (provider instanceof INBTUpdatable<?> updatable) {
            updatableCapability.put(loc, updatable);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == UPDATE_HELPER.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }
}
