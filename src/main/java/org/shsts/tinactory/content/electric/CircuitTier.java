package org.shsts.tinactory.content.electric;

public enum CircuitTier {
    ELECTRONIC(Voltage.ULV, CircuitComponentTier.NORMAL, "coated", "basic"),
    INTEGRATED(Voltage.LV, CircuitComponentTier.NORMAL, "phenolic", "good"),
    CPU(Voltage.MV, CircuitComponentTier.SMD, "plastic", "plastic"),
    NANO(Voltage.HV, CircuitComponentTier.SMD, "epoxy", "advanced"),
    QUANTUM(Voltage.EV, CircuitComponentTier.SMD, "fiber_reinforced", "extreme"),
    CRYSTAL(Voltage.IV, CircuitComponentTier.ADVANCED, "multilayer.fiber_reinforced", "elite"),
    WETWARE(Voltage.LuV, CircuitComponentTier.ADVANCED, "wetware", "wetware");

    public final Voltage baseVoltage;
    public final CircuitComponentTier componentTier;
    public final String board;
    public final String circuitBoard;

    CircuitTier(Voltage baseVoltage, CircuitComponentTier componentTier,
                String board, String circuitBoard) {
        this.baseVoltage = baseVoltage;
        this.componentTier = componentTier;
        this.board = board;
        this.circuitBoard = circuitBoard;
    }
}
