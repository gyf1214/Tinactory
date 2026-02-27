package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IInventoryView {
    long amountOf(CraftKey key);

    long extract(CraftKey key, long amount, boolean simulate);

    long insert(CraftKey key, long amount, boolean simulate);
}
