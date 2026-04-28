package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.util.MathUtil;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GridViewGroup extends ViewGroup {
    private final int itemWidth;
    private final int itemHeight;
    private final int verticalSpacing;
    private final Rect offset;

    private int columnCount = 1;
    private int rowCount = 1;
    private int horizontalSpacing = 0;
    private int buttonCount = 1;
    private int itemCount = 0;
    private int page = 0;

    public GridViewGroup(int itemWidth, int itemHeight, int verticalSpacing, Rect offset) {
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        this.verticalSpacing = verticalSpacing;
        this.offset = offset;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public int getButtonCount() {
        return buttonCount;
    }

    public int getPage() {
        return page;
    }

    public Rect getButtonRect(int index) {
        var column = index % columnCount;
        var row = index / columnCount;
        var x = column * (itemWidth + horizontalSpacing);
        var y = row * (itemHeight + verticalSpacing);
        return new Rect(x, y, itemWidth, itemHeight);
    }

    public boolean isLeftPageEnabled() {
        return page != 0;
    }

    public boolean isRightPageEnabled() {
        return page != getMaxPage() - 1;
    }

    public int getVisibleIndex(int index) {
        if (index < 0 || index >= buttonCount) {
            return -1;
        }
        var visibleIndex = index + page * buttonCount;
        return visibleIndex < itemCount ? visibleIndex : -1;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = Math.max(0, itemCount);
        setPage(page);
    }

    public void setPage(int page) {
        this.page = MathUtil.clamp(page, 0, getMaxPage() - 1);
    }

    @Override
    public void setRect(Rect rect) {
        var width = rect.width() + offset.width();
        var height = rect.height() + offset.height();

        columnCount = Math.max(1, width / itemWidth);
        rowCount = Math.max(1, (height + verticalSpacing) / (itemHeight + verticalSpacing));
        horizontalSpacing = columnCount > 1 ? (rect.width() - columnCount * itemWidth) / (columnCount - 1) : 0;
        buttonCount = rowCount * columnCount;
        setPage(page);
        super.setRect(rect);
    }

    private int getMaxPage() {
        return buttonCount > 0 ? Math.max(1, (itemCount + buttonCount - 1) / buttonCount) : 1;
    }
}
