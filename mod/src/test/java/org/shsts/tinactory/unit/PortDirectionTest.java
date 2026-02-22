package org.shsts.tinactory.unit;

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
}
