package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum SlotType {
    NONE(PortDirection.NONE, PortType.NONE),
    ITEM_INPUT(PortDirection.INPUT, PortType.ITEM),
    ITEM_OUTPUT(PortDirection.OUTPUT, PortType.ITEM),
    FLUID_INPUT(PortDirection.INPUT, PortType.FLUID),
    FLUID_OUTPUT(PortDirection.OUTPUT, PortType.FLUID);

    public final PortDirection direction;
    public final PortType portType;

    SlotType(PortDirection direction, PortType portType) {
        this.direction = direction;
        this.portType = portType;
    }

    public static SlotType fromName(String name) {
        return valueOf(name.toUpperCase());
    }
}
