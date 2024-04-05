package org.shsts.tinactory.registrate.handler;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.registrate.Registrate;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class DataHandler<P extends DataProvider> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Registrate registrate;
    protected final List<Consumer<P>> callbacks = new ArrayList<>();

    protected DataHandler(Registrate registrate) {
        this.registrate = registrate;
    }

    public void addCallback(Consumer<P> callback) {
        callbacks.add(callback);
    }

    public abstract void onGatherData(GatherDataEvent event);

    public void register(P provider) {
        LOGGER.info("Data Handler {} add {} callbacks", this, callbacks.size());
        for (var callback : callbacks) {
            callback.accept(provider);
        }
        clear();
    }

    public void clear() {
        callbacks.clear();
    }

    public static String modelPath(String path, String modid, String folder) {
        var loc = path.contains(":") ? new ResourceLocation(path) : new ResourceLocation(modid, path);
        var newPath = loc.getPath();
        if (!newPath.startsWith(ModelProvider.BLOCK_FOLDER + "/") &&
                !newPath.startsWith(ModelProvider.ITEM_FOLDER + "/")) {
            newPath = folder + "/" + newPath;
        }
        return (new ResourceLocation(loc.getNamespace(), newPath)).toString();
    }
}
