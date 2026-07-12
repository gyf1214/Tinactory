package org.shsts.tinactory.api.network;

import java.util.function.BiConsumer;

public interface IScheduling {
    void addConditions(BiConsumer<IScheduling, IScheduling> cons);
}
