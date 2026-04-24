package org.shsts.tinactory.unit.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.util.I18n;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class I18nTest {
    @Test
    void trKeepsKeyAndArgsWithoutBootstrappingMinecraft() {
        assertMinecraftStillNotBootstrapped();

        var component = I18n.tr("tinactory.unit.i18n.example", "value", 2);

        assertEquals("tinactory.unit.i18n.example", component.getKey());
        assertArrayEquals(new Object[]{"value", 2}, component.getArgs());
        assertMinecraftStillNotBootstrapped();
    }

    @Test
    void trFromResourceLocationConvertsSlashesToDotsWithoutBootstrappingMinecraft() {
        assertMinecraftStillNotBootstrapped();

        var component = I18n.tr(new ResourceLocation("tinactory", "gui/path/example"));

        assertEquals("tinactory.gui.path.example", component.getKey());
        assertMinecraftStillNotBootstrapped();
    }

    @Test
    void rawFormatsTextWithoutBootstrappingMinecraft() {
        assertMinecraftStillNotBootstrapped();

        var component = I18n.raw("value=%s count=%d", "test", 3);

        assertEquals("value=test count=3", component.getText());
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
