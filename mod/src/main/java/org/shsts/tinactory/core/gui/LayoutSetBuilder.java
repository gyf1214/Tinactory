package org.shsts.tinactory.core.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayoutSetBuilder<P> extends SimpleBuilder<Map<Voltage, Layout>, P, LayoutSetBuilder<P>> {
    private record SlotAndVoltages(Layout.SlotInfo slot, Collection<Voltage> voltages) {}

    private final List<Layout.WidgetInfo> images = new ArrayList<>();
    private final List<SlotAndVoltages> slots = new ArrayList<>();
    private int curSlot = 0;
    @Nullable
    private Layout.ProgressBarInfo progressBar = null;

    public LayoutSetBuilder(P parent) {
        super(parent);
    }

    public LayoutSetBuilder<P> dummySlot(int x, int y) {
        var slot = new Layout.SlotInfo(0, x, y, 0, SlotType.NONE, null);
        slots.add(new SlotAndVoltages(slot, Arrays.asList(Voltage.values())));
        return this;
    }

    public LayoutSetBuilder<P> slot(int port, SlotType type, int x, int y, Collection<Voltage> voltages) {
        var slot = new Layout.SlotInfo(curSlot++, x, y, port, type, null);
        slots.add(new SlotAndVoltages(slot, voltages));
        return this;
    }

    public LayoutSetBuilder<P> slot(int port, SlotType type, int x, int y) {
        return slot(port, type, x, y, Arrays.asList(Voltage.values()));
    }

    public LayoutSetBuilder<P> image(Rect rect, Texture tex) {
        images.add(new Layout.WidgetInfo(rect, tex));
        return this;
    }

    public LayoutSetBuilder<P> image(int x, int y, Texture tex) {
        return image(new Rect(x, y, tex.width(), tex.height()), tex);
    }

    public LayoutSetBuilder<P> progressBar(Rect rect, Texture tex, ProgressDirection dir) {
        progressBar = new Layout.ProgressBarInfo(rect, tex, dir);
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
            ret.put(voltage, buildLayout(voltage));
        }
        return ret;
    }

    public List<Layout> buildList(int size) {
        var ret = new ArrayList<Layout>();
        for (var i = 0; i < size; i++) {
            var slots = getSlots(Voltage.fromRank(i));
            ret.add(new Layout(slots, images, progressBar));
        }
        return ret;
    }

    public Layout buildLayout(Voltage voltage) {
        var slots = getSlots(voltage);
        return new Layout(slots, images, progressBar);
    }

    public Layout buildLayout() {
        return buildLayout(Voltage.MAX);
    }
}
