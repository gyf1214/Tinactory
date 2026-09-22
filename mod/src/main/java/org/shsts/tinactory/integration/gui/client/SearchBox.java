package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.util.I18n;

import java.util.function.Consumer;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SEARCH_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.SEARCH_ICON;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SearchBox extends Panel {
    public static final RectD SEARCH_ANCHOR = RectD.corners(0d, 0d, 1d, 0d);
    private static final int BOX_TOP_MARGIN = (SEARCH_SIZE - FONT_HEIGHT + 1) / 2;
    private static final RectD BOX_ANCHOR = RectD.corners(0d, 0d, 1d, 0d);
    private static final Rect BOX_OFFSET = Rect.corners(SEARCH_SIZE + SPACING * 2,
        BOX_TOP_MARGIN, -4, BOX_TOP_MARGIN + FONT_HEIGHT);
    private static final Rect ICON_OFFSET = new Rect(SPACING, 0, SEARCH_SIZE, SEARCH_SIZE);

    private final EditBox editBox;

    public SearchBox(MenuScreen<?> screen, Consumer<String> responder, int textColor,
        ChatFormatting hintColor) {
        super(screen);
        var icon = new StaticWidget(menu, SEARCH_ICON);
        this.editBox = Widgets.editBox();
        editBox.setBordered(false);
        editBox.setResponder(responder);
        editBox.setTextColor(textColor);
        editBox.setHint(I18n.tr("gui.recipebook.search_hint")
            .withStyle(ChatFormatting.ITALIC, hintColor));

        addChild(ICON_OFFSET, icon);
        addVanillaWidget(BOX_ANCHOR, BOX_OFFSET, 0, editBox);
    }

    public static SearchBox dark(MenuScreen<?> screen, Consumer<String> responder) {
        return new SearchBox(screen, responder, 0xFFE0E0E0, ChatFormatting.GRAY);
    }

    public static SearchBox light(MenuScreen<?> screen, Consumer<String> responder) {
        return new SearchBox(screen, responder, RenderUtil.TEXT_COLOR, ChatFormatting.DARK_GRAY);
    }

    public String getValue() {
        return editBox.getValue();
    }

    public void setValue(String val) {
        editBox.setValue(val);
    }
}
