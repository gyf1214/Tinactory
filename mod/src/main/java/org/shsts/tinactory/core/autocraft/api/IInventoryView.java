package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IInventoryView {
    long amountOf(CraftKey key);

    boolean consume(CraftKey key, long amount);

    void produce(CraftKey key, long amount);
}
