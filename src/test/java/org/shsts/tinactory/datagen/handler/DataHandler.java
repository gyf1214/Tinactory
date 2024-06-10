package org.shsts.tinactory.datagen.handler;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.datagen.DataGen;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DataHandler<P extends DataProvider> {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final DataGen dataGen;
    protected final List<Consumer<P>> callbacks = new ArrayList<>();

    public DataHandler(DataGen dataGen) {
        this.dataGen = dataGen;
    }

    protected void addCallback(Consumer<P> callback) {
        callbacks.add(callback);
    }

    protected abstract P createProvider(GatherDataEvent event);

    public void onGatherData(GatherDataEvent event) {
        var prov = createProvider(event);
        event.getGenerator().addProvider(prov);
    }

    public void register(P provider) {
        LOGGER.info("{}: add {} callbacks", this, callbacks.size());
        for (var callback : callbacks) {
            callback.accept(provider);
        }
        clear();
    }

    public void clear() {
        callbacks.clear();
    }

    @Override
    public String toString() {
        return "%s{%s}".formatted(getClass().getSimpleName(), dataGen.modid);
    }
}
