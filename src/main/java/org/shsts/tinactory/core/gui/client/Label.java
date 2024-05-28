package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Label extends MenuWidget {
    public static final int LINE_HEIGHT = 9;

    public enum Alignment {
        BEGIN(0d), MIDDLE(0.5), END(1d);

        Alignment(double value) {
            this.value = value;
        }

        private final double value;
    }

    private List<Component> lines = List.of();
    private int cacheWidth = 0;
    private int cacheHeight = 0;
    private final Font font = ClientUtil.getFont();

    public int color = RenderUtil.TEXT_COLOR;
    public Alignment horizontalAlign = Alignment.BEGIN;
    public Alignment verticalAlign = Alignment.BEGIN;

    public Label(Menu<?, ?> menu) {
        super(menu);
    }

    public Label(Menu<?, ?> menu, Alignment horizontalAlign, Component... lines) {
        super(menu);
        this.horizontalAlign = horizontalAlign;
        setLines(lines);
    }

    public void setLines(Component... values) {
        lines = List.of(values);
        cacheWidth = Arrays.stream(values)
                .mapToInt(font::width)
                .max().orElse(0);
        cacheHeight = values.length * LINE_HEIGHT;
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        int x = rect.inX(horizontalAlign.value) - (int) (cacheWidth * horizontalAlign.value);
        int y = rect.inY(verticalAlign.value) - (int) (cacheHeight * verticalAlign.value);
        for (var line : lines) {
            RenderUtil.renderText(poseStack, line, x, y, color);
            y += LINE_HEIGHT;
        }
    }
}
