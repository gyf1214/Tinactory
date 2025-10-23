package org.shsts.tinactory.core.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.shsts.tinactory.api.network.IScheduling;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Scheduling extends ForgeRegistryEntry<IScheduling> implements IScheduling {
    private final List<Supplier<IScheduling>> befores;
    private final List<Supplier<IScheduling>> afters;

    public Scheduling(List<Supplier<IScheduling>> befores, List<Supplier<IScheduling>> afters) {
        this.befores = befores;
        this.afters = afters;
    }

    @Override
    public void addConditions(BiConsumer<IScheduling, IScheduling> cons) {
        for (var before : befores) {
            cons.accept(this, before.get());
        }
        for (var after : afters) {
            cons.accept(after.get(), this);
        }
    }
}
