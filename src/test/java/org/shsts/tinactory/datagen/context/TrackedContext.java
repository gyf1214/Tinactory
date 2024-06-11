package org.shsts.tinactory.datagen.context;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.util.Lazy;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.tracking.TrackedType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TrackedContext<V> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final TrackedType<V> type;
    private final Supplier<Map<V, String>> tracked;
    private final Map<V, String> extraTracked = new HashMap<>();
    private final List<Supplier<? extends V>> processed;

    public TrackedContext(Registrate registrate, TrackedType<V> type) {
        this.type = type;
        this.tracked = Lazy.of(() -> registrate.getTracked(type));
        this.processed = new ArrayList<>();
    }

    public Map<V, String> getTrackedMap() {
        var ret = new HashMap<>(tracked.get());
        ret.putAll(extraTracked);
        return ret;
    }

    public Set<V> getTracked() {
        return getTrackedMap().keySet();
    }

    public void process(V obj) {
        processed.add(() -> obj);
    }

    public void process(Supplier<? extends V> obj) {
        processed.add(obj);
    }

    public void trackExtra(V obj, String key) {
        extraTracked.put(obj, key);
    }

    public void postValidate() {
        var processed = this.processed.stream().map($ -> (V) $.get())
                .collect(Collectors.toSet());
        var tracked = getTrackedMap();

        var missing = 0;
        for (var entry : tracked.entrySet()) {
            if (!processed.contains(entry.getKey())) {
                LOGGER.trace("Tracked {} {} not processed", type, entry.getValue());
                missing++;
            }
        }
        if (missing > 0) {
            LOGGER.warn("Tracked {} has {} / {} objects not processed",
                    type, missing, tracked.size());
        } else {
            LOGGER.info("Tracked {} all processed", type);
        }
    }
}
