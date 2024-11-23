package org.shsts.tinactory.core.common;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WeakMap<K, V> {
    public static class Ref<V> {
        @Nullable
        private V value;

        public Ref(V value) {
            this.value = value;
        }

        public void invalidate() {
            value = null;
        }

        public Optional<V> get() {
            return Optional.ofNullable(value);
        }
    }

    private final Map<K, Ref<V>> map = new HashMap<>();

    public Ref<V> put(K key, V value) {
        var ref = new Ref<>(value);
        map.put(key, ref);
        return ref;
    }

    public void put(K key, Ref<V> value) {
        map.put(key, value);
    }

    public Optional<V> get(K key) {
        return map.containsKey(key) ? map.get(key).get() : Optional.empty();
    }

    public void clear() {
        map.clear();
    }
}
