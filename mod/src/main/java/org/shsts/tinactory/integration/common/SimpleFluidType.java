package org.shsts.tinactory.integration.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.fluids.FluidType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFluidType extends FluidType {
    public SimpleFluidType(String translation) {
        super(Properties.create()
            .descriptionId(translation)
            .motionScale(1d)
            .canPushEntity(false)
            .canSwim(false)
            .canDrown(false)
            .fallDistanceModifier(1f)
            .pathType(null)
            .adjacentPathType(null)
            .density(0)
            .temperature(0)
            .viscosity(0));
    }
}
