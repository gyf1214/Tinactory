package org.shsts.tinactory.api.recipe;

import org.shsts.tinactory.api.logistics.IPort;

public interface IProcessingIngredient extends IProcessingObject {
    boolean consumePort(IPort port, boolean simulate);
}
