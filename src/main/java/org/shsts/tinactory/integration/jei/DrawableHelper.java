package org.shsts.tinactory.integration.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.gui.layout.Rect;
import org.shsts.tinactory.gui.layout.Texture;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class DrawableHelper {
    public static IDrawableStatic createStatic(IGuiHelper helper, Texture texture, Rect uvRect) {
        return helper.drawableBuilder(texture.loc(), uvRect.x(), uvRect.y(), uvRect.width(), uvRect.height())
                .setTextureSize(texture.width(), texture.height())
                .build();
    }

    public static IDrawableStatic createStatic(IGuiHelper helper, Texture texture, int width, int height) {
        return createStatic(helper, texture, new Rect(0, 0, width, height));
    }

    public static IDrawable createBackground(IGuiHelper helper, Layout layout) {
        var builder = ComposeDrawable.builder();
        for (var slot : layout.slots) {
            builder.add(helper.getSlotDrawable(), slot.x(), slot.y());
        }
        for (var image : layout.images) {
            var rect = image.rect();
            builder.add(DrawableHelper.createStatic(helper, image.texture(), rect.width(), rect.height()),
                    rect.x(), rect.y());
        }
        return builder.build();
    }
}
