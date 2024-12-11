package org.shsts.tinactory.core.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Layout {
    public record WidgetInfo(Rect rect, Texture texture) {}

    public record SlotInfo(int index, int x, int y, int port, SlotType type) {
        public SlotInfo setIndex(int index) {
            return new SlotInfo(index, x, y, port, type);
        }

        public SlotInfo setType(SlotType type) {
            return new SlotInfo(index, x, y, port, type);
        }
    }

    public record PortInfo(int slots, SlotType type) {}

    public final List<SlotInfo> slots;
    public final List<WidgetInfo> images;
    @Nullable
    public final WidgetInfo progressBar;
    public final Rect rect;
    public final List<PortInfo> ports = new ArrayList<>();
    public final List<List<SlotInfo>> portSlots = new ArrayList<>();

    public Layout(List<SlotInfo> slots, List<WidgetInfo> images, @Nullable WidgetInfo progressBar) {
        this.slots = slots;
        for (var slot : slots) {
            if (slot.port >= portSlots.size()) {
                var list = new ArrayList<SlotInfo>();
                portSlots.add(list);
                list.add(slot);
            } else {
                portSlots.get(slot.port).add(slot);
            }
        }
        for (var portSlot : portSlots) {
            ports.add(new PortInfo(portSlot.size(), portSlot.get(0).type));
        }
        this.images = images;
        this.progressBar = progressBar;

        var maxX = 0;
        var maxY = 0;
        for (var slot : slots) {
            maxX = Math.max(maxX, slot.x() + Menu.SLOT_SIZE);
            maxY = Math.max(maxY, slot.y() + Menu.SLOT_SIZE);
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

    public static <P> LayoutSetBuilder<P> builder(P parent) {
        return new LayoutSetBuilder<>(parent);
    }

    public static LayoutSetBuilder<?> builder() {
        return new LayoutSetBuilder<>(Unit.INSTANCE);
    }

    public int getXOffset() {
        return (Menu.CONTENT_WIDTH - rect.width()) / 2;
    }

    public record SlotWith<X>(SlotInfo slot, X val) {}

    private <S, T> List<SlotWith<T>> getSlotWithInfo(List<S> source,
        ToIntFunction<S> getPort, Function<S, T> getResult) {
        var currentSlotIndex = new HashMap<Integer, Integer>();
        var ret = new ArrayList<SlotWith<T>>();

        for (var item : source) {
            var port = getPort.applyAsInt(item);
            var slotIndex = currentSlotIndex.getOrDefault(port, 0);
            var slots = this.portSlots.get(port);
            if (slotIndex < slots.size()) {
                var slot = slots.get(slotIndex);
                ret.add(new SlotWith<>(slot, getResult.apply(item)));
                currentSlotIndex.put(port, slotIndex + 1);
            }
        }
        return ret;
    }

    public List<SlotWith<IProcessingIngredient>> getProcessingInputs(ProcessingRecipe recipe) {
        return getSlotWithInfo(recipe.inputs, ProcessingRecipe.Input::port, ProcessingRecipe.Input::ingredient);
    }

    public List<SlotWith<IProcessingResult>> getProcessingOutputs(ProcessingRecipe recipe) {
        return getSlotWithInfo(recipe.outputs, ProcessingRecipe.Output::port, ProcessingRecipe.Output::result);
    }

    public static final Layout EMPTY = builder().buildLayout();

    public static final Map<Voltage, Layout> EMPTY_SET = builder().buildObject();
}
