package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.registrate.Registrate;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class EntryBuilder<U, E, P, S extends EntryBuilder<U, E, P, S>> extends Builder<U, P, S> {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    protected E entry = null;
    protected final List<Consumer<E>> onCreateEntry = new ArrayList<>();

    protected EntryBuilder(Registrate registrate, String id, P parent) {
        super(registrate, parent, id);
    }

    protected abstract E createEntry();

    public E register() {
        LOGGER.trace("create entry {} {}", getClass().getSimpleName(), loc);
        entry = createEntry();
        for (var callback : onCreateEntry) {
            callback.accept(entry);
        }
        // free reference
        onCreateEntry.clear();
        return entry;
    }
}
