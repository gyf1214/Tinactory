package org.shsts.tinactory.datagen.context;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.util.Lazy;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.tracking.TrackedType;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class TrackedContext<V> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final TrackedType<V> type;
    private final Supplier<Map<V, String>> tracked;
    private final Set<V> processed;

    public TrackedContext(Registrate registrate, TrackedType<V> type) {
        this.type = type;
        this.tracked = Lazy.of(() -> registrate.getTracked(type));
        this.processed = new HashSet<>();
    }

    public Set<V> getTracked() {
        return tracked.get().keySet();
    }

    public void process(V obj) {
        processed.add(obj);
    }

    public void postValidate() {
        for (var entry : tracked.get().entrySet()) {
            if (!processed.contains(entry.getKey())) {
                LOGGER.warn("Tracked {} {} not processed", type, entry.getValue());
            }
        }
    }
}
