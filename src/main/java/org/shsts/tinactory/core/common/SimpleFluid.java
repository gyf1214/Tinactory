package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidAttributes;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFluid extends EmptyFluid {
    private final FluidAttributes.Builder builder;
    public final int displayColor;

    public SimpleFluid(ResourceLocation tex, int texColor, int displayColor) {
        this.builder = FluidAttributes.builder(tex, null)
            .color(texColor)
            .sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY);
        this.displayColor = displayColor;
    }

    @Override
    protected FluidAttributes createAttributes() {
        return builder.build(this);
    }

    @Override
    public boolean isSource(FluidState state) {
        return true;
    }
}
