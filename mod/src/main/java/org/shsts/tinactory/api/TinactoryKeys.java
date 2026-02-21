package org.shsts.tinactory.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.metrics.IMetricsCallback;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TinactoryKeys {
    public static final String ID = "tinactory";

    public static final String METRICS_CALLBACKS = "metrics_callback";
    public static final ResourceKey<Registry<IMetricsCallback>> METRICS_CALLBACKS_KEY =
        ResourceKey.createRegistryKey(new ResourceLocation(ID, METRICS_CALLBACKS));
}
