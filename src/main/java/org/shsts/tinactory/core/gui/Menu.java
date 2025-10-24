package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.BACKGROUND_TEX_RECT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Menu {
    public static final int WIDTH = 176;
    public static final int SPACING = 3;
    public static final int SLOT_SIZE = 18;
    public static final int FONT_HEIGHT = 9;
    public static final int PANEL_WIDTH = SLOT_SIZE * 9;
    public static final int PANEL_HEIGHT = 128;
    public static final int BUTTON_SIZE = SLOT_SIZE + 3;
    public static final int TECH_SIZE = 24;
    public static final int PORT_WIDTH = 42;
    public static final int PORT_HEIGHT = BUTTON_SIZE + 2;
    public static final int PORT_PADDING_TEXT = (PORT_HEIGHT - FONT_HEIGHT) / 2;
    public static final int PORT_PADDING_ICON = (PORT_HEIGHT - SLOT_SIZE) / 2 + 1;
    public static final int PORT_TEXT_COLOR = 0xFFFFAA00;
    public static final int PANEL_BORDER = 2;
    public static final int CONTENT_WIDTH = 9 * SLOT_SIZE;
    public static final int MARGIN_X = (WIDTH - CONTENT_WIDTH) / 2;
    public static final int MARGIN_VERTICAL = 3 + SPACING;
    public static final int MARGIN_TOP = MARGIN_VERTICAL + FONT_HEIGHT + SPACING;
    public static final Container EMPTY_CONTAINER = new SimpleContainer(0);
    public static final Rect BUTTON_PANEL_BG = BACKGROUND_TEX_RECT.offset(6, 6).enlarge(-12, -12);

    private Menu() {}
}
