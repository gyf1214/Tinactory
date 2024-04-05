package org.shsts.tinactory.core.gui;

import com.google.common.collect.ArrayListMultimap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.logistics.SlotType;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.builder.MenuBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
    }

    public final List<SlotInfo> slots;
    public final List<WidgetInfo> images;
    @Nullable
    public final WidgetInfo progressBar;
    public final Rect rect;
    public final ArrayListMultimap<Integer, SlotInfo> portSlots = ArrayListMultimap.create();

    public Layout(List<SlotInfo> slots, List<WidgetInfo> images, @Nullable WidgetInfo progressBar) {
        this.slots = slots;
        for (var slot : slots) {
            portSlots.put(slot.port(), slot);
        }
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

    public int getXOffset() {
        return (ContainerMenu.CONTENT_WIDTH - rect.width()) / 2;
    }

    public <T extends SmartBlockEntity, M extends ContainerMenu<T>, P extends BlockEntityBuilder<T, ?>>
    Transformer<MenuBuilder<T, M, P>> applyMenu() {
        return builder -> {
            var xOffset = getXOffset();
            for (var slot : slots) {
                var x = xOffset + slot.x;
                var y = slot.y;
                switch (slot.type.portType) {
                    case ITEM -> builder.slot(slot.index, x, y);
                    case FLUID -> builder.fluidSlot(slot.index, x, y);
                }
            }
            for (var image : images) {
                builder.staticWidget(image.rect.offset(xOffset, 0), image.texture);
            }
            if (progressBar != null) {
                builder.progressBar(progressBar.texture, progressBar.rect.offset(xOffset, 0),
                        be -> be.getCapability(AllCapabilities.PROCESSOR.get())
                                .map(IProcessor::getProgress)
                                .orElse(0.0d));
            }
            return builder;
        };
    }

    public record SlotWith<X>(SlotInfo slot, X val) {}

    private <S, T> List<SlotWith<T>>
    getSlotWithInfo(List<S> source, ToIntFunction<S> getPort, Function<S, T> getResult) {
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

    public List<SlotWith<IProcessingIngredient>> getProcessingInputs(ProcessingRecipe<?> recipe) {
        return getSlotWithInfo(recipe.inputs, ProcessingRecipe.Input::port, ProcessingRecipe.Input::ingredient);
    }

    public List<SlotWith<IProcessingResult>> getProcessingOutputs(ProcessingRecipe<?> recipe) {
        return getSlotWithInfo(recipe.outputs, ProcessingRecipe.Output::port, ProcessingRecipe.Output::result);
    }

    public static <P> LayoutSetBuilder<P> builder(P parent, Consumer<Map<Voltage, Layout>> onCreate) {
        return new LayoutSetBuilder<>(parent, onCreate);
    }

    public static LayoutSetBuilder<?> builder() {
        return new LayoutSetBuilder<>(Unit.INSTANCE);
    }
}
