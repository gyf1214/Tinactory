package org.shsts.tinactory.integration.common;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFluid extends EmptyFluid {
    @Nullable
    private FluidType fluidType = null;
    @Nullable
    private Supplier<? extends FluidType> fluidTypeSupp;
    public final int displayColor;

    public SimpleFluid(Supplier<? extends FluidType> fluidType, int displayColor) {
        this.fluidTypeSupp = fluidType;
        this.displayColor = displayColor;
    }

    @Override
    public FluidType getFluidType() {
        if (fluidType == null) {
            assert fluidTypeSupp != null;
            fluidType = fluidTypeSupp.get();
            fluidTypeSupp = null;
        }
        return fluidType;
    }

    @Override
    public boolean isSource(FluidState state) {
        return true;
    }
}
