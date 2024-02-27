package org.shsts.tinactory.api.electric;

public interface IElectricMachine {
    enum Type {
        GENERATOR, CONSUMER, BUFFER
    }

    long getVoltage();

    Type getMachineType();

    double getPower();
}
