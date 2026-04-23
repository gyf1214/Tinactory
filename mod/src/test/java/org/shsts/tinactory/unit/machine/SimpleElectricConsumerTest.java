package org.shsts.tinactory.unit.machine;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleElectricConsumerTest {
    @Test
    void shouldExposeVoltageAndConsumerMachineType() {
        var electric = new SimpleElectricConsumer(120L, 8.5d);

        assertEquals(120L, electric.getVoltage());
        assertEquals(ElectricMachineType.CONSUMER, electric.getMachineType());
    }

    @Test
    void shouldExposeGeneratedAndConsumedPower() {
        var electric = new SimpleElectricConsumer(120L, 8.5d);

        assertEquals(0d, electric.getPowerGen());
        assertEquals(8.5d, electric.getPowerCons());
    }
}
