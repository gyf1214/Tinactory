package org.shsts.tinactory.compat.waila;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.util.I18n;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

import java.util.List;

import static org.shsts.tinactory.core.gui.Menu.SPACING;

public abstract class ProviderBase implements IBlockComponentProvider {
    private static final int PROGRESS_TEXT_COLOR = 0xFFFFFFFF;

    private final ResourceLocation elementTag;

    private boolean hasSpace;
    protected IElementHelper helper;
    protected ITooltip tooltip;

    protected ProviderBase(ResourceLocation elementTag) {
        this.elementTag = elementTag;
    }

    protected static MutableComponent tr(String id, Object... args) {
        return I18n.tr("tinactory.tooltip." + id, args);
    }

    private void addSpace() {
        if (!hasSpace) {
            tooltip.add(helper.spacer(0, SPACING).tag(elementTag));
            hasSpace = true;
        }
    }

    protected void add(IElement element) {
        addSpace();
        tooltip.add(element.tag(elementTag));
    }

    protected void add(List<IElement> elements) {
        addSpace();
        for (var element : elements) {
            element.tag(elementTag);
        }
        tooltip.add(elements);
    }

    protected void newSpace() {
        hasSpace = false;
    }

    private ProgressStyle progressStyle(int color) {
        return helper.progressStyle().color(color).textColor(PROGRESS_TEXT_COLOR);
    }

    protected void addProgress(float val, Component text, int color) {
        add(helper.progress(val, text, progressStyle(color), BoxStyle.getNestedBox(), true));
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        this.tooltip = tooltip;
        this.helper = IElementHelper.get();
        hasSpace = false;
        doAppendTooltip(accessor.getServerData(), accessor, config);
    }

    protected abstract void doAppendTooltip(CompoundTag tag, BlockAccessor accessor, IPluginConfig config);
}
