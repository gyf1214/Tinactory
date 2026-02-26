package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.integration.PatternCellPortState;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternCellIntegrationTest {
    @Test
    void patternCellCapabilityShouldUseFixedByteAccounting() {
        var port = new PatternCellPortState(2048);
        var first = pattern("tinactory:first");
        var second = pattern("tinactory:second");

        assertTrue(port.insert(first));
        assertTrue(port.insert(second));

        assertEquals(2048, port.bytesCapacity());
        assertEquals(2, port.patterns().size());
        assertEquals(2 * PatternCellPortState.BYTES_PER_PATTERN, port.bytesUsed());
    }

    @Test
    void patternCellCapabilityShouldPersistToItemTag() {
        var port = new PatternCellPortState(2048);
        var first = pattern("tinactory:first");
        var second = pattern("tinactory:second");

        assertTrue(port.insert(first));
        assertTrue(port.insert(second));
        assertTrue(port.remove(first.patternId()));

        var clonedPort = new PatternCellPortState(2048);
        clonedPort.deserialize(port.serialize());

        assertEquals(List.of(second), clonedPort.patterns());
        assertFalse(clonedPort.remove(first.patternId()));
    }

    private static CraftPattern pattern(String id) {
        return new CraftPattern(
            id,
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_plate", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
    }
}
