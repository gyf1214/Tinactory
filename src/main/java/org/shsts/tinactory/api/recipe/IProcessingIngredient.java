package org.shsts.tinactory.api.recipe;

import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;

public interface IProcessingIngredient {
    PortType type();

    boolean consumePort(IPort port, boolean simulate);
}
