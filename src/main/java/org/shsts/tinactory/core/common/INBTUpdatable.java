package org.shsts.tinactory.core.common;

import net.minecraft.nbt.Tag;

public interface INBTUpdatable<T extends Tag> {
    boolean shouldSendUpdate();

    T serializeOnUpdate();

    void deserializeOnUpdate(T tag);

    @SuppressWarnings("unchecked")
    default void deserializeTagOnUpdate(Tag tag) {
        deserializeOnUpdate((T) tag);
    }
}
