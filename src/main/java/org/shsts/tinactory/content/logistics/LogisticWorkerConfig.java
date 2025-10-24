package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.StackHelper;

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
    private ItemStack itemFilter = ItemStack.EMPTY;
    private FluidStack fluidFilter = FluidStack.EMPTY;

    public boolean isValid() {
        return valid;
    }

    public Optional<LogisticComponent.PortKey> from() {
        return Optional.ofNullable(from);
    }

    public Optional<LogisticComponent.PortKey> to() {
        return Optional.ofNullable(to);
    }

    public PortType filterType() {
        if (!itemFilter.isEmpty()) {
            return PortType.ITEM;
        } else if (!fluidFilter.isEmpty()) {
            return PortType.FLUID;
        } else {
            return PortType.NONE;
        }
    }

    public ItemStack itemFilter() {
        return itemFilter;
    }

    public FluidStack fluidFilter() {
        return fluidFilter;
    }

    public void setValid(boolean val) {
        valid = val;
    }

    public void setFrom(UUID machineId, int portIndex) {
        from = new LogisticComponent.PortKey(machineId, portIndex);
    }

    public void setTo(UUID machineId, int portIndex) {
        to = new LogisticComponent.PortKey(machineId, portIndex);
    }

    public void setFilter(ItemStack val) {
        itemFilter = val;
        fluidFilter = FluidStack.EMPTY;
    }

    public void setFilter(FluidStack val) {
        fluidFilter = val;
        itemFilter = ItemStack.EMPTY;
    }

    public void clearFilter() {
        itemFilter = ItemStack.EMPTY;
        fluidFilter = FluidStack.EMPTY;
    }

    @Override
    public CompoundTag serializeNBT() {
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
        if (!itemFilter.isEmpty()) {
            tag.put("itemFilter", itemFilter.serializeNBT());
        } else if (!fluidFilter.isEmpty()) {
            tag.put("fluidFilter", StackHelper.serializeFluidStack(fluidFilter));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
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
        itemFilter = ItemStack.EMPTY;
        fluidFilter = FluidStack.EMPTY;
        if (tag.contains("itemFilter", Tag.TAG_COMPOUND)) {
            itemFilter = ItemStack.of(tag.getCompound("itemFilter"));
        } else if (tag.contains("fluidFilter", Tag.TAG_COMPOUND)) {
            fluidFilter = FluidStack.loadFluidStackFromNBT(tag.getCompound("fluidFilter"));
        }
    }

    public static LogisticWorkerConfig fromTag(CompoundTag tag) {
        var ret = new LogisticWorkerConfig();
        ret.valid = true;
        ret.deserializeNBT(tag);
        return ret;
    }
}
