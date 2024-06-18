package org.shsts.tinactory.content.electric;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum Voltage {
    PRIMITIVE(0), ULV(1), LV(2), MV(3), HV(4), EV(5), IV(6),
    MAXIMUM(15);

    public final int rank;
    public final long value;
    public final String id;

    Voltage(int rank) {
        this.rank = rank;
        this.value = rank == 0 ? 0 : 2L << (2L * rank);
        this.id = name().toLowerCase();
    }

    public static List<Voltage> between(Voltage from, Voltage to) {
        return Arrays.stream(Voltage.values())
                .filter(x -> x.rank >= from.rank && x.rank <= to.rank)
                .toList();
    }

    public static Voltage fromValue(long value) {
        for (var voltage : values()) {
            if (voltage.value >= value) {
                return voltage;
            }
        }
        return MAXIMUM;
    }

    public static Voltage fromRank(int rank) {
        return Arrays.stream(Voltage.values())
                .filter(v -> v.rank == rank)
                .findAny().orElseThrow();
    }
}
