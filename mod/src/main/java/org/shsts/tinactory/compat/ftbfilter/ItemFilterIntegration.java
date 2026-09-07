package org.shsts.tinactory.compat.ftbfilter;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.shsts.tinactory.integration.logistics.StackHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ItemFilterIntegration {
    private static final IFilterProvider PROVIDER;

    static {
        if (ModList.get().isLoaded("ftbfiltersystem")) {
            PROVIDER = new FtbFilterProvider();
        } else {
            PROVIDER = new IFilterProvider() {
                @Override
                public boolean isFilter(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean matches(ItemStack filter, ItemStack stack, HolderLookup.Provider provider) {
                    return StackHelper.canItemsStack(filter, stack);
                }
            };
        }
    }

    public static boolean matches(ItemStack filter, ItemStack stack, HolderLookup.Provider provider) {
        return PROVIDER.matches(filter, stack, provider);
    }

    public static boolean isFilter(ItemStack stack) {
        return PROVIDER.isFilter(stack);
    }

    public static void init() {}
}
