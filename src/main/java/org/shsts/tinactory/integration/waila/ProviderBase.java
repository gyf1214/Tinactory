package org.shsts.tinactory.integration.waila;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.api.ui.IProgressStyle;
import mcp.mobius.waila.impl.ui.ProgressStyle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.util.I18n;

import java.util.List;

import static org.shsts.tinactory.core.gui.Menu.SPACING;

public abstract class ProviderBase implements IComponentProvider {
    private static final int PROGRESS_TEXT_COLOR = 0xFFFFFFFF;

    private final ResourceLocation elementTag;

    private boolean hasSpace;
    protected IElementHelper helper;
    protected ITooltip tooltip;

    protected ProviderBase(ResourceLocation elementTag) {
        this.elementTag = elementTag;
    }

    protected static TranslatableComponent tr(String id, Object... args) {
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

    private IProgressStyle progressStyle(int color) {
        var ret = (ProgressStyle) helper.progressStyle().color(color).textColor(PROGRESS_TEXT_COLOR);
        ret.shadow = true;
        return ret;
    }

    protected void progress(float val, Component text, int color) {
        add(helper.progress(val, text, progressStyle(color), helper.borderStyle()));
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        this.tooltip = tooltip;
        this.helper = tooltip.getElementHelper();
        hasSpace = false;
        doAppendTooltip(accessor.getServerData(), accessor, config);
    }

    protected abstract void doAppendTooltip(CompoundTag tag, BlockAccessor accessor, IPluginConfig config);
}
