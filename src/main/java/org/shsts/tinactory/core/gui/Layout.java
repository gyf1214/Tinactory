package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.builder.MenuBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Layout {
    public record WidgetInfo(Rect rect, Texture texture) {}

    public enum SlotType {
        NONE(false, false),
        ITEM_INPUT(false, true),
        ITEM_OUTPUT(true, true),
        FLUID_INPUT(false, false),
        FLUID_OUTPUT(true, false);

        public final boolean output;
        public final boolean isItem;

        SlotType(boolean output, boolean isItem) {
            this.output = output;
            this.isItem = isItem;
        }
    }

    public record SlotInfo(int index, int x, int y, int port, SlotType type, Voltage requiredVoltage) {
        public SlotInfo setIndex(int index) {
            return new SlotInfo(index, this.x, this.y, this.port, this.type, this.requiredVoltage);
        }
    }

    public final List<SlotInfo> slots;
    public final List<WidgetInfo> images;
    @Nullable
    public final WidgetInfo progressBar;
    public final Rect rect;

    private Layout(List<SlotInfo> slots, List<WidgetInfo> images, @Nullable WidgetInfo progressBar) {
        this.slots = slots;
        this.images = images;
        this.progressBar = progressBar;

        var maxX = 0;
        var maxY = 0;
        for (var slot : slots) {
            maxX = Math.max(maxX, slot.x() + ContainerMenu.SLOT_SIZE);
            maxY = Math.max(maxY, slot.y() + ContainerMenu.SLOT_SIZE);
        }

        for (var image : images) {
            maxX = Math.max(maxX, image.rect.endX());
            maxY = Math.max(maxY, image.rect.endY());
        }
        if (progressBar != null) {
            maxX = Math.max(maxX, progressBar.rect.endX());
            maxY = Math.max(maxY, progressBar.rect.endY());
        }
        this.rect = new Rect(0, 0, maxX, maxY);
    }

    public <S extends MenuBuilder<?, ?, ?, S>> Transformer<S> applyMenu(int yOffset, Voltage voltage) {
        return builder -> {
            var xOffset = (ContainerMenu.CONTENT_WIDTH - this.rect.width()) / 2;
            var slots = this.getStackSlots(voltage);
            for (var slot : slots) {
                var x = xOffset + slot.x;
                var y = yOffset + slot.y;
                if (slot.type.isItem) {
                    builder.slot(slot.index, x, y);
                } else {
                    builder.fluidSlot(slot.index, x, y);
                }
            }
            for (var image : this.images) {
                builder.staticWidget(image.rect.offset(xOffset, yOffset), image.texture);
            }
            if (this.progressBar != null) {
                builder.progressBar(this.progressBar.texture, this.progressBar.rect.offset(xOffset, yOffset),
                        be -> be.getCapability(AllCapabilities.PROCESSING_MACHINE.get())
                                .map(machine -> machine.getProgress())
                                .orElse(0.0d));
            }
            return builder;
        };
    }

    public List<SlotInfo> getStackSlots() {
        return this.getStackSlots(Voltage.MAXIMUM);
    }

    public List<SlotInfo> getStackSlots(Voltage voltage) {
        var ret = new ArrayList<SlotInfo>();
        var fluidSlots = 0;
        var itemSlots = 0;
        for (var slot : this.slots) {
            if (slot.port < 0 || slot.type == SlotType.NONE ||
                    voltage.compareTo(slot.requiredVoltage) < 0) {
                continue;
            }
            var index = slot.type.isItem ? itemSlots++ : fluidSlots++;
            ret.add(slot.setIndex(index));
        }
        return ret;
    }

    public static class Builder {
        private final List<SlotInfo> slots = new ArrayList<>();
        private final List<WidgetInfo> images = new ArrayList<>();
        @Nullable
        private WidgetInfo progressBar = null;
        private SlotType curSlotType = SlotType.NONE;
        private int curPort = -1;
        private int curSlot = 0;

        public Builder dummySlot(int x, int y) {
            this.slots.add(new SlotInfo(0, x, y, 0, SlotType.NONE, Voltage.PRIMITIVE));
            return this;
        }

        public Builder port(SlotType type) {
            this.curPort++;
            this.curSlotType = type;
            return this;
        }

        public Builder slot(int x, int y, Voltage requiredVoltage) {
            assert this.curPort >= 0;
            this.slots.add(new SlotInfo(this.curSlot++, x, y, this.curPort, this.curSlotType, requiredVoltage));
            return this;
        }

        public Builder slot(int x, int y) {
            return this.slot(x, y, Voltage.PRIMITIVE);
        }

        public Builder image(Rect rect, Texture tex) {
            this.images.add(new WidgetInfo(rect, tex));
            return this;
        }

        public Builder progressBar(Texture tex, int x, int y) {
            this.progressBar = new WidgetInfo(new Rect(x, y, tex.width(), tex.height() / 2), tex);
            return this;
        }

        public Layout build() {
            return new Layout(this.slots, this.images, this.progressBar);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
