package org.shsts.tinactory.core.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum Voltage {
    PRIMITIVE(0, 0xFFFFFFFF), ULV(1, 0xFFC80000),
    LV(2, 0xFFDCDCDC), MV(3, 0xFFFF6400), HV(4, 0xFFFFFF1E),
    EV(5, 0xFF808080), IV(6, 0xFFF0F0F5), LUV(7, 0xFFE99797), ZPM(8, 0xFF7EC3C4),
    MAX(15, 0xFFFFFFFF);

    public final int rank;
    public final long value;
    public final String id;
    public final int color;

    Voltage(int rank, int color) {
        this.rank = rank;
        this.value = rank == 0 ? 0 : 2L << (2L * rank);
        this.color = color;
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
        return MAX;
    }

    public static Voltage fromRank(int rank) {
        for (var voltage : Voltage.values()) {
            if (voltage.rank == rank) {
                return voltage;
            }
        }
        throw new NoSuchElementException();
    }

    public static Voltage fromName(String id) {
        return valueOf(id.toUpperCase());
    }

    public String displayName() {
        return id.toUpperCase();
    }
}
