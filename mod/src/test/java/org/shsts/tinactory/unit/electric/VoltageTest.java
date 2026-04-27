package org.shsts.tinactory.unit.electric;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VoltageTest {
    @Test
    void betweenReturnsInclusiveRankRange() {
        assertEquals(List.of(Voltage.LV, Voltage.MV, Voltage.HV), Voltage.between(Voltage.LV, Voltage.HV));
        assertEquals(List.of(Voltage.MAX), Voltage.between(Voltage.MAX, Voltage.MAX));
    }

    @Test
    void fromValueMapsToFirstVoltageAtOrAboveRequestedValue() {
        assertSame(Voltage.PRIMITIVE, Voltage.fromValue(0));
        assertSame(Voltage.ULV, Voltage.fromValue(1));
        assertSame(Voltage.ULV, Voltage.fromValue(Voltage.ULV.value));
        assertSame(Voltage.LV, Voltage.fromValue(Voltage.ULV.value + 1));
        assertSame(Voltage.MAX, Voltage.fromValue(Voltage.MAX.value + 1));
    }

    @Test
    void fromRankFindsExactRankAndRejectsUnknownRanks() {
        assertSame(Voltage.PRIMITIVE, Voltage.fromRank(0));
        assertSame(Voltage.ZPM, Voltage.fromRank(8));
        assertSame(Voltage.MAX, Voltage.fromRank(15));
        assertThrows(NoSuchElementException.class, () -> Voltage.fromRank(9));
    }

    @Test
    void fromNameIsCaseInsensitiveAndDisplayDataMatchesEnumName() {
        assertSame(Voltage.HV, Voltage.fromName("hv"));
        assertSame(Voltage.ULV, Voltage.fromName("UlV"));

        assertEquals("zpm", Voltage.ZPM.id);
        assertEquals("ZPM", Voltage.ZPM.displayName());
        assertEquals(32L, Voltage.LV.value);
    }
}
