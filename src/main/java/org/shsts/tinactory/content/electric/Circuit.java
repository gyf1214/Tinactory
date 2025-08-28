package org.shsts.tinactory.content.electric;

import net.minecraft.world.item.Item;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

public record Circuit(CircuitTier tier, CircuitLevel level, IEntry<Item> entry) {
    public Item getItem() {
        return entry.get();
    }

    public Item circuitBoard() {
        return Circuits.circuitBoard(tier).get();
    }
}
