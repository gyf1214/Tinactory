package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
    @Nullable
    private TagKey<Item> tagFilter = null;
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

    public enum FilterType {
        NONE(PortType.NONE),
        ITEM(PortType.ITEM),
        TAG(PortType.ITEM),
        FLUID(PortType.FLUID);

        public final PortType portType;

        FilterType(PortType portType) {
            this.portType = portType;
        }
    }

    public FilterType filterType() {
        if (tagFilter != null) {
            return FilterType.TAG;
        } else if (!itemFilter.isEmpty()) {
            return FilterType.ITEM;
        } else if (!fluidFilter.isEmpty()) {
            return FilterType.FLUID;
        } else {
            return FilterType.NONE;
        }
    }

    public ItemStack itemFilter() {
        return itemFilter;
    }

    public TagKey<Item> tagFilter() {
        assert tagFilter != null;
        return tagFilter;
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

    public void resetFrom() {
        from = null;
    }

    public void setTo(UUID machineId, int portIndex) {
        to = new LogisticComponent.PortKey(machineId, portIndex);
    }

    public void resetTo() {
        to = null;
    }

    public void setFilter(ItemStack val) {
        tagFilter = null;
        itemFilter = val;
        fluidFilter = FluidStack.EMPTY;
    }

    public void setFilter(TagKey<Item> val) {
        tagFilter = val;
        itemFilter = ItemStack.EMPTY;
        fluidFilter = FluidStack.EMPTY;
    }

    public void setFilter(FluidStack val) {
        tagFilter = null;
        fluidFilter = val;
        itemFilter = ItemStack.EMPTY;
    }

    public void clearFilter() {
        tagFilter = null;
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
        if (tagFilter != null) {
            tag.putString("tagFilter", tagFilter.location().toString());
        } else if (!itemFilter.isEmpty()) {
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
        tagFilter = null;
        itemFilter = ItemStack.EMPTY;
        fluidFilter = FluidStack.EMPTY;
        if (tag.contains("tagFilter", Tag.TAG_STRING)) {
            tagFilter = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tag.getString("tagFilter")));
        } else if (tag.contains("itemFilter", Tag.TAG_COMPOUND)) {
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
