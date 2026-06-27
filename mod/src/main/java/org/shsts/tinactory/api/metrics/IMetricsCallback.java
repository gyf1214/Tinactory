package org.shsts.tinactory.api.metrics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMetricsCallback {
    void report(String name, List<String> labels, double value);
}
