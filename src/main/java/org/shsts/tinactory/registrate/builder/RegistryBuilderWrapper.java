package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.SmartRegistry;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;
import org.slf4j.Logger;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RegistryBuilderWrapper<T extends IForgeRegistryEntry<T>, P>
    extends EntryBuilder<RegistryBuilder<T>, SmartRegistry<T>, P, RegistryBuilderWrapper<T, P>> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Class<T> entryClass;
    @Nullable
    private Transformer<RegistryBuilder<T>> transformer = $ -> $;

    public RegistryBuilderWrapper(Registrate registrate, String id, Class<T> entryClass, P parent) {
        super(registrate, id, parent);
        this.entryClass = entryClass;
    }

    public RegistryBuilderWrapper<T, P> builder(Transformer<RegistryBuilder<T>> trans) {
        assert transformer != null;
        transformer = transformer.chain(trans);
        return self();
    }

    @Override
    protected RegistryBuilder<T> createObject() {
        assert transformer != null;
        var builder = new RegistryBuilder<T>();
        builder = transformer.apply(builder.setName(loc).setType(entryClass));
        // free reference
        transformer = null;
        return builder;
    }

    public void registerObject(NewRegistryEvent event) {
        LOGGER.debug("register registry {} {}", entryClass.getSimpleName(), loc);
        assert entry != null;
        var builder = buildObject();
        entry.setSupplier(event.create(builder));
    }

    @Override
    protected SmartRegistry<T> createEntry() {
        var entry = registrate.registryHandler.register(this);
        var handler = RegistryEntryHandler.forge(entryClass, entry);
        entry.setHandler(handler);
        registrate.putHandler(loc, handler);
        return entry;
    }

    public RegistryBuilderWrapper<T, P> onBake(IForgeRegistry.BakeCallback<T> cb) {
        return builder($ -> $.add(cb));
    }
}
