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
import org.shsts.tinactory.registrate.handler.RegistryHandler;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class RegistryBuilder<T extends IForgeRegistryEntry<T>, U extends T, P,
        S extends RegistryBuilder<T, U, P, S>>
        extends Builder<U, P, S> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final RegistryHandler<T> handler;
    protected final List<Consumer<RegistryEntry<U>>> onCreateEntry = new ArrayList<>();
    @Nullable
    protected RegistryEntry<U> entry = null;

    public final String id;

    protected RegistryBuilder(Registrate registrate, RegistryHandler<T> handler, String id, P parent) {
        super(registrate, parent);
        this.handler = handler;
        this.id = id;
    }

    public void registerObject(IForgeRegistry<T> registry) {
        LOGGER.debug("register object {} {}:{}", this.getClass(), this.registrate.modid, this.id);
        assert this.entry != null;
        var object = this.buildObject();
        object.setRegistryName(new ResourceLocation(this.registrate.modid, this.id));
        registry.register(object);
        this.entry.setObject(object);
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
        LOGGER.debug("create object entry {} {}:{}", this.getClass(), this.registrate.modid, this.id);
        this.entry = this.handler.register(this);
        for (var callback : this.onCreateEntry) {
            callback.accept(this.entry);
        }
        // free reference
        onCreateEntry.clear();
        return this.entry;
    }
}
