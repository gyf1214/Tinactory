package org.shsts.tinactory.api.electric;

public interface IElectricMachine {
    enum ElectricMachineType {
        GENERATOR, CONSUMER, BUFFER
    }

    long getVoltage();

    ElectricMachineType getMachineType();

    double getPower();
}
