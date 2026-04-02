package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.logistics.IIngredientKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IInventoryView {
    long amountOf(IIngredientKey key);

    long extract(IIngredientKey key, long amount, boolean simulate);

    long insert(IIngredientKey key, long amount, boolean simulate);
}
