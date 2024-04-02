package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.logistics.SlotType;
import org.shsts.tinactory.registrate.builder.MenuBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Layout {
    public record WidgetInfo(Rect rect, Texture texture) {}

    public record SlotInfo(int index, int x, int y, int port, SlotType type) {
        public SlotInfo setIndex(int index) {
            return new SlotInfo(index, this.x, this.y, this.port, this.type);
        }
    }

    public final List<SlotInfo> slots;
    public final List<WidgetInfo> images;
    @Nullable
    public final WidgetInfo progressBar;
    public final Rect rect;

    public Layout(List<SlotInfo> slots, List<WidgetInfo> images, @Nullable WidgetInfo progressBar) {
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

    public <S extends MenuBuilder<?, ?, ?, S>> Transformer<S> applyMenu(int yOffset) {
        return builder -> {
            var xOffset = (ContainerMenu.CONTENT_WIDTH - this.rect.width()) / 2;
            for (var slot : this.slots) {
                var x = xOffset + slot.x;
                var y = yOffset + slot.y;
                switch (slot.type.portType) {
                    case ITEM -> builder.slot(slot.index, x, y);
                    case FLUID -> builder.fluidSlot(slot.index, x, y);
                }
            }
            for (var image : this.images) {
                builder.staticWidget(image.rect.offset(xOffset, yOffset), image.texture);
            }
            if (this.progressBar != null) {
                builder.progressBar(this.progressBar.texture, this.progressBar.rect.offset(xOffset, yOffset),
                        be -> be.getCapability(AllCapabilities.PROCESSOR.get())
                                .map(IProcessor::getProgress)
                                .orElse(0.0d));
            }
            return builder;
        };
    }

    public static <P> LayoutSetBuilder<P> builder(P parent, Consumer<Map<Voltage, Layout>> onCreate) {
        return new LayoutSetBuilder<>(parent, onCreate);
    }

    public static LayoutSetBuilder<?> builder() {
        return new LayoutSetBuilder<>(Unit.INSTANCE);
    }
}
