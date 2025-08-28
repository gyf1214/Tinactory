package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.core.electric.Voltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleElectricConsumer implements IElectricMachine {
    private final long voltage;
    private final double consumption;

    public SimpleElectricConsumer(Voltage voltage, double amperage) {
        this.voltage = voltage.value;
        this.consumption = voltage.value * amperage;
    }

    @Override
    public long getVoltage() {
        return voltage;
    }

    @Override
    public ElectricMachineType getMachineType() {
        return ElectricMachineType.CONSUMER;
    }

    @Override
    public double getPowerGen() {
        return 0;
    }

    @Override
    public double getPowerCons() {
        return consumption;
    }
}
