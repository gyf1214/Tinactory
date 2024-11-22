package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

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

    public boolean isValid() {
        return valid;
    }

    public Optional<LogisticComponent.PortKey> from() {
        return Optional.ofNullable(from);
    }

    public Optional<LogisticComponent.PortKey> to() {
        return Optional.ofNullable(to);
    }

    public void setFrom(UUID machineId, int portIndex) {
        from = new LogisticComponent.PortKey(machineId, portIndex);
    }

    public void setTo(UUID machineId, int portIndex) {
        to = new LogisticComponent.PortKey(machineId, portIndex);
    }

    public void setValid(boolean val) {
        valid = val;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (from != null) {
            tag.putUUID("fromMachine", from.machineId());
            tag.putInt("fromPortIndex", from.portIndex());
        }
        if (to != null) {
            tag.putUUID("toMachine", to.machineId());
            tag.putInt("toPortIndex", to.portIndex());
        }
        tag.putBoolean("valid", valid);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
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
        valid = tag.getBoolean("valid");
    }

    public static LogisticWorkerConfig fromTag(CompoundTag tag) {
        var ret = new LogisticWorkerConfig();
        ret.valid = true;
        ret.deserializeNBT(tag);
        return ret;
    }
}
