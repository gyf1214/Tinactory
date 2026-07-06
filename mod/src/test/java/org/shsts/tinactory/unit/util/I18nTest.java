package org.shsts.tinactory.unit.util;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.I18n;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

class I18nTest {
    @Test
    void trKeepsKeyAndArgsWithoutBootstrappingMinecraft() {
        assertMinecraftStillNotBootstrapped();

        var component = I18n.tr("tinactory.unit.i18n.example", "value", 2);

        assertSame(TranslatableContents.TYPE, component.getContents().type());
        assertEquals("tinactory.unit.i18n.example", key(component));
        assertArrayEquals(new Object[]{"value", 2}, args(component));
        assertMinecraftStillNotBootstrapped();
    }

    @Test
    void trFromResourceLocationConvertsSlashesToDotsWithoutBootstrappingMinecraft() {
        assertMinecraftStillNotBootstrapped();

        var component = I18n.tr(modLoc("gui/path/example"), "arg");

        assertSame(TranslatableContents.TYPE, component.getContents().type());
        assertEquals("tinactory.gui.path.example", key(component));
        assertArrayEquals(new Object[]{"arg"}, args(component));
        assertMinecraftStillNotBootstrapped();
    }

    private static String key(MutableComponent component) {
        return ((TranslatableContents) component.getContents()).getKey();
    }

    private static Object[] args(MutableComponent component) {
        return ((TranslatableContents) component.getContents()).getArgs();
    }

    @Test
    void rawFormatsTextWithoutBootstrappingMinecraft() {
        assertMinecraftStillNotBootstrapped();

        var component = I18n.raw("value=%s count=%d", "test", 3);

        assertSame(PlainTextContents.TYPE, component.getContents().type());
        assertEquals("value=test count=3", ((PlainTextContents) component.getContents()).text());
        assertMinecraftStillNotBootstrapped();
    }

    @Test
    void translatableGetStringFallsBackToKeyWithoutBootstrappingMinecraft() {
        assertMinecraftStillNotBootstrapped();

        var component = I18n.tr("tinactory.unit.i18n.missing");

        assertEquals("tinactory.unit.i18n.missing", component.getString());
        assertMinecraftStillNotBootstrapped();
    }

    private static void assertMinecraftStillNotBootstrapped() {
        var message = bootstrapProbeMessage();
        assertThrows(IllegalArgumentException.class, () -> Bootstrap.checkBootstrapCalled(message));
    }

    private static Supplier<String> bootstrapProbeMessage() {
        return () -> "i18n unit test probe";
    }
}
