package org.shsts.tinactory.unit.common;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinycorelib.api.meta.MetaLoadingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetaConsumerTest {
    @Test
    void acceptMetaPassesThroughSuccessfulLoads() throws MetaLoadingException {
        var loc = new ResourceLocation("tinactory", "meta");
        var json = new JsonObject();
        json.addProperty("name", "value");
        var consumer = new RecordingMetaConsumer("sound");

        consumer.acceptMeta(loc, json);

        assertSame(loc, consumer.lastLoc);
        assertSame(json, consumer.lastJson);
        assertEquals("sound", consumer.name());
    }

    @Test
    void acceptMetaRethrowsExistingMetaLoadingException() {
        var expected = new MetaLoadingException("bad meta");
        var consumer = new ThrowingMetaConsumer("material", expected);

        var thrown = assertThrows(MetaLoadingException.class,
            () -> consumer.acceptMeta(new ResourceLocation("tinactory", "broken"), new JsonObject()));

        assertSame(expected, thrown);
        assertEquals("material", consumer.name());
    }

    @Test
    void acceptMetaWrapsRuntimeExceptions() {
        var cause = new IllegalStateException("boom");
        var consumer = new ThrowingMetaConsumer("machine", cause);

        var thrown = assertThrows(MetaLoadingException.class,
            () -> consumer.acceptMeta(new ResourceLocation("tinactory", "broken"), new JsonObject()));

        assertSame(cause, thrown.getCause());
    }

    private static final class RecordingMetaConsumer extends MetaConsumer {
        private ResourceLocation lastLoc;
        private JsonObject lastJson;

        private RecordingMetaConsumer(String name) {
            super(name);
        }

        @Override
        protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
            lastLoc = loc;
            lastJson = jo;
        }
    }

    private static final class ThrowingMetaConsumer extends MetaConsumer {
        private final RuntimeException runtimeException;
        private final MetaLoadingException metaLoadingException;

        private ThrowingMetaConsumer(String name, RuntimeException runtimeException) {
            super(name);
            this.runtimeException = runtimeException;
            this.metaLoadingException = null;
        }

        private ThrowingMetaConsumer(String name, MetaLoadingException metaLoadingException) {
            super(name);
            this.runtimeException = null;
            this.metaLoadingException = metaLoadingException;
        }

        @Override
        protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
            if (metaLoadingException != null) {
                throw metaLoadingException;
            }
            throw runtimeException;
        }
    }
}
