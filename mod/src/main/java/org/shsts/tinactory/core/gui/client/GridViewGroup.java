package org.shsts.tinactory.core.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;

import java.util.ArrayList;
import java.util.Collection;
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
    private int slotWidth;
    private int slotHeight;
    private int horizontalSpacing = 0;
    private int slotCount = 1;

    /**
     * width/height = 0 means expand to entire rect (i.e. row/height = 1).
     */
    public GridViewGroup(int itemWidth, int itemHeight, int verticalSpacing, Rect offset) {
        this.slotWidth = this.itemWidth = itemWidth;
        this.slotHeight = this.itemHeight = itemHeight;
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

    public Collection<T> slots() {
        return slots;
    }

    public T getSlot(int index) {
        return slots.get(index);
    }

    public Rect getSlotRect(int index) {
        var column = index % columnCount;
        var row = index / columnCount;
        var x = column * (slotWidth + horizontalSpacing) + offset.x();
        var y = row * (slotHeight + verticalSpacing) + offset.y();
        return new Rect(x, y, slotWidth, slotHeight);
    }

    @Override
    protected void updateDynamicChildren() {
        var width = rect.width() + offset.width();
        var height = rect.height() + offset.height();

        columnCount = itemWidth <= 0 ? 1 : Math.max(1, width / itemWidth);
        rowCount = itemHeight <= 0 ? 1 : Math.max(1, (height + verticalSpacing) / (itemHeight + verticalSpacing));
        horizontalSpacing = columnCount > 1 ? (width - columnCount * itemWidth) / (columnCount - 1) : 0;
        slotCount = rowCount * columnCount;
        slotWidth = itemWidth <= 0 ? width : itemWidth;
        slotHeight = itemHeight <= 0 ? height : itemHeight;
        syncSlots();
    }

    private void syncSlots() {
        while (slots.size() < slotCount) {
            if (slotFactory == null) {
                throw new IllegalStateException("GridViewGroup slot factory is not set");
            }
            var slot = slotFactory.apply(slots.size());
            slots.add(slot);
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
}
