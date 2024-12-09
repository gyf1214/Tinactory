package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.IMenu;

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

    public int color = RenderUtil.TEXT_COLOR;
    public Alignment horizontalAlign = Alignment.BEGIN;
    public Alignment verticalAlign = Alignment.BEGIN;
    public int spacing = Menu.SPACING;

    public Label(IMenu menu) {
        super(menu);
    }

    public Label(IMenu menu, Component... lines) {
        super(menu);
        setLines(lines);
    }

    public Label(Menu<?, ?> menu) {
        super(menu);
    }

    public Label(Menu<?, ?> menu, Component... lines) {
        super(menu);
        setLines(lines);
    }

    private void updateSize() {
        if (formattedLines.isEmpty()) {
            cacheWidth = lines.stream()
                .mapToInt(font::width)
                .max().orElse(0);
            cacheHeight = lines.size() * (FONT_HEIGHT + spacing) - spacing;
        } else {
            cacheWidth = formattedLines.stream()
                .mapToInt(font::width)
                .max().orElse(0);
            cacheHeight = formattedLines.size() * (FONT_HEIGHT + spacing) - spacing;
        }
    }

    public void setLines(Component... values) {
        formattedLines.clear();
        lines.clear();
        lines.addAll(Arrays.asList(values));
        updateSize();
    }

    public void setLine(int index, Component value) {
        formattedLines.clear();
        while (lines.size() <= index) {
            lines.add(TextComponent.EMPTY);
        }
        lines.set(index, value);
        updateSize();
    }

    public void setMultiline(Component value) {
        formattedLines.clear();
        lines.clear();
        formattedLines.addAll(font.split(value, rect.width()));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        int x = rect.inX(horizontalAlign.value) - (int) (cacheWidth * horizontalAlign.value);
        int y = rect.inY(verticalAlign.value) - (int) (cacheHeight * verticalAlign.value);
        if (formattedLines.isEmpty()) {
            for (var line : lines) {
                RenderUtil.renderText(poseStack, line, x, y, color);
                y += FONT_HEIGHT + spacing;
            }
        } else {
            for (var line : formattedLines) {
                RenderUtil.renderText(poseStack, line, x, y, color);
                y += FONT_HEIGHT + spacing;
            }
        }
    }
}
