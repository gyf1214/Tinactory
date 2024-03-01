package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.context.RegistryDataContext;
import org.shsts.tinactory.registrate.handler.DataHandler;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class RegistryEntryBuilder<T extends IForgeRegistryEntry<T>, U extends T, P,
        S extends RegistryEntryBuilder<T, U, P, S>>
        extends EntryBuilder<U, RegistryEntry<U>, P, S> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final RegistryEntryHandler<T> handler;

    protected RegistryEntryBuilder(Registrate registrate, RegistryEntryHandler<T> handler, String id, P parent) {
        super(registrate, id, parent);
        this.handler = handler;
    }

    public void registerObject(IForgeRegistry<T> registry) {
        LOGGER.debug("register object {} {}", registry.getRegistryName(), this.loc);
        assert this.entry != null;
        var object = this.buildObject();
        object.setRegistryName(this.loc);
        registry.register(object);
        this.entry.setObject(object);
    }

    protected <P1 extends DataProvider>
    void addDataCallback(DataHandler<P1> handler, Consumer<RegistryDataContext<T, U, P1>> cons) {
        this.onCreateEntry.add(entry ->
                handler.addCallback(provider ->
                        cons.accept(new RegistryDataContext<>(entry, provider))));
    }

    @Override
    protected RegistryEntry<U> createEntry() {
        return this.handler.register(this);
    }

    @FunctionalInterface
    public interface BuilderFactory<T1 extends IForgeRegistryEntry<T1>, P1,
            B extends RegistryEntryBuilder<T1, ?, P1, B>> {
        B create(Registrate registrate, RegistryEntryHandler<T1> handler, String id, P1 parent);
    }
}
