package org.shsts.tinactory.integration.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.function.Consumer;

import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Widgets {
    public static final int BUTTON_HEIGHT = 20;
    public static final Rect BUTTON_PANEL_TEX = new Rect(1, 1, 147, 166);
    public static final Rect BUTTON_PANEL_BG = BUTTON_PANEL_TEX.offset(6, 6).enlarge(-12, -12);

    public static Button button(MenuBase menu, Component label,
        @Nullable Component tooltip, Runnable onPress) {
        return new VanillaButton(menu, label, tooltip, onPress);
    }

    public static EditBox editBox() {
        var editBox = new EditBox(ClientUtil.getFont(), 0, 0, 0, 0, Component.empty()) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                return super.keyPressed(keyCode, scanCode, modifiers) ||
                    (canConsumeInput() && keyCode != 256 && keyCode != 258);
            }
        };
        editBox.setTextShadow(false);
        return editBox;
    }

    public static StaticWidget searchIcon(MenuBase menu) {
        return new StaticWidget(menu, RECIPE_BOOK_BG, 11, 15);
    }

    public static EditBox searchBox(Consumer<String> responder) {
        var ret = editBox();
        ret.setBordered(false);
        ret.setResponder(responder);
        ret.setHint(I18n.tr("gui.recipebook.search_hint")
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        return ret;
    }
}
