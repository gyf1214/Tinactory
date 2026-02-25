package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftRequestableKey(CraftKey.Type type, String id, String nbt) {
    public static AutocraftRequestableKey fromCraftKey(CraftKey key) {
        return new AutocraftRequestableKey(key.type(), key.id(), key.nbt());
    }

    public CraftKey toCraftKey() {
        if (type == CraftKey.Type.FLUID) {
            return CraftKey.fluid(id, nbt);
        }
        return CraftKey.item(id, nbt);
    }
}
