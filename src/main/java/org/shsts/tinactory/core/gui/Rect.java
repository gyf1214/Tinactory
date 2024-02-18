package org.shsts.tinactory.core.gui;

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

    public Rect resize(int newW, int newH) {
        return new Rect(x, y, newW, newH);
    }

    public Rect enlarge(int dw, int dh) {
        return new Rect(x, y, width + dw, height + dh);
    }

    public boolean in(double pX, double pY) {
        return pX >= (double) (x - 1) && pX < (double) (x + width + 1) &&
                pY >= (double) (y - 1) && pY < (double) (y + height + 1);
    }
}
