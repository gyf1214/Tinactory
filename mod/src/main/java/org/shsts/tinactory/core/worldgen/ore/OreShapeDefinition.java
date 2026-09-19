package org.shsts.tinactory.core.worldgen.ore;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreShapeDefinition<D>(ResourceLocation loc, IOreShape<D, ?> shape, D definition) {
    public OreShapeDefinition(IEntry<? extends IOreShape<D, ?>> shape, D definition) {
        this(shape.loc(), shape.get(), definition);
    }

    public IEntry<IOreShape<?, ?>> entry() {
        return CORE.createEntry(loc, shape);
    }

    public OreShapeDefinition<D> scaleArea(double areaScale) {
        return new OreShapeDefinition<>(loc, shape, shape.scaleDefinition(definition, areaScale));
    }
}
