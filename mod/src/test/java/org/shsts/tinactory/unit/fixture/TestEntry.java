package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

// TODO: later if TinyCoreLib exposes a public entry constructor, remove this.
public record TestEntry<R>(ResourceLocation loc, R value) implements IEntry<R> {
    @Override
    public R get() {
        return value;
    }
}
