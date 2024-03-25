package org.shsts.tinactory.core.logistics;

import org.shsts.tinactory.api.logistics.PortType;

public enum SlotType {
    NONE(false, PortType.NONE),
    ITEM_INPUT(false, PortType.ITEM),
    ITEM_OUTPUT(true, PortType.ITEM),
    FLUID_INPUT(false, PortType.FLUID),
    FLUID_OUTPUT(true, PortType.FLUID);

    public final boolean output;
    public final PortType portType;

    SlotType(boolean output, PortType portType) {
        this.output = output;
        this.portType = portType;
    }
}
