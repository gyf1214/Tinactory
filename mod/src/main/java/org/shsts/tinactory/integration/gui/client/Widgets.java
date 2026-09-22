package org.shsts.tinactory.integration.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.GREGTECH_LOGO;
import static org.shsts.tinactory.core.gui.Texture.SWITCH_BUTTON;

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

    public static void gregtechButton(MenuBase menu, Panel parent, Panel panel,
        RectD anchor, int x, int y, Component tooltip, Runnable extraCallback) {
        var button = new SimpleButton(menu, SWITCH_BUTTON, tooltip) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                panel.setActive(!panel.isActive());
                extraCallback.run();
            }
        };
        var overlay = new StaticWidget(menu, GREGTECH_LOGO);
        var offset = new Rect(x, y, SLOT_SIZE, SLOT_SIZE);
        parent.addChild(anchor, offset, button);
        parent.addChild(anchor, offset.offset(1, 1).enlarge(-1, -1), overlay);
    }
}
