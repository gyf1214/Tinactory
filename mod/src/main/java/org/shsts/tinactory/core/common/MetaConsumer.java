package org.shsts.tinactory.core.common;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.meta.IMetaConsumer;
import org.shsts.tinycorelib.api.meta.MetaLoadingException;
import org.slf4j.Logger;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MetaConsumer implements IMetaConsumer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String name;

    protected MetaConsumer(String name) {
        this.name = name;
    }

    protected abstract void doAcceptMeta(ResourceLocation loc, JsonObject jo);

    @Override
    public void acceptMeta(ResourceLocation loc, JsonObject jo) throws MetaLoadingException {
        MetaLoadingException thrown = null;
        try {
            doAcceptMeta(loc, jo);
        } catch (MetaLoadingException e) {
            thrown = e;
        } catch (RuntimeException e) {
            thrown = new MetaLoadingException(e);
        }
        if (thrown != null) {
            LOGGER.debug("Error loading meta {}:{}", name, loc, thrown);
            throw thrown;
        }
    }

    @Override
    public String name() {
        return name;
    }
}
