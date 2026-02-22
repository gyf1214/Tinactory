package org.shsts.tinactory.core.autocraft.model;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftKey(Type type, String id, String nbt) {
    public CraftKey {
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        nbt = nbt == null ? "" : nbt;
    }

    public static CraftKey item(String id, String nbt) {
        return new CraftKey(Type.ITEM, id, nbt);
    }

    public static CraftKey fluid(String id, String nbt) {
        return new CraftKey(Type.FLUID, id, nbt);
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public enum Type {
        ITEM,
        FLUID
    }
}
