package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ViewGroup implements IViewGroup {
    protected record ViewChild(RectD anchor, Rect offset, int zIndex, IViewNode child) {
        public Rect resolveRect(Rect parent) {
            var sx = parent.inX(anchor.x()) + offset.x();
            var tx = parent.inX(anchor.endX()) + offset.endX();
            var sy = parent.inY(anchor.y()) + offset.y();
            var ty = parent.inY(anchor.endY()) + offset.endY();
            return Rect.corners(sx, sy, tx, ty);
        }
    }

    protected final List<ViewChild> children = new ArrayList<>();
    protected Rect rect = Rect.ZERO;
    protected boolean active = true;

    @Override
    public void addChild(RectD anchor, Rect offset, int zIndex, IViewNode child) {
        children.add(new ViewChild(anchor, offset, zIndex, child));
    }

    public void removeChild(IViewNode child) {
        children.removeIf(viewChild -> viewChild.child() == child);
    }

    @Override
    public void initView() {
        children.sort(Comparator.comparing(ViewChild::zIndex));
        for (var child : children) {
            child.child().initView();
        }
    }

    @Override
    public void setRect(Rect rect) {
        this.rect = rect;
        for (var child : children) {
            child.child().setRect(child.resolveRect(rect));
        }
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
        for (var child : children) {
            child.child().setActive(active);
        }
    }
}
