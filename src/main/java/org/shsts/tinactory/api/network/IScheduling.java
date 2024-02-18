package org.shsts.tinactory.api.network;

import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.BiConsumer;

public interface IScheduling extends IForgeRegistryEntry<IScheduling> {
    void addConditions(BiConsumer<IScheduling, IScheduling> cons);
}
