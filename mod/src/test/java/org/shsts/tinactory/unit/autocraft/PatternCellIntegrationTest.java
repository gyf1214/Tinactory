package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PatternCellPortState;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestMachineConstraint;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternCellIntegrationTest {
    @Test
    void patternCellCapabilityShouldUseFixedByteAccounting() {
        var port = new PatternCellPortState(2048, TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, TestStackKey.CODEC);
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
        var port = new PatternCellPortState(2048, TestMachineConstraint.MACHINE_CONSTRAINT_CODEC, TestStackKey.CODEC);
        var first = pattern("tinactory:first");
        var second = pattern("tinactory:second");

        assertTrue(port.insert(first));
        assertTrue(port.insert(second));
        assertTrue(port.remove(first.patternUuid()));

        var clonedPort = new PatternCellPortState(2048, TestMachineConstraint.MACHINE_CONSTRAINT_CODEC,
            TestStackKey.CODEC);
        clonedPort.deserialize(port.serialize());

        assertEquals(List.of(second), clonedPort.patterns());
        assertFalse(clonedPort.remove(first.patternUuid()));
    }

    private static CraftPattern pattern(String id) {
        return TestAutocraftHelper.pattern(
            UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8)),
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(new CraftAmount(TestStackKey.item("minecraft:iron_plate", ""), 1)),
            TestAutocraftHelper.constraints("tinactory:mixer", 0));
    }
}
