package org.shsts.tinactory.api.recipe;

import org.shsts.tinactory.api.logistics.PortType;

public interface IProcessingObject {
    String codecName();

    PortType type();
}
