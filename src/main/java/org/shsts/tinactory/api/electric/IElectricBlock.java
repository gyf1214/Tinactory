package org.shsts.tinactory.api.electric;

import net.minecraft.world.level.block.state.BlockState;

public interface IElectricBlock {
    long getVoltage(BlockState state);

    double getResistance(BlockState state);
}
