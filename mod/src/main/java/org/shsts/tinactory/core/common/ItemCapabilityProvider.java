package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Due to capNbt not being serialized to packet, we have to put all cap info in the tag of a itemStack.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemCapabilityProvider extends CapabilityProvider {
    private static final String PARENT_KEY = "caps";

    private final ItemStack stack;
    private final String key;

    protected ItemCapabilityProvider(ItemStack stack, ResourceLocation id) {
        this.stack = stack;
        this.key = id.toString();
    }

    public void init() {
        var tag = stack.getTag();
        if (tag != null && tag.contains(PARENT_KEY, Tag.TAG_COMPOUND)) {
            var tag1 = tag.getCompound(PARENT_KEY);
            if (tag1.contains(key, Tag.TAG_COMPOUND)) {
                deserializeNBT(tag1.getCompound(key));
            }
        }
    }

    protected void syncTag() {
        var tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundTag();
            stack.setTag(tag);
        }
        if (!tag.contains(PARENT_KEY, Tag.TAG_COMPOUND)) {
            var tag1 = new CompoundTag();
            tag.put(PARENT_KEY, tag1);
            tag = tag1;
        } else {
            tag = tag.getCompound(PARENT_KEY);
        }
        var capTag = serializeNBT();
        tag.put(key, capTag);
    }

    protected abstract CompoundTag serializeNBT();

    protected abstract void deserializeNBT(CompoundTag tag);
}
