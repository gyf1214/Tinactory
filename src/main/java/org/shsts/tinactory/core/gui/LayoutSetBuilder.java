package org.shsts.tinactory.core.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.common.BuilderBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayoutSetBuilder<P> extends BuilderBase<Map<Voltage, Layout>, P, LayoutSetBuilder<P>> {
    private record SlotAndVoltages(Layout.SlotInfo slot, Collection<Voltage> voltages) {}

    private final List<Layout.WidgetInfo> images = new ArrayList<>();
    private final List<SlotAndVoltages> slots = new ArrayList<>();
    private SlotType curSlotType = SlotType.NONE;
    private int curPort = -1;
    private int curSlot = 0;
    @Nullable
    private Layout.WidgetInfo progressBar = null;

    public LayoutSetBuilder(P parent) {
        super(parent);
    }

    public LayoutSetBuilder<P> dummySlot(int x, int y) {
        var slot = new Layout.SlotInfo(0, x, y, 0, SlotType.NONE);
        slots.add(new SlotAndVoltages(slot, Arrays.asList(Voltage.values())));
        return this;
    }

    public LayoutSetBuilder<P> port(SlotType type) {
        curPort++;
        curSlotType = type;
        return this;
    }

    public LayoutSetBuilder<P> slot(int x, int y, Collection<Voltage> voltages) {
        assert curPort >= 0;
        var slot = new Layout.SlotInfo(curSlot++, x, y, curPort, curSlotType);
        slots.add(new SlotAndVoltages(slot, voltages));
        return this;
    }

    public LayoutSetBuilder<P> slot(int x, int y, Voltage fromVoltage) {
        return slot(x, y, Voltage.between(fromVoltage, Voltage.MAXIMUM));
    }

    public LayoutSetBuilder<P> slot(int x, int y, Voltage fromVoltage, Voltage toVoltage) {
        return slot(x, y, Voltage.between(fromVoltage, toVoltage));
    }

    public LayoutSetBuilder<P> slot(int x, int y) {
        return slot(x, y, Arrays.asList(Voltage.values()));
    }

    public LayoutSetBuilder<P> slots(int x, int y, int rows, int columns, Collection<Voltage> voltages) {
        for (var i = 0; i < rows; i++) {
            for (var j = 0; j < columns; j++) {
                slot(x + j * SLOT_SIZE, y + i * SLOT_SIZE, voltages);
            }
        }
        return this;
    }

    public LayoutSetBuilder<P> slots(int x, int y, int rows, int columns) {
        return slots(x, y, rows, columns, Arrays.asList(Voltage.values()));
    }

    public LayoutSetBuilder<P> slots(int x, int y, int rows, int columns, Voltage from) {
        return slots(x, y, rows, columns, Voltage.between(from, Voltage.MAXIMUM));
    }

    public LayoutSetBuilder<P> slots(int x, int y, int rows, int columns, Voltage from, Voltage to) {
        return slots(x, y, rows, columns, Voltage.between(from, to));
    }

    public LayoutSetBuilder<P> image(Rect rect, Texture tex) {
        images.add(new Layout.WidgetInfo(rect, tex));
        return this;
    }

    public LayoutSetBuilder<P> image(int x, int y, Texture tex) {
        return image(new Rect(x, y, tex.width(), tex.height()), tex);
    }

    public LayoutSetBuilder<P> placeHolder(Rect rect) {
        return image(rect, Texture.VOID);
    }

    public LayoutSetBuilder<P> progressBar(Texture tex, int x, int y) {
        progressBar = new Layout.WidgetInfo(new Rect(x, y, tex.width(), tex.height() / 2), tex);
        return this;
    }

    private List<Layout.SlotInfo> getSlots(Voltage voltage) {
        var ret = new ArrayList<Layout.SlotInfo>();
        var fluidSlots = 0;
        var itemSlots = 0;
        for (var slot : slots) {
            if (!slot.voltages.contains(voltage)) {
                continue;
            }
            var index = 0;
            switch (slot.slot.type().portType) {
                case ITEM -> index = itemSlots++;
                case FLUID -> index = fluidSlots++;
            }
            ret.add(slot.slot.setIndex(index));
        }
        return ret;
    }

    @Override
    protected Map<Voltage, Layout> createObject() {
        var ret = new HashMap<Voltage, Layout>();
        for (var voltage : Voltage.values()) {
            var slots = getSlots(voltage);
            ret.put(voltage, new Layout(slots, images, progressBar));
        }
        return ret;
    }

    public Layout buildLayout() {
        var slots = getSlots(Voltage.MAXIMUM);
        return new Layout(slots, images, progressBar);
    }
}
