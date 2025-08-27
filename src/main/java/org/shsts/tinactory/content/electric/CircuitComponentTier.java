package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum CircuitComponentTier {
    NORMAL(0, ""), SMD(1, "smd"), ADVANCED(2, "advanced_smd");

    public final int rank;
    public final String prefix;

    CircuitComponentTier(int rank, String prefix) {
        this.rank = rank;
        this.prefix = prefix;
    }

    public String getName(String component) {
        var name = prefix.isEmpty() ? component : component + "/" + prefix;
        return "circuit_component/" + name;
    }
}
