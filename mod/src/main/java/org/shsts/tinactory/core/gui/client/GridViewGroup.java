package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.util.MathUtil;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GridViewGroup extends ViewGroup {
    private final int itemWidth;
    private final int itemHeight;
    private final int verticalSpacing;
    private final int bottomReservedSpace;
    private final RectD pageButtonAnchor;
    private final Rect pageButtonOffset;
    private final int pageButtonMargin;

    private int columnCount = 1;
    private int rowCount = 1;
    private int horizontalSpacing = 0;
    private int buttonCount = 1;
    private int itemCount = 0;
    private int page = 0;

    public GridViewGroup(int itemWidth, int itemHeight, int verticalSpacing, int bottomReservedSpace,
        RectD pageButtonAnchor, Rect pageButtonOffset, int pageButtonMargin) {

        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        this.verticalSpacing = verticalSpacing;
        this.bottomReservedSpace = bottomReservedSpace;
        this.pageButtonAnchor = pageButtonAnchor;
        this.pageButtonOffset = pageButtonOffset;
        this.pageButtonMargin = pageButtonMargin;
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

    public RectD getPageButtonAnchor() {
        return pageButtonAnchor;
    }

    public Rect getLeftPageButtonOffset() {
        return pageButtonOffset.offset(-pageButtonMargin - pageButtonOffset.width(), 0);
    }

    public Rect getRightPageButtonOffset() {
        return pageButtonOffset.offset(pageButtonMargin, 0);
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
        columnCount = Math.max(1, rect.width() / itemWidth);
        rowCount = Math.max(1, (rect.height() + verticalSpacing - bottomReservedSpace) /
            (itemHeight + verticalSpacing));
        horizontalSpacing = columnCount > 1 ? (rect.width() - columnCount * itemWidth) / (columnCount - 1) : 0;
        buttonCount = rowCount * columnCount;
        setPage(page);
        super.setRect(rect);
    }

    private int getMaxPage() {
        return buttonCount > 0 ? Math.max(1, (itemCount + buttonCount - 1) / buttonCount) : 1;
    }
}
