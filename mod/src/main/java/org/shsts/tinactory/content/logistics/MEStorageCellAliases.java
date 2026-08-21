package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEStorageCellAliases {
    private static final String[] FAMILIES = {"component/storage_component", "logistics/item_storage_cell",
        "logistics/fluid_storage_cell", "logistics/pattern_cell"};
    private static final String[] LEGACY_NAMES = {"1m", "4m", "16m", "64m"};

    private MEStorageCellAliases() {}

    public static void registerAliases(RegisterEvent event) {
        if (event.getRegistryKey() != Registries.ITEM) {
            return;
        }
        var registry = event.getRegistry(Registries.ITEM);
        for (var family : FAMILIES) {
            for (var index = 0; index < LEGACY_NAMES.length; index++) {
                registry.addAlias(id(family + "/" + LEGACY_NAMES[index]), id(family + "/tier_" + (index + 1)));
            }
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("tinactory", path);
    }
}
