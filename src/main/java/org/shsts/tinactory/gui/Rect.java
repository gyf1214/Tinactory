package org.shsts.tinactory.gui;

import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public record Rect(int x, int y, int width, int height) {
    public int endX() {
        return x + width;
    }

    public int endY() {
        return y + height;
    }

    public Rect offset(int dx, int dy) {
        return new Rect(x + dx, y + dy, width, height);
    }
}
