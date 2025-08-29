package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum CircuitLevel {
    MINI(-2),
    MICRO(-1),
    NORMAL(0),
    ASSEMBLY(1),
    WORKSTATION(2),
    MAINFRAME(3);

    public final int voltageOffset;

    public CircuitLevel next() {
        return values()[voltageOffset - MINI.voltageOffset + 1];
    }

    CircuitLevel(int voltageOffset) {
        this.voltageOffset = voltageOffset;
    }

    public static CircuitLevel fromName(String name) {
        return valueOf(name.toUpperCase());
    }
}
