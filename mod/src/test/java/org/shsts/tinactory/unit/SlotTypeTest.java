package org.shsts.tinactory.unit;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.SlotType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotTypeTest {
    @Test
    void fromNameShouldBeCaseInsensitive() {
        assertEquals(SlotType.ITEM_INPUT, SlotType.fromName("item_input"));
        assertEquals(SlotType.ITEM_OUTPUT, SlotType.fromName("Item_Output"));
        assertEquals(SlotType.FLUID_INPUT, SlotType.fromName("FLUID_INPUT"));
        assertEquals(SlotType.FLUID_OUTPUT, SlotType.fromName("fluid_output"));
    }
}
