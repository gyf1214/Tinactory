package org.shsts.tinactory.compat.ftbfilter;

import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.integration.logistics.StackHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FtbFilterProvider implements IFilterProvider {
    @Override
    public boolean isFilter(ItemStack stack) {
        return FTBFilterSystemAPI.api().isFilterItem(stack);
    }

    @Override
    public boolean matches(ItemStack filter, ItemStack stack, HolderLookup.Provider provider) {
        var api = FTBFilterSystemAPI.api();
        if (api.isFilterItem(filter)) {
            return api.doesFilterMatch(filter, stack, provider);
        } else {
            return StackHelper.canItemsStack(filter, stack);
        }
    }
}
