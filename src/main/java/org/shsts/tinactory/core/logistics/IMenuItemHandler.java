package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

/**
 * The forge capability system requires that every capability has different types,
 * which requires a new class for the menu handler.
 */
@FunctionalInterface
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMenuItemHandler {
    IItemHandler asItemHandler();

    static LazyOptional<IMenuItemHandler> cap(IItemHandler handler) {
        IMenuItemHandler ret = () -> handler;
        return LazyOptional.of(() -> ret);
    }
}
