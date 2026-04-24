package org.shsts.tinactory.unit.gui;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.Texture;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextureTest {
    @Test
    void shouldPrefixExtensionlessPathsWithTexturesAndPngSuffix() {
        var texture = new Texture(new ResourceLocation("tinactory", "gui/sample"), 12, 34);

        assertEquals(new ResourceLocation("tinactory", "textures/gui/sample.png"), texture.loc());
        assertEquals(12, texture.width());
        assertEquals(34, texture.height());
    }

    @Test
    void shouldPreserveNamespaceAndExistingPngPath() {
        var texture = new Texture(new ResourceLocation("minecraft", "textures/gui/widgets.png"), 256, 256);

        assertEquals(new ResourceLocation("minecraft", "textures/gui/widgets.png"), texture.loc());
    }
}
