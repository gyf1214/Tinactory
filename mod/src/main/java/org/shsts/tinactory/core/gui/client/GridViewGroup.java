package org.shsts.tinactory.core.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GridViewGroup<T extends IViewNode> extends ViewGroup {
    private final int itemWidth;
    private final int itemHeight;
    private final int verticalSpacing;
    private final Rect offset;
    private final List<T> slots = new ArrayList<>();
    @Nullable
    private IntFunction<T> slotFactory = null;

    private int columnCount = 1;
    private int rowCount = 1;
    private int horizontalSpacing = 0;
    private int slotCount = 1;
    private int itemCount = 0;
    private int page = 0;

    public GridViewGroup(int itemWidth, int itemHeight, int verticalSpacing, Rect offset) {
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        this.verticalSpacing = verticalSpacing;
        this.offset = offset;
    }

    public GridViewGroup(int itemWidth, int itemHeight, int verticalSpacing, Rect offset,
        IntFunction<T> slotFactory) {
        this(itemWidth, itemHeight, verticalSpacing, offset);
        setSlotFactory(slotFactory);
    }

    public void setSlotFactory(IntFunction<T> slotFactory) {
        this.slotFactory = slotFactory;
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

    public int getSlotCount() {
        return slotCount;
    }

    public int getPage() {
        return page;
    }

    public T getSlot(int index) {
        return slots.get(index);
    }

    public Rect getSlotRect(int index) {
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
        if (index < 0 || index >= slotCount) {
            return -1;
        }
        var visibleIndex = index + page * slotCount;
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
        slotCount = rowCount * columnCount;
        setPage(page);
        syncSlots();
        super.setRect(rect);
    }

    private void syncSlots() {
        while (slots.size() < slotCount) {
            if (slotFactory == null) {
                throw new IllegalStateException("GridViewGroup slot factory is not set");
            }
            var slot = slotFactory.apply(slots.size());
            slots.add(slot);
            if (initialized) {
                slot.initView();
            }
            slot.setActive(active);
        }
        while (slots.size() > slotCount) {
            var slot = slots.remove(slots.size() - 1);
            removeChild(slot);
        }
        for (var i = 0; i < slots.size(); i++) {
            var slot = slots.get(i);
            removeChild(slot);
            addChild(getSlotRect(i), slot);
        }
    }

    private int getMaxPage() {
        return slotCount > 0 ? Math.max(1, (itemCount + slotCount - 1) / slotCount) : 1;
    }
}
