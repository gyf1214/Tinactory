package org.shsts.tinactory.api.electric;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IElectricBlock {
    long getVoltage(BlockState state);

    double getResistance(BlockState state);

    static boolean canVoltagesConnect(long voltage, long voltage1) {
        return voltage == 0 || voltage1 == 0 || voltage == voltage1;
    }

    static boolean canVoltagesConnect(long voltage, BlockState state) {
        return state.getBlock() instanceof IElectricBlock electricBlock &&
                canVoltagesConnect(voltage, electricBlock.getVoltage(state));
    }
}
