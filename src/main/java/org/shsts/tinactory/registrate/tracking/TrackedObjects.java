package org.shsts.tinactory.registrate.tracking;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TrackedObjects {
    private final Map<TrackedType<?>, Map<?, String>> objects;

    public TrackedObjects() {
        this.objects = TrackedType.ALL_TYPES.stream()
                .collect(Collectors.toMap($ -> $, $ -> new HashMap<>()));
    }

    @SuppressWarnings("unchecked")
    public <V> void put(TrackedType<V> type, V value, String key) {
        ((Map<V, String>) objects.get(type)).put(value, key);
    }

    @SuppressWarnings("unchecked")
    public <V> Map<V, String> getObjects(TrackedType<V> type) {
        return (Map<V, String>) objects.get(type);
    }
}
