package org.shsts.tinactory.unit.gui;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.Texture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

class TextureTest {
    @Test
    void shouldPrefixExtensionlessPathsWithTexturesAndPngSuffix() {
        var texture = new Texture(modLoc("gui/sample"), 12, 34);

        assertEquals(modLoc("textures/gui/sample.png"), texture.loc());
        assertEquals(12, texture.width());
        assertEquals(34, texture.height());
    }

    @Test
    void shouldPreserveNamespaceAndExistingPngPath() {
        var texture = new Texture(mcLoc("textures/gui/widgets.png"), 256, 256);

        assertEquals(mcLoc("textures/gui/widgets.png"), texture.loc());
    }
}
