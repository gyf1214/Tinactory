package org.shsts.tinactory.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Scheduling extends ForgeRegistryEntry<Scheduling> {
    protected final List<Supplier<Scheduling>> befores;
    protected final List<Supplier<Scheduling>> afters;

    private static List<Supplier<Scheduling>> toLazy(Collection<Supplier<Scheduling>> supps) {
        return supps.stream().<Supplier<Scheduling>>map(Lazy::of).toList();
    }

    public Scheduling(List<Supplier<Scheduling>> befores, List<Supplier<Scheduling>> afters) {
        this.befores = toLazy(befores);
        this.afters = toLazy(afters);
    }

    public void addConditions(BiConsumer<Scheduling, Scheduling> cons) {
        for (var before : this.befores) {
            cons.accept(this, before.get());
        }
        for (var after : this.afters) {
            cons.accept(after.get(), this);
        }
    }
}
