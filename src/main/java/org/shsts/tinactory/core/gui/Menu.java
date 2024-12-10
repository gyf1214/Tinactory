package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Menu {
    public static final int WIDTH = 176;
    public static final int SLOT_SIZE = 18;
    public static final int CONTENT_WIDTH = 9 * SLOT_SIZE;
    public static final int MARGIN_HORIZONTAL = (WIDTH - CONTENT_WIDTH) / 2;
    public static final int FONT_HEIGHT = 9;
    public static final int SPACING = 3;
    public static final int MARGIN_VERTICAL = 3 + SPACING;
    public static final int MARGIN_TOP = MARGIN_VERTICAL + FONT_HEIGHT + SPACING;
    public static final Container EMPTY_CONTAINER = new SimpleContainer(0);

    private Menu() {}
}
