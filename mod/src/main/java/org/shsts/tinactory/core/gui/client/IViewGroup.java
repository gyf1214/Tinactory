package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IViewGroup extends IViewNode {
    void addChild(RectD anchor, Rect offset, int zIndex, IViewNode child);

    default void addChild(RectD anchor, Rect offset, IViewNode child) {
        addChild(anchor, offset, 0, child);
    }

    default void addChild(Rect offset, IViewNode child) {
        addChild(RectD.ZERO, offset, child);
    }

    default void addChild(IViewNode child) {
        addChild(Rect.ZERO, child);
    }

    default void addGroup(Rect offset, IViewGroup panel) {
        addChild(RectD.FULL, offset, panel);
    }

    default void addGroup(IViewGroup panel) {
        addGroup(Rect.ZERO, panel);
    }
}
