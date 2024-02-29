package org.shsts.tinactory.core.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFluid extends EmptyFluid {
    protected final FluidAttributes.Builder builder;

    public SimpleFluid(ResourceLocation stillTexture, int color) {
        this.builder = FluidAttributes.builder(stillTexture, null)
                .color(color)
                .sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY);
    }

    @Override
    protected FluidAttributes createAttributes() {
        return this.builder.build(this);
    }
}
