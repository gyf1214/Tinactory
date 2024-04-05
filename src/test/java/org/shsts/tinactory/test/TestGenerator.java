package org.shsts.tinactory.test;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TestGenerator extends Machine implements IElectricMachine {
    private final long voltage;
    private final double power;

    public TestGenerator(BlockEntityType<?> type, BlockPos pos, BlockState state,
                         Voltage voltage, double amperage) {
        super(type, pos, state);
        this.voltage = voltage.value;
        this.power = amperage * voltage.value;
    }

    public static BlockEntityBuilder.Factory<TestGenerator> factory(Voltage voltage, double amperage) {
        return (type, pos, state) -> new TestGenerator(type, pos, state, voltage, amperage);
    }


    @Override
    public long getVoltage() {
        return this.voltage;
    }

    @Override
    public ElectricMachineType getMachineType() {
        return ElectricMachineType.GENERATOR;
    }

    @Override
    public double getPowerCons() {
        return 0;
    }

    @Override
    public double getPowerGen() {
        return this.power;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.ELECTRIC_MACHINE.get()) {
            return LazyOptional.of(() -> this).cast();
        }
        return super.getCapability(cap, side);
    }
}
