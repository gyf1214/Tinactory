package org.shsts.tinactory.core.worldgen.ore;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreShapeInstance<I>(ResourceLocation loc, IOreShape<?, I> shape, I instance) {
    public OreShapeInstance(IEntry<? extends IOreShape<?, I>> shape, I instance) {
        this(shape.loc(), shape.get(), instance);
    }

    public IEntry<IOreShape<?, ?>> entry() {
        return CORE.createEntry(loc, shape);
    }
}
