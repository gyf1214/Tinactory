package org.shsts.tinactory.api.recipe;

import org.shsts.tinactory.api.logistics.IPort;

public interface IProcessingIngredient {
    boolean consumePort(IPort port, boolean simulate);
}
