package org.shsts.tinactory.core.metrics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistry;
import org.shsts.tinactory.api.metrics.IMetricsCallback;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetricsManager {
    private static final List<IMetricsCallback> callbacks = new ArrayList<>();

    public static void onBake(IForgeRegistry<IMetricsCallback> registry) {
        callbacks.clear();
        callbacks.addAll(registry.getValues());
    }

    public static void report(String name, List<String> labels, double value) {
        for (var cb : callbacks) {
            cb.report(name, labels, value);
        }
    }
}
