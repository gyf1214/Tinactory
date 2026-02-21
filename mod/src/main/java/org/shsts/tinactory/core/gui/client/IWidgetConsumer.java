package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IWidgetConsumer {
    void addGuiComponent(RectD anchor, Rect offset, int zIndex, GuiComponent widget);

    default <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry> void addWidget(
        RectD anchor, Rect offset, int zIndex, T widget) {
        addGuiComponent(anchor, offset, zIndex, widget);
    }

    default <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry> void addWidget(
        RectD anchor, Rect offset, T widget) {
        addGuiComponent(anchor, offset, 0, widget);
    }

    default <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry> void addWidget(
        Rect offset, T widget) {
        addGuiComponent(RectD.ZERO, offset, 0, widget);
    }

    default <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry> void addWidget(T widget) {
        addGuiComponent(RectD.ZERO, Rect.ZERO, 0, widget);
    }

    default void addPanel(RectD anchor, Rect offset, int zIndex, Panel panel) {
        addGuiComponent(anchor, offset, zIndex, panel);
    }

    default void addPanel(RectD anchor, Rect offset, Panel panel) {
        addGuiComponent(anchor, offset, 0, panel);
    }

    default void addPanel(Rect offset, Panel panel) {
        addGuiComponent(RectD.FULL, offset, 0, panel);
    }

    default void addPanel(Panel panel) {
        addGuiComponent(RectD.FULL, Rect.ZERO, 0, panel);
    }
}
