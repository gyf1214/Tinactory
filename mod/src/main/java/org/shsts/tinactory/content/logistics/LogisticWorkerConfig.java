package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.shsts.tinactory.core.util.CodecHelper;

import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerConfig implements INBTSerializable<CompoundTag> {
    public static final String PREFIX = "workerConfig_";

    private boolean valid = false;
    @Nullable
    private LogisticComponent.PortKey from = null;
    @Nullable
    private LogisticComponent.PortKey to = null;
    private FilterEntry filter = FilterEntry.EMPTY;

    public boolean isValid() {
        return valid;
    }

    public Optional<LogisticComponent.PortKey> from() {
        return Optional.ofNullable(from);
    }

    public Optional<LogisticComponent.PortKey> to() {
        return Optional.ofNullable(to);
    }

    public FilterEntry.Type filterType() {
        return filter.type();
    }

    public FilterEntry filter() {
        return filter;
    }

    public void setValid(boolean val) {
        valid = val;
    }

    public void setFrom(UUID machineId, int portIndex) {
        from = new LogisticComponent.PortKey(machineId, portIndex);
    }

    public void resetFrom() {
        from = null;
    }

    public void setTo(UUID machineId, int portIndex) {
        to = new LogisticComponent.PortKey(machineId, portIndex);
    }

    public void resetTo() {
        to = null;
    }

    public void setFilter(FilterEntry val) {
        filter = val;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putBoolean("valid", valid);
        if (from != null) {
            tag.putUUID("fromMachine", from.machineId());
            tag.putInt("fromPortIndex", from.portIndex());
        }
        if (to != null) {
            tag.putUUID("toMachine", to.machineId());
            tag.putInt("toPortIndex", to.portIndex());
        }
        var filterTag = CodecHelper.encodeTag(provider, FilterEntry.CODEC, filter);
        tag.merge((CompoundTag) filterTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        valid = tag.getBoolean("valid");
        if (tag.contains("fromMachine", Tag.TAG_INT_ARRAY) && tag.contains("fromPortIndex", Tag.TAG_INT)) {
            from = new LogisticComponent.PortKey(tag.getUUID("fromMachine"), tag.getInt("fromPortIndex"));
        } else {
            from = null;
        }
        if (tag.contains("toMachine", Tag.TAG_INT_ARRAY) && tag.contains("toPortIndex", Tag.TAG_INT)) {
            to = new LogisticComponent.PortKey(tag.getUUID("toMachine"), tag.getInt("toPortIndex"));
        } else {
            to = null;
        }
        filter = CodecHelper.parseTag(provider, FilterEntry.CODEC, tag);
    }

    public static LogisticWorkerConfig fromTag(HolderLookup.Provider provider, CompoundTag tag) {
        var ret = new LogisticWorkerConfig();
        ret.deserializeNBT(provider, tag);
        return ret;
    }
}
