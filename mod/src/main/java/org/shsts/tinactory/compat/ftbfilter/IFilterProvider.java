package org.shsts.tinactory.compat.ftbfilter;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IFilterProvider {
    boolean isFilter(ItemStack stack);

    boolean matches(ItemStack filter, ItemStack stack, HolderLookup.Provider provider);
}
