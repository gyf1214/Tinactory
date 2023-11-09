package org.shsts.tinactory.gui.layout;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.machine.IProcessingMachine;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.registrate.builder.MenuBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Layout {
    private record WidgetInfo(Rect rect, Texture texture) {}

    private record SlotInfo(int index, int x, int y) {}

    private final List<SlotInfo> slots;
    private final List<WidgetInfo> images;
    @Nullable
    private final WidgetInfo progressBar;
    private final Rect rect;

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

    public <S extends MenuBuilder<?, ?, ?, S>> Transformer<S> applyMenu(int yOffset) {
        return builder -> {
            var xOffset = (ContainerMenu.CONTENT_WIDTH - this.rect.width()) / 2;
            for (var slot : this.slots) {
                builder.slot(slot.index, xOffset + slot.x, yOffset + slot.y);
            }
            for (var image : this.images) {
                builder.staticWidget(image.rect.offset(xOffset, yOffset), image.texture);
            }
            if (this.progressBar != null) {
                builder.progressBar(this.progressBar.texture, this.progressBar.rect.offset(xOffset, yOffset),
                        IProcessingMachine::getProgress);
            }
            return builder;
        };
    }

    public static class Builder {
        private final List<SlotInfo> slots = new ArrayList<>();
        private final List<WidgetInfo> images = new ArrayList<>();
        @Nullable
        private WidgetInfo progressBar = null;

        public Builder slot(int slot, int x, int y) {
            this.slots.add(new SlotInfo(slot, x, y));
            return this;
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
