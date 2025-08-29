package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record Circuit(CircuitTier tier, CircuitLevel level, IEntry<Item> entry) {
    public Item getItem() {
        return entry.get();
    }

    public Item getCircuitBoard() {
        return Circuits.circuitBoard(tier).get();
    }

    public Voltage getVoltage() {
        return Voltage.fromRank(tier.baseVoltage.rank + level.voltageOffset);
    }
}
