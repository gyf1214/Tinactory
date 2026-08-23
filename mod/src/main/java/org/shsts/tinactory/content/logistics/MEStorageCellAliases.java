package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEStorageCellAliases {
    private static final String[] FAMILIES = {
        "component/storage_component",
        "logistics/item_storage_cell",
        "logistics/fluid_storage_cell",
        "logistics/pattern_cell"
    };
    private static final String[] LEGACY_NAMES = {"1m", "4m", "16m", "64m"};

    private MEStorageCellAliases() {}

    public static void registerAliases(RegisterEvent event) {
        var registry = event.getRegistry(Registries.ITEM);
        if (registry == null) {
            return;
        }
        for (var family : FAMILIES) {
            for (var index = 0; index < LEGACY_NAMES.length; index++) {
                registry.addAlias(modLoc(family + "/" + LEGACY_NAMES[index]),
                    modLoc(family + "/tier_" + (index + 1)));
            }
        }
    }
}
