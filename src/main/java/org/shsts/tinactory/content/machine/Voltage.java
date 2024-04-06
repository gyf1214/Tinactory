package org.shsts.tinactory.content.machine;

public enum Voltage {
    PRIMITIVE(0), ULV(1), LV(2), MV(3), HV(4), EV(5), IV(6),
    MAXIMUM(15);

    public final int rank;
    public final long value;
    public final String id;

    Voltage(int rank) {
        this.rank = rank;
        this.value = 2L << (2L * rank);
        this.id = name().toLowerCase();
    }
}
