package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleElectricConsumer implements IElectricMachine {
    private final long voltage;
    private final double power;

    public SimpleElectricConsumer(long voltage, double power) {
        this.voltage = voltage;
        this.power = power;
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
        return power;
    }
}
