package org.shsts.tinactory.content.electric;

public enum CircuitLevel {
    MINI(-2),
    MICRO(-1),
    NORMAL(0),
    ASSEMBLY(1),
    WORKSTATION(2),
    MAINFRAME(3);

    public final int voltageOffset;

    CircuitLevel(int voltageOffset) {
        this.voltageOffset = voltageOffset;
    }
}
