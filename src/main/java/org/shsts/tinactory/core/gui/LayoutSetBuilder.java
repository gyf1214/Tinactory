package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.SimpleBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayoutSetBuilder<P> extends SimpleBuilder<Map<Voltage, Layout>, P, LayoutSetBuilder<P>> {
    private record SlotAndVoltage(Layout.SlotInfo slot, Voltage voltage) {}

    private final List<SlotAndVoltage> slots = new ArrayList<>();
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
        slots.add(new SlotAndVoltage(slot, Voltage.PRIMITIVE));
        return this;
    }

    public LayoutSetBuilder<P> port(SlotType type) {
        curPort++;
        curSlotType = type;
        return this;
    }

    public LayoutSetBuilder<P> slot(int x, int y, Voltage requiredVoltage) {
        assert curPort >= 0;
        var slot = new Layout.SlotInfo(curSlot++, x, y, curPort, curSlotType);
        slots.add(new SlotAndVoltage(slot, requiredVoltage));
        return this;
    }

    public LayoutSetBuilder<P> slot(int x, int y) {
        return slot(x, y, Voltage.PRIMITIVE);
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
            if (voltage.compareTo(slot.voltage) < 0) {
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
            ret.put(voltage, new Layout(slots, List.of(), progressBar));
        }
        return ret;
    }

    public Layout buildLayout() {
        var slots = getSlots(Voltage.MAXIMUM);
        return new Layout(slots, List.of(), progressBar);
    }
}
