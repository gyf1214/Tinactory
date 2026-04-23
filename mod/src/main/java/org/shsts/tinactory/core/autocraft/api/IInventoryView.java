package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.logistics.IStackKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IInventoryView {
    long amountOf(IStackKey key);

    long extract(IStackKey key, long amount, boolean simulate);

    long insert(IStackKey key, long amount, boolean simulate);
}
