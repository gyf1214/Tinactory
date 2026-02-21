package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.NoSuchElementException;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum CircuitTier {
    ELECTRONIC(Voltage.ULV, CircuitComponentTier.NORMAL, "coated", "basic"),
    INTEGRATED(Voltage.LV, CircuitComponentTier.NORMAL, "phenolic", "good"),
    CPU(Voltage.MV, CircuitComponentTier.NORMAL, "plastic", "plastic"),
    NANO(Voltage.HV, CircuitComponentTier.SMD, "advanced_plastic", "advanced"),
    QUANTUM(Voltage.EV, CircuitComponentTier.SMD, "epoxy", "extreme"),
    CRYSTAL(Voltage.IV, CircuitComponentTier.ADVANCED, "advanced_epoxy", "elite"),
    WETWARE(Voltage.LUV, CircuitComponentTier.ADVANCED, "wetware", "wetware");

    public final int rank;
    public final Voltage baseVoltage;
    public final CircuitComponentTier componentTier;
    public final String board;
    public final String circuitBoard;

    CircuitTier(Voltage baseVoltage, CircuitComponentTier componentTier,
        String board, String circuitBoard) {
        this.rank = baseVoltage.rank - 1;
        this.baseVoltage = baseVoltage;
        this.componentTier = componentTier;
        this.board = board;
        this.circuitBoard = circuitBoard;
    }

    public static CircuitTier fromName(String name) {
        return valueOf(name.toUpperCase());
    }

    public static CircuitTier fromRank(int rank) {
        for (var tier : CircuitTier.values()) {
            if (tier.rank == rank) {
                return tier;
            }
        }
        throw new NoSuchElementException();
    }
}
