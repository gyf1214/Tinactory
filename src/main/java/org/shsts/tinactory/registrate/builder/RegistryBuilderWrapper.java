package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.SmartRegistry;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RegistryBuilderWrapper<T extends IForgeRegistryEntry<T>, P>
        extends Builder<RegistryBuilder<T>, P, RegistryBuilderWrapper<T, P>> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public final String id;
    protected final Class<T> entryClass;

    @Nullable
    protected Transformer<RegistryBuilder<T>> transformer = $ -> $;
    @Nullable
    protected SmartRegistry<T> entry = null;

    public RegistryBuilderWrapper(Registrate registrate, String id, Class<T> entryClass, P parent) {
        super(registrate, parent);
        this.id = id;
        this.entryClass = entryClass;
    }

    public RegistryBuilderWrapper<T, P> builder(Transformer<RegistryBuilder<T>> transformer) {
        assert this.transformer != null;
        this.transformer = this.transformer.chain(transformer);
        return self();
    }

    @Override
    public RegistryBuilder<T> buildObject() {
        assert this.transformer != null;
        var builder = new RegistryBuilder<T>();
        var loc = new ResourceLocation(this.registrate.modid, this.id);
        builder = this.transformer.apply(builder.setName(loc).setType(this.entryClass));
        // free reference
        this.transformer = null;
        return builder;
    }

    public void registerObject(NewRegistryEvent event) {
        LOGGER.debug("register registry {} {}:{}", this.entryClass, this.registrate.modid, this.id);
        assert this.entry != null;
        var builder = this.buildObject();
        this.entry.setSupplier(event.create(builder));
    }

    public SmartRegistry<T> register() {
        LOGGER.debug("create registry entry {} {}:{}", this.entryClass, this.registrate.modid, this.id);
        this.entry = this.registrate.registryHandler.register(this);
        var handler = RegistryEntryHandler.forge(this.registrate, this.entryClass, this.entry);
        this.entry.setHandler(handler);
        this.registrate.putHandler(handler);
        return this.entry;
    }

    public RegistryBuilderWrapper<T, P> onBake(IForgeRegistry.BakeCallback<T> cb) {
        return this.builder($ -> $.add(cb));
    }
}
