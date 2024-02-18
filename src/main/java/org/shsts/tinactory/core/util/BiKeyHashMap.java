package org.shsts.tinactory.core.util;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BiKeyHashMap<K1, K2, V> {
    protected final Map<K1, Map<K2, V>> primaryMap = new HashMap<>();
    protected final Map<K2, Map<K1, V>> secondaryMap = new HashMap<>();

    public void put(K1 k1, K2 k2, V v) {
        this.primaryMap.computeIfAbsent(k1, $ -> new HashMap<>()).put(k2, v);
        this.secondaryMap.computeIfAbsent(k2, $ -> new HashMap<>()).put(k1, v);
    }

    public Optional<V> get(K1 k1, K2 k2) {
        return Optional.ofNullable(this.primaryMap.get(k1)).flatMap(
                map -> Optional.ofNullable(map.get(k2)));
    }

    public Set<Map.Entry<K2, V>> getPrimary(K1 k1) {
        return this.primaryMap.getOrDefault(k1, Collections.emptyMap())
                .entrySet();
    }

    public Set<Map.Entry<K1, V>> getSecondary(K2 k2) {
        return this.secondaryMap.getOrDefault(k2, Collections.emptyMap())
                .entrySet();
    }

    public void remove(K1 k1, K2 k2) {
        Optional.ofNullable(this.primaryMap.get(k1)).ifPresent(map -> map.remove(k2));
        Optional.ofNullable(this.secondaryMap.get(k2)).ifPresent(map -> map.remove(k1));
    }

    public void removeAllPrimary(K1 k1) {
        for (var k2 : this.primaryMap.getOrDefault(k1, Collections.emptyMap()).keySet()) {
            Optional.ofNullable(this.secondaryMap.get(k2)).ifPresent(map -> map.remove(k1));
        }
        this.primaryMap.remove(k1);
    }

    public void removeAllSecondary(K2 k2) {
        for (var k1 : this.secondaryMap.getOrDefault(k2, Collections.emptyMap()).keySet()) {
            Optional.ofNullable(this.primaryMap.get(k1)).ifPresent(map -> map.remove(k2));
        }
        this.secondaryMap.remove(k2);
    }

    public void clear() {
        this.primaryMap.clear();
        this.secondaryMap.clear();
    }
}
