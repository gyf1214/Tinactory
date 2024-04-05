package org.shsts.tinactory.content.machine;

public enum Voltage {
    PRIMITIVE(0), ULV(8), LV(32), MV(128), HV(512),
    MAXIMUM(2147483648L);

    public final long value;
    public final String id;

    Voltage(long value) {
        this.value = value;
        this.id = name().toLowerCase();
    }
}
