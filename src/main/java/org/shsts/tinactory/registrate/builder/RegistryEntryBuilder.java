package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.context.RegistryDataContext;
import org.shsts.tinactory.registrate.handler.DataHandler;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class RegistryEntryBuilder<T extends IForgeRegistryEntry<T>, U extends T, P,
        S extends RegistryEntryBuilder<T, U, P, S>>
        extends Builder<U, P, S> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final RegistryEntryHandler<T> handler;
    protected final List<Consumer<RegistryEntry<U>>> onCreateEntry = new ArrayList<>();
    protected final List<Consumer<U>> onCreateObject = new ArrayList<>();
    @Nullable
    protected RegistryEntry<U> entry = null;

    public final String id;

    protected RegistryEntryBuilder(Registrate registrate, RegistryEntryHandler<T> handler, String id, P parent) {
        super(registrate, parent);
        this.handler = handler;
        this.id = id;
    }

    public void registerObject(IForgeRegistry<T> registry) {
        LOGGER.debug("register object {} {}:{}", registry.getRegistryName(), this.registrate.modid, this.id);
        assert this.entry != null;
        var object = this.buildObject();
        object.setRegistryName(new ResourceLocation(this.registrate.modid, this.id));
        registry.register(object);
        this.entry.setObject(object);
        for (var cb : this.onCreateObject) {
            cb.accept(object);
        }
        this.onCreateObject.clear();
    }

    @Override
    public P build() {
        this.register();
        return this.parent;
    }

    protected <P1 extends DataProvider>
    void addDataCallback(DataHandler<P1> handler, Consumer<RegistryDataContext<T, U, P1>> cons) {
        this.onCreateEntry.add(entry ->
                handler.addCallback(provider ->
                        cons.accept(new RegistryDataContext<>(entry, provider))));
    }

    public RegistryEntry<U> register() {
        LOGGER.debug("create object entry {} {}:{}", this.handler.getEntryClass(), this.registrate.modid, this.id);
        this.entry = this.handler.register(this);
        for (var callback : this.onCreateEntry) {
            callback.accept(this.entry);
        }
        // free reference
        onCreateEntry.clear();
        return this.entry;
    }

    @FunctionalInterface
    public interface BuilderFactory<T1 extends IForgeRegistryEntry<T1>, P1,
            B extends RegistryEntryBuilder<T1, ?, P1, B>> {
        B create(Registrate registrate, RegistryEntryHandler<T1> handler, String id, P1 parent);
    }
}
