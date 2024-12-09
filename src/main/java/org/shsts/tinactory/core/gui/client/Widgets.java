package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.core.gui.Texture.VANILLA_WIDGETS;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Widgets {
    public static final int EDIT_BOX_LINE_HEIGHT = 14;
    public static final int BUTTON_HEIGHT = 20;

    public static Button simpleButton(IMenu menu, Component label,
        @Nullable Component tooltip, Runnable onPress) {
        return new Button(menu, tooltip) {
            private final Texture texture = VANILLA_WIDGETS;
            private final Font font = ClientUtil.getFont();
            private final int textWidth = font.width(label);

            @Override
            public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                var y = isHovering(mouseX, mouseY) ? 66 + BUTTON_HEIGHT : 66;
                var w = rect.width() / 2;
                var rect1 = new Rect(rect.x(), rect.y(), w, rect.height());
                RenderUtil.blit(poseStack, texture, getBlitOffset(), rect1, 0, y);
                RenderUtil.blit(poseStack, texture, getBlitOffset(), rect1.offset(w, 0), 200 - w, y);

                font.drawShadow(poseStack, label, rect.x() + w - (float) textWidth / 2,
                    rect.y() + (float) (rect.height() - font.lineHeight) / 2,
                    RenderUtil.WHITE);
            }

            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                onPress.run();
            }
        };
    }

    public static Button simpleButton(Menu<?, ?> menu, Component label,
        @Nullable Component tooltip, Runnable onPress) {
        return new Button(menu, tooltip) {
            private final Texture texture = VANILLA_WIDGETS;
            private final Font font = ClientUtil.getFont();
            private final int textWidth = font.width(label);

            @Override
            public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                var y = isHovering(mouseX, mouseY) ? 66 + BUTTON_HEIGHT : 66;
                var w = rect.width() / 2;
                var rect1 = new Rect(rect.x(), rect.y(), w, rect.height());
                RenderUtil.blit(poseStack, texture, getBlitOffset(), rect1, 0, y);
                RenderUtil.blit(poseStack, texture, getBlitOffset(), rect1.offset(w, 0), 200 - w, y);

                font.drawShadow(poseStack, label, rect.x() + w - (float) textWidth / 2,
                    rect.y() + (float) (rect.height() - font.lineHeight) / 2,
                    RenderUtil.WHITE);
            }

            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                onPress.run();
            }
        };
    }

    public static EditBox editBox() {
        return new EditBox(ClientUtil.getFont(), 0, 0, 0, 0, TextComponent.EMPTY) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                // solve the bug
                return super.keyPressed(keyCode, scanCode, modifiers) ||
                    (canConsumeInput() && keyCode != 256 && keyCode != 258);
            }
        };
    }
}
