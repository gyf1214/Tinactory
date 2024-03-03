package org.shsts.tinactory.content;

import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.network.CableSet;

public final class AllBlocks {
    public static final CableSet CABLE_SET;

    static {
        CABLE_SET = CableSet.builder()
                .add(Voltage.ULV, AllMaterials.IRON, 1.0d)
                .build();
    }

    public static void init() {}
}
