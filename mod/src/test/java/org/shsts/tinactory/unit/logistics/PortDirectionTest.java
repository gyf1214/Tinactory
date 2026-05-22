package org.shsts.tinactory.unit.logistics;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortDirection;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortDirectionTest {
    @Test
    void invertShouldMapDirections() {
        assertEquals(PortDirection.NONE, PortDirection.NONE.invert());
        assertEquals(PortDirection.OUTPUT, PortDirection.INPUT.invert());
        assertEquals(PortDirection.INPUT, PortDirection.OUTPUT.invert());
    }

    @Test
    void shouldHaveRepresentableName() {
        assertEquals("none", PortDirection.NONE.getSerializedName());
        assertEquals("output", PortDirection.OUTPUT.getSerializedName());
        assertEquals("input", PortDirection.INPUT.getSerializedName());
    }

    @Test
    void shouldParseFromName() {
        assertEquals(PortDirection.NONE, PortDirection.fromName("none"));
        assertEquals(PortDirection.OUTPUT, PortDirection.fromName("output"));
        assertEquals(PortDirection.INPUT, PortDirection.fromName("input"));
    }
}
