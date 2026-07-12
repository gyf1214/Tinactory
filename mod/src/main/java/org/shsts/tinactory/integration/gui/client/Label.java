package org.shsts.tinactory.integration.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Label extends MenuWidget {
    public enum Alignment {
        BEGIN(0d), MIDDLE(0.5), END(1d);

        Alignment(double value) {
            this.value = value;
        }

        private final double value;
    }

    private final List<Component> lines = new ArrayList<>();
    private final List<FormattedCharSequence> formattedLines = new ArrayList<>();
    private int cacheWidth = 0;
    private int cacheHeight = 0;
    private final Font font = ClientUtil.getFont();
    @Nullable
    private Component multiline = null;
    private float scale = 1f;

    public int color = RenderUtil.TEXT_COLOR;
    public Alignment horizontalAlign = Alignment.BEGIN;
    public Alignment verticalAlign = Alignment.BEGIN;
    public int spacing = Menu.SPACING;

    public Label(MenuBase menu) {
        super(menu);
    }

    public Label(MenuBase menu, Component... lines) {
        super(menu);
        setLines(lines);
    }

    private void updateSize() {
        if (formattedLines.isEmpty()) {
            var width = lines.stream()
                .mapToInt(font::width)
                .max().orElse(0);
            cacheWidth = scaledSize(width);
            cacheHeight = scaledSize(lines.size() * (FONT_HEIGHT + spacing) - spacing);
        } else {
            var width = formattedLines.stream()
                .mapToInt(font::width)
                .max().orElse(0);
            cacheWidth = scaledSize(width);
            cacheHeight = scaledSize(formattedLines.size() * (FONT_HEIGHT + spacing) - spacing);
        }
    }

    private int scaledSize(int value) {
        return (int) Math.ceil((double) value * (double) scale);
    }

    private int scaledLineSpacing() {
        return scaledSize(FONT_HEIGHT + spacing);
    }

    private void updateMultiline() {
        if (multiline != null && rect != null) {
            formattedLines.clear();
            formattedLines.addAll(font.split(multiline, Math.max(1, (int) ((float) rect.width() / scale))));
            updateSize();
        }
    }

    public void setLines(Component... values) {
        multiline = null;
        formattedLines.clear();
        lines.clear();
        lines.addAll(Arrays.asList(values));
        updateSize();
    }

    public void setLine(int index, Component value) {
        multiline = null;
        formattedLines.clear();
        while (lines.size() <= index) {
            lines.add(Component.empty());
        }
        lines.set(index, value);
        updateSize();
    }

    public void setMultiline(Component value) {
        multiline = value;
        lines.clear();
        updateMultiline();
    }

    public Label setScale(float value) {
        if (value <= 0f) {
            throw new IllegalArgumentException("Label scale must be positive");
        }
        scale = value;
        updateMultiline();
        updateSize();
        return this;
    }

    @Override
    public void setRect(Rect rect) {
        super.setRect(rect);
        updateMultiline();
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var rect = rect();
        int x = rect.inX(horizontalAlign.value) - (int) (cacheWidth * horizontalAlign.value);
        int y = rect.inY(verticalAlign.value) - (int) (cacheHeight * verticalAlign.value);
        if (formattedLines.isEmpty()) {
            for (var line : lines) {
                RenderUtil.renderText(graphics, line, x, y, color, scale);
                y += scaledLineSpacing();
            }
        } else {
            for (var line : formattedLines) {
                RenderUtil.renderText(graphics, line, x, y, color, scale);
                y += scaledLineSpacing();
            }
        }
    }
}
