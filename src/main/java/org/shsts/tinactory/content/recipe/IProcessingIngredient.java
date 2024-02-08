package org.shsts.tinactory.content.recipe;

import org.shsts.tinactory.content.logistics.IPort;

public interface IProcessingIngredient {
    boolean consumePort(IPort port, boolean simulate);
}
