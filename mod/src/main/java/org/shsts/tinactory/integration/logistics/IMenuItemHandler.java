package org.shsts.tinactory.integration.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * The forge capability system requires that every capability has different types,
 * which requires a new class for the menu handler.
 */
@FunctionalInterface
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMenuItemHandler {
    IItemHandler asItemHandler();
}
