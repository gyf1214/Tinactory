package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.SmartRegistry;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RegistryBuilderWrapper<T extends IForgeRegistryEntry<T>, P>
        extends EntryBuilder<RegistryBuilder<T>, SmartRegistry<T>, P, RegistryBuilderWrapper<T, P>> {

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Class<T> entryClass;
    @Nullable
    protected Transformer<RegistryBuilder<T>> transformer = $ -> $;

    public RegistryBuilderWrapper(Registrate registrate, String id, Class<T> entryClass, P parent) {
        super(registrate, id, parent);
        this.entryClass = entryClass;
    }

    public RegistryBuilderWrapper<T, P> builder(Transformer<RegistryBuilder<T>> transformer) {
        assert this.transformer != null;
        this.transformer = this.transformer.chain(transformer);
        return self();
    }

    @Override
    public RegistryBuilder<T> createObject() {
        assert this.transformer != null;
        var builder = new RegistryBuilder<T>();
        builder = this.transformer.apply(builder.setName(this.loc).setType(this.entryClass));
        // free reference
        this.transformer = null;
        return builder;
    }

    public void registerObject(NewRegistryEvent event) {
        LOGGER.debug("register registry {} {}", this.entryClass, this.loc);
        assert this.entry != null;
        var builder = this.buildObject();
        this.entry.setSupplier(event.create(builder));
    }

    @Override
    protected SmartRegistry<T> createEntry() {
        var entry = this.registrate.registryHandler.register(this);
        var handler = RegistryEntryHandler.forge(this.loc, this.entryClass, entry);
        entry.setHandler(handler);
        this.registrate.putHandler(this.loc, handler);
        return entry;
    }

    public RegistryBuilderWrapper<T, P> onBake(IForgeRegistry.BakeCallback<T> cb) {
        return this.builder($ -> $.add(cb));
    }
}
