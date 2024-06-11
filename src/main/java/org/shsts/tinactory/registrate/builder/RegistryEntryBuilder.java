package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RegistryEntry;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class RegistryEntryBuilder<T extends IForgeRegistryEntry<T>, U extends T, P,
        S extends RegistryEntryBuilder<T, U, P, S>>
        extends EntryBuilder<U, RegistryEntry<U>, P, S> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final RegistryEntryHandler<T> handler;

    protected RegistryEntryBuilder(Registrate registrate, RegistryEntryHandler<T> handler, String id, P parent) {
        super(registrate, id, parent);
        this.handler = handler;
    }

    public void registerObject(IForgeRegistry<T> registry) {
        LOGGER.trace("register object {} {}", registry.getRegistryName(), loc);
        assert entry != null;
        var object = createObject();
        object.setRegistryName(loc);
        registry.register(object);
        for (var cb : onCreateObject) {
            cb.accept(object);
        }
        onCreateObject.clear();
        entry.setObject(object);
    }

    @Override
    protected RegistryEntry<U> createEntry() {
        return handler.register(this);
    }

    @FunctionalInterface
    public interface BuilderFactory<T1 extends IForgeRegistryEntry<T1>, P1,
            B extends RegistryEntryBuilder<T1, ?, P1, B>> {
        B create(Registrate registrate, RegistryEntryHandler<T1> handler, String id, P1 parent);
    }
}
