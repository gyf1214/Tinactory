package org.shsts.tinactory.compat.ftbfilter;

import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.shsts.tinactory.integration.logistics.StackHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ItemFilterIntegration {
    @FunctionalInterface
    private interface Matcher {
        boolean matches(ItemStack filter, ItemStack stack, HolderLookup.Provider provider);
    }

    private static final Matcher MATCHER;

    static {
        if (ModList.get().isLoaded("ftbfiltersystem")) {
            MATCHER = FTBFilterSystemAPI.api()::doesFilterMatch;
        } else {
            MATCHER = (filter, stack, provider) -> StackHelper.canItemsStack(filter, stack);
        }
    }

    public static boolean matches(ItemStack filter, ItemStack stack, HolderLookup.Provider provider) {
        return MATCHER.matches(filter, stack, provider);
    }

    public static void init() {}
}
