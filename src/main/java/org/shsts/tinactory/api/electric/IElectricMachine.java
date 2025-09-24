package org.shsts.tinactory.api.electric;

public interface IElectricMachine {
    long getVoltage();

    ElectricMachineType getMachineType();

    double getPowerGen();

    double getPowerCons();
}
